package io.github.reoseah.hematurgy.resource.book;


import io.github.reoseah.hematurgy.screen.client.HemonomiconScreen;
import net.minecraft.client.font.TextRenderer;

public class ChapterMarker implements BookElement {
    @Override
    public void populate(BookLayout.Builder builder, TextRenderer textRenderer) {
        if (builder.getCurrentPage() % 2 != 0) {
            builder.advancePage();
        } else if (!builder.isNewPage()) {
            builder.advancePage();
            builder.advancePage();
        }

        int x = 256 / 2 - HemonomiconScreen.FULL_BOOKMARK_WIDTH;
        int y = builder.getMinY() + 4 + builder.getCurrentChapter() * HemonomiconScreen.BOOKMARK_HEIGHT;

        builder.markPageAsChapter();
        builder.addWidget((context, mouseX, mouseY, delta) -> {
            context.drawTexture(HemonomiconScreen.TEXTURE, x, y, HemonomiconScreen.FULL_BOOKMARK_U, HemonomiconScreen.FULL_BOOKMARK_V, HemonomiconScreen.FULL_BOOKMARK_WIDTH, HemonomiconScreen.BOOKMARK_HEIGHT);
        });
        builder.advancePage();
    }
}
