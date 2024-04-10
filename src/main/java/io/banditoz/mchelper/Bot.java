package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.database.Database;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.List;

public class Bot {
    public static void main(String[] args) throws Exception {
        // required for RSS reader library, a little silly
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        List<String> argsList = Arrays.asList(args);
        if (argsList.size() > 0 && argsList.contains("dumpcommands")) {
            CommandsToMarkdown.commandsToMarkdown();
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
            new MCHelperImpl();
        }
    }
}
