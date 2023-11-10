package br.com.techsneeker.scanner;

import br.com.techsneeker.client.ClientHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class ScannerContract {

    public ScannerContract() {
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        idCleaner = scheduler.scheduleAtFixedRate(this::clearCachedIds, 0, 2, TimeUnit.MINUTES);
    }

    protected int amountItemsPerSearch = 100;
    protected long maximumPrice = 250000L;
    protected long minimumProfit = 250000L;

    protected ScheduledExecutorService scheduler;
    protected ScheduledFuture<?> idCleaner;
    protected JsonObject lbJson;

    protected Set<String> cachedIds = new HashSet<>();
    protected ClientHttp client = new ClientHttp();

    public abstract void start();
    public abstract void stop();

    protected abstract void pooling();
    protected abstract void builder(String json, int amt);

    protected void lbUpdater() {
        String jsonValue = null;
        jsonValue = client.getLowestBin();
        this.lbJson = JsonParser.parseString(jsonValue).getAsJsonObject();
    }

    private void clearCachedIds() {
        this.cachedIds = new HashSet<>();
    }
}
