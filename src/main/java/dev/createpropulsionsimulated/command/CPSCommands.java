package dev.createpropulsionsimulated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.createpropulsionsimulated.content.thruster.ThrusterBlockEntity;
import dev.createpropulsionsimulated.debug.CPSDebugManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber
public final class CPSCommands {
    private CPSCommands() {
    }

    @SubscribeEvent
    public static void register(final RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("cps")
                .then(Commands.literal("debug")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(CPSCommands::toggleDebug)))
                .then(Commands.literal("scan")
                        .executes(CPSCommands::scanNearbyThrusters))
        );
    }

    private static int toggleDebug(final CommandContext<CommandSourceStack> context) {
        final ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        final boolean enabled = BoolArgumentType.getBool(context, "enabled");
        CPSDebugManager.setEnabled(player, enabled);

        final Component msg = Component.literal("[CPS] Force vector debug: " + (enabled ? "ON" : "OFF"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GOLD);
        context.getSource().sendSuccess(() -> msg, false);
        return 1;
    }

    private static int scanNearbyThrusters(final CommandContext<CommandSourceStack> context) {
        final ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        final int radius = 32;
        final BlockPos center = player.blockPosition();
        final List<ThrusterBlockEntity> thrusters = new ArrayList<>();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    final var blockEntity = player.serverLevel().getBlockEntity(new BlockPos(x, y, z));
                    if (blockEntity instanceof ThrusterBlockEntity thruster) {
                        thrusters.add(thruster);
                    }
                }
            }
        }

        thrusters.sort(Comparator.comparingDouble(t -> t.getBlockPos().distSqr(center)));

        context.getSource().sendSuccess(() -> Component.literal("[CPS] Nearby thrusters: " + thrusters.size()).withStyle(ChatFormatting.AQUA), false);

        final int limit = Math.min(10, thrusters.size());
        for (int i = 0; i < limit; i++) {
            final ThrusterBlockEntity t = thrusters.get(i);
            final Component line = Component.literal(" - " + t.getBlockPos() + " facing=" + t.getFacing()
                    + " thrust=" + String.format("%.2f", t.getCurrentThrust())
                    + " throttle=" + String.format("%.2f", t.getThrottle())
                    + " fuelMb=" + t.getFuelAmountMb()
                    + " clear=" + t.getUnobstructedBlocks())
                    .withStyle(ChatFormatting.GRAY);
            context.getSource().sendSuccess(() -> line, false);
        }

        return thrusters.size();
    }
}
