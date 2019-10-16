package io.banditoz.mchelper.commands;

import net.dv8tion.jda.api.entities.ChannelType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSEvalCommand extends ElevatedCommand {
    private ScriptEngine engine;
    public JSEvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");

    }

    @Override
    public String commandName() {
        return "!eval";
    }

    // Partially stolen from https://github.com/DV8FromTheWorld/Yui/blob/0eaeed13d97ab40225542a40014f79566e430daf/src/main/java/net/dv8tion/discord/commands/EvalCommand.java
    @Override
    protected void onCommand() {
        if (commandArgsString.startsWith("```js")) {
            commandArgsString = commandArgsString.replace("```js", "").replace("```", "");
        }
        try {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.api," +
                    "Packages.net.dv8tion.jda.api.entities," +
                    "Packages.net.dv8tion.jda.api.entities.impl," +
                    "Packages.net.dv8tion.jda.api.managers," +
                    "Packages.net.dv8tion.jda.api.managers.impl," +
                    "Packages.net.dv8tion.jda.api.utils);");
            engine.put("event", e);
            engine.put("message", e.getMessage());
            engine.put("channel", e.getChannel());
            engine.put("args", commandArgs);
            engine.put("api", e.getJDA());
            if (e.isFromType(ChannelType.TEXT))
            {
                engine.put("guild", e.getGuild());
                engine.put("member", e.getMember());
            }
            Object out = engine.eval(
                    "(function() {" +
                            "with (imports) {" +
                            (commandArgsString) +
                            "}" +
                            "})();");
            if (out == null) {
                sendReply(null); // checked in CommandUtils
            }
            else {
                sendReply(out.toString());
            }
        } catch (Exception ex) {
            CommandUtils.sendExceptionMessage(e, ex, logger, true, true);
        }
    }
}
