package io.banditoz.mchelper.commands;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.StatisticsDao;
import io.banditoz.mchelper.utils.database.dao.StatisticsDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoCommand extends Command {
    private static final Pattern LOAD_PATTERN = Pattern.compile("(?:\\S+\\s+){2}\\S+");

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

        String uptime = DateUtils.humanReadableDuration(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()));

        List<User> users = new ArrayList<>();
        ce.getEvent().getJDA().getGuilds().forEach(guild -> guild.getMembers().forEach(member -> users.add(member.getUser())));
        int totalUsers = users.size();
        long distinctUsers = users.stream().distinct().count();

        StatisticsDao dao = new StatisticsDaoImpl(ce.getDatabase());
        int commandsRun = ce.getMCHelper().getCommandHandler().getCommandsRun();
        int commandsRunEver = dao.getTotalCommandsRun();

        ThreadPoolExecutor tpe = ce.getMCHelper().getThreadPoolExecutor();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads (active/all)", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Load Average", getLoadAverage(), true)
                .addField("Guilds", Integer.toString(ce.getEvent().getJDA().getGuilds().size()), true)
                .addField("Users (distinct/total)", String.format("%d/%d", distinctUsers, totalUsers), true)
                .addField("Running Commands", String.format("%d/%d", tpe.getActiveCount(), tpe.getMaximumPoolSize()), true)
                .addField("Commands Run (session/forever)", String.format("%d/%d", commandsRun, commandsRunEver), true)
                .addField("Uptime", uptime, true);
        ce.sendEmbedReply(eb.build());
        return Status.SUCCESS;
    }

    private String getLoadAverage() {
        try (InputStream inputStream = Runtime.getRuntime().exec("cat /proc/loadavg").getInputStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            String ssdf = s.next();
            Matcher m = LOAD_PATTERN.matcher(ssdf);
            m.find();
            return m.group();
        } catch (Exception e) {
            return "??? ??? ???";
        }
    }
}
