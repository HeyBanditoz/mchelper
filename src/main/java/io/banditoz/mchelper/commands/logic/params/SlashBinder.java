package io.banditoz.mchelper.commands.logic.params;

import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Binds a slash command's options against a {@link ParamSpec}.
 */
public final class SlashBinder {
    private SlashBinder() {
    }

    /**
     * @param spec  The parameters to bind.
     * @param event The interaction carrying the options.
     * @param ctx   Resolution context.
     * @return One value per declared parameter, in order. Optional parameters are wrapped, never null.
     * @throws BindException If a required option is missing, or one cannot be converted.
     */
    public static Object[] bind(ParamSpec spec, SlashCommandInteractionEvent event, BindContext ctx) throws BindException {
        List<Arg<?>> params = spec.params();
        Object[] values = new Object[params.size()];

        for (int i = 0; i < params.size(); i++) {
            Arg<?> param = params.get(i);
            OptionMapping option = event.getOption(param.name());

            if (option == null) {
                if (param.required()) {
                    // Discord enforces required options, so this means our definition and theirs disagree.
                    throw new BindException(param.name(), "is required, but Discord did not send it.");
                }
                values[i] = Optional.empty();
                continue;
            }
            if (option.getType() != param.type().optionType()) {
                throw new BindException(param.name(), "expected " + param.type().optionType()
                        + ", but Discord sent " + option.getType() + ".");
            }

            Object parsed;
            try {
                parsed = param.type().fromOption(option, ctx);
            } catch (BindException ex) {
                throw ex.getParamName() == null ? new BindException(param.name(), ex.getMessage()) : ex;
            }
            values[i] = param.required() ? parsed : Optional.of(parsed);
        }
        return values;
    }
}
