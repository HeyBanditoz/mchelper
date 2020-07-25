package io.banditoz.mchelper.utils;

import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Help {
    private String commandName;
    private String description;
    private String parameters;
    private ArgumentParser parser;
    private boolean isElevated;

    public Help(String commandName, boolean isElevated) {
        this.commandName = commandName;
        this.isElevated = isElevated;
    }

    public Help withParameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

    public Help withDescription(String description) {
        this.description = description;
        return this;
    }

    public Help withParser(ArgumentParser parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public String toString() {
        if (parser == null) {
            return "`" + commandName + "` - " +
                    ((parameters == null) ? "<no parameters> " : parameters) + " - " +
                    description + ((isElevated) ? " (ELEVATED)" : "");
        }
        else {
            return "```\n" + parser.formatHelp() + "```";
        }
    }
}