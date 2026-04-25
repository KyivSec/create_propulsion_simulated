package dev.createpropulsionsimulated.content.thruster;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.createpropulsionsimulated.config.ThrusterConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class CreativeThrusterPowerScrollValueBehaviour extends ScrollValueBehaviour {
    protected static final int TOTAL_STEPS = 100;
    private final int maxThrust;
    private final int forcePerStep;

    public CreativeThrusterPowerScrollValueBehaviour(final SmartBlockEntity be) {
        super(Component.translatable("createpropulsionsimulated.gui.creative_thruster.power_behaviour"), be, new CreativeThrusterValueBox());
        this.maxThrust = Math.max(10, (int) Math.round(ThrusterConfig.CREATIVE_THRUSTER_MAX_THRUST.get()));
        this.forcePerStep = Math.max(1, this.maxThrust / TOTAL_STEPS);
        this.between(0, TOTAL_STEPS - 1);
    }

    public float getTargetThrust() {
        return Math.min(this.maxThrust, (this.value + 1) * this.forcePerStep);
    }

    public void setTargetThrust(final int targetThrust) {
        final int clampedThrust = Math.clamp(targetThrust, this.forcePerStep, this.maxThrust);
        final int targetValue = Math.clamp((int) Math.ceil(clampedThrust / (double) this.forcePerStep) - 1, 0, TOTAL_STEPS - 1);
        this.setValue(targetValue);
    }

    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        final ImmutableList<Component> row = ImmutableList.of(CreateLang.builder().text("pN").component());
        return new ValueSettingsBoard(this.label, TOTAL_STEPS - 1, 10, row, new ValueSettingsFormatter(this::formatBoardValue));
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings valueSetting, final boolean ctrlHeld) {
        int newValue = valueSetting.value();
        newValue = Math.max(0, Math.min(newValue, TOTAL_STEPS - 1));
        if (this.getValue() == newValue) {
            return;
        }
        this.setValue(newValue);
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, this.value);
    }

    @Override
    public String formatValue() {
        final int forceInPixelNewtons = Math.min(this.maxThrust, (this.value + 1) * this.forcePerStep);
        return Integer.toString(forceInPixelNewtons);
    }

    public MutableComponent formatBoardValue(final ValueSettings settings) {
        final int forceInPixelNewtons = Math.min(this.maxThrust, (settings.value() + 1) * this.forcePerStep);
        return CreateLang.builder().add(CreateLang.number(forceInPixelNewtons)).text(" pN").component();
    }
}
