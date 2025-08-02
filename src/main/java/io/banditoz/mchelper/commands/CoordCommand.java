package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.CoordinatePoint;
import io.banditoz.mchelper.database.dao.CoordsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class CoordCommand extends Command {
    private final CoordsDao dao;

    @Inject
    public CoordCommand(CoordsDao dao) {
        this.dao = dao;
    }

    @Override
    public String commandName() {
        return "coords";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<save|add,show|list,delete|remove,help>")
                .withDescription("Saves coordinates to the database. See !coords help");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (ce.getCommandArgs().length <= 1) {
            help(ce);
        }
        String name = ce.getCommandArgs()[1];
        if (name.equalsIgnoreCase("save") || name.equalsIgnoreCase("add")) {
            CoordinatePoint point = new CoordinatePoint(ce.getCommandArgs()[3], ce.getCommandArgs()[4],
                    ce.getCommandArgs()[2], ce.getEvent().getAuthor().getIdLong(), ce.getGuild().getIdLong());
            dao.savePoint(point);
            ce.sendReply(point + " saved.");
        }
        else if (name.equalsIgnoreCase("show") || name.equalsIgnoreCase("list")) {
            if (ce.getCommandArgs().length > 2) {
                ce.sendReply(dao.getPointByName(ce.getCommandArgs()[2], ce.getGuild()).toString());
            }
            else {
                StringBuilder s = new StringBuilder("Coordinates:\n");
                dao.getAllPointsForGuild(ce.getGuild()).forEach(p -> s.append(p.getName()).append(": ").append(p.toString()).append('\n'));
                ce.sendReply(s.toString());
            }
        }
        else if (name.equalsIgnoreCase("delete") || name.equalsIgnoreCase("remove")) {
            dao.deletePointByName(ce.getCommandArgs()[2], ce.getGuild());
            ce.sendReply("Deleted.");
        }
        else if (name.equalsIgnoreCase("help")) {
            help(ce);
        }
        else {
            ce.sendReply("Unrecognized operator " + name + ".");
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }

    private void help(CommandEvent ce) {
        String s = "save or add: adds new coord to list. \n" +
                "show or list: display all coords \n" +
                "show <name> or list <name>: display coord with <name>.\n" +
                "delete or remove: remove an item";
        ce.sendReply(s);
    }
}
