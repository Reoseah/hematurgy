package io.github.reoseah.hematurgy.resource.book_element;

import io.github.reoseah.hematurgy.resource.BookLayout;
import io.github.reoseah.hematurgy.resource.BookProperties;
import net.minecraft.client.font.TextRenderer;

public interface BookElement {
    void populate(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer);
}
