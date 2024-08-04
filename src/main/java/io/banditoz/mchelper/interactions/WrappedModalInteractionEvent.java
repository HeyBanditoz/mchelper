package io.banditoz.mchelper.interactions;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public record WrappedModalInteractionEvent(ModalInteractionEvent event, MCHelper mcHelper) implements Interaction {
    @Override
    public User getUser() {
        return event.getUser();
    }

    @Override
    public void removeListener() {
        mcHelper.getInteractionListener().removeInteractableByModalId(event.getModalId());
    }
}
