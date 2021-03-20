package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.utils.DateUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TarkovMarketResult {
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("shortName")
    private String shortName;
    @JsonProperty("price")
    private Integer price;
    @JsonProperty("basePrice")
    private Integer basePrice;
    @JsonProperty("avg24hPrice")
    private Integer avg24hPrice;
    @JsonProperty("avg7daysPrice")
    private Integer avg7daysPrice;
    @JsonProperty("traderName")
    private String traderName;
    @JsonProperty("traderPrice")
    private Integer traderPrice;
    @JsonProperty("traderPriceCur")
    private String traderPriceCur;
    @JsonProperty("updated")
    private LocalDateTime updated;
    @JsonProperty("slots")
    private Integer slots;
    @JsonProperty("diff24h")
    private Double diff24h;
    @JsonProperty("diff7days")
    private Double diff7days;
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("link")
    private String link;
    @JsonProperty("wikiLink")
    private String wikiLink;
    @JsonProperty("img")
    private String img;
    @JsonProperty("imgBig")
    private String imgBig;
    @JsonProperty("bsgId")
    private String bsgId;
    @JsonProperty("isFunctional")
    private Boolean isFunctional;

    public MessageEmbed getAsEmbed() {
        String sDiff24h = (diff24h > 0 ? diff24h + "%" : "(" + diff24h * -1 + "%)");
        String sDiff7d = (diff7days > 0 ? diff7days + "%" : "(" + diff7days * -1 + "%)");
        return new EmbedBuilder()
                .setTitle(name, wikiLink)
                .setDescription("[Market Link](" + link + ")")
                .setThumbnail(imgBig)
                .addField("Lowest Price", DecimalFormat.getInstance().format(price) + traderPriceCur, true)
                .addField("Avg 24h Price", DecimalFormat.getInstance().format(avg24hPrice) + traderPriceCur, true)
                .addField("Price Difference", "1 day: **" + sDiff24h + "**\n7 days: **" + sDiff7d + "**", true)
                .setFooter("Price updated on " + DateUtils.getLocallyZonedRFC1123(updated))
                .build();

    }

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("shortName")
    public String getShortName() {
        return shortName;
    }

    @JsonProperty("shortName")
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @JsonProperty("price")
    public Integer getPrice() {
        return price;
    }

    @JsonProperty("price")
    public void setPrice(Integer price) {
        this.price = price;
    }

    @JsonProperty("basePrice")
    public Integer getBasePrice() {
        return basePrice;
    }

    @JsonProperty("basePrice")
    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }

    @JsonProperty("avg24hPrice")
    public Integer getAvg24hPrice() {
        return avg24hPrice;
    }

    @JsonProperty("avg24hPrice")
    public void setAvg24hPrice(Integer avg24hPrice) {
        this.avg24hPrice = avg24hPrice;
    }

    @JsonProperty("avg7daysPrice")
    public Integer getAvg7daysPrice() {
        return avg7daysPrice;
    }

    @JsonProperty("avg7daysPrice")
    public void setAvg7daysPrice(Integer avg7daysPrice) {
        this.avg7daysPrice = avg7daysPrice;
    }

    @JsonProperty("traderName")
    public String getTraderName() {
        return traderName;
    }

    @JsonProperty("traderName")
    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    @JsonProperty("traderPrice")
    public Integer getTraderPrice() {
        return traderPrice;
    }

    @JsonProperty("traderPrice")
    public void setTraderPrice(Integer traderPrice) {
        this.traderPrice = traderPrice;
    }

    @JsonProperty("traderPriceCur")
    public String getTraderPriceCur() {
        return traderPriceCur;
    }

    @JsonProperty("traderPriceCur")
    public void setTraderPriceCur(String traderPriceCur) {
        this.traderPriceCur = traderPriceCur;
    }

    @JsonProperty("updated")
    public LocalDateTime getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    @JsonProperty("slots")
    public Integer getSlots() {
        return slots;
    }

    @JsonProperty("slots")
    public void setSlots(Integer slots) {
        this.slots = slots;
    }

    @JsonProperty("diff24h")
    public Double getDiff24h() {
        return diff24h;
    }

    @JsonProperty("diff24h")
    public void setDiff24h(Double diff24h) {
        this.diff24h = diff24h;
    }

    @JsonProperty("diff7days")
    public Double getDiff7days() {
        return diff7days;
    }

    @JsonProperty("diff7days")
    public void setDiff7days(Double diff7days) {
        this.diff7days = diff7days;
    }

    @JsonProperty("icon")
    public String getIcon() {
        return icon;
    }

    @JsonProperty("icon")
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
    }

    @JsonProperty("wikiLink")
    public String getWikiLink() {
        return wikiLink;
    }

    @JsonProperty("wikiLink")
    public void setWikiLink(String wikiLink) {
        this.wikiLink = wikiLink;
    }

    @JsonProperty("img")
    public String getImg() {
        return img;
    }

    @JsonProperty("img")
    public void setImg(String img) {
        this.img = img;
    }

    @JsonProperty("imgBig")
    public String getImgBig() {
        return imgBig;
    }

    @JsonProperty("imgBig")
    public void setImgBig(String imgBig) {
        this.imgBig = imgBig;
    }

    @JsonProperty("bsgId")
    public String getBsgId() {
        return bsgId;
    }

    @JsonProperty("bsgId")
    public void setBsgId(String bsgId) {
        this.bsgId = bsgId;
    }

    @JsonProperty("isFunctional")
    public Boolean getIsFunctional() {
        return isFunctional;
    }

    @JsonProperty("isFunctional")
    public void setIsFunctional(Boolean isFunctional) {
        this.isFunctional = isFunctional;
    }
}
