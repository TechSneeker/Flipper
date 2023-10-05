package br.com.techsneeker.service;

import br.com.techsneeker.object.Item;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.nullicorn.nedit.type.NBTCompound;
import org.apache.commons.lang3.StringUtils;

public class ItemController {

    public static String getFormattedNameId(Item item) {
        NBTCompound extraAttributes = item.getExtraAttributes();

        if (item.isPet()) {
           return buildFormattedPetName(extraAttributes);
        }

        return (String) extraAttributes.get("id");
    }

    private static String buildFormattedPetName(NBTCompound extraAttributes) {
        String jsonString = extraAttributes.getString("petInfo");
        JsonObject petInfo = JsonParser.parseString(jsonString).getAsJsonObject();

        String petTier = petInfo.get("tier").getAsString();
        String petType = petInfo.get("type").getAsString();

        if (StringUtils.containsWhitespace(petType)) {
            petType = petType.replace(" ", "_");
        }

        Integer numbering = Utils.numberByTier(petTier);
        return String.format("%s;%d", petType, numbering);
    }

}
