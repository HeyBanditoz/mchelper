package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Param;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.MorseUtils;
import jakarta.inject.Singleton;

@Singleton
public class ToMorseCommand extends Command {
    @Override
    public String commandName() {
        return "tomorse";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<string>")
                .withDescription("Encodes a message to morse code.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.sendReply(MorseUtils.toMorse(ce.getCommandArgsString()));
        return Status.SUCCESS;
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                                 @Param(desc = "String to convert to morse code.") String morse) {
        sce.sendReply(MorseUtils.toMorse(morse));
        return Status.SUCCESS;
    }
}
