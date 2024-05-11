package io.github.reoseah.hematurgy.resource.book;

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

    public BookSlot[] getBookSlots(int page) {
        // TODO build and keep a separate map of slots per page
        Stream<BookSlot> leftSlots = this.getPage(page).stream()
                .filter(drawable -> drawable instanceof BookElementWithSlots)
                .flatMap(drawable -> Arrays.stream(((BookElementWithSlots) drawable).getSlots()));
        Stream<BookSlot> rightSlots = this.getPage(page + 1).stream()
                .filter(drawable -> drawable instanceof BookElementWithSlots)
                .flatMap(drawable -> Arrays.stream(((BookElementWithSlots) drawable).getSlots()));

        return Stream.concat(leftSlots, rightSlots).limit(16).toArray(BookSlot[]::new);
    }

    public static class Builder {
        /**
         * The x coordinates of the left and right pages. See {@link #getCurrentX()} and {@link #getNextX()}.
         */
        private final int leftX, rightX;
        private final int y;
        /**
         * The area of the page that can be used for text and alike.
         */
        public final int width, height;

        private final Int2ObjectMap<List<Drawable>> pages = new Int2ObjectArrayMap<>();
        private final IntList chapterPages = new IntArrayList();

        private int currentPageIdx;
        private int currentY;

        public Builder(int leftX, int rightX, int y, int width, int height) {
            this.leftX = leftX;
            this.rightX = rightX;
            this.y = y;
            this.width = width;
            this.height = height;

            this.currentPageIdx = 0;
            this.currentY = this.y;
        }

        public BookLayout build() {
            return new BookLayout(this.pages, chapterPages);
        }

        public int getCurrentPage() {
            return this.currentPageIdx;
        }

        public void addWidget(Drawable drawable) {
            this.pages.computeIfAbsent(currentPageIdx, ArrayList::new).add(drawable);
        }

        public void advancePage() {
            this.currentPageIdx++;
            this.currentY = this.y;
        }

        public boolean isNewPage() {
            return currentY == this.y;
        }

        public int getCurrentY() {
            return this.currentY;
        }

        public void setCurrentY(int newY) {
            if (newY < this.getMaxY()) {
                this.currentY = newY;
            } else {
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
            return this.y;
        }

        public int getMaxY() {
            return this.y + this.height;
        }

        public void markPageAsChapter() {
            this.chapterPages.add(this.currentPageIdx);
        }

        public int getCurrentChapter() {
            return this.chapterPages.size();
        }
    }
}
