package io.banditoz.mchelper.interactions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for all messages that need to listen to a button interaction from Discord.
 */
public class ButtonInteractable extends Interactable<Button, WrappedButtonClickEvent> {
    /**
     * A {@link ConcurrentHashMap} of Buttons that we are listening for. Made concurrent, so we can only add or remove
     * buttons on one thread at a time, as there's a potential for multiple threads to access this.
     */
    private final Map<Button, Consumer<WrappedButtonClickEvent>> buttons = new ConcurrentHashMap<>();
    /** The {@link Message} that the buttons are associated with. */
    private final Message message;
    /** The {@link CommandEvent} that originally created this. */
    private final CommandEvent ce;
    private final static Logger LOGGER = LoggerFactory.getLogger(ButtonInteractable.class);

    /**
     * Constructs a new {@link ButtonInteractable} to be listened to by the {@link InteractionListener}.
     *
     * @param map            A {@link Map} containing a {@link} Button and a {@link Consumer} that contains a
     *                       {@link WrappedButtonClickEvent}.
     * @param canInteract    Whether the user can interact with the entire {@link ButtonInteractable} or not.
     * @param timeoutSeconds The timeout when we should remove the {@link Button}s and stop accepting input, in seconds.
     */
    public ButtonInteractable(@NotNull Map<Button, Consumer<WrappedButtonClickEvent>> map, @NotNull Predicate<User> canInteract,
                              long timeoutSeconds, @NotNull Message m, @Nullable CommandEvent ce) {
        super(canInteract, timeoutSeconds);
        buttons.putAll(map);
        this.message = m;
        this.ce = ce;
    }

    /**
     * Runs the {@link Consumer} we have for the {@link WrappedButtonClickEvent} if the {@link Predicate} passes.
     *
     * @param event The {@link WrappedButtonClickEvent} to handle.
     */
    public void handleEvent(WrappedButtonClickEvent event) {
        if (test(event)) {
            Consumer<WrappedButtonClickEvent> consumer = buttons.get(event.getEvent().getButton());
            if (consumer != null) {
                consumer.accept(event);
            }
        }
        else {
            event.getEvent().deferEdit().queue();
        }
    }

    /**
     * Checks the internal {@link Map} for a button.
     *
     * @param button The {@link Button} to check for.
     * @return true if the {@link Button} is contained in the map somewhere, false otherwise.
     */
    @Override
    public boolean contains(Button button) {
        return buttons.containsKey(button);
    }

    /**
     * Removes all {@link Button} in the referenced {@link Message}.
     *
     * @deprecated Use {@link #destroy(WrappedButtonClickEvent)} as that is the smoother path.
     */
    public void destroy() {
        LOGGER.debug("Removing all buttons for " + message);
        super.destroy();
        message.editMessageComponents(Collections.emptyList()).queue();
    }

    /**
     * Removes all {@link Button} in the referenced {@link Message}. If the supplied {@link ButtonInteractionEvent}
     * hasn't been acknowledged yet, it'll acknowledge the event by removing the buttons. Else, edit the message (the
     * former is smoother)
     */
    public void destroy(WrappedButtonClickEvent event) {
        LOGGER.debug("Removing all buttons for " + message);
        super.destroy();
        if (event.getEvent().isAcknowledged()) {
            message.editMessageComponents(Collections.emptyList()).queue();
        }
        else {
            event.getEvent().editMessage(MessageEditBuilder.fromMessage(event.getMessage()).setComponents(Collections.emptyList()).build()).queue();
        }
    }

    /**
     * Removes all {@link Button} in the referenced {@link Message}.
     */
    public void destroy(MessageEmbed me) {
        LOGGER.debug("Removing all buttons for " + message);
        super.destroy();
        message.editMessageComponents(Collections.emptyList()).setEmbeds(me).queue();
    }

    public void destroyAndAddNewButtons(ActionRow... rows) {
        LOGGER.debug("Removing AND REPLACING buttons for " + message);
        super.destroy();
        message.editMessageComponents(rows).queue();
    }

    public void destroyAndAddNewButtons(MessageEmbed me, ButtonInteractionEvent event, ActionRow... rows) {
        LOGGER.debug("Removing AND REPLACING buttons and embeds for " + event);
        super.destroy();
        MessageEditData msgEdit = new MessageEditBuilder()
                .setEmbeds(me)
                .setComponents(rows)
                .build();
        if (!event.isAcknowledged()) {
            event.editMessage(msgEdit).queue();
        }
        else {
            event.getMessage().editMessage(msgEdit).queue();
        }
    }

    public ScheduledFuture<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(ScheduledFuture<?> future) {
        this.timeoutFuture = future;
    }

    public Message getMessage() {
        return message;
    }

    public CommandEvent getCommandEvent() {
        return ce;
    }
}
