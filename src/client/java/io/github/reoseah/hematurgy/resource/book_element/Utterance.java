package io.github.reoseah.hematurgy.resource.book_element;

import io.github.reoseah.hematurgy.network.StartUtterancePayload;
import io.github.reoseah.hematurgy.network.StopUtterancePayload;
import io.github.reoseah.hematurgy.screen.client.HemonomiconScreen;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class Utterance extends BookSimpleElement {
    protected final String translationKey;
    protected final Identifier id;
    protected final float duration;

    public Utterance(String translationKey, Identifier id, float duration) {
        this.translationKey = translationKey;
        this.id = id;
        this.duration = duration;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        MutableText text = Text.translatable(this.translationKey);
        List<OrderedText> lines = textRenderer.wrapLines(text, width - 12);
        List<String> linesAsString = lines.stream().map(t -> {
            StringBuilder builder = new StringBuilder();
            t.accept((index, style, codePoint) -> {
                builder.appendCodePoint(codePoint);
                return true;
            });
            return builder.toString();
        }).toList();

        int height = 0;
        for (String line : linesAsString) {
            if (line.isEmpty()) {
                height += 4;
            } else {
                height += textRenderer.fontHeight;
            }
        }
        return height;
    }

    @Override
    protected int getVerticalGap() {
        return super.getVerticalGap() + 2;
    }

    @Override
    protected Drawable createWidget(int x, int y, int width, int maxHeight, TextRenderer textRenderer) {
        return new UtteranceWidget(this.translationKey, x, y, width, maxHeight, textRenderer);
    }

    private class UtteranceWidget implements Drawable, Element {
        private final int buttonX;
        private final int buttonY;
        private static final int BUTTON_WIDTH = 12;
        private static final int BUTTON_HEIGHT = 14;

        private final TextRenderer textRenderer;
        private final List<OrderedText> lines;
        private final List<String> linesAsString;
        private final IntList linesY;
        private final int textLength;
        private final int x;

        private boolean mouseDown = false;
        private long mouseDownTime = 0L;

        public UtteranceWidget(String translationKey, int x, int y, int width, int height, TextRenderer textRenderer) {
            this.textRenderer = textRenderer;

            MutableText text = Text.translatable(translationKey);
            this.lines = textRenderer.wrapLines(text, width - BUTTON_WIDTH);
            this.linesAsString = lines.stream().map(t -> {
                StringBuilder builder = new StringBuilder();
                t.accept((index, style, codePoint) -> {
                    builder.appendCodePoint(codePoint);
                    return true;
                });
                return builder.toString();
            }).toList();

            int nonWrappedLength = text.getString().length();
            this.textLength = nonWrappedLength - lines.size() + 1;

            this.linesY = new IntArrayList(linesAsString.size());
            int nextY = y;
            for (String line : linesAsString) {
                this.linesY.add(nextY);
                if (line.isEmpty()) {
                    nextY += 4;
                } else {
                    nextY += textRenderer.fontHeight;
                }
            }

            this.buttonX = x - 2;
            this.buttonY = y - 2;
            this.x = x;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            if (this.mouseDown || mouseX > buttonX && mouseY > buttonY
                    && mouseX < buttonX + BUTTON_WIDTH && mouseY < buttonY + BUTTON_HEIGHT) {
                ctx.drawTexture(HemonomiconScreen.TEXTURE, buttonX, buttonY, 48, 210, BUTTON_WIDTH, BUTTON_HEIGHT);
            } else {
                ctx.drawTexture(HemonomiconScreen.TEXTURE, buttonX, buttonY, 48, 194, BUTTON_WIDTH, BUTTON_HEIGHT);
            }

            long mouseDownTime = this.mouseDown ? System.currentTimeMillis() - this.mouseDownTime : 0;
            float progress = (mouseDownTime / 1000f / Utterance.this.duration);

            if (progress > 1) {
//                ClientPlayNetworking.send(HemonomiconPackets.UTTERANCE, Util.make(
//                        PacketByteBufs.create(),
//                        buf -> buf.writeIdentifier(Utterance.this.id)
//                ));

                this.mouseDown = false;
                this.mouseDownTime = 0;
            }

            int leftToRender = Math.round(progress * textLength);

            for (int i = 0; i < lines.size(); i++) {
                ctx.drawText(textRenderer, lines.get(i), x + BUTTON_WIDTH, linesY.getInt(i), 0x000000, false);
                if (leftToRender > 0) {
                    ctx.drawText(textRenderer, linesAsString.get(i).substring(0, Math.min(leftToRender, linesAsString.get(i).length())), x + BUTTON_WIDTH, linesY.getInt(i), 0xce1e00, false);
                    leftToRender -= linesAsString.get(i).length();
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX > buttonX && mouseY > buttonY
                    && mouseX < buttonX + BUTTON_WIDTH && mouseY < buttonY + BUTTON_HEIGHT) {
                this.mouseDown = true;
                this.mouseDownTime = System.currentTimeMillis();

                ClientPlayNetworking.send(new StartUtterancePayload(id));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.mouseDown) {
                this.mouseDown = false;
                this.mouseDownTime = 0;

                ClientPlayNetworking.send(new StopUtterancePayload());
                return true;
            }
            return false;
        }

        @Override
        public void setFocused(boolean focused) {

        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }
}