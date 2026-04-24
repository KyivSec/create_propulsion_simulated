package dev.createpropulsionsimulated.content.thruster;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.config.ThrusterConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ThrusterFuelRegistry {
    public static final TagKey<Fluid> FUEL_TAG = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "fuel"));
    public static final TagKey<Fluid> TURPENTINE_TAG = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath("c", "turpentine"));

    private static volatile List<String> cachedConfigEntries = List.of();
    private static volatile Map<ResourceLocation, FluidThrusterProperties> cachedProfiles = Map.of();

    private ThrusterFuelRegistry() {
    }

    public static boolean isFuel(final FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        final Fluid fluid = stack.getFluid();
        return fluid.defaultFluidState().is(FUEL_TAG)
                || fluid.defaultFluidState().is(TURPENTINE_TAG)
                || propertiesByFluidId().containsKey(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static Optional<FluidThrusterProperties> getProperties(final FluidStack stack) {
        if (!isFuel(stack)) {
            return Optional.empty();
        }
        final ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return Optional.of(propertiesByFluidId().getOrDefault(fluidId, FluidThrusterProperties.DEFAULT));
    }

    private static Map<ResourceLocation, FluidThrusterProperties> propertiesByFluidId() {
        final List<? extends String> currentEntries = ThrusterConfig.FUEL_PROPERTIES.get();
        if (!cachedConfigEntries.equals(currentEntries)) {
            synchronized (ThrusterFuelRegistry.class) {
                if (!cachedConfigEntries.equals(currentEntries)) {
                    final Map<ResourceLocation, FluidThrusterProperties> rebuilt = new HashMap<>();
                    for (final String rawEntry : currentEntries) {
                        parseEntry(rawEntry).ifPresent(entry -> rebuilt.put(entry.fluidId(), entry.properties()));
                    }
                    cachedProfiles = Map.copyOf(rebuilt);
                    cachedConfigEntries = List.copyOf(currentEntries);
                }
            }
        }
        return cachedProfiles;
    }

    private static Optional<ParsedFuelEntry> parseEntry(final String rawEntry) {
        if (rawEntry == null) {
            return Optional.empty();
        }
        final String entry = rawEntry.trim();
        if (entry.isEmpty()) {
            return Optional.empty();
        }

        final String[] assignment = entry.split("=", 2);
        if (assignment.length != 2) {
            return Optional.empty();
        }
        final ResourceLocation fluidId = ResourceLocation.tryParse(assignment[0].trim());
        if (fluidId == null) {
            return Optional.empty();
        }

        final String[] values = assignment[1].trim().split(",", 2);
        if (values.length != 2) {
            return Optional.empty();
        }
        try {
            final float thrustPercent = Float.parseFloat(values[0].trim());
            final float burnPercent = Float.parseFloat(values[1].trim());
            if (!Float.isFinite(thrustPercent) || !Float.isFinite(burnPercent) || thrustPercent < 0.0f || burnPercent < 0.0f) {
                return Optional.empty();
            }
            return Optional.of(new ParsedFuelEntry(
                    fluidId,
                    new FluidThrusterProperties(thrustPercent / 100.0f, burnPercent / 100.0f)
            ));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static String formatEntry(final String fluidId, final int thrustPercent, final int burnPercent) {
        return String.format(Locale.ROOT, "%s=%d,%d", fluidId, thrustPercent, burnPercent);
    }

    private record ParsedFuelEntry(ResourceLocation fluidId, FluidThrusterProperties properties) {
    }
}
