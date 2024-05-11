package io.github.reoseah.hematurgy;

import io.github.reoseah.hematurgy.resource.BookLoader;
import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import io.github.reoseah.hematurgy.screen.client.HemonomiconScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;

public class HematurgyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(ctx -> ctx.addModels(new ModelIdentifier("hematurgy", "hemonomicon_in_hand", "inventory")));

        ResourceManagerHelperImpl.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new BookLoader());

        HandledScreens.register(HemonomiconScreenHandler.TYPE, HemonomiconScreen::new);
    }
}