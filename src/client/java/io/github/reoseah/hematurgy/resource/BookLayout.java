package io.github.reoseah.hematurgy.resource;

import io.github.reoseah.hematurgy.resource.book_element.BookElementWithSlots;
import io.github.reoseah.hematurgy.resource.book_element.BookSlot;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.gui.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record BookLayout(Int2ObjectMap<List<Drawable>> pages, IntList chapterPages) {
    public BookLayout(Int2ObjectMap<List<Drawable>> pages, IntList chapterPages) {
        this.pages = Int2ObjectMaps.unmodifiable(pages);
        this.chapterPages = IntLists.unmodifiable(chapterPages);
    }

    public int getPageCount() {
        return this.pages.keySet().intStream().max().orElse(1);
    }

    public List<Drawable> getPage(int page) {
        return this.pages.getOrDefault(page, Collections.emptyList());
    }

    public BookSlot[] getFoldSlots(int leftPage) {
        // TODO build and keep a separate map of slots per page
        Stream<BookSlot> leftSlots = this.getPage(leftPage).stream()
                .filter(drawable -> drawable instanceof BookElementWithSlots)
                .flatMap(drawable -> Arrays.stream(((BookElementWithSlots) drawable).getSlots()));
        Stream<BookSlot> rightSlots = this.getPage(leftPage + 1).stream()
                .filter(drawable -> drawable instanceof BookElementWithSlots)
                .flatMap(drawable -> Arrays.stream(((BookElementWithSlots) drawable).getSlots()));

        return Stream.concat(leftSlots, rightSlots).limit(16).toArray(BookSlot[]::new);
    }

    public static class Builder {
        /**
         * The x coordinates of the left and right pages. See {@link #getCurrentX()} and {@link #getNextX()}.
         */
        private final int leftX, rightX;
        private final int paddingTop;
        private final int pageHeight;

        private final Int2ObjectMap<List<Drawable>> pages = new Int2ObjectArrayMap<>();
        private final IntList chapterPages = new IntArrayList();

        private int currentPageIdx;
        private int currentY;
        private boolean allowWrap = true;

        public Builder(BookProperties properties) {
            this.leftX = properties.leftPageOffset;
            this.rightX = properties.rightPageOffset;
            this.paddingTop = properties.topOffset;
            this.pageHeight = properties.pageHeight;

            this.currentPageIdx = 0;
            this.currentY = this.paddingTop;
        }

        public BookLayout build() {
            return new BookLayout(this.pages, this.chapterPages);
        }

        public int getCurrentPage() {
            return this.currentPageIdx;
        }

        public void addWidget(Drawable drawable) {
            this.pages.computeIfAbsent(this.currentPageIdx, ArrayList::new).add(drawable);
        }

        public void advancePage() {
            if (this.allowWrap) {
                this.currentPageIdx++;
                this.currentY = this.paddingTop;
            }
        }

        public boolean isNewPage() {
            return this.currentY == this.paddingTop;
        }

        public int getCurrentY() {
            return this.currentY;
        }

        public void setCurrentY(int newY) {
            if (newY < this.getMaxY()) {
                this.currentY = newY;
            } else if (this.allowWrap) {
                this.advancePage();
            }
        }

        public int getCurrentX() {
            return this.currentPageIdx % 2 == 0 ? this.leftX : this.rightX;
        }

        public int getNextX() {
            return this.currentPageIdx % 2 == 0 ? this.rightX : this.leftX;
        }

        public int getMinY() {
            return this.paddingTop;
        }

        public int getMaxY() {
            return this.paddingTop + this.pageHeight;
        }

        public void markPageAsChapter() {
            this.chapterPages.add(this.currentPageIdx);
        }

        public int getCurrentChapter() {
            return this.chapterPages.size();
        }

        public boolean isWrapAllowed() {
            return this.allowWrap;
        }

        public void allowWrap(boolean allowWrap) {
            this.allowWrap = allowWrap;
        }
    }
}
