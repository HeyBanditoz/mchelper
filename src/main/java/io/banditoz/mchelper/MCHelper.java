package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.commands.UnitsCommand;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;

public class MCHelper {
    public static JDA jda;

    public static void setupBot() throws LoginException, InterruptedException {
        Settings settings = SettingsManager.getInstance().getSettings();
        jda = new JDABuilder(settings.getDiscordToken()).build();
        jda.awaitReady();
        jda.addEventListener(new BashCommand());
        jda.addEventListener(new InfoCommand());
        jda.addEventListener(new EvalCommand());
        jda.addEventListener(new EangleCommand());
        jda.addEventListener(new NetherCommand());
        jda.addEventListener(new OverworldCommand());
        jda.addEventListener(new UnitsCommand());
        jda.addEventListener(new CoordCommand());
        jda.addEventListener(new TeXCommand());
        jda.addEventListener(new TeXListener());
    }
}
