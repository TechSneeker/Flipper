package br.com.techsneeker.object;

import br.com.techsneeker.object.enums.DeletedItem;

public class Filter {

    public static boolean isIgnorable(String category, String name) {
        return !isAllowedCategory(category) || isDeletedItem(name);
    }

    private static boolean isAllowedCategory(String category) {

        switch (category) {
            case "weapon":
            case "accessories":
            case "misc":
            case "armor":
                return true;
        }

        return false;
    }

    private static boolean isDeletedItem(String name) {
        return DeletedItem.isExist(name);
    }
}
