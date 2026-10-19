package io.banditoz.mchelper.commands.logic.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TextBinder {
    private TextBinder() {
    }

    /**
     * @param spec    The parameters to bind.
     * @param rawArgs Everything after the command name, from the message's <i>raw</i> content so mentions
     *                still look like mentions.
     * @param ctx     Resolution context.
     * @return One value per declared parameter, in order. Optional parameters are wrapped, never null.
     * @throws BindException If a required parameter is missing or a token cannot be parsed.
     */
    public static Object[] bind(ParamSpec spec, String rawArgs, BindContext ctx) throws BindException {
        List<String> tokens = tokenize(rawArgs == null ? "" : rawArgs);
        List<Arg<?>> params = spec.params();
        Object[] values = new Object[params.size()];
        int cursor = 0;

        for (int i = 0; i < params.size(); i++) {
            Arg<?> param = params.get(i);
            String raw;
            if (param.rest()) {
                raw = cursor < tokens.size() ? String.join(" ", tokens.subList(cursor, tokens.size())) : null;
                cursor = tokens.size();
            }
            else {
                raw = cursor < tokens.size() ? tokens.get(cursor++) : null;
            }

            if (raw == null) {
                if (param.required()) {
                    throw new BindException(param.name(), "is required, but was not given.");
                }
                values[i] = Optional.empty();
                continue;
            }

            Object parsed;
            try {
                parsed = param.type().parseText(raw, ctx);
            } catch (BindException ex) {
                throw ex.getParamName() == null ? new BindException(param.name(), ex.getMessage()) : ex;
            }
            values[i] = param.required() ? parsed : Optional.of(parsed);
        }
        return values;
    }

    /**
     * Splits on whitespace, but keeps {@code "quoted runs"} together so a parameter in the middle of the
     * argument list can contain spaces. The quotes themselves are stripped.
     *
     * @param input The raw argument string.
     * @return The tokens.
     */
    static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean started = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                started = true;
            }
            else if (Character.isWhitespace(c) && !inQuotes) {
                if (started) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
            }
            else {
                current.append(c);
                started = true;
            }
        }
        if (started) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
