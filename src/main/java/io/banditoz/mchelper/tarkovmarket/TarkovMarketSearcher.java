package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TarkovMarketSearcher {
    private final MCHelper MCHELPER;

    public TarkovMarketSearcher(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public List<Item> getMarketResultsBySearch(final String search) throws IOException, HttpResponseException {
        ObjectMapper om = MCHELPER.getObjectMapper();
        String query = """
                {
                  itemsByName(name: "") {
                    name
                    traderPrices {
                      trader {
                        name
                      }
                      price
                    }
                    avg24hPrice
                    high24hPrice
                    low24hPrice
                    lastLowPrice
                    wikiLink
                    link
                    updated
                    imageLink
                    changeLast48h
                    types
                  }
                }""";
        Map<String, String> jsonIntermediate = Map.of("query", query);
        String jsonQuery = om.writeValueAsString(jsonIntermediate);
        Request request = new Request.Builder()
                .url("https://tarkov-tools.com/graphql")
                .post(RequestBody.create(MediaType.get("application/json"), jsonQuery))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        String json = MCHELPER.performHttpRequest(request);
        Response data = om.readValue(json, Response.class);
        List<BoundExtractedResult<Item>> items = FuzzySearch.extractSorted(
                search,
                data.data().items(),
                Item::name,
                FuzzySearch::partialRatio,
                60
        );
        LinkedList<Item> sortedItems = new LinkedList<>();
        // put guns first, the order of things the API returns is undefined
        // should really tweak this algorithm a bit, it's probably inefficient as hell, but it seems to work

        for (BoundExtractedResult<Item> item : items) {
            if (item.getReferent().types().contains(ItemType.GUN)) {
                sortedItems.addFirst(item.getReferent());
            }
            else {
                sortedItems.addLast(item.getReferent());
            }
        }
        return sortedItems;
    }
}
