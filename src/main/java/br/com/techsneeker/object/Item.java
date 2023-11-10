package br.com.techsneeker.object;

import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;

import java.io.IOException;

public class Item {

    private String id;
    private String name;
    private long value;
    private long profit;
    private NBTCompound extraAttributes;

    public long getProfit() {
        return profit;
    }

    public void setProfit(long profit) {
        this.profit = profit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public NBTCompound getExtraAttributes() {
        return extraAttributes;
    }

    public void setExtraAttributes(String itemBytes) {
        try {
            NBTCompound result = NBTReader.readBase64(itemBytes);
            NBTList list = (NBTList) result.get("i");

            this.extraAttributes = (NBTCompound) ((NBTCompound) list.get(0))
                    .getCompound("tag").get("ExtraAttributes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPet() {
        try {
            return ((String) extraAttributes.get("id")).equalsIgnoreCase("pet");
        } catch (NullPointerException e) {
            return false;
        }
    }
}
