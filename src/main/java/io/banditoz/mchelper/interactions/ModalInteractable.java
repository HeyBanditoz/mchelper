package io.banditoz.mchelper.interactions;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModalInteractable extends Interactable<ModalInteraction, WrappedModalInteractionEvent> {
    private final Consumer<WrappedModalInteractionEvent> consumer;
    private final String modalId;

    // TODO javadoc
    public ModalInteractable(@NotNull Predicate<User> canInteract, long timeoutSeconds,
                             @NotNull String modalId,
                             @NotNull Consumer<WrappedModalInteractionEvent> consumer) {
        super(canInteract, timeoutSeconds);
        this.consumer = consumer;
        this.modalId = modalId;
    }

    @Override
    public boolean contains(ModalInteraction modal) {
        return modalId.equals(modal.getModalId());
    }

    public String getModalId() {
        return modalId;
    }

    @Override
    public void handleEvent(WrappedModalInteractionEvent event) {
        if (test(event) && consumer != null) {
            consumer.accept(event);
        }
    }

    public static final class Builder {
        private Consumer<WrappedModalInteractionEvent> consumer;
        private String modalId;
        private Predicate<User> canInteract;
        private long timeoutSeconds;

        public Builder() {
        }

        public Builder setCanInteract(Predicate<User> val) {
            canInteract = val;
            return this;
        }

        public Builder setConsumer(Consumer<WrappedModalInteractionEvent> val) {
            consumer = val;
            return this;
        }

        public Builder setModalId(String val) {
            modalId = val;
            return this;
        }

        public Builder setTimeoutSeconds(long val) {
            timeoutSeconds = val;
            return this;
        }

        public ModalInteractable build() {
            return new ModalInteractable(canInteract, timeoutSeconds, modalId, consumer);
        }
    }
}
