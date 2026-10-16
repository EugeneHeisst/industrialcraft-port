package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;

public interface IPersonalBlock {
    /** Server: the first player to ask claims an unowned block; operators may always access it. */
    boolean permitsAccess(GameProfile profile);
    GameProfile getOwner();
}
