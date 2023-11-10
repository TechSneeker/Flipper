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
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OnCooldownScanner extends ScannerContract {

    private ScheduledFuture<?> lbSearching;
    private ScheduledFuture<?> ahWatching;

    public OnCooldownScanner() {
        super();
        this.lbUpdater();
    }

    public OnCooldownScanner(long maximumPrice, long minimumProfit) {
        super();
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
        lbSearching = scheduler.scheduleAtFixedRate(this::lbUpdater, 0, 2500, TimeUnit.MILLISECONDS);
        ahWatching = scheduler.scheduleWithFixedDelay(this::pooling, 0, 250, TimeUnit.MILLISECONDS);
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
        this.builder(client.getAuction(), amountItemsPerSearch);
    }

    @Override
    protected void builder(String jsonValue, int maxIterations) {
        List<Item> profitableItems = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(jsonValue);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("auctions");

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            boolean auctionBin = object.get("bin").getAsBoolean();

            if (!auctionBin) {
                continue;
            }

            String category = object.get("category").getAsString();
            String itemName = object.get("item_name").getAsString();
            String description = object.get("item_lore").getAsString();

            if (Filter.isIgnorable(category, itemName, description)) {
                continue;
            }

            long lastUpdated = object.get("last_updated").getAsLong();

            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));
            LocalDateTime auctionTime = Utils.epochMilliToDate(lastUpdated);

            Duration duration = Duration.between(auctionTime, now);

            if (!(duration.getSeconds() <= 20)) {
                continue;
            }

            Item item = new Item();
            item.setId(object.get("uuid").getAsString());

            if (!cachedIds.contains(item.getId())) {
                item.setExtraAttributes(object.get("item_bytes").getAsString());
                item.setValue(object.get("starting_bid").getAsLong());
                item.setName(object.get("item_name").getAsString());

                String formattedName = ItemController.getFormattedNameId(item);
                JsonElement lowestBinElement = lbJson.get(formattedName);

                if (lowestBinElement != null) {
                    long lowestBin = lowestBinElement.getAsLong();
                    long itemPrice = item.getValue();
                    long profit = ProfitCalculator.getProfit(itemPrice, lowestBin);

                    if (profit >= minimumProfit && itemPrice <= maximumPrice) {
                        addToCache(item.getId());
                        item.setProfit(profit);
                        profitableItems.add(item);
                    }
                }
            }

        }

        profitableItems.sort((o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }

            return Long.compare(o2.getProfit(), o1.getProfit());
        });

        Item mostProfitableItem = profitableItems.isEmpty() ? null : profitableItems.get(0);

        if (mostProfitableItem != null) {
            Utils.sendToClipboard("/viewauction " + mostProfitableItem.getId());
            System.out.println(mostProfitableItem.getName() + " - " + mostProfitableItem.getValue() + " Profit: " + mostProfitableItem.getProfit() + " /viewauction " + mostProfitableItem.getId());
        }
    }

    private void addToCache(String id) {
        cachedIds.add(id);
    }

}
