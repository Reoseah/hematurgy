package io.github.reoseah.hematurgy.resource.book;

import net.minecraft.client.font.TextRenderer;

public interface BookElement {
    void populate(BookLayout.Builder builder, TextRenderer textRenderer);
}
