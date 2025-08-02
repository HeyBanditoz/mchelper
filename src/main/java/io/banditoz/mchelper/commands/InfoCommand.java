package io.banditoz.mchelper.commands;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.management.OperatingSystemMXBean;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.database.dao.StatisticsDao;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

@Singleton
public class InfoCommand extends Command {
    // TODO kludge
    @Inject
    CommandHandler commandHandler;
    @Inject
    ThreadPoolExecutor tpe;
    @Inject
    @Nullable
    StatisticsDao dao;

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
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();

        String uptime = TimeFormat.DATE_TIME_LONG.format(startTime) + "\nStarted " + TimeFormat.RELATIVE.format(startTime);

        List<User> users = new ArrayList<>();
        ce.getEvent().getJDA().getGuilds().forEach(guild -> guild.getMembers().forEach(member -> users.add(member.getUser())));
        int totalUsers = users.size();
        long distinctUsers = users.stream().distinct().count();

        int commandsRun = commandHandler.getCommandsRun();
        int commandsRunEver = dao == null ? 0 : dao.getTotalCommandsRun();

        String version = """
                %s %s
                %s %s""".formatted(System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"), System.getProperty("java.vm.name"), System.getProperty("java.version"));
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Bot Statistics")
                .addField("Heap Usage", String.format("%dMB/%dMB", usedJVMMemory, totalJVMMemory), true)
                .addField("Threads (active/all)", String.format("%d/%d", Thread.activeCount(), Thread.getAllStackTraces().size()), true)
                .addField("CPU Usage", new DecimalFormat("###.###%").format(bean.getProcessCpuLoad()), true)
                .addField("Load Average", getLoadAverage(), true)
                .addField("Guilds", Integer.toString(ce.getEvent().getJDA().getGuilds().size()), true)
                .addField("Users (guild/distinct/total)", String.format("%d/%d/%d", ce.getGuild().getMembers().size(), distinctUsers, totalUsers), true)
                .addField("Running Commands", String.format("%d/%d", tpe.getActiveCount(), tpe.getMaximumPoolSize()), true)
                .addField("Commands Run (session/forever)", String.format("%d/%d", commandsRun, commandsRunEver), true)
                .addField("Uptime", uptime, true)
                .addField("Java Version", version, true);
        ce.sendEmbedReply(eb.build());
        return Status.SUCCESS;
    }

    private String getLoadAverage() {
        try (InputStream inputStream = Runtime.getRuntime().exec("cat /proc/loadavg").getInputStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            String loadAvg = s.next();
            Matcher m = LOAD_PATTERN.matcher(loadAvg);
            m.find();
            return m.group();
        } catch (Exception e) {
            return "??? ??? ???";
        }
    }
}
