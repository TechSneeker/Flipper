package br.com.techsneeker.service;

import br.com.techsneeker.object.enums.Reforge;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Utils {


    public static String[] reforgesByCategory(String category) {

        switch (category) {
            case "weapon": return Reforge.REFORGES_WEAPON;
            case "misc": return Reforge.REFORGES_MISC;
            case "accessories": return Reforge.REFORGES_ACCESSORY;
            case "armor": return Reforge.REFORGES_ARMOR;
        }

       return null;
    }

    public static Integer numberByTier(String tier) {

        switch (tier) {
            case "COMMON": return 0;
            case "UNCOMMON": return 1;
            case "RARE": return 2;
            case "EPIC": return 3;
            case "LEGENDARY": return 4;
            case "MYTHIC": return 5;
        }

        return null;
    }

    public static void sendToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
        Toolkit.getDefaultToolkit().beep();
    }

}
