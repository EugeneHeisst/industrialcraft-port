package ic2.core.energy;

/** Dense solver for the network matrix: Cholesky like the original EJML symmPosDef solver, LU with partial pivoting as fallback. */
final class LinearSolver {
    private final int n;
    private final double[][] factor;
    private final int[] pivot;
    private final boolean cholesky;

    private LinearSolver(int n, double[][] factor, int[] pivot, boolean cholesky) { this.n = n; this.factor = factor; this.pivot = pivot; this.cholesky = cholesky; }

    /** @return null if the matrix can't be decomposed */
    static LinearSolver decompose(double[][] a) {
        int n = a.length;
        double[][] l = new double[n][n];
        boolean ok = true;
        for (int j = 0; j < n && ok; j++) {
            double d = a[j][j];
            for (int k = 0; k < j; k++) d -= l[j][k] * l[j][k];
            if (!(d > 0)) { ok = false; break; }
            l[j][j] = Math.sqrt(d);
            for (int i = j + 1; i < n; i++) {
                double s = a[i][j];
                for (int k = 0; k < j; k++) s -= l[i][k] * l[j][k];
                l[i][j] = s / l[j][j];
            }
        }
        if (ok) return new LinearSolver(n, l, null, true);
        double[][] lu = new double[n][];
        for (int i = 0; i < n; i++) lu[i] = a[i].clone();
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int k = 0; k < n; k++) {
            int max = k;
            for (int i = k + 1; i < n; i++) if (Math.abs(lu[i][k]) > Math.abs(lu[max][k])) max = i;
            if (Math.abs(lu[max][k]) < 1e-12) return null;
            if (max != k) { var t = lu[k]; lu[k] = lu[max]; lu[max] = t; int tp = p[k]; p[k] = p[max]; p[max] = tp; }
            for (int i = k + 1; i < n; i++) {
                lu[i][k] /= lu[k][k];
                for (int j = k + 1; j < n; j++) lu[i][j] -= lu[i][k] * lu[k][j];
            }
        }
        return new LinearSolver(n, lu, p, false);
    }

    double[] solve(double[] b) {
        double[] y = new double[n];
        if (cholesky) {
            for (int i = 0; i < n; i++) { double s = b[i]; for (int k = 0; k < i; k++) s -= factor[i][k] * y[k]; y[i] = s / factor[i][i]; }
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) { double s = y[i]; for (int k = i + 1; k < n; k++) s -= factor[k][i] * x[k]; x[i] = s / factor[i][i]; }
            return x;
        }
        for (int i = 0; i < n; i++) { double s = b[pivot[i]]; for (int k = 0; k < i; k++) s -= factor[i][k] * y[k]; y[i] = s; }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) { double s = y[i]; for (int k = i + 1; k < n; k++) s -= factor[i][k] * x[k]; x[i] = s / factor[i][i]; }
        return x;
    }
}
