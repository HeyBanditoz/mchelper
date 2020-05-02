package io.banditoz.mchelper.commands;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
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
    protected void onCommand(CommandEvent ce) {
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long usedJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long totalJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        String uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase(); // https://stackoverflow.com/a/40487511
        String messageCacheSize = "null";
        if (MCHelper.getMessageCache() != null) {
            messageCacheSize = Integer.toString(MCHelper.getMessageCache().getSize());
        }
        AtomicInteger users = new AtomicInteger();
        ce.getEvent().getJDA().getGuilds().forEach(guild -> users.addAndGet(guild.getMemberCount()));
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Guilds", Integer.toString(ce.getEvent().getJDA().getGuilds().size()), true)
                .addField("Users", Integer.toString(users.get()), true)
                .addField("Running Commands", String.format("%d/%d", ES.getActiveCount(), ES.getMaximumPoolSize()), true)
                .addField("Uptime", uptime, true)
                .addField("Message Cache Size", messageCacheSize, true);
        ce.sendEmbedReply(eb.build());
    }
}
