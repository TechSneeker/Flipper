package br.com.techsneeker.service;

import br.com.techsneeker.object.Item;
import br.com.techsneeker.object.enums.DeletedItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Builder {

    private final String jsonValue;

    public Builder(String response) {
        this.jsonValue = response;
    }

    public Item[] buildItems(int amountItems) {
        JsonElement jsonElement = JsonParser.parseString(jsonValue);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("auctions");

        Item[] itemsArray = new Item[amountItems];
        int amountBuilt = 0;

        for (JsonElement itemElement : jsonArray) {

            if (amountBuilt == amountItems) {
                break;
            }

            Item item = getItemFromElement(itemElement);

            if (item == null) continue;

            itemsArray[amountBuilt] = item;
            amountBuilt++;
        }

        return itemsArray;
    }

    private Item getItemFromElement(JsonElement itemElement) {
        JsonObject jsonItem = itemElement.getAsJsonObject();

        boolean bin = jsonItem.get("bin").getAsBoolean();
        boolean claimed = jsonItem.get("claimed").getAsBoolean();
        boolean deletedItem = DeletedItem.isExist(jsonItem.get("item_name").getAsString());

        if (deletedItem || claimed || !bin) return null;

        Item item = new Item();
        item.setId(jsonItem.get("uuid").getAsString());
        item.setName(jsonItem.get("item_name").getAsString());
        item.setDescription(jsonItem.get("extra").getAsString());
        item.setItemBytes(jsonItem.get("item_bytes").getAsString());
        item.setRarity(jsonItem.get("tier").getAsString());
        item.setValue(jsonItem.get("starting_bid").getAsLong());
        item.setLastUpdate(jsonItem.get("last_updated").getAsLong());

        return item;
    }

}
