package io.github.reoseah.hematurgy.resource;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import io.github.reoseah.hematurgy.Hematurgy;
import io.github.reoseah.hematurgy.resource.book_element.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookLoader extends SinglePreparationResourceReloader<JsonObject> implements IdentifiableResourceReloadListener {
    public static final Gson GSON = new Gson();

    public static BookElement[] elements = {};

    public static BookLayout buildLayout(BookProperties properties, TextRenderer textRenderer) {
        var builder = new BookLayout.Builder(properties);
        for (BookElement element : elements) {
            element.populate(builder, properties, textRenderer);
        }
        return builder.build();
    }

    @Override
    protected JsonObject prepare(ResourceManager manager, Profiler profiler) {
        Optional<Resource> optional = manager.getResource(new Identifier("hematurgy:hemonomicon.json"));
        if (optional.isPresent()) {
            Resource resource = optional.get();
            try (BufferedReader reader = resource.getReader()) {
                return GSON.fromJson(new JsonReader(reader), JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new JsonObject();
    }

    @Override
    protected void apply(JsonObject prepared, ResourceManager manager, Profiler profiler) {
        List<BookElement> elements = new ArrayList<>();

        for (var element : JsonHelper.getArray(prepared, "elements", new JsonArray())) {
            try {
                elements.add(readElement(JsonHelper.asObject(element, "element")));
            } catch (Exception e) {
                Hematurgy.LOGGER.error("Error reading book element, skipping", e);
            }
        }

        BookLoader.elements = elements.toArray(new BookElement[0]);
    }

    private static BookElement readElement(JsonObject json) {
        String type = JsonHelper.getString(json, "type");
        return switch (type) {
            case "heading" -> new Heading(JsonHelper.getString(json, "translation_key"));
            case "paragraph" -> new Paragraph(JsonHelper.getString(json, "translation_key"));
            case "page_break" -> new PageBreak();
            case "chapter" -> new Chapter(JsonHelper.getString(json, "translation_key"));
            case "fold" -> {
                JsonArray leftJson = JsonHelper.getArray(json, "left", new JsonArray());
                JsonArray rightJson = JsonHelper.getArray(json, "right", new JsonArray());
                BookSimpleElement[] left = readSimpleElements(leftJson);
                BookSimpleElement[] right = readSimpleElements(rightJson);
                yield new Fold(left, right);
            }
            case "vertically_centered" -> {
                var element = readElement(JsonHelper.getObject(json, "element"));
                if (element instanceof BookSimpleElement simple) {
                    yield new VerticalCenterElement(simple);
                } else {
                    throw new JsonParseException("Cannot use special element here: " + element);
                }
            }
            case "utterance" -> {
                String translationKey = JsonHelper.getString(json, "translation_key");
                Identifier id = new Identifier(JsonHelper.getString(json, "id"));
                int duration = JsonHelper.getInt(json, "duration");
                yield new Utterance(translationKey, id, duration);
            }
            case "inventory" -> {
                JsonArray slotsJson = JsonHelper.getArray(json, "slots");

                if (slotsJson.size() >= 16) {
                    throw new JsonParseException("Too many slots for inventory element");
                }

                SlotConfiguration[] slots = new SlotConfiguration[slotsJson.size()];
                for (int j = 0; j < slotsJson.size(); j++) {
                    JsonObject slot = JsonHelper.asObject(slotsJson.get(j), "slot");
                    int x = JsonHelper.getInt(slot, "x");
                    int y = JsonHelper.getInt(slot, "y");
                    Identifier background = slot.has("background") ? new Identifier(JsonHelper.getString(slot, "background")) : null;
                    boolean output = slot.has("output") && JsonHelper.getBoolean(slot, "output");
                    Ingredient ingredient = slot.has("ingredient") ? Ingredient.ALLOW_EMPTY_CODEC.parse(JsonOps.INSTANCE, slot.get("ingredient")).getOrThrow() : null;

                    slots[j] = new SlotConfiguration(x, y, output, ingredient, background);
                }

                BookInventory.Image background = null;
                if (json.has("background")) {
                    JsonObject backgroundJson = JsonHelper.asObject(json.get("background"), "background");
                    Identifier texture = new Identifier(JsonHelper.getString(backgroundJson, "texture"));
                    int x = JsonHelper.getInt(backgroundJson, "x");
                    int y = JsonHelper.getInt(backgroundJson, "y");
                    int u = JsonHelper.getInt(backgroundJson, "u");
                    int v = JsonHelper.getInt(backgroundJson, "v");
                    int width = JsonHelper.getInt(backgroundJson, "width");
                    int height = JsonHelper.getInt(backgroundJson, "height");
                    background = new BookInventory.Image(texture, x, y, u, v, width, height);
                }
                int height = JsonHelper.getInt(json, "height", 0);

                yield new BookInventory(height, background, slots);
            }
//            case "illustration" -> {
//                Identifier texture = new Identifier(JsonHelper.getString(json, "texture"));
//                int u = JsonHelper.getInt(json, "u");
//                int v = JsonHelper.getInt(json, "v");
//                int width = JsonHelper.getInt(json, "width");
//                int height = JsonHelper.getInt(json, "height");
//                yield new Illustration(texture, u, v, width, height);
//            }
            default -> throw new JsonParseException("Unknown element type: " + type);
        };
    }

    private static BookSimpleElement[] readSimpleElements(JsonArray elementsJson) {
        BookSimpleElement[] elements = new BookSimpleElement[elementsJson.size()];
        for (int i = 0; i < elementsJson.size(); i++) {
            var element = readElement(JsonHelper.asObject(elementsJson.get(i), "element"));
            if (element instanceof BookSimpleElement simple) {
                elements[i] = simple;
            } else {
                throw new JsonParseException("Cannot use special element here: " + element);
            }
        }
        return elements;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("hematurgy:hemonomicon");
    }
}
