package io.github.reoseah.hematurgy.network;

import io.github.reoseah.hematurgy.Hematurgy;
import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HemonomiconNetworking {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(UseBookmarkPayload.ID, UseBookmarkPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StartUtterancePayload.ID, StartUtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StopUtterancePayload.ID, StopUtterancePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof HemonomiconScreenHandler hemonomiconScreen) {
                hemonomiconScreen.currentPage.set(payload.page());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof HemonomiconScreenHandler hemonomiconScreen) {
                // TODO: Start utterance
                Hematurgy.LOGGER.warn("Utterances are not yet implemented.");
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof HemonomiconScreenHandler hemonomiconScreen) {
                // TODO: Stop utterance
                Hematurgy.LOGGER.warn("Utterances are not yet implemented.");
            }
        });
    }
}
