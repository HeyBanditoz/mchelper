package io.banditoz.mchelper.utils;

public class Help {
    private String commandName;
    private String description;
    private String parameters;
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

    @Override
    public String toString() {
        return commandName + " - " +
                ((parameters == null)? "<no parameters> " : parameters) + " - " +
                description + ((isElevated) ? " (ELEVATED)" : "");
    }
}