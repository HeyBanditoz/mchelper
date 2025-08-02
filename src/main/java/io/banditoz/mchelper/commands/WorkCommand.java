package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.TaskResponse;
import io.banditoz.mchelper.database.dao.TasksDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;

@Singleton
@RequiresDatabase
public class WorkCommand extends Command {
    private final ObjectMapper objectMapper;
    private final AccountManager am;
    private final TasksDao dao;

    private final List<TaskResponse> workResponses = new ArrayList<>();
    private final List<TaskResponse> rareResponses = new ArrayList<>();
    private final Random random = new SecureRandom();
    private static final Color rareColor = new Color(219, 173, 44);
    private static final BigDecimal five = new BigDecimal("5");

    @Inject
    public WorkCommand(ObjectMapper objectMapper,
                       AccountManager am,
                       TasksDao dao) {
        this.objectMapper = objectMapper;
        this.am = am;
        this.dao = dao;
    }

    @PostConstruct
    public void populateResponses() {
        try {
            List<TaskResponse> tempList = objectMapper.readValue(getClass().getClassLoader()
                    .getResource("tasks_responses.json")
                    .openStream(), objectMapper.getTypeFactory().constructCollectionType(List.class, TaskResponse.class));
            tempList.stream().filter(t -> t.task() == Task.WORK).filter(TaskResponse::notRare).forEach(workResponses::add);
            tempList.stream().filter(t -> t.task() == Task.WORK).filter(TaskResponse::rare).forEach(rareResponses::add);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

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
        LocalDateTime ldt = dao.getWhenCanExecute(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
        if (ldt.isBefore(LocalDateTime.now())) {
            BigDecimal randAmount = Task.WORK.getRandomAmount();
            if (am.isUserShadowbanned(ce.getEvent().getAuthor())) {
                BigDecimal earnings = randAmount.multiply(BigDecimal.valueOf(0.25));
                BigDecimal newBal = am.add(earnings, ce.getEvent().getAuthor().getIdLong(), "daily work (shadowbanned 75% of full earnings)");
                dao.putOrUpdateTask(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
                sendMessage(ce, earnings, newBal, false);
            }
            else {
                if (random.nextDouble() <= 0.10) {
                    BigDecimal earnings = randAmount.multiply(five);
                    BigDecimal newBal = am.add(earnings, ce.getEvent().getAuthor().getIdLong(), "daily work (rare)");
                    dao.putOrUpdateTask(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
                    sendMessage(ce, earnings, newBal, true);
                }
                else {
                    BigDecimal newBal = am.add(randAmount, ce.getEvent().getAuthor().getIdLong(), "daily work");
                    dao.putOrUpdateTask(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
                    sendMessage(ce, randAmount, newBal, false);
                }
            }
            return Status.SUCCESS;
        }
        else {
            long unix = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            ce.sendReply("You cannot work until " + TimeFormat.DATE_TIME_LONG.format(unix));
            return Status.COOLDOWN;
        }
    }

    private void sendMessage(CommandEvent ce, BigDecimal earnings, BigDecimal newBal, boolean isRare) {
        TaskResponse randResponse = isRare ? rareResponses.get(random.nextInt(rareResponses.size())) : workResponses.get(random.nextInt(workResponses.size()));
        String stringResponse = randResponse.getResponse(ce.getEvent().getAuthor().getIdLong(), earnings, Task.WORK);
        EmbedBuilder eb = new EmbedBuilder()
                .appendDescription(stringResponse)
                .setFooter("New Balance: $" + AccountManager.format(newBal));
        if (isRare) {
            eb.setColor(rareColor);
        }
        ce.sendEmbedReply(eb.build());
    }
}
