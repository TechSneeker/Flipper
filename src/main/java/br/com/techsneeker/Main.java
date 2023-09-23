package br.com.techsneeker;

import br.com.techsneeker.client.ClientHttp;
import br.com.techsneeker.object.Item;
import br.com.techsneeker.service.Builder;
import br.com.techsneeker.service.ItemController;

public class Main {

    public static void main(String[] args)  {

        ClientHttp client = new ClientHttp();

        String auction = client.getAuction();
        String lowest = client.getLowestBin();

        Builder builder = new Builder(auction);
        Item[] items = builder.buildItems(1000);

        for (Item item : items) {
            ItemController controller = new ItemController(item);
            System.out.println(controller.getFormattedNameId());
        }

    }

}