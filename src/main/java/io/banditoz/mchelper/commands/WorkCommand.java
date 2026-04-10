package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.database.TaskResponse;
import io.banditoz.mchelper.database.dao.TasksDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.money.TipGenerator;
import io.banditoz.mchelper.money.TipModifierCache;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class WorkCommand extends Command {
    private final ObjectMapper objectMapper;
    private final AccountManager am;
    private final TasksDao dao;
    private final TipGenerator tipGenerator;
    private final TipModifierCache tipModifierCache;
    private final InteractionListener interactionListener;

    private final List<TaskResponse> workResponses = new ArrayList<>();
    private final List<TaskResponse> rareResponses = new ArrayList<>();
    private final Random random = new SecureRandom();
    private static final Color rareColor = new Color(219, 173, 44);
    private static final BigDecimal five = new BigDecimal("5");
    private static final Logger log = LoggerFactory.getLogger(WorkCommand.class);

    @Inject
    public WorkCommand(ObjectMapper objectMapper,
                       AccountManager am,
                       TasksDao dao,
                       TipGenerator tipGenerator,
                       TipModifierCache tipModifierCache,
                       InteractionListener interactionListener) {
        this.objectMapper = objectMapper;
        this.am = am;
        this.dao = dao;
        this.tipGenerator = tipGenerator;
        this.tipModifierCache = tipModifierCache;
        this.interactionListener = interactionListener;
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
        LocalDateTime ldt = dao.getWhenCanExecute(ce.getUser().getIdLong(), Task.WORK);
        if (ldt.isBefore(LocalDateTime.now())) {
            Map<Button, Consumer<WrappedButtonClickEvent>> tipButtons = tipGenerator.getTipButtons(ce.getUser().getIdLong());
            // each tip consumer, after it runs, needs to run the actual work command consumer (andThen)
            tipButtons.forEach((button, consumer) -> tipButtons.replace(button, consumer.andThen(this::work)));
            ArrayList<Button> buttons = new ArrayList<>(tipButtons.keySet());
            Collections.shuffle(buttons);
            List<ActionRow> actionRows = ActionRow.partitionOf(buttons);
            MessageCreateData m = new MessageCreateBuilder()
                    .setContent("This week is employer appreciation week! Would you like to tip your employer?")
                    .setComponents(actionRows)
                    .build();
            ce.getEvent().getChannel().sendMessage(m).queue(message -> {
                ButtonInteractable i = new ButtonInteractable(tipButtons, ce.getEvent().getAuthor()::equals, 300, message, ce);
                interactionListener.addInteractable(i);
            });
            return Status.SUCCESS;
        }
        else {
            long unix = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            ce.sendReply("You cannot work until " + TimeFormat.DATE_TIME_LONG.format(unix));
            return Status.COOLDOWN;
        }
    }

    private void work(WrappedButtonClickEvent wrappedButtonClickEvent) {
        CommandEvent ce = wrappedButtonClickEvent.getCommandEvent();

        try {
            BigDecimal randAmount = Task.WORK.getRandomAmount();
            long userId = wrappedButtonClickEvent.getUser().getIdLong();
            BigDecimal tipModifier = tipModifierCache.get(wrappedButtonClickEvent.getUser().getIdLong());
            String tipStr = tipModifier.multiply(new BigDecimal("100")).toPlainString() + "%";
            wrappedButtonClickEvent.removeListenerAndDestroy(new EmbedBuilder().setColor(Color.MAGENTA).setTitle("Tip!").setDescription("You tipped " + tipStr).build());

            if (am.isUserShadowbanned(wrappedButtonClickEvent.getUser())) {
                BigDecimal earnings = randAmount.multiply(BigDecimal.valueOf(0.25));
                BigDecimal modifier = earnings.multiply(tipModifier);
                earnings = earnings.subtract(modifier);
                BigDecimal newBal = am.add(earnings, userId, "daily work (shadowbanned 75% of full earnings) (tip " + tipStr + ")");
                dao.putOrUpdateTask(ce.getUser().getIdLong(), Task.WORK);
                sendMessage(ce, earnings, newBal, false);
            }
            else {
                if (random.nextDouble() <= 0.10) {
                    BigDecimal earnings = randAmount.multiply(five);
                    BigDecimal modifier = earnings.multiply(tipModifier);
                    earnings = earnings.subtract(modifier);
                    BigDecimal newBal = am.add(earnings, userId, "daily work (rare) (tip " + tipStr + ")");
                    dao.putOrUpdateTask(userId, Task.WORK);
                    sendMessage(ce, earnings, newBal, true);
                }
                else {
                    BigDecimal modifier = randAmount.multiply(tipModifier);
                    randAmount = randAmount.subtract(modifier);
                    BigDecimal newBal = am.add(randAmount, userId, "daily work (tip " + tipStr + ")");
                    dao.putOrUpdateTask(userId, Task.WORK);
                    sendMessage(ce, randAmount, newBal, false);
                }
            }
        } catch (Exception ex) {
            log.error("This is not good.", ex);
            ce.sendReply("Something broke :(");
        }
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce) throws Exception {
        sce.sendEphermalReply("Only text command is supported for now! 😉");
        return Status.FAIL;
    }

    private void sendMessage(ICommandEvent ce, BigDecimal earnings, BigDecimal newBal, boolean isRare) {
        TaskResponse randResponse = isRare ? rareResponses.get(random.nextInt(rareResponses.size())) : workResponses.get(random.nextInt(workResponses.size()));
        String stringResponse = randResponse.getResponse(ce.getUser().getIdLong(), earnings, Task.WORK);
        EmbedBuilder eb = new EmbedBuilder()
                .appendDescription(stringResponse)
                .setFooter("New Balance: $" + AccountManager.format(newBal));
        if (isRare) {
            eb.setColor(rareColor);
        }
        ce.sendEmbedReply(eb.build());
    }
}
