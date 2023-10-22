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

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OnCooldownScanner extends ScannerContract {

    public OnCooldownScanner() {
        scheduler = Executors.newScheduledThreadPool(3);
        this.lbUpdater();
    }

    public OnCooldownScanner(long maximumPrice, long minimumProfit) {
        scheduler = Executors.newScheduledThreadPool(3);
        this.maximumPrice = maximumPrice;
        this.minimumProfit = minimumProfit;
        this.lbUpdater();
    }

    public OnCooldownScanner configAmount(int amount) {
        super.amountItemsPerSearch = amount;
        return this;
    }

    @Override
    public void start() {
        lbSearching = scheduler.scheduleAtFixedRate(this::lbUpdater, 0, 5, TimeUnit.SECONDS);
        ahSearching = scheduler.scheduleWithFixedDelay(this::pooling, 1, 2, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        if (lbSearching != null && ahSearching != null) {
            lbSearching.cancel(false);
            ahSearching.cancel(false);
        }
    }

    @Override
    protected void pooling() {
        this.builder(client.getAuction(), amountItemsPerSearch);
    }

    @Override
    protected void builder(String jsonValue, int maxIterations) {
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

            Item item = this.filter(itemElement);

            if (item == null) {
                continue;
            }

            if (!idCached.contains(item.getId())) {
                String formattedName = ItemController.getFormattedNameId(item);
                JsonElement lowestBinElement = lbJson.get(formattedName);

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

    @Override
    protected Item filter(JsonElement itemElement) {
        JsonObject jsonItem = itemElement.getAsJsonObject();

        String category = jsonItem.get("category").getAsString();
        String itemName = jsonItem.get("item_name").getAsString();

        if (Filter.isIgnorable(category, itemName)) {
            return null;
        }

        long lastUpdated = jsonItem.get("last_updated").getAsLong();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime auctionTime = Utils.epochMilliToDate(lastUpdated);

        Duration duration = Duration.between(auctionTime, now);

        if (!(duration.getSeconds() <= 20)) {
            return null;
        }

        boolean bin = jsonItem.get("bin").getAsBoolean();
        boolean claimed = jsonItem.get("claimed").getAsBoolean();

        if (!bin || claimed) {
            return null;
        }

        Item item = new Item();
        item.setName(itemName);
        item.setLastUpdate(lastUpdated);
        item.setId(jsonItem.get("uuid").getAsString());
        item.setExtraAttributes(jsonItem.get("item_bytes").getAsString());
        item.setValue(jsonItem.get("starting_bid").getAsLong());

        return item;
    }

    private void addToCache(String id) {
        idCached.add(id);
    }

}
