package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ItemType {
    @JsonProperty("any")
    ANY,
    @JsonProperty("ammo")
    AMMO,
    @JsonProperty("ammoBox")
    AMMO_BOX,
    @JsonProperty("armor")
    ARMOR,
    @JsonProperty("backpack")
    BACKPACK,
    @JsonProperty("barter")
    BARTER,
    @JsonProperty("container")
    CONTAINER,
    @JsonProperty("glasses")
    GLASSES,
    @JsonProperty("grenade")
    GRENADE,
    @JsonProperty("gun")
    GUN,
    @JsonProperty("helmet")
    HELMET,
    @JsonProperty("keys")
    KEYS,
    @JsonProperty("markedOnly")
    MARKED_ONLY,
    @JsonProperty("mods")
    MODS,
    @JsonProperty("noFlea")
    NO_FLEA,
    @JsonProperty("pistolGrip")
    PISTOL_GRIP,
    @JsonProperty("provisions")
    PROVISIONS,
    @JsonProperty("unLootable")
    UNLOOTABLE,
    @JsonProperty("wearable")
    WEARABLE,
    @JsonProperty("rig")
    RIG,
    @JsonProperty("headphones")
    HEADPHONES,
    @JsonProperty("suppressor")
    SUPPRESSOR,
    @JsonProperty("disabled")
    DISABLED;
}
