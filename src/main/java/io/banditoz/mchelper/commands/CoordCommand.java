package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.TwoDimensionalPoint;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;

public class CoordCommand extends Command {
    @Override
    public String commandName() {
        return "!coords";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<save|add,show|list,delete|remove,help>")
                .withDescription("Saves coordinates to the database. See !coords help");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        GuildData gd = Database.getInstance().getGuildDataById(ce.getGuild());
        if (ce.getCommandArgs().length > 1) {
            if (ce.getCommandArgs()[1].equalsIgnoreCase("save") || ce.getCommandArgs()[1].equalsIgnoreCase("add")) {
                if (!gd.getCoordinates().containsKey(ce.getCommandArgs()[2])) {
                    TwoDimensionalPoint point = new TwoDimensionalPoint(ce.getCommandArgs()[3], ce.getCommandArgs()[4]);
                    gd.getCoordinates().put(ce.getCommandArgs()[2], point);
                    Database.getInstance().saveDatabase();
                    ce.sendReply(point + " saved.");
                } else {
                    ce.sendReply("\"" + ce.getCommandArgs()[2] + "\" already exists.");
                }
            } else if (ce.getCommandArgs()[1].equalsIgnoreCase("show") || ce.getCommandArgs()[1].equalsIgnoreCase("list")) {
                if (ce.getCommandArgs().length > 2) {
                    if (gd.getCoordinates().containsKey(ce.getCommandArgs()[2])) {
                        ce.sendReply(gd.getCoordinates().get(ce.getCommandArgs()[2]).toString());
                    } else {
                        ce.sendReply("Point \"" + ce.getCommandArgs()[2] + "\" does not exist.");
                    }
                } else {
                    StringBuilder s = new StringBuilder("Coordinates:\n");
                    gd.getCoordinates().forEach((k, p) -> s.append(k).append(": ").append(p.toString()).append("\n"));
                    ce.sendReply(s.toString());
                }
            } else if (ce.getCommandArgs()[1].equalsIgnoreCase("delete") || ce.getCommandArgs()[1].equalsIgnoreCase("remove")) {
                if (gd.getCoordinates().containsKey(ce.getCommandArgs()[2])) {
                    gd.getCoordinates().remove(ce.getCommandArgs()[2]);
                    Database.getInstance().saveDatabase();
                    ce.sendReply("Deleted.");
                } else {
                    ce.sendReply("Point \"" + ce.getCommandArgs()[2] + "\" does not exist.");
                }
            } else if (ce.getCommandArgs()[1].equalsIgnoreCase("help")) {
                help(ce);
            } else {
                ce.sendReply("Unrecognized operator " + ce.getCommandArgs()[1] + ".");
            }
        } else {
            help(ce);
        }
    }

    private void help(CommandEvent ce) {
        String s = "save or add: adds new coord to list. \n" +
                "show or list: display all coords \n" +
                "show <name> or list <name>: display coord with <name>.\n" +
                "delete or remove: remove an item";
        ce.sendReply(s);
    }
}
