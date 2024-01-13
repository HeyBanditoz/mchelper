package io.banditoz.mchelper.commands.logic;

import io.avaje.config.Config;
import net.dv8tion.jda.api.entities.User;

public class CommandPermissions {
    public static boolean isBotOwner(User attempter) {
        return Config.list().of("mchelper.owners").contains(attempter.getId());
    }
}
