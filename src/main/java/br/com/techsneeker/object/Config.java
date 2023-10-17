package br.com.techsneeker.object;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, Object> userPreferences = new HashMap<>();

    public static Map<String, Object> collectPreferences() {
        printLogotype();
        collectScanner();
        collectMinPrices();
        return userPreferences;
    }

    private static void collectMinPrices() {
        System.out.print("Maximum item price:");
        Long itemPrice = scanner.nextLong();

        System.out.print("Minimum item profit:");
        Long itemProfit = scanner.nextLong();

        clearConsole();

        userPreferences.put("maximumPrice", itemPrice);
        userPreferences.put("minimumProfit", itemProfit);
    }

    private static void collectScanner() {
        System.out.println("[1] xxxxx");
        System.out.println("[2] xxxxx\n");
        System.out.print("Select the flip mode: ");
        long flipMode = scanner.nextLong();

        clearConsole();

        if (flipMode == 1) {
            userPreferences.put("scanner", "common");
        } else if (flipMode == 2) {
            userPreferences.put("scanner", "cooldown");
        }
    }

    private static void clearConsole() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }

        printLogotype();
    }

    private static void printLogotype() {
        System.out.println("\n" +
                " ______   __         __     ______   ______   ______     ______    \n" +
                "/\\  ___\\ /\\ \\       /\\ \\   /\\  == \\ /\\  == \\ /\\  ___\\   /\\  == \\   \n" +
                "\\ \\  __\\ \\ \\ \\____  \\ \\ \\  \\ \\  _-/ \\ \\  _-/ \\ \\  __\\   \\ \\  __<   \n" +
                " \\ \\_\\    \\ \\_____\\  \\ \\_\\  \\ \\_\\    \\ \\_\\    \\ \\_____\\  \\ \\_\\ \\_\\ \n" +
                "  \\/_/     \\/_____/   \\/_/   \\/_/     \\/_/     \\/_____/   \\/_/ /_/ \n" +
                "                                                                   \n");
    }

}
