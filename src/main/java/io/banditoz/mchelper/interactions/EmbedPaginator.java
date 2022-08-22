package io.banditoz.mchelper.interactions;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EmbedPaginator {
    private final List<MessageEmbed> pages;
    private ButtonInteractable bi; // can't be final because we're accessing from a lambda
    private final ButtonListener bl;
    private final TimeUnit unit;
    private final long time;
    private final MessageChannel channel;
    private final Predicate<User> canInteract;
    private final Button prev = Button.primary(UUID.randomUUID().toString(), PREV);
    private final Button stop = Button.danger(UUID.randomUUID().toString(), STOP);
    private final Button next = Button.primary(UUID.randomUUID().toString(), NEXT);

    private int cursor = 0;

    private static final Emoji PREV = Emoji.fromUnicode("\u25C0");
    private static final Emoji STOP = Emoji.fromUnicode("\uD83D\uDEAE");
    private static final Emoji NEXT = Emoji.fromUnicode("\u25B6");

    /**
     * @param bl          The {@link ButtonListener} to attach to.
     * @param channel     The {@link MessageChannel} to reply to.
     * @param pages       A list of {@link MessageEmbed} to use. The zeroth element will be used in the initial message.
     * @param time        How long to wait.
     * @param unit        The {@link TimeUnit} for how long we should wait.
     * @param canInteract Whether the user can interact with this {@link EmbedPaginator} or not
     */
    public EmbedPaginator(@NotNull ButtonListener bl, @NotNull MessageChannel channel, @NotNull List<MessageEmbed> pages,
                          long time, @NotNull TimeUnit unit, @NotNull Predicate<User> canInteract) {
        this.pages = pages;
        this.bl = bl;
        this.unit = unit;
        this.time = time;
        this.channel = channel;
        this.canInteract = canInteract;
    }

    public void go() {
        Map<Button, Consumer<WrappedButtonClickEvent>> map = Map.of(
                prev, this::handlePrev,
                stop, this::handleStop,
                next, this::handleNext
        );
        MessageCreateData messageCreate = new MessageCreateBuilder()
                .setEmbeds(pages.get(0))
                .addActionRow(prev, stop, next)
                .build();
        channel.sendMessage(messageCreate).queue(message -> {
            this.bi = new ButtonInteractable(map, canInteract, unit.toSeconds(time), message);
            bl.addInteractable(bi);
        });
    }

    private void handlePrev(WrappedButtonClickEvent event) {
        if (cursor == 0) {
            // can call without actually editing anything, just so it doesn't fail on the Discord client
            event.getEvent().deferEdit().queue();
            return;
        }
        event.getEvent().editMessageEmbeds(pages.get(--cursor)).queue();
    }

    private void handleNext(WrappedButtonClickEvent event) {
        if (cursor == pages.size() - 1) {
            // can call without actually editing anything, just so it doesn't fail on the Discord client
            event.getEvent().deferEdit().queue();
            return;
        }
        event.getEvent().editMessageEmbeds(pages.get(++cursor)).queue();
    }

    private void handleStop(WrappedButtonClickEvent event) {
        event.removeListenerAndDestroy();
    }
}
