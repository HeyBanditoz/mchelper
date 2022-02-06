package io.banditoz.mchelper.interactions;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class WrappedButtonClickEvent {
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

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link ButtonListener}'s list, and removes all
     * buttons.
     */
    public void removeListenerAndDestroy() {
        bi.destroy();
        this.MCHelper.getButtonListener().removeInteractableByButton(event.getButton());
    }

    /**
     * Removes the underlying {@link ButtonInteractable} from {@link ButtonListener}'s list, and removes all
     * buttons
     *
     * @param finalEmbed The {@link MessageEmbed} that is to be replaced.
     */
    public void removeListenerAndDestroy(MessageEmbed finalEmbed) {
        bi.destroy(finalEmbed);
        this.MCHelper.getButtonListener().removeInteractableByButton(event.getButton());
    }
}
