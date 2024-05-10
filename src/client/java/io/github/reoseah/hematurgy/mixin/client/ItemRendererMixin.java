package io.github.reoseah.hematurgy.mixin.client;

import io.github.reoseah.hematurgy.item.HemonomiconItem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    private @Final ItemModels models;
    @Shadow
    private @Final BuiltinModelItemRenderer builtinModelItemRenderer;

    @Shadow
    protected abstract void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);

    @Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
    public void setHemonomiconModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> ci) {
        Item item = stack.getItem();
        if (item == HemonomiconItem.INSTANCE) {
            BakedModel model = this.models.getModelManager().getModel(new ModelIdentifier("hematurgy", "hemonomicon_in_hand", "inventory"));
            ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
            model = model.getOverrides().apply(model, stack, clientWorld, entity, seed);
            ci.setReturnValue(model == null ? this.models.getModelManager().getMissingModel() : model);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderItem", cancellable = true)
    public void renderHemonomicon(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() == HemonomiconItem.INSTANCE) {
            matrices.push();
            boolean gui = mode == ModelTransformationMode.GUI;
            boolean notInHand = gui || mode == ModelTransformationMode.GROUND || mode == ModelTransformationMode.FIXED;
            if (notInHand) {
                model = this.models.getModelManager().getModel(new ModelIdentifier("hematurgy", "hemonomicon", "inventory"));
            }
            model.getTransformation().getTransformation(mode).apply(leftHanded, matrices);
            matrices.translate(-0.5D, -0.5D, -0.5D);
            if (model.isBuiltin() || !notInHand) {
                this.builtinModelItemRenderer.render(stack, mode, matrices, vertexConsumers, light, overlay);
            } else {
                RenderLayer itemLayer = RenderLayers.getItemLayer(stack, true);
                RenderLayer layer;
                if (gui && Objects.equals(itemLayer, TexturedRenderLayers.getEntityTranslucentCull())) {
                    layer = TexturedRenderLayers.getEntityTranslucentCull();
                } else {
                    layer = itemLayer;
                }

                VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, layer, true, stack.hasGlint());
                this.renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer);
            }
            matrices.pop();
            ci.cancel();
        }
    }
}