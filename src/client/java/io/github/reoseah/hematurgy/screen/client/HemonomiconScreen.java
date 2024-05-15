package io.github.reoseah.hematurgy.screen.client;

import io.github.reoseah.hematurgy.network.UseBookmarkPayload;
import io.github.reoseah.hematurgy.resource.BookLayout;
import io.github.reoseah.hematurgy.resource.BookLoader;
import io.github.reoseah.hematurgy.resource.BookProperties;
import io.github.reoseah.hematurgy.resource.book_element.BookSlot;
import io.github.reoseah.hematurgy.resource.book_element.Chapter;
import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import io.github.reoseah.hematurgy.screen.MutableSlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class HemonomiconScreen extends HandledScreen<HemonomiconScreenHandler> {
    public static final Identifier TEXTURE = new Identifier("hematurgy:textures/gui/hemonomicon.png");

    private static final int PAGE_WIDTH = 102;
    private static final int PAGE_HEIGHT = 140;
    private static final int TOP_OFFSET = 15;
    private static final int LEFT_PAGE_OFFSET = 17;
    private static final int RIGHT_PAGE_OFFSET = 138;

    private static final int BOOKMARK_OFFSET = TOP_OFFSET + 4;
    private static final int BOOKMARK_HEIGHT = 20;
    private static final int FULL_BOOKMARK_WIDTH = 135;
    private static final int FULL_BOOKMARK_U = 64;
    private static final int FULL_BOOKMARK_V = 192;
    private static final int HIDDEN_BOOKMARK_WIDTH = 16;
    private static final int HIDDEN_BOOKMARK_U = 224;
    private static final int HIDDEN_BOOKMARK_V = 192;

    private static final int PLAYER_SLOTS_U = 0;
    private static final int PLAYER_SLOTS_V = 224;
    private static final int PLAYER_SLOTS_HEIGHT = 32;
    private static final int PLAYER_SLOTS_WIDTH = 176;

    private static final int SLOT_U = 202;
    private static final int SLOT_V = 224;
    private static final int RESULT_SLOT_U = 176;
    private static final int RESULT_SLOT_V = 224;

    private final BookProperties properties = new BookProperties(TEXTURE, PAGE_WIDTH, PAGE_HEIGHT, TOP_OFFSET, LEFT_PAGE_OFFSET, RIGHT_PAGE_OFFSET, BOOKMARK_OFFSET, BOOKMARK_HEIGHT, FULL_BOOKMARK_WIDTH, FULL_BOOKMARK_U, FULL_BOOKMARK_V, HIDDEN_BOOKMARK_WIDTH, HIDDEN_BOOKMARK_U, HIDDEN_BOOKMARK_V);
    private BookLayout layout;

    private PageTurnWidget previousPageButton;
    private PageTurnWidget nextPageButton;

    private int lastCurrentPage;
    private List<Drawable> leftPage;
    private List<Drawable> rightPage;

    private int playerSlotsY;

    public HemonomiconScreen(HemonomiconScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 180;
        this.playerInventoryTitleX = Integer.MIN_VALUE;
        this.playerInventoryTitleY = Integer.MIN_VALUE;
        this.titleX = Integer.MIN_VALUE;
        this.titleY = Integer.MIN_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        if (this.layout == null) {
            this.layout = BookLoader.buildLayout(this.properties, this.textRenderer);
        }

        this.clearChildren();
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 26, this.y + 156, false, button -> {
            this.client.interactionManager.clickButton(this.handler.syncId, HemonomiconScreenHandler.PREVIOUS_PAGE_BUTTON);
            this.handler.currentPage.set(this.handler.currentPage.get() - 2);
        }, true));
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 206, this.y + 156, true, button -> {
            this.client.interactionManager.clickButton(this.handler.syncId, HemonomiconScreenHandler.NEXT_PAGE_BUTTON);
            this.handler.currentPage.set(this.handler.currentPage.get() + 2);
        }, true));
        this.onPageChange();

        this.playerSlotsY = Math.min(this.backgroundHeight + 4, this.height - this.y - PLAYER_SLOTS_HEIGHT);
        for (Slot slot : this.handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                ((MutableSlot) slot).hematurgy$setPos(slot.x, this.playerSlotsY + 8);
            }
        }
    }

    private void onPageChange() {
        int page = this.handler.currentPage.get();
        this.previousPageButton.visible = page > 0;
        this.nextPageButton.visible = page + 1 < this.layout.getPageCount();

        this.leftPage = this.layout.getPage(page);
        this.rightPage = this.layout.getPage(page + 1);

        this.updateSlots();

        this.lastCurrentPage = page;
        this.setFocused(null);
    }

    private void updateSlots() {
        BookSlot[] slots = this.layout.getFoldSlots(this.handler.currentPage.get());

//        this.handler.updateSlots(slots);
//        ClientPlayNetworking.send(HemonomiconPackets.PAGE_SLOTS, Util.make(PacketByteBufs.create(), buf -> {
//            buf.writeVarInt(slots.length);
//            for (SlotDefinition slot : slots) {
//                slot.write(buf);
//            }
//        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (this.lastCurrentPage != this.handler.currentPage.get()) {
            this.onPageChange();
        }

        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);

        context.drawTexture(this.properties.texture, 0, 0, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(this.properties.texture, (this.backgroundWidth - PLAYER_SLOTS_WIDTH) / 2, this.playerSlotsY, PLAYER_SLOTS_U, PLAYER_SLOTS_V, PLAYER_SLOTS_WIDTH, PLAYER_SLOTS_HEIGHT);

        mouseX -= this.x;
        mouseY -= this.y;
        for (Drawable element : this.leftPage) {
            element.render(context, mouseX, mouseY, delta);
        }
        for (Drawable element : this.rightPage) {
            element.render(context, mouseX, mouseY, delta);
        }

        int currentPage = this.handler.currentPage.get();
        int i = 0;
        for (Int2ObjectMap.Entry<Chapter> entry : this.layout.chapters().int2ObjectEntrySet()) {
            int chapterPage = entry.getIntKey();
            if (chapterPage != currentPage) {
                int bookmarkY = this.properties.getBookmarkY(i);
                int bookmarkX = 256 / 2 + (chapterPage > currentPage ? this.properties.bookmarkFullWidth - this.properties.bookmarkHiddenWidth : -this.properties.bookmarkFullWidth);

                boolean hovered = mouseX > bookmarkX && mouseX < bookmarkX + this.properties.bookmarkHiddenWidth && mouseY > bookmarkY && mouseY < bookmarkY + this.properties.bookmarkHeight;

                if (chapterPage < currentPage) {
                    context.drawTexture(this.properties.texture, bookmarkX, bookmarkY, this.properties.bookmarkHiddenU, this.properties.bookmarkHiddenV + (hovered ? this.properties.bookmarkHeight : 0), this.properties.bookmarkHiddenWidth, this.properties.bookmarkHeight);
                } else {
                    context.drawTexture(this.properties.texture, bookmarkX, bookmarkY, this.properties.bookmarkHiddenU + this.properties.bookmarkHiddenWidth, this.properties.bookmarkHiddenV + (hovered ? this.properties.bookmarkHeight : 0), this.properties.bookmarkHiddenWidth, this.properties.bookmarkHeight);
                }
                if (hovered) {
                    context.drawTooltip(this.textRenderer, Text.translatable(entry.getValue().translationKey), mouseX, mouseY);
                }
            }
            i++;
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int currentPage = this.handler.currentPage.get();
        int i = 0;
        for (Int2ObjectMap.Entry<Chapter> entry : this.layout.chapters().int2ObjectEntrySet()) {
            int chapterPage = entry.getIntKey();

            if (chapterPage != currentPage) {
                int bookmarkY = this.y + this.properties.getBookmarkY(i);
                int bookmarkX = this.x + this.properties.getBookmarkX(chapterPage < currentPage);

                if (mouseX > bookmarkX && mouseX < bookmarkX + this.properties.bookmarkHiddenWidth && mouseY > bookmarkY && mouseY < bookmarkY + this.properties.bookmarkHeight) {
                    ClientPlayNetworking.send(new UseBookmarkPayload(chapterPage));

                    this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
                    return true;
                }
            }
            i++;
        }

        for (Drawable drawable : this.leftPage) {
            if (drawable instanceof Element element) {
                if (element.mouseClicked(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        for (Drawable drawable : this.rightPage) {
            if (drawable instanceof Element element) {
                if (element.mouseClicked(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Drawable drawable : this.leftPage) {
            if (drawable instanceof Element element) {
                if (element.mouseReleased(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        for (Drawable drawable : this.rightPage) {
            if (drawable instanceof Element element) {
                if (element.mouseReleased(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        if (mouseY >= this.y + this.playerSlotsY && mouseY <= this.y + this.playerSlotsY + PLAYER_SLOTS_HEIGHT) {
            return mouseX < left || mouseX >= (left + this.backgroundWidth);
        }
        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }
}