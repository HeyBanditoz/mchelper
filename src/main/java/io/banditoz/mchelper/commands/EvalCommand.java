package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.ChannelType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EvalCommand extends ElevatedCommand {
    private ScriptEngine engine;

    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("groovy");
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("\\`\\`\\`groovy<newline>\\`\\`\\`")
                .withDescription("Evaluates Groovy. If you don't use code blocks, a return is added to the beginning of the code," +
                        "otherwise, if you are using code blocks, you should return something.");
    }

    @Override
    public String commandName() {
        return "eval";
    }

    // Partially stolen from https://github.com/DV8FromTheWorld/Yui/blob/0eaeed13d97ab40225542a40014f79566e430daf/src/main/java/net/dv8tion/discord/commands/EvalCommand.java
    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        String args;
        if (ce.getCommandArgsString().startsWith("```groovy")) {
            args = ce.getCommandArgsString().replace("```groovy", "").replace("```", "");
        }
        else {
            args = ce.getCommandArgsString();
        }
        String imports = "import net.dv8tion.jda.*;\n" +
                "import java.util.*;\n" +
                "import io.banditoz.mchelper.utils.database.*;\n";
        engine.put("ce", ce);
        engine.put("args", ce.getCommandArgs());
        engine.put("jda", ce.getEvent().getJDA());
        if (ce.getEvent().isFromType(ChannelType.TEXT)) {
            engine.put("guild", ce.getEvent().getGuild());
            engine.put("member", ce.getEvent().getMember());
        }
        Object out = engine.eval(imports + args);
        if (out == null) {
            ce.sendReply(null); // checked in CommandUtils
        }
        else {
            ce.sendReply(out.toString());
        }
    }
}
