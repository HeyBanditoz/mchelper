package io.banditoz.mchelper.commands;

import com.sun.management.HotSpotDiagnosticMXBean;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.utils.Help;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class HeapDumpCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "heapdump";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("<boolean>")
                .withDescription("Dumps the heap.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        boolean live;
        String fileName;

        if (ce.getCommandArgs().length == 1) {
            live = true;
        }
        else {
            live = Boolean.parseBoolean(ce.getCommandArgs()[1]);
        }

        fileName = "./heapdump-" + OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT) + (live ? "-true" : "-false") + ".hprof";

        long before = System.nanoTime();
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                    server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(fileName, live);
            long after = System.nanoTime() - before;
            ce.sendReply("Done. Heap dump (with filename `" + fileName + "`) created in " + (after / 1000000) + " ms.");
        } catch (IOException ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
