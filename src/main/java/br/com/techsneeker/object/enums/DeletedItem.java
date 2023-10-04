package br.com.techsneeker.object.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DeletedItem {

    private static final List<String> ITEM_KEY_WORD = List.of(
            "rune", "skin", "cake"
    );

    public static boolean isExist(String mainName) {
        return ITEM_KEY_WORD.stream().anyMatch(excludeName ->
                StringUtils.containsIgnoreCase(mainName, excludeName));
    }

}
