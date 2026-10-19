package io.banditoz.mchelper.commands.logic.params;

import javax.annotation.Nullable;

public class BindException extends Exception {
    @Nullable
    private final String paramName;

    public BindException(@Nullable String paramName, String message) {
        super(message);
        this.paramName = paramName;
    }

    @Nullable
    public String getParamName() {
        return paramName;
    }

    /**
     * @param commandName The command that failed to bind, for the usage line.
     * @param spec        The spec that was being bound against.
     * @return A message to send back to whoever ran the command.
     */
    public String render(String commandName, ParamSpec spec) {
        StringBuilder sb = new StringBuilder();
        if (paramName != null) {
            sb.append("`").append(paramName).append("`: ");
        }
        sb.append(getMessage());
        String usage = spec.toUsage();
        sb.append("\nUsage: `").append(commandName);
        if (usage != null) {
            sb.append(' ').append(usage);
        }
        sb.append('`');
        return sb.toString();
    }
}
