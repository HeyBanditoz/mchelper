package io.banditoz.mchelper.interactions;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * The interaction listener class, listening for either:
 * <ul>
 *     <li>{@link ButtonInteractionEvent} to dispatch to {@link ButtonInteractable}.</li>
 *     <li>{@link ModalInteractionEvent} to dispatch to {@link ModalInteractable}.</li>
 * </ul>
 */
public class InteractionListener extends ListenerAdapter {
    private final ScheduledExecutorService SES;
    private final List<Interactable<?, ?>> INTERACTABLES = new CopyOnWriteArrayList<>();
    private final MCHelper MCHELPER;
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionListener.class);

    public InteractionListener(MCHelper mcHelper) {
        this.SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("BL-Scheduled-%d").build());
        this.MCHELPER = mcHelper;
    }

    public void addInteractable(Interactable<?, ?> i) {
        INTERACTABLES.add(i);
        long timeout = i.getTimeoutSeconds();
        if (i.getTimeoutSeconds() > 0) {
            i.setTimeoutFuture(SES.schedule(() -> {
                i.destroy();
                INTERACTABLES.remove(i);
            }, timeout, TimeUnit.SECONDS));
        }
    }

    public void removeInteractableByButton(Button button) {
        INTERACTABLES.removeIf(interactable -> {
            if (interactable instanceof ButtonInteractable bi) {
                return bi.contains(button);
            }
            return false;
        });
    }

    public void removeInteractableByModalId(String modalId) {
        INTERACTABLES.removeIf(interactable -> {
            if (interactable instanceof ModalInteractable mi) {
                return mi.getModalId().equals(modalId);
            }
            return false;
        });
    }

    /**
     * @return The number of {@link ButtonInteractable ButtonInteractables} contained within this InteractionListener.
     * {@link ModalInteractable ModalInteractables} don't block.
     */
    public int getActiveInteractables() {
        return (int) INTERACTABLES.stream().filter(i -> i instanceof ButtonInteractable).count();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.isAcknowledged()) {
            return; // processed by another button interaction listener
        }
        try {
            LOGGER.debug("We have " + INTERACTABLES.size() + " interactables.");
            INTERACTABLES.stream()
                    .filter(bi -> bi instanceof ButtonInteractable)
                    .map(bi -> (ButtonInteractable) bi)
                    .filter(i -> i.contains(event.getButton()))
                    .findFirst()
                    .ifPresentOrElse(i -> {
                        ScheduledFuture<?> future = i.getTimeoutFuture();
                        if (future != null) {
                            LOGGER.debug("Refreshing ScheduledFuture for " + event.getButton().getId() + " (had " + future.getDelay(TimeUnit.SECONDS) + " seconds left.)");
                            future.cancel(false);
                            i.setTimeoutFuture(SES.schedule(() -> {
                                i.destroy();
                                INTERACTABLES.remove(i);
                            }, i.getTimeoutSeconds(), TimeUnit.SECONDS));
                        }
                i.handleEvent(new WrappedButtonClickEvent(event, i, MCHELPER));
            }, () -> event.reply("Unfortunately, the button `" + event.getButton().getId() + "` you clicked " +
                            "wasn't contained within the InteractionListener. It could have expired, or otherwise departed this world. " +
                            "This button will never be valid again. Sorry!").setEphemeral(true).queue());
        } catch (Exception ex) {
            LOGGER.error("Error when handling the button!", ex);
            event.reply("Error handling the button! " + ex).setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            LOGGER.debug("We have " + INTERACTABLES.size() + " interactables.");
            INTERACTABLES.stream()
                    .filter(bi -> bi instanceof ModalInteractable)
                    .map(bi -> (ModalInteractable) bi)
                    .filter(i -> i.contains(event.getInteraction()))
                    .findFirst()
                    .ifPresentOrElse(i -> {
                        ScheduledFuture<?> future = i.getTimeoutFuture();
                        if (future != null) {
                            LOGGER.debug("Refreshing ScheduledFuture for " + event.getModalId() + " (had " + future.getDelay(TimeUnit.SECONDS) + " seconds left.)");
                            future.cancel(false);
                            i.setTimeoutFuture(SES.schedule(() -> {
                                i.destroy();
                                INTERACTABLES.remove(i);
                            }, i.getTimeoutSeconds(), TimeUnit.SECONDS));
                        }
                        i.handleEvent(new WrappedModalInteractionEvent(event, MCHELPER));
                    }, () -> event.reply("Unfortunately, the modal `" + event.getModalId() + "` you submitted " +
                            "wasn't contained within the InteractionListener. It could have expired, or otherwise departed this world. " +
                            "This modal will never be valid again. Sorry!").setEphemeral(true).queue());
        } catch (Exception ex) {
            LOGGER.error("Error when handling the modal!", ex);
            event.reply("Error handling the modal! " + ex).setEphemeral(true).queue(unused -> {}, unused -> {});
        }
    }
}
