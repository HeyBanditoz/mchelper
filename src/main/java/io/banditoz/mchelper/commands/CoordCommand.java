package io.banditoz.mchelper.commands;

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
    protected void onCommand() {
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
                sendExceptionMessage(ex);
            }
        }
        if (commandArgs.length>1) {
            if (commandArgs[1].equalsIgnoreCase("save") || commandArgs[1].equalsIgnoreCase("add")) {
                if (!map.containsKey(commandArgs[2])) {
                    try {
                        NamedCoordinatePoint n = new NamedCoordinatePoint(commandArgs[2], commandArgs[3], commandArgs[4]);
                        map.put(n.getName(), n);
                        FileOutputStream f = new FileOutputStream(new File("coords.txt"));
                        ObjectOutputStream o = new ObjectOutputStream(f);
                        map.forEach((k, p) -> write(p, o));
                        o.close();
                        f.close();
                        sendReply("Added.");
                    } catch (IOException ex) {
                        sendExceptionMessage(ex);
                    }
                } else {
                    sendReply("\"" + commandArgs[2] + "\" already exists.");
                }
            } else if (commandArgs[1].equalsIgnoreCase("show") || commandArgs[1].equalsIgnoreCase("list")) {
                if (commandArgs.length > 2) {
                    if (map.containsKey(commandArgs[2])) {
                        sendReply(map.get(commandArgs[2]).toString());
                    } else {
                        sendReply("Point \"" + commandArgs[2] + "\" does not exist.");
                    }
                } else {
                    StringBuilder s = new StringBuilder();
                    map.forEach((k, p) -> s.append(p.toString() + "\n"));
                    sendReply(s.toString());
                }
            } else if (commandArgs[1].equalsIgnoreCase("delete") || commandArgs[1].equalsIgnoreCase("remove")) {
                try {
                    if (map.containsKey(commandArgs[2])) {
                        map.remove(commandArgs[2]);
                        FileOutputStream f = new FileOutputStream(new File("coords.txt"));
                        ObjectOutputStream o = new ObjectOutputStream(f);
                        map.forEach((k, p) -> write(p, o));
                        f.close();
                        o.close();
                        sendReply("Deleted.");
                    } else {
                        sendReply("Point \"" + commandArgs[2] + "\" does not exist.");
                    }
                } catch (IOException ex) {
                    sendExceptionMessage(ex);
                }
            } else if (commandArgs[1].equalsIgnoreCase("help")) {
                help();
            } else {
                sendReply("Unrecognized operator " + commandArgs[1] + ".");
            }
        } else {
            help();
        }
    }

    protected void write(NamedCoordinatePoint p, ObjectOutputStream o) {
        try {
            o.writeObject(p);
        } catch (IOException ex) {
            sendExceptionMessage(ex);
        }
    }

    protected void help() {
        String s = "save or add: adds new coord to list. \n" +
                "show or list: display all coords \n" +
                "show <name> or list <name>: display coord with <name>.\n" +
                "delete or remove: remove an item";
        sendReply(s);
    }
}
