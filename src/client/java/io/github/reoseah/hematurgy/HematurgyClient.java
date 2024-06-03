package io.github.reoseah.hematurgy;

import io.github.reoseah.hematurgy.item.BloodSourceComponent;
import io.github.reoseah.hematurgy.item.RitualSickleItem;
import io.github.reoseah.hematurgy.item.SentientBladeItem;
import io.github.reoseah.hematurgy.item.SyringeItem;
import io.github.reoseah.hematurgy.resource.BookLoader;
import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import io.github.reoseah.hematurgy.screen.client.HemonomiconScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class HematurgyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(ctx -> ctx.addModels(new ModelIdentifier("hematurgy", "hemonomicon_in_hand", "inventory")));

        ModelPredicateProviderRegistry.register(RitualSickleItem.INSTANCE, new Identifier("hematurgy:has_target"),
                (stack, world, entity, seed) -> stack.contains(BloodSourceComponent.TYPE) ? 1F : 0F);
        ModelPredicateProviderRegistry.register(SentientBladeItem.INSTANCE, new Identifier("hematurgy:has_target"),
                (stack, world, entity, seed) -> stack.contains(BloodSourceComponent.TYPE) ? 1F : 0F);
        ModelPredicateProviderRegistry.register(SyringeItem.INSTANCE, new Identifier("hematurgy:has_target"),
                (stack, world, entity, seed) -> stack.contains(BloodSourceComponent.TYPE) ? 1F : 0F);


        ResourceManagerHelperImpl.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new BookLoader());

        HandledScreens.register(HemonomiconScreenHandler.TYPE, HemonomiconScreen::new);
    }
}