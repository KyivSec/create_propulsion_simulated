package dev.createpropulsionsimulated.content.ponder;

import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CPSPonderPlugin extends CreatePonderPlugin {
    @Override
    public String getModId() {
        return CreatePropulsionSimulated.MOD_ID;
    }

    @Override
    public void registerScenes(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CPSPonderScenes.register(helper);
    }

    @Override
    public void registerTags(final PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    @Override
    public void registerSharedText(final SharedTextRegistrationHelper helper) {
    }
}
