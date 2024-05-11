package io.github.reoseah.hematurgy.mixin;


import io.github.reoseah.hematurgy.item.HemonomiconItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public class LecternBlockMixin extends Block {
    @Unique
    private static final BooleanProperty IS_BOOK_HEMONOMICON = HemonomiconItem.IS_BOOK_HEMONOMICON;

    public LecternBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    protected void init(AbstractBlock.Settings settings, CallbackInfo ci) {
        this.setDefaultState(this.getDefaultState().with(IS_BOOK_HEMONOMICON, false));
    }

    @Inject(at = @At("HEAD"), method = "appendProperties")
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(IS_BOOK_HEMONOMICON);
    }

    @Inject(at = @At("RETURN"), method = "setHasBook")
    private static void setHasBook(Entity user, World world, BlockPos pos, BlockState state, boolean hasBook, CallbackInfo ci) {
        if (world.getBlockEntity(pos) instanceof LecternBlockEntity be) {
            boolean isHemonomicon = be.getBook().isOf(HemonomiconItem.INSTANCE);
            world.setBlockState(pos, world.getBlockState(pos).with(IS_BOOK_HEMONOMICON, isHemonomicon));
//            if (!world.isClient && world instanceof ServerWorld serverWorld) {
//                ActivityTracker tracker = ActivityTracker.get(serverWorld);
//                if (isHemonomicon) {
//                    tracker.getOrCreateLectern(pos);
//                } else {
//                    tracker.removeLectern(pos);
//                }
//            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (state.get(IS_BOOK_HEMONOMICON) && random.nextInt(4) == 0) {
            float dx = world.random.nextFloat();
            float dy = 1.1F + world.random.nextFloat() - 0.5F;
            float dz = world.random.nextFloat();

            world.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + dx,
                    pos.getY() + dy,
                    pos.getZ() + dz,
                    0.0D, 0.0D, 0.0D);
        }
    }
}
