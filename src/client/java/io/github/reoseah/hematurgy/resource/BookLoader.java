package io.github.reoseah.hematurgy.resource;


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.reoseah.hematurgy.Hematurgy;
import io.github.reoseah.hematurgy.resource.book.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.font.TextRenderer;
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

    public static BookLayout buildLayout(int leftPageX, int rightPageX, int y, int width, int height, TextRenderer textRenderer) {
        var builder = new BookLayout.Builder(leftPageX, rightPageX, y, width, height);
        for (BookElement element : BookLoader.elements) {
            element.populate(builder, textRenderer);
        }
        return builder.build();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("hematurgy:hemonomicon");
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

        for (JsonElement element : JsonHelper.getArray(prepared, "elements", new JsonArray())) {
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
//            case "title_page" -> new TitlePage(JsonHelper.getString(json, "translation_key"));
            case "heading" -> new Heading(JsonHelper.getString(json, "translation_key"));
            case "paragraph" -> new Paragraph(JsonHelper.getString(json, "translation_key"));
//            case "page_break" -> new PageBreak();
//            case "blank_verso_page" -> new BlankVersoPage();
            case "chapter" -> new ChapterMarker();
//            case "utterance" -> {
//                String translationKey = JsonHelper.getString(json, "translation_key");
//                Identifier id = new Identifier(JsonHelper.getString(json, "id"));
//                int duration = JsonHelper.getInt(json, "duration");
//                yield new Utterance(translationKey, id, duration);
//            }
//            case "inventory" -> {
//                JsonArray slotsJson = JsonHelper.getArray(json, "slots");
//
//                if (slotsJson.size() >= 16) {
//                    throw new JsonParseException("Too many slots for inventory element");
//                }
//
//                BookSlot[] slots = new BookSlot[slotsJson.size()];
//                for (int j = 0; j < slotsJson.size(); j++) {
//                    JsonObject slot = JsonHelper.asObject(slotsJson.get(j), "slot");
//                    int x = JsonHelper.getInt(slot, "x");
//                    int y = JsonHelper.getInt(slot, "y");
//                    Identifier background = slot.has("background") ? new Identifier(JsonHelper.getString(slot, "background")) : null;
//                    boolean output = slot.has("output") && JsonHelper.getBoolean(slot, "output");
//                    Ingredient ingredient = slot.has("ingredient") ? Ingredient.ALLOW_EMPTY_CODEC.parse(JsonOps.INSTANCE, slot.get("ingredient")).get().orThrow() : null;
//
//                    slots[j] = new BookSlot(x, y, output, ingredient, background);
//                }
//
//                BookInventory.Image background = null;
//                if (json.has("background")) {
//                    JsonObject backgroundJson = JsonHelper.asObject(json.get("background"), "background");
//                    Identifier texture = new Identifier(JsonHelper.getString(backgroundJson, "texture"));
//                    int x = JsonHelper.getInt(backgroundJson, "x");
//                    int y = JsonHelper.getInt(backgroundJson, "y");
//                    int u = JsonHelper.getInt(backgroundJson, "u");
//                    int v = JsonHelper.getInt(backgroundJson, "v");
//                    int width = JsonHelper.getInt(backgroundJson, "width");
//                    int height = JsonHelper.getInt(backgroundJson, "height");
//                    background = new BookInventory.Image(texture, x, y, u, v, width, height);
//                }
//                int height = JsonHelper.getInt(json, "height", 0);
//
//                yield new BookInventory(height, background, slots);
//            }
//            case "non_breaking_group" -> {
//                JsonArray elementsJson = JsonHelper.getArray(json, "elements");
//                SimpleElement[] group = new SimpleElement[elementsJson.size()];
//                for (int j = 0; j < elementsJson.size(); j++) {
//                    JsonObject elementJson = JsonHelper.asObject(elementsJson.get(j), "element");
//                    BookElement element = readElement(elementJson);
//                    if (element instanceof SimpleElement simple) {
//                        group[j] = simple;
//                    } else {
//                        throw new JsonParseException("Non-breaking group cannot contain special elements: " + element);
//                    }
//                }
//                yield new Group(group);
//            }
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
}
