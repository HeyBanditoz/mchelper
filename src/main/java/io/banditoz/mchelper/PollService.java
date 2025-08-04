package io.banditoz.mchelper;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.Priority;
import io.banditoz.mchelper.database.Poll;
import io.banditoz.mchelper.database.PollQuestion;
import io.banditoz.mchelper.database.PollType;
import io.banditoz.mchelper.database.Question;
import io.banditoz.mchelper.database.dao.PollsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.utils.ProgressBar;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
@Priority(0)
public class PollService extends ListenerAdapter {
    private final JDA jda;
    private final PollsDao dao;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, Poll> polls = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PollService.class);
    /** <a href="https://en.wikipedia.org/wiki/Regional_indicator_symbol">Regional indicator symbol</a> */
    private static final String[] letters = {"\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA",
                                             "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF",
                                             "\uD83C\uDDF0", "\uD83C\uDDF1", "\uD83C\uDDF2", "\uD83C\uDDF3", "\uD83C\uDDF4",
                                             "\uD83C\uDDF5", "\uD83C\uDDF6", "\uD83C\uDDF7", "\uD83C\uDDF8", "\uD83C\uDDF9"};

    @Inject
    public PollService(JDA jda,
                       PollsDao dao,
                       ScheduledExecutorService scheduledExecutorService) {
        this.jda = jda;
        this.dao = dao;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @PostConstruct
    public void populatePolls() {
        try {
            List<Poll> polls = dao.getAllPolls();
            for (int i = 0; i < polls.size(); i++) {
                Poll p = polls.get(i);
                MessageChannel channel = jda.getChannelById(MessageChannel.class, p.channelId());
                if (channel == null) {
                    log.warn("Channel was null from poll {}.", p);
                    continue;
                }
                // ratelimit populating polls
                scheduledExecutorService.schedule(() -> channel.retrieveMessageById(p.messageId()).queue(message -> addToMap(p),
                                throwable -> log.warn("Poll " + p + " couldn't be built. " +
                                        "Message probably doesn't exist, or we don't have permission to see it. Orphaned data cleanup may be required.", throwable)),
                        i + 1, TimeUnit.SECONDS);
            }
            log.info("Polls loaded. We have {} active polls.", polls.size());
        } catch (SQLException e) {
            log.error("Could not populate polls.", e);
        }
    }

    private void addToMap(Poll p) {
        // poll questions
        for (PollQuestion pq : p.questions()) {
            polls.put(pq.buttonUuid(), p);
        }
        // cancel button
        polls.put(p.closedButtonUuid(), p);
    }

    public void createPollAndSendMessage(String title, List<String> desiredQuestions, PollType pollType, MessageReceivedEvent e) throws SQLException {
        if (desiredQuestions.size() > 20) {
            throw new IllegalArgumentException("Number of questions must be less than or equal to 20. Got " + desiredQuestions.size());
        }
        if (title.length() > 250) {
            throw new IllegalArgumentException("Title must be less or equal 250 characters. Got " + title.length());
        }
        desiredQuestions.stream().filter(s -> s.length() > 64).findAny().ifPresent(s -> {
            throw new IllegalArgumentException("One or more of your questions is longer than 64 characters.");
        });

        List<Question> questions = desiredQuestions.stream().map(s -> new Question(UUID.randomUUID().toString(), s)).toList();

        UUID closedButtonUuid = UUID.randomUUID();
        List<Button> rows = new ArrayList<>();
        // LinkedHashMap has predictable insertion order, used to maintain order question when building initial embed
        Map<PollQuestion, Integer> dummyPqs = new LinkedHashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            rows.add(Button.primary(q.buttonUuid(), Emoji.fromUnicode(letters[i])));
            dummyPqs.put(new PollQuestion(0, 0, q.question(), (short) i, q.buttonUuid()), 0);
        }
        rows.add(Button.danger(closedButtonUuid.toString(), "\uD83D\uDEAE"));

        MessageCreateData createdMessage = new MessageCreateBuilder()
                .addComponents(ActionRow.partitionOf(rows))
                .setEmbeds(generateEmbed(title, pollType, e.getAuthor(), dummyPqs.keySet().stream().toList(), dummyPqs))
                .build();
        // using complete() to track command failures
        Message message = e.getChannel().sendMessage(createdMessage).complete();
        Poll t = dao.createPoll(title, questions, pollType, e, message.getIdLong(), closedButtonUuid);
        log.info("Poll created. {}", t);
        addToMap(t);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.isAcknowledged()) {
            // processed by another button interaction listener
            return;
        }
        Poll p = polls.get(event.getComponentId());
        if (p == null) {
            // the button (most likely) doesn't belong to the poll listener; return, and let ButtonListener ack it (that component ID could belong to them.)
            return;
        }
        String componentId = event.getComponentId();
        try {
            if (componentId.equals(p.closedButtonUuid())) {
                handleEnd(event, p);
            }
            else {
                handleVote(event, p);
            }
        } catch (SQLException ex) {
            log.error("Error while handling voting/end.", ex);
            event.reply(ex.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleVote(ButtonInteractionEvent event, Poll p) throws SQLException {
        dao.toggleVote(event.getComponentId(), event.getUser(), p);
        event.editMessageEmbeds(generateEmbed(p)).queue();
    }

    private void handleEnd(ButtonInteractionEvent event, Poll p) throws SQLException {
        if (p.authorId() == event.getMember().getIdLong()) {
            dao.closePollById(p.id());
            log.info("Poll ended. {}", p);
            for (PollQuestion pq : p.questions()) {
                polls.remove(pq.buttonUuid());
            }
            event.editComponents(Collections.emptyList()).queue();
        }
    }

    private MessageEmbed generateEmbed(Poll p) throws SQLException {
        // TODO cache results maybe?
        Map<PollQuestion, Integer> results = dao.getResults(p.questions());
        User u = jda.getUserById(p.authorId());
        return generateEmbed(p.title(), p.type(), u, p.questions(), results);
    }

    private MessageEmbed generateEmbed(String title, PollType type, User u, List<PollQuestion> questions, Map<PollQuestion, Integer> results) {
        int resultsSum = results.values().stream().reduce(Integer::sum).orElse(0);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("\uD83D\uDCCA " + escape(title))
                .setFooter(type.nameAsCapitalized() + " mode. " + "Added by " + (u == null ? "Unknown#0000" : u.getName() + '.'), (u == null ? "https://discord.com/assets/28174a34e77bb5e5310ced9f95cb480b.png" : u.getAvatarUrl()));
        for (int i = 0; i < questions.size(); i++) {
            PollQuestion pq = questions.get(i);
            int c = results.get(pq);
            eb.addField(letters[i] + " **" + escape(pq.question()) + "**:", generateCodeBlockedBar((double) c / resultsSum) + ' ' + c + " vote" + (c == 1 ? ". (" : "s. (")  + (int) Math.round(((double) c / resultsSum * 100.0) * 10.0) / 10.0 + "%)", false);
        }
        return eb.build();
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        try {
            disablePollsByMessageId(List.of(event.getMessageIdLong()));
        } catch (SQLException e) {
            log.warn("Could not disable poll.", e);
        }
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        try {
            disablePollsByMessageId(event.getMessageIds().stream().map(Long::valueOf).toList());
        } catch (SQLException e) {
            log.warn("Could not disable poll.", e);
        }
    }

    public void disablePollsByMessageId(List<Long> messages) throws SQLException {
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("Messages list cannot be empty!");
        }
        dao.closePollsByMessageIds(messages);
        // clean up button -> polls map, so we don't hold unnecessary references
        polls.values()
                .stream()
                .filter(poll -> messages.contains(poll.messageId()))
                .map(Poll::questions)
                .flatMap(List::stream)
                .forEach(pollQuestion -> polls.remove(pollQuestion.buttonUuid()));
    }

    private String generateCodeBlockedBar(double c) {
        try {
            return '`' + ProgressBar.generateProgressBar(c, 0.0, 1.0, 20) + '`';
        } catch (Exception ex) {
            log.warn("Error generating progress bar with c=" + c + ".", ex);
            return "`!PROGRESSBARERROR!`";
        }
    }
}
