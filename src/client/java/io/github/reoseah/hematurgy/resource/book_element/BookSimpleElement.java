package io.github.reoseah.hematurgy.resource.book_element;

import io.github.reoseah.hematurgy.resource.BookLayout;
import io.github.reoseah.hematurgy.resource.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public abstract class BookSimpleElement implements BookElement {
    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        int elementHeight = this.getHeight(properties.pageWidth, textRenderer);

        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getVerticalGap());
        if (elementY + elementHeight > builder.getMaxY() && builder.isWrapAllowed() && !builder.isNewPage()) {
            builder.advancePage();
            elementY = builder.getCurrentY();
        }
        int elementX = builder.getCurrentX();
        Drawable renderer = this.createWidget(elementX, elementY, properties.pageWidth, builder.getMaxY() - elementY, textRenderer);
        builder.addWidget(renderer);
        builder.setCurrentY(elementY + elementHeight);
    }

    /**
     * @return number of pixels to offset from the previous element
     */
    protected int getVerticalGap() {
        return 4;
    }

    /**
     * @return how many pixels this element wants to take on a page
     */
    protected abstract int getHeight(int width, TextRenderer textRenderer);

    /**
     * Returns a drawable for this element.
     * <p>
     * Implement {@link net.minecraft.client.gui.Element} to also handle mouse events,
     * {@link BookElementWithSlots} to have item slots.
     *
     * @see net.minecraft.client.gui.Element
     * @see BookElementWithSlots
     */
    protected abstract Drawable createWidget(int x, int y, int width, int maxHeight, TextRenderer textRenderer);
}
