package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TeXRenderer;

public class TeXCommand extends Command {
    @Override
    public String commandName() {
        return "!tex";
    }

    @Override
    protected void onCommand() {
        try {
            TeXRenderer.sendTeXToChannel(e, commandArgsString);
        } catch (Exception ex) {
            sendExceptionMessage(ex);
        }
    }
}
