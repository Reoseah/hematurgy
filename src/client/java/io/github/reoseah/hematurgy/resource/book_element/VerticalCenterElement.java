package io.github.reoseah.hematurgy.resource.book_element;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public class VerticalCenterElement extends BookSimpleElement {
    private final BookSimpleElement element;

    public VerticalCenterElement(BookSimpleElement element) {
        this.element = element;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        // forces new page
        return 10000;
    }

    @Override
    protected Drawable createWidget(int x, int y, int width, int maxHeight, TextRenderer textRenderer) {
        int height = this.element.getHeight(width, textRenderer);
        int newY = y + (maxHeight - height) / 2;
        if (newY > y) {
            newY -= this.element.getVerticalGap();
        }
        return this.element.createWidget(x, newY, width, maxHeight, textRenderer);
    }
}
