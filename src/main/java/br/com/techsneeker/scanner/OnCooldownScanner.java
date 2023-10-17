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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnCooldownScanner extends ScannerContract {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Set<String> idsCached = new HashSet<>();

    private int amountItemsPerSearch = 100;
    private Long maximumPrice = 250000L;
    private Long minimumProfit = 250000L;

    public OnCooldownScanner() {
        this.client = new ClientHttp();
        this.lbUpdater();
    }

    public OnCooldownScanner(Long maximumPrice, Long minimumProfit) {
        this.maximumPrice = maximumPrice;
        this.minimumProfit = minimumProfit;
        this.client = new ClientHttp();
        this.lbUpdater();
    }

    public OnCooldownScanner configAmount(int amount) {
        this.amountItemsPerSearch = amount;
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
        this.buildItems(client.getAuction(), amountItemsPerSearch);
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

            if (item == null) {
                continue;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime auctionTime = item.getLastUpdate();

            Duration duration = Duration.between(auctionTime, now);
            long seconds = duration.getSeconds();

            if (!(seconds < 20)) {
                continue;
            }

            if (!idsCached.contains(item.getId())) {
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

    private void addToCache(String value) {
        this.idsCached.add(value);
    }

}
