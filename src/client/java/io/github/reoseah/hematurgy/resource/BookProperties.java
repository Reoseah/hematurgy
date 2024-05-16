package io.github.reoseah.hematurgy.resource;

import net.minecraft.util.Identifier;

public final class BookProperties {
    public final Identifier texture;
    public final int pageWidth;
    public final int pageHeight;
    public final int topOffset;
    public final int leftPageOffset;
    public final int rightPageOffset;
    public final int bookmarkOffset;
    public final int bookmarkHeight;
    public final int bookmarkFullWidth;
    public final int bookmarkFullU;
    public final int bookmarkFullV;
    public final int bookmarkHiddenWidth;
    public final int bookmarkHiddenU;
    public final int bookmarkHiddenV;
    public final int slotU;
    public final int slotV;
    public final int resultSlotU;
    public final int resultSlotV;

    public BookProperties(Identifier texture, int pageWidth, int pageHeight, int topOffset, int leftPageOffset,
                          int rightPageOffset, int bookmarkOffset, int bookmarkHeight, int bookmarkFullWidth,
                          int bookmarkFullU, int bookmarkFullV, int bookmarkHiddenWidth, int bookmarkHiddenU,
                          int bookmarkHiddenV, int slotU, int slotV, int resultSlotU, int resultSlotV) {
        this.texture = texture;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.topOffset = topOffset;
        this.leftPageOffset = leftPageOffset;
        this.rightPageOffset = rightPageOffset;
        this.bookmarkOffset = bookmarkOffset;
        this.bookmarkHeight = bookmarkHeight;
        this.bookmarkFullWidth = bookmarkFullWidth;
        this.bookmarkFullU = bookmarkFullU;
        this.bookmarkFullV = bookmarkFullV;
        this.bookmarkHiddenWidth = bookmarkHiddenWidth;
        this.bookmarkHiddenU = bookmarkHiddenU;
        this.bookmarkHiddenV = bookmarkHiddenV;
        this.slotU = slotU;
        this.slotV = slotV;
        this.resultSlotU = resultSlotU;
        this.resultSlotV = resultSlotV;
    }

    public int getBookmarkY(int idx) {
        return this.bookmarkOffset + idx * this.bookmarkHeight;
    }

    public int getBookmarkX(boolean isLeft) {
        if (isLeft) {
            return 256 / 2 - this.bookmarkFullWidth;
        } else {
            return 256 / 2 + this.bookmarkFullWidth - this.bookmarkHiddenWidth;
        }
    }
}
