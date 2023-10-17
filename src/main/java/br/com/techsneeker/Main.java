package br.com.techsneeker;

import br.com.techsneeker.object.Config;
import br.com.techsneeker.scanner.CommonScanner;
import br.com.techsneeker.scanner.OnCooldownScanner;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Object> userPreferences = Config.collectPreferences();

        String scanner = (String) userPreferences.get("scanner");
        Long maximumPrice = (Long) userPreferences.get("minimumPrice");
        Long minimumProfit = (Long) userPreferences.get("minimumProfit");

        if (scanner.equals("common")) {
            CommonScanner commonScanner = new CommonScanner(maximumPrice, minimumProfit);
        }

        if (scanner.equals("cooldown")) {
            OnCooldownScanner onCooldownScanner = new OnCooldownScanner(maximumPrice, minimumProfit);
            onCooldownScanner.configAmount(100).start();
        }
    }

}