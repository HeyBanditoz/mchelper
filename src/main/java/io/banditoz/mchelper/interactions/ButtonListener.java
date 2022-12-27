package io.banditoz.mchelper.interactions;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * The button listener class, listening for {@link ButtonInteractionEvent} to dispatch to {@link ButtonInteractable}.
 */
public class ButtonListener extends ListenerAdapter {
    private final ScheduledExecutorService SES;
    private final List<ButtonInteractable> INTERACTABLES = new CopyOnWriteArrayList<>();
    private final MCHelper MCHELPER;
    private final Logger LOGGER = LoggerFactory.getLogger(ButtonListener.class);

    public ButtonListener(MCHelper mcHelper) {
        this.SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("BL-Scheduled-%d").build());
        this.MCHELPER = mcHelper;
    }

    public void addInteractable(ButtonInteractable i) {
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
        INTERACTABLES.removeIf(buttonInteractable -> buttonInteractable.containsButton(button));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.isAcknowledged()) {
            return; // processed by another button interaction listener
        }
        try {
            LOGGER.debug("We have " + INTERACTABLES.size() + " interactables.");
            INTERACTABLES.stream().filter(i -> i.containsButton(event.getButton())).findFirst().ifPresent(i -> {
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
            });
        } catch (Exception ex) {
            LOGGER.error("Error when handling the button!", ex);
            event.reply("Error handling the button! " + ex).setEphemeral(true).queue();
        }
    }
}
