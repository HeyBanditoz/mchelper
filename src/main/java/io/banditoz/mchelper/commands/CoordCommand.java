package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.CoordinatePoint;
import io.banditoz.mchelper.utils.database.dao.CoordsDao;
import io.banditoz.mchelper.utils.database.dao.CoordsDaoImpl;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.util.List;

public class CoordCommand extends Command {
    @Override
    public String commandName() {
        return "coords";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParser(getDefualtArgs());
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        CoordsDao dao = new CoordsDaoImpl(ce.getDatabase());
        Namespace args = getDefualtArgs().parseArgs(ce.getCommandArgsWithoutName());
        if (args.get("subcommand").equals("add")) {
            List<String> argsAsString = args.getList("point");
            ce.sendReply(argsAsString.toString());
            CoordinatePoint point = new CoordinatePoint(argsAsString.get(1), argsAsString.get(2),
                    argsAsString.get(3), ce.getEvent().getAuthor().getIdLong(), ce.getGuild().getIdLong());
            dao.savePoint(point);
            ce.sendReply(point + " saved.");
        }
        /*
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
        }
        else {
            ce.sendReply("Unrecognized operator " + name + ".");
        }
        */
    }

    private ArgumentParser getDefualtArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("coords").addHelp(false).build();
        parser.description("Manipulate this guild's coordinate list.");
        Subparsers subparsers = parser.addSubparsers().dest("subcommand");
        subparsers.addParser("add").aliases("save")
                .addArgument("point")
                .nargs("*");
        subparsers.addParser("remove").aliases("delete")
                .addArgument("name")
                .nargs("*");
        subparsers.addParser("list").aliases("show");
        return parser;
    }
}
