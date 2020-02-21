package io.banditoz.mchelper.utils.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.utils.TwoDimensionalPoint;

import java.util.HashMap;

public class GuildData {
    @JsonProperty("coordinates")
    private HashMap<String, TwoDimensionalPoint> coordinates;

    @JsonProperty("defaultChannel")
    private String defaultChannel;

    @JsonProperty("prefix")
    private Character prefix;

    @JsonProperty("postQotdToDefaultChannel")
    private Boolean postQotdToDefaultChannel;

    public GuildData() {
        this.coordinates = new HashMap<>();
        this.defaultChannel = "";
        this.prefix = '!';
        this.postQotdToDefaultChannel = false;
    }

    public HashMap<String, TwoDimensionalPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(HashMap<String, TwoDimensionalPoint> coordinates) {
        this.coordinates = coordinates;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public char getPrefix() {
        return prefix;
    }

    public void setPrefix(char prefix) {
        this.prefix = prefix;
    }

    public Boolean getPostQotdToDefaultChannel() {
        return postQotdToDefaultChannel;
    }

    public void setPostQotdToDefaultChannel(Boolean postQotdToDefaultChannel) {
        this.postQotdToDefaultChannel = postQotdToDefaultChannel;
    }
}
