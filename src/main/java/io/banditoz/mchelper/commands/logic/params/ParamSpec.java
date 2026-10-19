package io.banditoz.mchelper.commands.logic.params;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A command's declared parameters, in positional order.
 * <p>
 * Built by the {@link Params} chain, then read by everything that needs to know a command's shape: the text
 * binder, the slash binder, the Discord slash command definition, and the help command.
 *
 * @param params The parameters, in order.
 */
public record ParamSpec(List<Arg<?>> params) {
    public ParamSpec {
        params = List.copyOf(params);
        validate(params);
    }

    /**
     * @return A usage string such as {@code <to> <amount> [memo...]}, or null when the command takes no
     * parameters, matching what {@link io.banditoz.mchelper.utils.Help} expects.
     */
    @Nullable
    public String toUsage() {
        if (params.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Arg<?> param : params) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(param.required() ? '<' : '[')
                    .append(param.name())
                    .append(param.rest() ? "..." : "")
                    .append(param.required() ? '>' : ']');
        }
        return sb.toString();
    }

    private static void validate(List<Arg<?>> params) {
        Set<String> seen = new HashSet<>();
        boolean sawOptional = false;
        for (Arg<?> param : params) {
            if (!seen.add(param.name())) {
                throw new IllegalArgumentException("Duplicate parameter name '" + param.name() + "'.");
            }
            if (param.required() && sawOptional) {
                throw new IllegalArgumentException("Parameter '" + param.name() + "' is required but follows"
                        + " an optional parameter, which cannot be bound positionally from text."
                        + " Declare the optional parameters last.");
            }
            sawOptional |= !param.required();
        }
    }
}
