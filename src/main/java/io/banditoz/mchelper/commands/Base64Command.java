package io.banditoz.mchelper.commands;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.enumOf;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Singleton;

@Singleton
public class Base64Command extends BoundCommand {
    public enum Mode {
        ENCODE,
        DECODE
    }

    @Override
    public String commandName() {
        return "base64";
    }

    @Override
    protected String description() {
        return "Encodes or decodes base64.";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .arg("mode", "Whether to encode or decode.", enumOf(Mode.class))
                .rest("text", "The text to convert.", this::convert);
    }

    private Status convert(ICommandEvent ce, Mode mode, String text) {
        String result = switch (mode) {
            case ENCODE -> Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
            case DECODE -> new String(Base64.getDecoder().decode(text.trim()), StandardCharsets.UTF_8);
        };
        ce.sendReply(result);
        return Status.SUCCESS;
    }
}
