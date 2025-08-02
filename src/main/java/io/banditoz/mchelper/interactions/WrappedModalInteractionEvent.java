package io.banditoz.mchelper.interactions;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public record WrappedModalInteractionEvent(ModalInteractionEvent event, InteractionListener interactionListener) implements Interaction {
    @Override
    public User getUser() {
        return event.getUser();
    }

    @Override
    public void removeListener() {
        interactionListener.removeInteractableByModalId(event.getModalId());
    }
}
