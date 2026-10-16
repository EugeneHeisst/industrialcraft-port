package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Owner of a personal block, stored like the original's ownerGameProfile tag. */
public final class PersonalOwner {
    public GameProfile owner;

    public void read(CompoundTag nbt) {
        owner = null;
        if (!nbt.contains("ownerGameProfile")) return;
        CompoundTag tag = nbt.getCompound("ownerGameProfile");
        UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : null;
        if (id != null) owner = new GameProfile(id, tag.getString("Name"));
    }

    public void write(CompoundTag nbt) {
        if (owner == null) return;
        CompoundTag tag = new CompoundTag();
        tag.put("Id", NbtUtils.createUUID(owner.getId()));
        tag.putString("Name", owner.getName());
        nbt.put("ownerGameProfile", tag);
    }

    public static boolean isOp(Level level, GameProfile profile) {
        return level instanceof ServerLevel sl && sl.getServer().getPlayerList().isOp(profile);
    }

    /** @param claimed called when an unowned block got claimed by the profile (server only) */
    public boolean permits(Level level, GameProfile profile, Runnable claimed) {
        if (profile == null) return owner == null;
        if (level != null && !level.isClientSide) {
            if (owner == null) { owner = profile; claimed.run(); return true; }
            if (isOp(level, profile)) return true;
        } else if (owner == null) return true;
        return owner.getId().equals(profile.getId());
    }

    public Component ownerMessage() { return Component.literal("This safe is owned by " + (owner == null ? "nobody" : owner.getName())); }

    public static boolean isAdmin(Player player) { return isOp(player.level(), player.getGameProfile()); }
}
