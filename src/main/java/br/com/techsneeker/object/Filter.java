package br.com.techsneeker.object;

import br.com.techsneeker.object.enums.DeletedItem;
import org.apache.commons.lang3.StringUtils;

public class Filter {

    public static boolean isIgnorable(String category, String name, String description) {
        return !isAllowedCategory(category) || isDeletedItem(name) || hasSpecifiedDescription(description);
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

    private static boolean hasSpecifiedDescription(String description) {
        return StringUtils.containsIgnoreCase(description, "furniture");
    }

}
