package io.github.reoseah.hematurgy.resource.book;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

public abstract class BookSplittableElement extends BookSimpleElement {
    @Override
    public void populate(BookLayout.Builder builder, TextRenderer textRenderer) {
        int elementY = builder.getCurrentY() + (builder.isNewPage() ? 0 : this.getVerticalGap());
        int elementHeight = this.getHeight(builder.width, textRenderer);

        if (elementY + elementHeight > builder.getMaxY()
                && this.canSplit(elementHeight, builder.getMaxY() - elementY, textRenderer)) {
            int elementX = builder.getCurrentX();
            int nextX = builder.getNextX();

            WidgetPair result = this.createWidgetPair(elementX, elementY, builder.width, builder.getMaxY() - elementY, nextX, builder.getMinY(), builder.height, textRenderer);

            builder.addWidget(result.current());
            builder.advancePage();
            builder.addWidget(result.next());

            builder.setCurrentY(builder.getMinY() + result.nextHeight());
        } else {
            super.populate(builder, textRenderer);
        }
    }

    protected abstract boolean canSplit(int height, int maxHeight, TextRenderer textRenderer);

    protected abstract WidgetPair createWidgetPair(int x, int y, int width, int maxHeight, int nextX, int nextY, int nextHeight, TextRenderer textRenderer);

    protected record WidgetPair(Drawable current, Drawable next, int nextHeight) {
    }
}
