package io.banditoz.mchelper.commands.permissions;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class CommandPermissions {
    public static boolean isBotOwner(User attempter){
        Settings settings = SettingsManager.getInstance().getSettings();
        List<String> botOwners = settings.getBotOwners();
        for (String owner : botOwners) {
            if (owner.compareTo(attempter.getId()) == 0) {
                return true;
            }
        }
        return false;
    }
}
