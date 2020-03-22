package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.NamedQuote;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddquoteCommand extends Command {
    @Override
    public String commandName() {
        return "addquote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("\"<name>\" quote")
                .withDescription("Adds a quote to the database.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        String name;
        String quote;
        Pattern p = Pattern.compile("\"(.*?)\"\\s+");
        Matcher m = p.matcher(ce.getCommandArgsString());
        if (m.find()) {
            quote = m.group().replace("\"", "");
            name = m.replaceFirst("");
        }
        else {
            throw new IllegalArgumentException("Bad arguments.");
        }
        Database.getInstance().getGuildDataById(ce.getGuild()).getQuotes().add(new NamedQuote(name, quote));
        Database.getInstance().saveDatabase();
        ce.sendReply("Quote added.");
    }
}