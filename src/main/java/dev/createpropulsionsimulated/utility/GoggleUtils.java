package dev.createpropulsionsimulated.utility;

import net.minecraft.ChatFormatting;

public final class GoggleUtils {
    private GoggleUtils() {
    }

    public static String makeObstructionBar(final int unobstructedLength, final int totalLength) {
        final int total = Math.max(1, totalLength);
        final int unobstructed = Math.clamp(unobstructedLength, 0, total);
        final int obstructed = total - unobstructed;
        return " " + "█".repeat(unobstructed) + "▒".repeat(obstructed) + " ";
    }

    public static ChatFormatting efficiencyColor(final float efficiency) {
        if (efficiency < 10) {
            return ChatFormatting.RED;
        }
        if (efficiency < 60) {
            return ChatFormatting.GOLD;
        }
        if (efficiency < 100) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.GREEN;
    }
}
