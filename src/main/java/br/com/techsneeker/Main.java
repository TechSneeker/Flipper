package br.com.techsneeker;

import br.com.techsneeker.object.Config;
import br.com.techsneeker.scanner.CommonScanner;
import br.com.techsneeker.scanner.OnCooldownScanner;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Object> userPreferences = Config.collectPreferences();

        String scanner = (String) userPreferences.get("scanner");
        long maximumPrice = (long) userPreferences.get("maximumPrice");
        long minimumProfit = (long) userPreferences.get("minimumProfit");

        if (scanner.equals("common")) {
            CommonScanner cScanner = new CommonScanner(maximumPrice, minimumProfit);
            cScanner.configAmount(1000).start();
        }

        if (scanner.equals("cooldown")) {
            OnCooldownScanner onCooldownScanner = new OnCooldownScanner(maximumPrice, minimumProfit);
            onCooldownScanner.configAmount(100).start();
        }

    }

}