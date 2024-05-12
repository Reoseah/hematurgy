package io.github.reoseah.hematurgy.resource.book_element;


import io.github.reoseah.hematurgy.resource.BookLayout;
import io.github.reoseah.hematurgy.resource.BookProperties;
import net.minecraft.client.font.TextRenderer;

public class ChapterMarker implements BookElement {
    @Override
    public void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        if (builder.getCurrentPage() % 2 != 0) {
            builder.advancePage();
        } else if (!builder.isNewPage()) {
            builder.advancePage();
            builder.advancePage();
        }

        int x = 256 / 2 - properties.bookmarkFullWidth;
        int y = properties.getBookmarkY(builder.getCurrentChapter());

        builder.markPageAsChapter();
        builder.addWidget((context, mouseX, mouseY, delta) -> {
            context.drawTexture(properties.texture, x, y, properties.bookmarkFullU, properties.bookmarkFullV, properties.bookmarkFullWidth, properties.bookmarkHeight);
        });
        builder.advancePage();
    }
}
