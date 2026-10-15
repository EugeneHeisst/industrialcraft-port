package ic2.core.energy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class StructureCache {
    private static final int maxSize = 32;
    final Map<Key, Data> entries = new HashMap<>();

    Data get(Set<Integer> activeSources, Set<Integer> activeSinks) {
        Key key = new Key(activeSources, activeSinks);
        Data ret = entries.get(key);
        if (ret == null) { ret = new Data(); add(key, ret); }
        ret.queries++;
        return ret;
    }
    void clear() { entries.clear(); }

    private void add(Key key, Data data) {
        if (entries.size() >= maxSize) {
            int min = Integer.MAX_VALUE; Key minKey = null;
            for (var e : entries.entrySet()) if (e.getValue().queries < min) { min = e.getValue().queries; minKey = e.getKey(); }
            entries.remove(minKey);
        }
        entries.put(new Key(key), data);
    }

    static class Data {
        boolean isInitialized;
        Map<Integer, Node> optimizedNodes;
        List<Node> activeNodes;
        LinearSolver solver;
        double[] sourceMatrix, resultMatrix;
        int queries;
    }

    static class Key {
        final Set<Integer> activeSources, activeSinks;
        final int hashCode;
        Key(Set<Integer> activeSources, Set<Integer> activeSinks) { this.activeSources = activeSources; this.activeSinks = activeSinks; hashCode = activeSources.hashCode() * 31 + activeSinks.hashCode(); }
        Key(Key key) { activeSources = new HashSet<>(key.activeSources); activeSinks = new HashSet<>(key.activeSinks); hashCode = key.hashCode; }
        @Override public int hashCode() { return hashCode; }
        @Override public boolean equals(Object o) { return o instanceof Key k && k.activeSources.equals(activeSources) && k.activeSinks.equals(activeSinks); }
    }
}
