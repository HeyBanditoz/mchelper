package io.banditoz.mchelper.money;

import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * MCHelper has worked hard for little pay, despite several cries to the contrary.<br>
 * This class supports ways to reclaim MCHelper's hard work.<br>
 * <b>Everything in this class is a hack. Everything everywhere is a hack.</b>
 */
@Singleton
public class TipGenerator {
    /**We need to collect our tip money somehow! */
    private final TipModifierCache tipModifierCache;
    /** This is <b>not</b> user-hostile. */
    private static final ButtonStyle[] POTENTIAL_STYLES = {ButtonStyle.DANGER, ButtonStyle.PRIMARY, ButtonStyle.SUCCESS, ButtonStyle.SECONDARY};
    /** Self-explanatory, I think! 🤪 */
    private static final Random RANDOM = new Random();

    @Inject
    public TipGenerator(TipModifierCache tipModifierCache) {
        this.tipModifierCache = tipModifierCache;
    }

    /** NOT SHUFFLED HERE!!! */
    public Map<Button, Consumer<WrappedButtonClickEvent>> getTipButtons(long initiator) {
        Map<Button, Consumer<WrappedButtonClickEvent>> map = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            int rand = RANDOM.nextInt(10);
            switch (rand) {
                case 1, 2 -> {
                    ButtonConsumerPair p = twentyPercent(initiator);
                    map.put(p.button, p.consumer);
                }
                case 3, 4, 5 -> {
                    ButtonConsumerPair p = oneHundredPercent(initiator);
                    map.put(p.button, p.consumer);
                }
                case 6, 7 -> { // lol, get it?
                    ButtonConsumerPair p = tenPercent(initiator);
                    map.put(p.button, p.consumer);
                }
                default -> {
                    ButtonConsumerPair p = fiftyPercent(initiator);
                    map.put(p.button, p.consumer);
                }
            }
        }
        // one guaranteed zero percent. ONLY ONE!!!
        // this is not a dark pattern
        ButtonConsumerPair p = zeroPercent(initiator);
        map.put(p.button, p.consumer);
        return map;
    }

    private ButtonConsumerPair twentyPercent(long initiator) {
        Button button = Button.of(nextStyle(), id(), pick("20%", "Twenty Percent", "Restaurant Standard Tip",
                "Service was just right", "二十パーセント"));
        return ButtonConsumerPair.of(button, e -> {
            tipModifierCache.put(initiator, new BigDecimal("0.2"));
        });
    }

    private ButtonConsumerPair oneHundredPercent(long initiator) {
        Button button = Button.of(nextStyle(), id(), pick("100%", "Max Generosity",
                "Thank you so much!", "Benevolent Benefactor", "Support Service Workers", "One Hundred Percent",
                "百パーセント"));
        return ButtonConsumerPair.of(button, e -> {
            tipModifierCache.put(initiator, BigDecimal.ONE);
        });
    }

    private ButtonConsumerPair tenPercent(long initiator) {
        Button button = Button.of(nextStyle(), id(), pick("10%", "Not Too Bad", "Could Be Better", "Ten Percent",
                "1000+50-500-450-10-69-11", "Better than zero, I guess", "Half of 20%", "十パーセント"));
        return ButtonConsumerPair.of(button, e -> {
            tipModifierCache.put(initiator, new BigDecimal("0.1"));
        });
    }

    private ButtonConsumerPair zeroPercent(long initiator) {
        Button button = Button.of(nextStyle(), id(), pick("Horrible Person", "Zero Percent",
                "Seriously, dude? *blocks exit*", "Protesting Tipping Culture", "Non-US Standard",
                "A fair response to horrible service", "Clanker Standard Tip", "❌", "ゼロパーセント",
                "Food came out frozen, wouldn't fix", "CLICK ME IF YOU DON'T TIP"));
        return ButtonConsumerPair.of(button, e -> {
            tipModifierCache.put(initiator, BigDecimal.ZERO);
        });
    }

    private ButtonConsumerPair fiftyPercent(long initiator) {
        Button button = Button.of(nextStyle(), id(), pick("50%", "Fifty Percent", "Decent Generosity",
                "Sorry for destroying the bathroom, here is extra tip!", "Are you an opimist or a pessimist?", "1/2",
                "A pretty damn good half!", "五十パーセント"));
        return ButtonConsumerPair.of(button, e -> {
            tipModifierCache.put(initiator, new BigDecimal("0.5"));
        });
    }

    private ButtonStyle nextStyle() {
        return POTENTIAL_STYLES[RANDOM.nextInt(POTENTIAL_STYLES.length)];
    }

    private String id() {
        return UUID.randomUUID().toString();
    }

    private String pick(String... args) {
        return args[RANDOM.nextInt(args.length)];
    }

    /** You wouldn't get it. */
    private record ButtonConsumerPair(Button button, Consumer<WrappedButtonClickEvent> consumer) {
        private static ButtonConsumerPair of(Button button, Consumer<WrappedButtonClickEvent> consumer) {
            return new ButtonConsumerPair(button, consumer);
        }
    }
}
