package io.banditoz.mchelper.commands;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class InfoCommand extends Command {
    @Override
    public String commandName() {
        return "info";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Returns various bot statistics.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long usedJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long totalJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        String uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase(); // https://stackoverflow.com/a/40487511
        AtomicInteger users = new AtomicInteger();
        ce.getEvent().getJDA().getGuilds().forEach(guild -> users.addAndGet(guild.getMemberCount()));
        ThreadPoolExecutor tpe = ce.getMCHelper().getThreadPoolExecutor();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Guilds", Integer.toString(ce.getEvent().getJDA().getGuilds().size()), true)
                .addField("Users", Integer.toString(users.get()), true)
                .addField("Running Commands", String.format("%d/%d", tpe.getActiveCount(), tpe.getMaximumPoolSize()), true)
                .addField("Uptime", uptime, true);
        ce.sendEmbedReply(eb.build());
        return Status.SUCCESS;
    }
}
