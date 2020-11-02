package io.banditoz.mchelper.utils;

public class RoleObject {
    private String emote;
    private String name;
    private String role_id;

    public RoleObject(String emote, String name, String role_id) {
        this.emote = emote;
        this.name = name;
        this.role_id = role_id;
    }

    public String getEmote() {
        return emote;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }
}
