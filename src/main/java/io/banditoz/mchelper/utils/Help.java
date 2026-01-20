package io.banditoz.mchelper.utils;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;

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

    public String getCommandName() {
        return commandName;
    }

    public String getDescription() {
        return description;
    }

    public String getParameters() {
        return parameters;
    }

    public ArgumentParser getParser() {
        return parser;
    }

    public boolean isElevated() {
        return isElevated;
    }

    @Override
    public String toString() {
        if (parser == null) {
            return "`" + commandName + "` - " +
                    ((parameters == null) ? "\\<no parameters\\> " : escape(parameters).replaceAll("<", "\\\\<").replaceAll(">", "\\\\>")) + " - " +
                    escape(description) + ((isElevated) ? " (ELEVATED)" : "");
        }
        else {
            return "```\n" + parser.formatHelp() + "```";
        }
    }
}