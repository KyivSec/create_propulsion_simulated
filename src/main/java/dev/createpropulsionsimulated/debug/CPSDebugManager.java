package dev.createpropulsionsimulated.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CPSDebugManager {
    private static final Set<UUID> DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();

    private CPSDebugManager() {
    }

    public static boolean setEnabled(final ServerPlayer player, final boolean enabled) {
        final UUID id = player.getUUID();
        if (enabled) {
            return DEBUG_PLAYERS.add(id);
        }
        return DEBUG_PLAYERS.remove(id);
    }

    public static boolean isEnabled(final ServerPlayer player) {
        return DEBUG_PLAYERS.contains(player.getUUID());
    }

    public static boolean hasWatchersNear(final ServerLevel level, final BlockPos pos, final double maxDistance) {
        final double maxDistanceSq = maxDistance * maxDistance;
        for (final ServerPlayer player : level.players()) {
            if (!DEBUG_PLAYERS.contains(player.getUUID())) {
                continue;
            }
            if (player.distanceToSqr(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d) <= maxDistanceSq) {
                return true;
            }
        }
        return false;
    }
}
