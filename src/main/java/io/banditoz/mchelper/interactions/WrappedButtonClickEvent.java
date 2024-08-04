package io.banditoz.mchelper.interactions;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WrappedButtonClickEvent implements Interaction {
    private static final Logger log = LoggerFactory.getLogger(WrappedButtonClickEvent.class);
    private final ButtonInteractionEvent event;
    private final ButtonInteractable bi;
    private final MCHelper MCHelper;

    public WrappedButtonClickEvent(ButtonInteractionEvent event, ButtonInteractable bi, MCHelper MCHelper) {
        this.event = event;
        this.bi = bi;
        this.MCHelper = MCHelper;
    }

    public ButtonInteractionEvent getEvent() {
        return event;
    }

    public MCHelper getMCHelper() {
        return MCHelper;
    }

    public User getUser() {
        return event.getUser();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link InteractionListener}'s list, and removes all
     * buttons.
     */
    public void removeListenerAndDestroy() {
        bi.destroy(this);
        removeListener();
    }

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link InteractionListener}'s list.
     */
    @Override
    public void removeListener() {
        this.MCHelper.getInteractionListener().removeInteractableByButton(event.getButton());
    }

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link InteractionListener}'s list, and removes all
     * buttons
     *
     * @param finalEmbed The {@link MessageEmbed} that is to be replaced.
     */
    public void removeListenerAndDestroy(MessageEmbed finalEmbed) {
        bi.destroy(finalEmbed);
        removeListener();
    }

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link InteractionListener}'s list, and <i>replaces</i> all
     * buttons with the new {@link ActionRow ActionRows.}
     *
     * @param rows The {@link ActionRow ActionRows} to use.
     * @param bi   The {@link ButtonInteractable} to take place of the old one.
     */
    public void destroyThenReplaceWith(ButtonInteractable bi, ActionRow... rows) {
        bi.destroyAndAddNewButtons(rows);
        removeListener();
        MCHelper.getInteractionListener().addInteractable(bi);
    }

    public void destroyThenAddReplayer(MessageEmbed finalEmbed) {
        if (bi.getCommandEvent() == null) {
            throw new IllegalArgumentException("Cannot replay as the CommandEvent supplied to the ButtonListener is null.");
        }
        InteractionListener bl = MCHelper.getInteractionListener();
        Button replay = Button.primary(UUID.randomUUID().toString(), "♻");
        removeListener();
        if (event.isAcknowledged()) {
            log.info("{} was previously acknowledged. Editing the original message without button acknowledgement instead.", event);
        }
        bi.destroyAndAddNewButtons(finalEmbed, event, ActionRow.of(replay));
        ButtonInteractable replayInteraction = new ButtonInteractable(Map.of(replay, event -> {
            event.removeListenerAndDestroy();
            bi.getCommandEvent().replay();
        }), user -> user.equals(bi.getCommandEvent().getUser()), 15, bi.getMessage(), bi.getCommandEvent());
        bl.addInteractable(replayInteraction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedButtonClickEvent that = (WrappedButtonClickEvent) o;
        return Objects.equals(event.getIdLong(), that.event.getIdLong());
    }

    @Override
    public int hashCode() {
        return Long.hashCode(event.getIdLong());
    }
}
