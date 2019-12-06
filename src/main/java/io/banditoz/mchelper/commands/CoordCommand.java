package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.NamedCoordinatePoint;

import java.io.*;
import java.util.HashMap;

public class CoordCommand extends Command {
    private static HashMap<String, NamedCoordinatePoint> map = new HashMap<>();
    @Override
    public String commandName() {
        return "!coords";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<save|add,show|list,delete|remove,help>")
                .withDescription("Saves coordinates to a file. See !coords help");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        if (map.isEmpty()) {
            try {
                FileInputStream fi = new FileInputStream(new File("coords.txt"));
                ObjectInputStream oi = new ObjectInputStream(fi);
                boolean done = false;
                while (!done) {
                    try {
                        NamedCoordinatePoint n = (NamedCoordinatePoint) oi.readObject();
                        map.put(n.getName(),n);
                    } catch (EOFException ex) {
                        done = true;
                    }
                }
                fi.close();
                oi.close();
            } catch (ClassNotFoundException | IOException ex) {
                ce.sendExceptionMessage(ex);
            }
        }
        if (ce.getCommandArgs().length>1) {
            if (ce.getCommandArgs()[1].equalsIgnoreCase("save") || ce.getCommandArgs()[1].equalsIgnoreCase("add")) {
                if (!map.containsKey(ce.getCommandArgs()[2])) {
                    try {
                        NamedCoordinatePoint n = new NamedCoordinatePoint(ce.getCommandArgs()[2], ce.getCommandArgs()[3], ce.getCommandArgs()[4]);
                        map.put(n.getName(), n);
                        FileOutputStream f = new FileOutputStream(new File("coords.txt"));
                        ObjectOutputStream o = new ObjectOutputStream(f);
                        map.forEach((k, p) -> {
                            try {
                                write(p, o);
                            } catch (IOException e) {
                                ce.sendExceptionMessage(e);
                            }
                        });
                        o.close();
                        f.close();
                        ce.sendReply("Added.");
                    } catch (IOException ex) {
                        ce.sendExceptionMessage(ex);
                    }
                } else {
                    ce.sendReply("\"" + ce.getCommandArgs()[2] + "\" already exists.");
                }
            } else if (ce.getCommandArgs()[1].equalsIgnoreCase("show") || ce.getCommandArgs()[1].equalsIgnoreCase("list")) {
                if (ce.getCommandArgs().length > 2) {
                    if (map.containsKey(ce.getCommandArgs()[2])) {
                        ce.sendReply(map.get(ce.getCommandArgs()[2]).toString());
                    } else {
                        ce.sendReply("Point \"" + ce.getCommandArgs()[2] + "\" does not exist.");
                    }
                } else {
                    StringBuilder s = new StringBuilder();
                    map.forEach((k, p) -> s.append(p.toString()).append("\n"));
                    ce.sendReply(s.toString());
                }
            } else if (ce.getCommandArgs()[1].equalsIgnoreCase("delete") || ce.getCommandArgs()[1].equalsIgnoreCase("remove")) {
                try {
                    if (map.containsKey(ce.getCommandArgs()[2])) {
                        map.remove(ce.getCommandArgs()[2]);
                        FileOutputStream f = new FileOutputStream(new File("coords.txt"));
                        ObjectOutputStream o = new ObjectOutputStream(f);
                        map.forEach((k, p) -> {
                            try {
                                write(p, o);
                            } catch (IOException e) {
                                ce.sendExceptionMessage(e);
                            }
                        });
                        f.close();
                        o.close();
                        ce.sendReply("Deleted.");
                    } else {
                        ce.sendReply("Point \"" + ce.getCommandArgs()[2] + "\" does not exist.");
                    }
                } catch (IOException ex) {
                    ce.sendExceptionMessage(ex);
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

    private void write(NamedCoordinatePoint p, ObjectOutputStream o) throws IOException {
        o.writeObject(p);
    }

    private void help(CommandEvent ce) {
        String s = "save or add: adds new coord to list. \n" +
                "show or list: display all coords \n" +
                "show <name> or list <name>: display coord with <name>.\n" +
                "delete or remove: remove an item";
        ce.sendReply(s);
    }
}
