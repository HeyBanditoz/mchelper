package io.banditoz.mchelper.interactions;

import java.util.List;
import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.avaje.inject.Priority;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.MeterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interaction listener class, listening for either:
 * <ul>
 *     <li>{@link ButtonInteractionEvent} to dispatch to {@link ButtonInteractable}.</li>
 *     <li>{@link ModalInteractionEvent} to dispatch to {@link ModalInteractable}.</li>
 * </ul>
 */
@Singleton
@Priority(1)
public class InteractionListener extends ListenerAdapter implements AutoCloseable {
    private final ScheduledExecutorService SES;
    private final List<Interactable<?, ?>> INTERACTABLES = new CopyOnWriteArrayList<>();
    private final LongHistogram buttonProcessingDelay;
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionListener.class);

    @Inject
    public InteractionListener(MeterProvider meter) {
        this.SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("BL-Scheduled-%d").build());
        this.buttonProcessingDelay = meter
                .meterBuilder("interaction_metrics")
                .build()
                .histogramBuilder("mchelper_interaction_processing_delay")
                .setDescription("Histogram tracking the processing delay of an interaction.")
                .ofLongs()
                .setExplicitBucketBoundariesAdvice(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 15L, 20L, 25L, 50L, 75L, 100L, 250L, 500L, 750L, 1000L, 2500L))
                .build();
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
                        measure(() -> i.handleEvent(new WrappedButtonClickEvent(event, i, this)), "button", i.getCommandEvent(), event.getButton());
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
                        measure(() -> i.handleEvent(new WrappedModalInteractionEvent(event, this)), "modal", null, null);
                        i.handleEvent(new WrappedModalInteractionEvent(event, this));
                    }, () -> event.reply("Unfortunately, the modal `" + event.getModalId() + "` you submitted " +
                            "wasn't contained within the InteractionListener. It could have expired, or otherwise departed this world. " +
                            "This modal will never be valid again. Sorry!").setEphemeral(true).queue());
        } catch (Exception ex) {
            LOGGER.error("Error when handling the modal!", ex);
            event.reply("Error handling the modal! " + ex).setEphemeral(true).queue(unused -> {}, unused -> {});
        }
    }

    private void measure(Runnable runnable, String interactionType, CommandEvent commandEvent, Button button) {
        long before = System.currentTimeMillis();
        boolean exceptionally = false;
        try {
            runnable.run();
        } catch (Exception e) {
            // rare case, button interactions shouldn't throw uncaught exceptions
            exceptionally = true;
            throw e; // rethrow
        } finally {
            long after = System.currentTimeMillis();
            AttributesBuilder attrs = Attributes.builder()
                    .put("interaction_type", interactionType)
                    .put("exceptionally", Boolean.toString(exceptionally));

            if ("button".equals(interactionType) && commandEvent != null && commandEvent.commandName() != null) {
                attrs.put("command", commandEvent.commandName());
            }
            else {
                LOGGER.warn("CommandEvent or the command name was null for a button interaction. Printing stacktrace...", new Exception().fillInStackTrace());
            }

            if (button != null) {
                if (button.getLabel().isEmpty()) {
                    // button is an emoji
                    attrs.put("button", button.getEmoji().getFormatted());
                }
                else {
                    // button has text
                    attrs.put("button", button.getLabel());
                }
            }
            buttonProcessingDelay.record(after - before, attrs.build());
        }
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("Shutting down InteractionListener...");
        long then = System.currentTimeMillis();
        int activeInteractables;
        while ((activeInteractables = getActiveInteractables()) != 0) {
            LOGGER.info("Waited {} ms for {} interactables to finish...", (System.currentTimeMillis() - then), activeInteractables);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
