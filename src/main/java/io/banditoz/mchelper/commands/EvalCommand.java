package io.banditoz.mchelper.commands;

import io.avaje.inject.BeanScope;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nullable;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class EvalCommand extends ElevatedCommand {
    private final ScriptEngineManager manager;
    private final Database database;
    private final BeanScope beanScope;

    private static final Pattern LANG_CODE_BLOCK_MATCHER = Pattern.compile(".*[\\s\\S]*?```(?:\\w+)?\\n([\\s\\S]*?)```");

    @Inject
    public EvalCommand(@Nullable Database database, BeanScope beanScope) {
        manager = new ScriptEngineManager();
        this.database = database;
        this.beanScope = beanScope;
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("\\`\\`\\`groovy<newline>\\`\\`\\`")
                .withDescription("Evaluates Groovy. If you don't use code blocks, a return is added to the beginning " +
                        "of the code, otherwise, if you are using code blocks, you should return something.");
    }

    @Override
    public String commandName() {
        return "eval";
    }

    // Partially stolen from https://github.com/DV8FromTheWorld/Yui/blob/0eaeed13d97ab40225542a40014f79566e430daf/src/main/java/net/dv8tion/discord/commands/EvalCommand.java
    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ScriptEngine engine = manager.getEngineByName("groovy");
        Compilable compiler = (Compilable) engine;
        String rawArgs = ce.getEvent().getMessage().getContentRaw();
        Matcher argsMatcher = LANG_CODE_BLOCK_MATCHER.matcher(rawArgs);
        String args = argsMatcher.matches() ? argsMatcher.group(1) : ce.getCommandArgsString().replace("`", "");

        String imports = """
                import java.sql.*;
                import net.dv8tion.jda.*;
                import io.banditoz.mchelper.database.*;
                import io.banditoz.mchelper.money.*;
                import io.jenetics.facilejdbc.*;
                """;
        engine.put("ce", ce);
        engine.put("args", ce.getCommandArgs());
        engine.put("jda", ce.getEvent().getJDA());
        engine.put("db", database);
        engine.put("database", database);
        engine.put("bs", beanScope);
        engine.put("beanScope", beanScope);
        if (ce.getEvent().isFromGuild()) {
            engine.put("guild", ce.getEvent().getGuild());
            engine.put("member", ce.getEvent().getMember());
        }
        long beforeCompile = System.currentTimeMillis();
        CompiledScript groovyScript = compiler.compile(imports + args);
        long compileDuration = System.currentTimeMillis() - beforeCompile;

        long beforeRun = System.currentTimeMillis();
        Object out = groovyScript.eval();
        long runDuration = System.currentTimeMillis() - beforeRun;

        if (out == null) {
            ce.sendReply("Executed in " + runDuration + "ms. (Compile took " + compileDuration + " ms.)\n<null output>");
        }
        else {
            ce.sendPastableReply("Executed in " + runDuration + " ms. (Compile took " + compileDuration + " ms.)\n```\n" + out.toString() + "```");
        }
        return Status.SUCCESS;
    }
}
