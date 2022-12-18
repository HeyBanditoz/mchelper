package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TarkovMarketSearcher {
    private final MCHelper MCHELPER;
    private final List<Item> CACHE = new ArrayList<>(2500);
    private Instant refreshAgain;

    public TarkovMarketSearcher(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public List<Item> getMarketResultsBySearch(final String search) throws IOException, HttpResponseException {
        if (refreshAgain == null || Instant.now().isAfter(refreshAgain)) {
            ObjectMapper om = MCHELPER.getObjectMapper();
            String query = """
                    {
                      items {
                        name
                        sellFor {
                          vendor {
                            name
                          }
                          priceRUB
                        }
                        avg24hPrice
                        high24hPrice
                        low24hPrice
                        lastLowPrice
                        wikiLink
                        link
                        updated
                        imageLink
                        changeLast48hPercent
                        types
                        shortName
                      }
                    }""";
            Map<String, String> jsonIntermediate = Map.of("query", query);
            String jsonQuery = om.writeValueAsString(jsonIntermediate);
            Response data = MCHELPER.getHttp().getTarkovClient().getTarkovMarketData(jsonQuery);
            // filter out flea market from all results
            for (Item item : data.data().items()) {
                item.vendorPrices().removeIf(vendorPrice -> vendorPrice.vendor().vendorName() == VendorName.FLEA);
            }
            CACHE.clear();
            CACHE.addAll(data.data().items());
            // phew, adding items succeeded! now update the refresh time
            refreshAgain = Instant.now().plus(15, ChronoUnit.MINUTES);
        }
        // so it would have been better to have a Map of Item#shortName, Item, but we can't do that because
        // shortName is not guaranteed to be unique. when I ran it, there were 11 hits of GEN3, so we have to do these
        // wonderful ~2450 string comparisons :)
        List<Item> setAside = new ArrayList<>(15);
        for (Item item : CACHE) {
            if (search.equalsIgnoreCase(item.shortName())) {
                setAside.add(item);
            }
        }
        List<BoundExtractedResult<Item>> items = FuzzySearch.extractSorted(
                search,
                CACHE,
                Item::name,
                FuzzySearch::tokenSortPartialRatio,
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
        sortedItems.removeIf(setAside::contains);
        // exact match in list, put them first
        for (Item item : setAside) {
            sortedItems.addFirst(item);
        }
        return sortedItems;
    }
}
