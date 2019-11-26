package io.banditoz.mchelper.commands;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

public class InfoCommand extends Command {
    @Override
    public String commandName() {
        return "!info";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Returns various bot statistics.");
    }

    @Override
    protected void onCommand() {
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long usedJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long totalJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Guilds", Integer.toString(e.getJDA().getGuilds().size()), true)
                .addField("Users", Integer.toString(e.getJDA().getUsers().size()), true)
                .addField("Running Commands", String.format("%d/%d", ES.getActiveCount(), ES.getMaximumPoolSize()), true);
        sendEmbedReply(eb.build());
    }
}
