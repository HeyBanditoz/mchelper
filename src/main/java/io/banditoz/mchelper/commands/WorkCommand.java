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
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.Color;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Requires(database = true)
public class WorkCommand extends Command {
    private final List<TaskResponse> workResponses = new ArrayList<>();
    private final List<TaskResponse> rareResponses = new ArrayList<>();
    private final Random random = new SecureRandom();
    private static final Color rareColor = new Color(219, 173, 44);
    private static final BigDecimal five = new BigDecimal("5");
    private static final List<String> benefactorizedResponses = List.of(
            "%MENTION% worked hard for their money, but %BENEFACTOR% was a scammer and took %AMOUNT% over the phone!",
            "%BENEFACTOR% hacked %MENTION%'s machine and took %AMOUNT% worth of Bitcoin!",
            "%BENEFACTOR% started a memecoin, and %MENTION% decided to buy into it. %BENEFACTOR% rugpulled and earned %AMOUNT% from the venture!",
            "%BENEFACTOR% managed to convince %MENTION% to join DoTerra/Nu Skin/Beachbody/Young Living/Younique/any other stupid MLM and earned %AMOUNT% as commission." +
                    " (Sorry, this one only makes sense if you've been around Utah.)",
            """
            %MENTION% gave %BENEFACTOR% a taste of their whiskey (worth %AMOUNT%) and gave them some advice.
            *🎶You've got to know when to hold 'em
            Know when to fold 'em
            Know when to walk away
            And know when to run
            You never count your money
            When you're sittin' at the table
            There'll be time enough for countin'
            When the dealin's done.🎶*""",
            "%MENTION% stared into t̸͎̘̦̰̟́͆͑͆̐͑̐͛͘h̶̨̹̘̱͔̹̗̺̬̳̋̉̂̈͊̓͘e̷̡̝̳͗͋̾̓ ̴̣̼̒̒̄̂͌̈́̐̀̈́͜ṿ̴̢͔̮̞͈͈̓̔o̶̥̰̫̼͍͑̊ǐ̸͍̦̖͖̝͔̌̿̔̓̿̋d̵̡̨̳̜͇̼̥̘̳͉̑̈́̽̃͂̔̄̿͋̋, but it was just %BENEFACTOR% who took %AMOUNT% from them lol."
    );

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
            tempList.stream().filter(t -> t.task() == Task.WORK).filter(TaskResponse::notRare).forEach(workResponses::add);
            tempList.stream().filter(t -> t.task() == Task.WORK).filter(TaskResponse::rare).forEach(rareResponses::add);
        }
        TasksDao dao = new TasksDaoImpl(ce.getDatabase());
        AccountManager am = ce.getMCHelper().getAccountManager();
        LocalDateTime ldt = dao.getWhenCanExecute(ce.getEvent().getAuthor().getIdLong(), Task.WORK);
        if (ldt.isBefore(LocalDateTime.now())) {
            BigDecimal randAmount = Task.WORK.getRandomAmount().multiply(new BigDecimal(3));
            if (true) { // advanced diff pollution prevention/obfuscation techniques
                long invoker = ce.getEvent().getAuthor().getIdLong();
                // MONEY THEFT!!!
                Set<Long> membersInGuild = ce.getGuild().getMembers()
                        .stream()
                        .map(ISnowflake::getIdLong)
                        .collect(Collectors.toSet());
                long ourBenefactor = am.getAccountsWithTxnsInLastNDays(90) // get only last 90 days, to exclude orphaned accounts
                        .stream()
                        .filter(s -> s != invoker) // exclude ourselves
                        .filter(membersInGuild::contains) // only include people in the guild the user ran from (so the benefactor can be notified)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("This should never happen.")); // intentionally vague error message
                BigDecimal newBal = am.add(randAmount, ourBenefactor, "daily work (april fools 2024, invoked by " + invoker + ")");
                dao.putOrUpdateTask(invoker, Task.WORK);
                sendBenefactoredMessage(ce, randAmount, newBal, ourBenefactor);
                return Status.SUCCESS;
                // END MONEY THEFT!!!
            }
            else {
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

    private void sendBenefactoredMessage(CommandEvent ce, BigDecimal earnings, BigDecimal newBal, long benefactor) {
        String response = benefactorizedResponses.get(random.nextInt(benefactorizedResponses.size()))
                .replace("%MENTION%", "<@" + ce.getEvent().getAuthor().getIdLong() + ">")
                .replace("%AMOUNT%", "$" + AccountManager.format(earnings))
                .replace("%BENEFACTOR%", "<@" + benefactor + ">");
        MessageEmbed embed = new EmbedBuilder()
                .appendDescription(response)
                .setFooter("Benefactor's New Balance: $" + AccountManager.format(newBal))
                .build();
        MessageCreateData msg = new MessageCreateBuilder()
                .setEmbeds(embed)
                .setAllowedMentions(List.of(Message.MentionType.USER))
                .setContent("*<@" + benefactor + ">, heads up!*")
                .build();
        ce.getEvent().getChannel().sendMessage(msg).queue();
    }
}
