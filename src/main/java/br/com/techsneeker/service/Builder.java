package br.com.techsneeker.service;

import br.com.techsneeker.object.Filter;
import br.com.techsneeker.object.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class Builder {

    public static List<Item> buildItems(String jsonValue, int maxAmountItems) {
        JsonElement jsonElement = JsonParser.parseString(jsonValue);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("auctions");

        List<Item> items = new ArrayList<>();

        for (JsonElement itemElement : jsonArray) {

            if (items.size() >= maxAmountItems) {
                break;
            }

            Item item = getItemFromElement(itemElement);

            if (item != null) {
                items.add(item);
            }

        }

        return items;
    }

    private static Item getItemFromElement(JsonElement itemElement) {
        JsonObject jsonItem = itemElement.getAsJsonObject();

        String category = jsonItem.get("category").getAsString();
        String itemName = jsonItem.get("item_name").getAsString();

        if (Filter.isIgnorable(category, itemName)) {
            return null;
        }

        boolean bin = jsonItem.get("bin").getAsBoolean();
        boolean claimed = jsonItem.get("claimed").getAsBoolean();

        if (!bin || claimed) return null;

        Item item = new Item();
        item.setName(itemName);
        item.setId(jsonItem.get("uuid").getAsString());
        item.setExtraAttributes(jsonItem.get("item_bytes").getAsString());
        item.setValue(jsonItem.get("starting_bid").getAsLong());
        item.setLastUpdate(jsonItem.get("last_updated").getAsLong());

        return item;
    }

}
