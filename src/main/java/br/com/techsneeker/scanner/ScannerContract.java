package br.com.techsneeker.scanner;

import br.com.techsneeker.client.ClientHttp;
import br.com.techsneeker.object.Item;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

abstract class ScannerContract {

    protected int amountItemsPerSearch = 100;
    protected long maximumPrice = 250000L;
    protected long minimumProfit = 250000L;

    protected ScheduledExecutorService scheduler;
    protected ScheduledFuture<?> lbSearching;
    protected ScheduledFuture<?> ahSearching;
    protected JsonObject lbJson;

    protected Set<String> idCached = new HashSet<>();
    protected ClientHttp client = new ClientHttp();

    public abstract void start();
    public abstract void stop();
    protected abstract void pooling();

    protected abstract Item filter(JsonElement el);
    protected abstract void builder(String json, int amt);

    protected void lbUpdater() {
        String jsonValue = client.getLowestBin();
        this.lbJson = JsonParser.parseString(jsonValue).getAsJsonObject();
    }
}
