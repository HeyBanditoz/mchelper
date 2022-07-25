package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public record ReactionRole(int id, long guildId, Emoji emoji, String name, long roleId) {
    public ReactionRole(int id, long guildId, String emoji, String name, long roleId) {
        this(id, guildId, Emoji.fromFormatted(emoji), name, roleId);
    }
}
