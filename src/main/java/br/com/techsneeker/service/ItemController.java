package br.com.techsneeker.service;

import br.com.techsneeker.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class ItemController {

    private final String itemBytes;

    public ItemController(String itemBytes) {
        this.itemBytes = itemBytes;
    }

    public String getFormattedNameId() throws IOException {
        NBTCompound result = NBTReader.readBase64(itemBytes);

        NBTList list = (NBTList) result.get("i");
        NBTCompound extraAttributes = (NBTCompound) ((NBTCompound)
                list.get(0)).getCompound("tag").get("ExtraAttributes");

        String id = (String) extraAttributes.get("id");

        if (StringUtils.equalsIgnoreCase(id, "pet")) {

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

        return id;
    }

}
