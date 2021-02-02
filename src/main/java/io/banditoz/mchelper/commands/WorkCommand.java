package io.banditoz.mchelper.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.TaskResponse;
import io.banditoz.mchelper.utils.database.dao.TasksDao;
import io.banditoz.mchelper.utils.database.dao.TasksDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class WorkCommand extends Command {
    private final List<TaskResponse> workResponses;

    @Override
    public String commandName() {
        return "work";
    }

    public WorkCommand(ObjectMapper om) throws IOException {
        // TODO this kinda sucks. Lol.
        List<TaskResponse> tempList = om.readValue(getClass().getClassLoader()
                .getResource("tasks_responses.json")
                .openStream(), om.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));
        workResponses = tempList.stream().filter(taskResponse -> taskResponse.getTask() == Task.WORK).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Work for money every day.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        TasksDao dao = new TasksDaoImpl(ce.getDatabase());
        LocalDateTime ldt = dao.getWhenCanExecute(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
        if (ldt.isBefore(LocalDateTime.now())) {
            BigDecimal earnings = Task.WORK.getRandomAmount();
            BigDecimal newBal = ce.getMCHelper().getAccountManager().add(earnings, ce.getEvent().getAuthor().getIdLong(), "daily work");
            TaskResponse randResponse = workResponses.get(ThreadLocalRandom.current().nextInt(workResponses.size()));
            String stringResponse = randResponse.getResponse(ce.getEvent().getAuthor().getIdLong(), earnings, Task.WORK);
            dao.putOrUpdateTask(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
            ce.sendEmbedReply(new EmbedBuilder().appendDescription(stringResponse).setFooter("New Balance: $" + AccountManager.format(newBal)).build());
            return Status.SUCCESS;
        }
        else {
            Duration d = Duration.between(LocalDateTime.now(), ldt);
            ce.sendReply("You cannot work for another " + DateUtils.humanReadableDuration(d) + ".");
            return Status.COOLDOWN;
        }
    }
}
