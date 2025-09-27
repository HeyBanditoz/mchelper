package io.banditoz.mchelper;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.di.ConfigPlugin;
import io.opentelemetry.api.OpenTelemetry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Bot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    public static void main(String[] args) throws Exception {
        long before = System.currentTimeMillis();
        printSplash();
        // required for RSS reader library and avaje
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        List<String> argsList = Arrays.asList(args);
        if (argsList.size() > 0 && argsList.contains("dumpcommands")) {
//            CommandsToMarkdown.commandsToMarkdown();
            System.exit(0);
        }
        else if (argsList.size() > 0 && argsList.contains("migrate")) {
            new Database(OpenTelemetry.noop()).migrate(true);
            System.exit(0);
        }
        else if (argsList.size() > 0) {
            System.err.println("Unknown argument " + argsList.get(0));
        }
        else {
            log.info("Beginning dependency injection...");
            BeanScopeBuilder builder = BeanScope.builder();
            builder.configPlugin(new ConfigPlugin());
            builder.shutdownHook(true);
            BeanScope beanScope = builder.build();
            List<ListenerAdapter> listenerAdapters = beanScope.listByPriority(ListenerAdapter.class);
            JDA jda = beanScope.get(JDA.class);
            listenerAdapters.forEach(jda::addEventListener);
            log.info("Added {} listeners to JDA!", listenerAdapters.size());
            log.info("MCHelper initialization finished in {} seconds.", new DecimalFormat("#.#").format((System.currentTimeMillis() - before) / 1000D));
        }
    }

    private static void printSplash() {
        Logger logger = LoggerFactory.getLogger(Bot.class);
        logger.info("___  ________  _   _      _                 ");
        logger.info("|  \\/  /  __ \\| | | |    | |                ");
        logger.info("| .  . | /  \\/| |_| | ___| |_ __   ___ _ __ ");
        logger.info("| |\\/| | |    |  _  |/ _ \\ | '_ \\ / _ \\ '__|");
        logger.info("| |  | | \\__/\\| | | |  __/ | |_) |  __/ |   ");
        logger.info("\\_|  |_/\\____/\\_| |_/\\___|_| .__/ \\___|_|   ");
        logger.info("                           | |              ");
        logger.info("                           |_|              ");
        logger.info("MCHelper version {} using JDA {} running on JVM {} committed on {}", Version.GIT_SHA, JDAInfo.VERSION, Runtime.version(), Version.GIT_DATE);
    }
}
