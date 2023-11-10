package br.com.techsneeker.scanner;

import br.com.techsneeker.object.Filter;
import br.com.techsneeker.object.Item;
import br.com.techsneeker.service.ItemController;
import br.com.techsneeker.service.ProfitCalculator;
import br.com.techsneeker.service.Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommonScanner extends ScannerContract {

    private ScheduledFuture<?> lbSearching;
    private ScheduledFuture<?> ahWatching;
    private long cacheLastUpdated = 0L;

    public CommonScanner() {
        super();
        this.lbUpdater();
    }

    public CommonScanner(long maximumPrice, long minimumProfit) {
        super();
        this.maximumPrice = maximumPrice;
        this.minimumProfit = minimumProfit;
        this.lbUpdater();
    }

    public CommonScanner configAmount(int amount) {
        this.amountItemsPerSearch = amount;
        return this;
    }

    @Override
    public void start() {
        lbSearching = scheduler.scheduleAtFixedRate(this::lbUpdater, 0, 5000, TimeUnit.MILLISECONDS);
        ahWatching = scheduler.scheduleAtFixedRate(this::pooling, 0, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (lbSearching != null && ahWatching != null) {
            lbSearching.cancel(true);
            ahWatching.cancel(true);
            idCleaner.cancel(true);

            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        }
    }

    @Override
    protected void pooling() {
        String jsonValue = client.getAuction();
        long lastUpdated = client.getLastUpdated(jsonValue);

        if (lastUpdated > cacheLastUpdated) {
            cacheLastUpdated = lastUpdated;
            this.builder(jsonValue, amountItemsPerSearch);
        }
    }

    @Override
    protected void builder(String jsonValue, int maxIterations) {
        JsonElement jsonElement = JsonParser.parseString(jsonValue);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("auctions");

        List<Item> profitableItems = jsonArray.asList().parallelStream().filter(element -> {
            JsonObject object = element.getAsJsonObject();

            boolean auctionBin = object.get("bin").getAsBoolean();

            if (!auctionBin) {
                return false;
            }

            String category = object.get("category").getAsString();
            String itemName = object.get("item_name").getAsString();
            String description = object.get("item_lore").getAsString();

            if (Filter.isIgnorable(category, itemName, description)) {
                return false;
            }

            Item item = new Item();
            item.setId(object.get("uuid").getAsString());

            if (!cachedIds.contains(item.getId())) {
                item.setExtraAttributes(object.get("item_bytes").getAsString());
                item.setValue(object.get("starting_bid").getAsLong());

                String formattedName = ItemController.getFormattedNameId(item);
                JsonElement lowestBinElement = lbJson.get(formattedName);

                if (lowestBinElement != null) {
                    long lowestBin = lowestBinElement.getAsLong();
                    long itemPrice = item.getValue();
                    long profit = ProfitCalculator.getProfit(itemPrice, lowestBin);

                    if (profit >= minimumProfit && itemPrice <= maximumPrice) {
                        addToCache(item.getId());
                        item.setProfit(profit);
                        return true;
                    }
                }
            }

            return false;

        }).map(this::buildItemFromElement).sorted((o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }

            return Long.compare(o2.getProfit(), o1.getProfit());
        }).collect(Collectors.toList());

        Item mostProfitableItem = profitableItems.isEmpty() ? null : profitableItems.get(0);

        if (mostProfitableItem != null) {
            Utils.sendToClipboard("/viewauction " + mostProfitableItem.getId());
        }
    }

    protected Item buildItemFromElement(JsonElement itemElement) {
        JsonObject jsonItem = itemElement.getAsJsonObject();

        Item item = new Item();
        item.setId(jsonItem.get("uuid").getAsString());
        item.setExtraAttributes(jsonItem.get("item_bytes").getAsString());
        item.setValue(jsonItem.get("starting_bid").getAsLong());

        return item;
    }

    public void addToCache(String id) {
        cachedIds.add(id);
    }

}
