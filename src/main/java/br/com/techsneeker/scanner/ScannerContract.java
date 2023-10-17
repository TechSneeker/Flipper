package br.com.techsneeker.scanner;

import br.com.techsneeker.client.ClientHttp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.ScheduledFuture;

abstract class ScannerContract {

    protected ScheduledFuture<?> lbSearching;
    protected ScheduledFuture<?> ahSearching;
    protected ClientHttp client;
    protected JsonObject lbJson;

    public abstract void start();
    public abstract void stop();
    protected abstract void pooling();

    protected void lbUpdater() {
        String jsonValue = client.getLowestBin();
        this.lbJson = JsonParser.parseString(jsonValue).getAsJsonObject();
    }
}
