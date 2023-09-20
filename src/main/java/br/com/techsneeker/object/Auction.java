package br.com.techsneeker.object;

public class Auction {

    private String id;
    private String sellerAuctions;
    private String sellerProfile;
    private Item item;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSellerAuctions() {
        return sellerAuctions;
    }

    public void setSellerAuctions(String sellerAuctions) {
        this.sellerAuctions = sellerAuctions;
    }

    public String getSellerProfile() {
        return sellerProfile;
    }

    public void setSellerProfile(String sellerProfile) {
        this.sellerProfile = sellerProfile;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
