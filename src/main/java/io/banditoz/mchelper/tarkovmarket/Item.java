package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public record Item(@JsonProperty("imageLink") String imageLink,
                   @JsonProperty("sellFor") List<VendorPrice> vendorPrices,
                   @JsonProperty("name") String name,
                   @JsonProperty("link") String link,
                   @JsonProperty("avg24hPrice") int avg24hPrice,
                   @JsonProperty("high24hPrice") int high24hPrice,
                   @JsonProperty("low24hPrice") int low24hPrice,
                   @JsonProperty("lastLowPrice") int lastLowPrice,
                   @JsonProperty("updated") LocalDateTime updated,
                   @JsonProperty("wikiLink") String wikiLink,
                   @JsonProperty("changeLast48hPercent") double changeLast48h,
                   @JsonProperty("types") EnumSet<ItemType> types,
                   @JsonProperty("shortName") String shortName) {

    public String getFormattedInt(int i) {
        return DecimalFormat.getInstance().format(i) + '₽';
    }

    private Optional<VendorPrice> getBestTraderPrice() {
        return vendorPrices.stream().min(VendorPrice::compareTo);
    }

    public MessageEmbed getAsEmbed() {
        String bestPrice = "<unknown>";
        String sDiff48h = (changeLast48h > 0 ? changeLast48h + "%" : "(" + changeLast48h * -1 + "%)");
        Optional<VendorPrice> bestTraderPrice = getBestTraderPrice();
        if (bestTraderPrice.isPresent()) {
            VendorPrice tp = bestTraderPrice.get();
            String traderName = tp.vendor().vendorName().name();
            traderName = traderName.substring(0, 1).toUpperCase() + traderName.substring(1).toLowerCase();
            bestPrice = "**" + traderName + ":** " + DecimalFormat.getInstance().format(tp.price()) + '₽';
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(name, wikiLink)
                .setDescription("[Market Link](" + link + ")")
                .setThumbnail(imageLink == null || imageLink.isEmpty() ? "https://tarkov-tools.com/images/unknown-item-icon.jpg" : imageLink)
                .addField("Last Lowest Price", getFormattedInt(lastLowPrice), true)
                .addField("24h Prices", "Low: " + getFormattedInt(low24hPrice) + "\nAvg: " + getFormattedInt(avg24hPrice) + "\nHigh: " + getFormattedInt(high24hPrice), true)
                .addField("Price Difference", "2 days: **" + sDiff48h + "**", true)
                .addField("Best To", bestPrice, true)
                .addField("Types", types.toString(), true)
                .setTimestamp(updated);
        if (types.contains(ItemType.NO_FLEA)) {
            builder.setColor(Color.RED);
        }
        return builder.build();
    }
}