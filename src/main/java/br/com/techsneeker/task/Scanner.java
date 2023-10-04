package br.com.techsneeker.task;

import br.com.techsneeker.client.ClientHttp;
import br.com.techsneeker.object.Item;
import br.com.techsneeker.service.Builder;
import br.com.techsneeker.service.ItemController;
import br.com.techsneeker.service.ProfitCalculator;
import br.com.techsneeker.service.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Scanner {

    private static final ClientHttp CLIENT = new ClientHttp();
    private int amountItemsPerSearch = 1000;
    private JsonObject lowestBinJson;
    private String idCached = "";
    private AtomicInteger count = new AtomicInteger(0);

    public Scanner() {
        // config default
    }

    public Scanner(int amountItems) {
        this.amountItemsPerSearch = amountItems;
    }

    public void start() {

        this.updateLowestBin();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(this::updateLowestBin, 0, 3,TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(this::searchProfitableItems, 0, 1, TimeUnit.SECONDS);

    }

    private void searchProfitableItems() {
        count.incrementAndGet();
        System.out.println("Searching... " + count.get() + "x");
        String auction = CLIENT.getAuction();
        Builder itemBuilder = new Builder(auction);

        Item[] items = itemBuilder.buildItems(amountItemsPerSearch);

        Item profitableItem = new Item();
        long profitableValue = 0L;

        for (Item item : items) {

            boolean isCached = idCached.contains(item.getId());
            if (isCached) continue;

            ItemController controller = new ItemController(item);
            String formattedName = controller.getFormattedNameId();

            if (lowestBinJson.get(formattedName) == null) {
                return;
            }

            long lowestBin = lowestBinJson.get(formattedName).getAsLong();
            long itemPrice = item.getValue();

            long profit = ProfitCalculator.getProfit(itemPrice, lowestBin);

            if (profit >= 800000) {
                profitableValue = profit;
                profitableItem = item;
                addToCache(item.getId());
                break;
            }

        }

        if (profitableItem.getId() == null) return;

        Utils.sendToClipboard("/viewauction " + profitableItem.getId());
        System.out.println(profitableItem.getName() + " - " + profitableItem.getValue() + "    Profit: " +  profitableValue + "/viewauction " + profitableItem.getId());
    }

    private void updateLowestBin() {
        String jsonValue = CLIENT.getLowestBin();
        this.lowestBinJson = JsonParser.parseString(jsonValue).getAsJsonObject();
    }

    private void addToCache(String id) {
        this.idCached = idCached + "," + id;
    }

}
