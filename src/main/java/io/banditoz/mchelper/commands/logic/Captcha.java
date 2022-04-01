package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.Random;
import java.util.UUID;

/**
 * Represents a captcha to solve. STOP ALL BOTS!!!!!!!
 * Side note: I probably should have used the ButtonInteractable API I wrote... but oh well LOL
 */
public class Captcha {
    private final Random random = new Random();
    private static final Button DUMMY_BUTTON = Button.primary("1", "1");
    private static final ButtonStyle[] STYLES = new ButtonStyle[]{ButtonStyle.PRIMARY, ButtonStyle.DANGER, ButtonStyle.SECONDARY, ButtonStyle.SUCCESS};

    /** The {@link UUID} of the {@link Button} that will pass the captcha. */
    private UUID correctUUID;
    /** The {@link User} that was identified as a BOT!!!!!! GET HIM!!!!!!! */
    private final User challenger;
    private final Command c;
    private final MessageReceivedEvent event;
    private Message message;

    public Captcha(User challenger, Command c, MessageReceivedEvent event) {
        this.challenger = challenger;
        this.c = c;
        this.event = event;
        sendMessage(false);
    }

    /**
     * An extremely complex proprietary algorithm that stops bots from using the bot!!! 100% foolproof!!!!!!!!
     * Definitely not hackingly written either! Perfect code!
     *
     * @return An array of {@link ActionRow}s that contain the captcha.
     */
    public ActionRow[] generateButtons() {
        int correctButton = random.nextInt(25);
        int k = 0;
        ActionRow[] rows = new ActionRow[5];
        for (int i = 0; i < rows.length; i++) {
            // hack; this is the shittiest hack I have ever written, only out of laziness, there is a MUCH BETTER WAY to
            // do this, but APRIL FOOLS LOL!
            rows[i] = ActionRow.of(DUMMY_BUTTON, DUMMY_BUTTON, DUMMY_BUTTON, DUMMY_BUTTON, DUMMY_BUTTON);
            for (int j = 0; j < 5; j++) {
                ButtonStyle styleToUse = STYLES[random.nextInt(STYLES.length)];
                if (k == correctButton) {
                    correctUUID = UUID.randomUUID();
                    rows[i].getComponents().set(j, Button.of(styleToUse, correctUUID.toString(), "i"));
                }
                else {
                    rows[i].getComponents().set(j, Button.of(styleToUse, UUID.randomUUID().toString(), random.nextBoolean() ? "l" : "L"));
                }
                k++;
            }
        }
        return rows;
    }

    public void sendMessage(boolean includeBotSupporter) {
        String content;
        if (!includeBotSupporter) {
            content = """
                Due to the rise of BOTS using the bot, antibot measures were implemented!
                Please pass the captcha to execute your command.
                """;
        }
        else {
            content = """
                YOU CAN'T CHEAT YOUR WAY OUT OF THIS ONE BOT!
                ***PASS THE CAPTCHA!***
                """;
        }
        message = event.getMessage().reply(new MessageBuilder()
                        .setActionRows(generateButtons())
                        .setContent(content)
                        .build())
                .complete();
    }

    public UUID getCorrectUUID() {
        return correctUUID;
    }

    public User getChallenger() {
        return challenger;
    }

    public Command getCommand() {
        return c;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
