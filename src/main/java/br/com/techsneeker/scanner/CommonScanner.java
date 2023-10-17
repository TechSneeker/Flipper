package br.com.techsneeker.scanner;

import br.com.techsneeker.client.ClientHttp;
import br.com.techsneeker.object.Filter;
import br.com.techsneeker.object.Item;
import br.com.techsneeker.service.ItemController;
import br.com.techsneeker.service.ProfitCalculator;
import br.com.techsneeker.service.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommonScanner {

    private final ClientHttp client = new ClientHttp();
    private final Set<String> idCache = new HashSet<>();

    private JsonObject lowestBinJson;
    private Long maximumPrice = 250000L;
    private Long minimumProfit = 250000L;

    public CommonScanner() {
        this.configThreads();
    }

    public CommonScanner(Long maximumPrice, Long minimumProfit) {
        this.maximumPrice = maximumPrice;
        this.minimumProfit = minimumProfit;
        this.configThreads();
    }

    private void configThreads() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updateLowestBin, 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::searchProfitableItems, 1, 1, TimeUnit.SECONDS);
    }

    private void searchProfitableItems() {
        buildItems(client.getAuction(), 100);
    }

    private void updateLowestBin() {
        String jsonValue = client.getLowestBin();
        this.lowestBinJson = JsonParser.parseString(jsonValue).getAsJsonObject();
    }

    private void buildItems(String jsonValue, int maxIterations) {
        long start = System.nanoTime();

        Item profitableItem = null;
        long profitableValue = 0L;

        JsonElement jsonElement = JsonParser.parseString(jsonValue);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("auctions");

        int i = 0;
        for (JsonElement itemElement : jsonArray) {
            if (i >= maxIterations) {
                break;
            }
            i++;

            Item item = getItemFromElement(itemElement);

            if (item != null && !idCache.contains(item.getId())) {
                String formattedName = ItemController.getFormattedNameId(item);
                JsonElement lowestBinElement = lowestBinJson.get(formattedName);

                if (lowestBinElement != null) {
                    long lowestBin = lowestBinElement.getAsLong();
                    long itemPrice = item.getValue();
                    long profit = ProfitCalculator.getProfit(itemPrice, lowestBin);

                    if (profit >= minimumProfit && itemPrice <= maximumPrice) {
                        profitableValue = profit;
                        profitableItem = item;
                        break;
                    }
                }
            }
        }

        long end = System.nanoTime();
        long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("Time: " + elapsedTimeMillis + "ms");

        if (profitableItem != null) {
            addToCache(profitableItem.getId());
            Utils.sendToClipboard("/viewauction " + profitableItem.getId());
            System.out.println(profitableItem.getName() + " - " + profitableItem.getValue() + "    Profit: " + profitableValue + "/viewauction " + profitableItem.getId());
        }
    }

    private Item getItemFromElement(JsonElement itemElement) {
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

    public void addToCache(String id) {
        idCache.add(id);
    }

}
