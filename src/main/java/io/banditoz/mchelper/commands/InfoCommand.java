package io.banditoz.mchelper.commands;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.EmbedBuilder;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

public class InfoCommand extends Command {
    @Override
    public String commandName() {
        return "!info";
    }

    @Override
    public void onCommand() {
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long usedJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long totalJVMMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Guilds", Integer.toString(MCHelper.jda.getGuilds().size()), true)
                .addField("Users", Integer.toString(MCHelper.jda.getGuilds().size()), true);
        sendEmbedReply(eb.build());
    }
}
