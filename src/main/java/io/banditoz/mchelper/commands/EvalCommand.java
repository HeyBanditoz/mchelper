package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.ChannelType;

import javax.script.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EvalCommand extends ElevatedCommand {
    private final ScriptEngineManager manager;

    public EvalCommand() {
        manager = new ScriptEngineManager();
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("\\`\\`\\`java<newline>\\`\\`\\`")
                .withDescription("Evaluates JShell. If you don't use code blocks, a return is added to the beginning of the code," +
                        "otherwise, if you are using code blocks, you should return something.");
    }

    @Override
    public String commandName() {
        return "eval";
    }

    // Partially stolen from https://github.com/DV8FromTheWorld/Yui/blob/0eaeed13d97ab40225542a40014f79566e430daf/src/main/java/net/dv8tion/discord/commands/EvalCommand.java
    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ScriptEngine engine = manager.getEngineByName("java");
        // quick n' dirty zeroth (command prefix and name) removal.
        String args = ce.getEvent().getMessage().getContentRaw().replaceFirst("." + commandName() + "\\s+", "");
        if (args.startsWith("```java")) {
            args = args.replace("```java", "").replace("```", "");
        }
        if (!args.contains("return ")) {
            args = "return " + args;
        }
        StringBuilder imports = new StringBuilder("""
                import java.util.*;
                import io.banditoz.mchelper.utils.database.*;
                import io.banditoz.mchelper.utils.database.dao.*;
                import io.banditoz.mchelper.commands.logic.*;
                import net.dv8tion.jda.api.*;
                import net.dv8tion.jda.api.entities.*;
                """);

        List<String> argsList = new ArrayList<>(List.of(args.split("\n")));
        Iterator<String> iterator = argsList.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.startsWith("import ")) {
                iterator.remove();
                imports.append(line);
            }
        }
        args = String.join("\n", argsList).stripLeading().trim();

        String initial = """
                package script;
                %s

                public class Script {
                    public CommandEvent ce;
                    public Database db;
                    public String[] args;
                    public JDA jda;
                    public Guild guild;
                    public Member member;
                                
                    public Object run() throws Exception {
                        %s%s
                    }
                }
                """.formatted(imports.toString(), args, !args.endsWith(";") ? ";" : "");

        Compilable c = (Compilable) engine;
        long before = System.currentTimeMillis();
        CompiledScript cs = null;
        try {
            cs = c.compile(initial);
        } catch (ScriptException e) {
            ScriptException ex = new ScriptException("```\n" + e.getMessage() + "```");
            ex.initCause(e);
            throw ex;
        }
        long compileDuration = System.currentTimeMillis() - before;
        Bindings b = engine.createBindings();
        b.put("ce", ce);
        b.put("db", ce.getDatabase());
        b.put("args", ce.getCommandArgs());
        b.put("jda", ce.getEvent().getJDA());
        if (ce.getEvent().isFromType(ChannelType.TEXT)) {
            b.put("guild", ce.getEvent().getGuild());
            b.put("member", ce.getEvent().getMember());
        }
        before = System.currentTimeMillis();
        Object out = cs.eval(b);
        long runDuration = System.currentTimeMillis() - before;
        if (out == null) {
            ce.sendReply("Executed in " + runDuration + "ms. (Compile took " + compileDuration + " ms.)\n<null output>");
        }
        else {
            ce.sendPastableReply("Executed in " + runDuration + " ms. (Compile took " + compileDuration + " ms.)\n```\n" + out.toString() + "```");
        }
        return Status.SUCCESS;

    }
}
