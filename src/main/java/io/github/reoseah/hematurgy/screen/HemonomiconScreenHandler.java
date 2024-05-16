package io.github.reoseah.hematurgy.screen;

import io.github.reoseah.hematurgy.item.HemonomiconItem;
import io.github.reoseah.hematurgy.resource.book_element.SlotConfiguration;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HemonomiconScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<HemonomiconScreenHandler> TYPE = new ScreenHandlerType<>(HemonomiconScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final int PREVIOUS_PAGE_BUTTON = 0;
    public static final int NEXT_PAGE_BUTTON = 1;

    public final Context context;
    public final Property currentPage;
    public final Property isUttering;
    public final Inventory inventory = new HemonomiconInventory(this);

    public HemonomiconScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new ClientContext());
    }

    public HemonomiconScreenHandler(int syncId, PlayerInventory playerInv, Context context) {
        super(TYPE, syncId);
        this.context = context;
        this.currentPage = this.addProperty(context.createProperty(HemonomiconItem.CURRENT_PAGE));
        this.isUttering = this.addProperty(Property.create());

        for (int i = 0; i < 16; i++) {
            this.addSlot(new ConfigurableSlot(this.inventory, i, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }

        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInv, x, 48 + x * 18, 185));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.context.canUse(player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        switch (id) {
            case PREVIOUS_PAGE_BUTTON -> {
                int page = this.currentPage.get();
                if (page < 2) {
                    return false;
                }
                this.currentPage.set(page - 2);
//                this.dropInventory(player, this.inventory);

//                this.context.onHemonomiconRead(player.getWorld().getTime());
                return true;
            }
            case NEXT_PAGE_BUTTON -> {
                int page = this.currentPage.get();
                this.currentPage.set(page + 2);
//                this.dropInventory(player, this.inventory);

//                this.context.onHemonomiconRead(player.getWorld().getTime());
                return true;
            }
//            case START_UTTERANCE -> {
//                this.isUttering.set(1);
//                return true;
//            }
//            case STOP_UTTERANCE -> {
//                context.onUnfinishedUtterance(player.getWorld().getTime());
//                this.onStoppedUttering();
//                return true;
//            }
        }
        return false;
    }
    public void configureSlots(SlotConfiguration[] definitions) {
        for (int i = 0; i < definitions.length; i++) {
            ((ConfigurableSlot) this.slots.get(i)).setConfiguration(definitions[i]);
        }
        for (int i = definitions.length; i < 16; i++) {
            ((ConfigurableSlot) this.slots.get(i)).setConfiguration(null);
        }
    }

    public static abstract class Context {
        public abstract Property createProperty(DataComponentType<Integer> component);

        public abstract boolean canUse(PlayerEntity player);

//        public abstract ActivitySource getActivitySource();
    }

    public static class ClientContext extends Context {
        @Override
        public Property createProperty(DataComponentType<Integer> component) {
            return Property.create();
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

//        @Override
//        public ActivitySource getActivitySource() {
//            return null;
//        }
    }

    public static class LecternContext extends Context {
        private final World world;
        private final BlockPos pos;
        private final ItemStack stack;

        public LecternContext(World world, BlockPos pos, ItemStack stack) {
            this.world = world;
            this.pos = pos;
            this.stack = stack;
        }

        @Override
        public Property createProperty(DataComponentType<Integer> component) {
            return new Property() {
                @Override
                public int get() {
                    return stack.getOrDefault(component, 0);
                }

                @Override
                public void set(int value) {
                    stack.set(component, value);

                    BlockEntity be = world.getBlockEntity(pos);
                    if (be != null) {
                        be.markDirty();
                    }
                }
            };
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return this.world.getBlockState(this.pos).getBlock() instanceof LecternBlock && this.world.getBlockEntity(this.pos) instanceof LecternBlockEntity lectern && lectern.getBook() == this.stack && player.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
        }

//        @Override
//        public ActivitySource getActivitySource() {
//            if (this.world instanceof ServerWorld serverWorld) {
//                ActivityTracker tracker = ActivityTracker.get(serverWorld);
//                return tracker.getOrCreateLectern(this.pos);
//            }
//            return null;
//        }
    }

    public static class HandContext extends Context {
        private final Hand hand;
        private final ItemStack stack;

        public HandContext(Hand hand, ItemStack stack) {
            this.hand = hand;
            this.stack = stack;
        }

        @Override
        public Property createProperty(DataComponentType<Integer> component) {
            return new Property() {
                @Override
                public int get() {
                    return stack.getOrDefault(component, 0);
                }

                @Override
                public void set(int value) {
                    stack.set(component, value);
                }
            };
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return player.getStackInHand(this.hand) == this.stack;
        }

//        @Override
//        public ActivitySource getActivitySource() {
//            return null;
//        }
    }

    private static class HemonomiconInventory extends SimpleInventory {
        private final ScreenHandler handler;

        public HemonomiconInventory(ScreenHandler handler) {
            super(16);
            this.handler = handler;
        }

        public ItemStack removeStack(int slot, int amount) {
            ItemStack stack = super.removeStack(slot, amount);
            if (!stack.isEmpty()) {
                this.handler.onContentChanged(this);
            }
            return stack;
        }

        public void setStack(int slot, ItemStack stack) {
            super.setStack(slot, stack);
            this.handler.onContentChanged(this);
        }
    }
}
