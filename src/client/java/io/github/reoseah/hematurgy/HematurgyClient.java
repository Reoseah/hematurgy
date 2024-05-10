package io.github.reoseah.hematurgy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;

public class HematurgyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(ctx -> ctx.addModels(new ModelIdentifier("hematurgy", "hemonomicon_in_hand", "inventory")));
	}
}