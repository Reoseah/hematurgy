package io.github.reoseah.hematurgy.network;

import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HemonomiconNetworking {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(UseBookmarkCustomPayload.ID, UseBookmarkCustomPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkCustomPayload.ID, (payload, context) -> {
            System.out.println("Use bookmark received");
            if (context.player().currentScreenHandler instanceof HemonomiconScreenHandler hemonomiconScreen) {
                hemonomiconScreen.currentPage.set(payload.page());
            }
        });
    }
}
