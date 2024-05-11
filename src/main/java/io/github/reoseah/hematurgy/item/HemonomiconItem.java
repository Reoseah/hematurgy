package io.github.reoseah.hematurgy.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HemonomiconItem extends Item {
    /**
     * Property added to lectern block with mixins. True when a lectern holds Hemonomicon.
     */
    public static final BooleanProperty IS_BOOK_HEMONOMICON = BooleanProperty.of("is_book_hemonomicon");

    public static final Item INSTANCE = new HemonomiconItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof());

    protected HemonomiconItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    public static boolean handleLecternInteraction(PlayerEntity player, World world, Hand hand, ItemStack stack, BlockPos pos, BlockState state, LecternBlockEntity lectern) {
        ItemStack lecternStack = lectern.getBook();

        if (lecternStack.isEmpty() && stack.isOf(HemonomiconItem.INSTANCE)) {
            return LecternBlock.putBookIfAbsent(player, world, pos, state, stack);
        }

        if (lecternStack.isOf(HemonomiconItem.INSTANCE)) {
            if (!world.isClient) {
                if (player.isSneaking()) {
                    lectern.setBook(ItemStack.EMPTY);
                    LecternBlock.setHasBook(player, world, pos, state, false);
                    if (!player.getInventory().insertStack(lecternStack)) {
                        player.dropItem(lecternStack, false);
                    }
                } else {
//                    player.openHandledScreen(new NamedScreenHandlerFactory() {
//                        @Override
//                        public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player1) {
//                            return new HemonomiconScreenHandler(syncId, playerInv, new HemonomiconScreenHandler.LecternContext(world, pos, lecternStack));
//                        }
//
//                        @Override
//                        public Text getDisplayName() {
//                            return lecternStack.getName();
//                        }
//                    });
                }
            }
            return true;
        }
        return false;
    }
}
