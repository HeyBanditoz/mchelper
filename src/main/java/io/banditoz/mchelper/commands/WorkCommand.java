package io.banditoz.mchelper.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.TaskResponse;
import io.banditoz.mchelper.utils.database.dao.TasksDao;
import io.banditoz.mchelper.utils.database.dao.TasksDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Requires(database = true)
public class WorkCommand extends Command {
    private List<TaskResponse> workResponses;
    private final Random random = new SecureRandom();

    @Override
    public String commandName() {
        return "work";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Work for money every day.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (workResponses.isEmpty()) {
            // lazy evaluation I guess, lol
            ObjectMapper om = ce.getMCHelper().getObjectMapper();
            List<TaskResponse> tempList = om.readValue(getClass().getClassLoader()
                    .getResource("tasks_responses.json")
                    .openStream(), om.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));
            tempList.stream().filter(taskResponse -> taskResponse.getTask() == Task.WORK).forEach(workResponses::add);
        }
        TasksDao dao = new TasksDaoImpl(ce.getDatabase());
        LocalDateTime ldt = dao.getWhenCanExecute(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
        if (ldt.isBefore(LocalDateTime.now())) {
            BigDecimal earnings = Task.WORK.getRandomAmount();
            BigDecimal newBal = ce.getMCHelper().getAccountManager().add(earnings, ce.getEvent().getAuthor().getIdLong(), "daily work");
            TaskResponse randResponse = workResponses.get(random.nextInt(workResponses.size()));
            String stringResponse = randResponse.getResponse(ce.getEvent().getAuthor().getIdLong(), earnings, Task.WORK);
            dao.putOrUpdateTask(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
            ce.sendEmbedReply(new EmbedBuilder().appendDescription(stringResponse).setFooter("New Balance: $" + AccountManager.format(newBal)).build());
            return Status.SUCCESS;
        }
        else {
            long unix = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            ce.sendReply("You cannot work until " + TimeFormat.DATE_TIME_LONG.format(unix));
            return Status.COOLDOWN;
        }
    }
}
