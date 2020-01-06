package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a set of dice meant for role-playing games. Offers a constructor and a static one that can parse
 * basic dice notation. See {@link #parse(String)}
 *
 * @author Ulises (Stack Overflow)
 */
public class RPGDice {
    private static final Pattern DICE_PATTERN = Pattern.compile("(?<A>\\d*)d(?<B>\\d+)(?>(?<MULT>[x/])(?<C>\\d+))?(?>(?<ADD>[+-])(?<D>\\d+))?");
    private static final SecureRandom random = new SecureRandom();

    private int rolls;
    private int faces;
    private int multiplier;
    private int additive;

    public RPGDice(int rolls, int faces, int multiplier, int additive) {
        this.rolls = rolls;
        this.faces = faces;
        this.multiplier = multiplier;
        this.additive = additive;

        // bounds
        if (rolls >= 1000) {
            throw new IllegalArgumentException("Number of rolls must be less than 1000!");
        }
        if (faces >= 1000) {
            throw new IllegalArgumentException("Number of faces must be less than 1000!");
        }
    }

    /**
     * Rolls this RPGDice, using SecureRandom to generate numbers.
     *
     * @return A String containing the results of the dice roll. If the String exceeds 2000 characters, it will only
     * show the final total, adhering to Discord's character limit.
     */
    public String roll() {
        int[] rolls = new int[this.rolls];
        int total = 0;
        // roll the dice, then store the results in rolls[]
        for (int i = 0; i < this.rolls; i++) {
            rolls[i] = random.nextInt(faces)+1;
            rolls[i] = rolls[i] * multiplier + additive;
            total += rolls[i];
        }
        // get each roll, then store the individual result in a StringBuilder
        StringBuilder rollsStringBuilder = new StringBuilder();
        for (int i = 0; i < rolls.length; i++) {
            rollsStringBuilder.append(rolls[i]);
            if (i < rolls.length - 1) {
                rollsStringBuilder.append(", ");
            }
        }
        // put total at the end
        if (rolls.length != 1) {
            rollsStringBuilder.append(" -> ");
            rollsStringBuilder.append(total);
        }
        // just show total if too big
        if (rollsStringBuilder.length() >= 2000) {
            rollsStringBuilder = new StringBuilder()
                    .append(total);
        }
        // TODO Do this not at the end of the string
        if (multiplier != 1) {
            rollsStringBuilder.append(" Multiplier: ");
            rollsStringBuilder.append(multiplier);
        }
        if (additive != 0) {
            rollsStringBuilder.append(" Additive: ");
            rollsStringBuilder.append(additive);
        }
        return rollsStringBuilder.toString();
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static Integer getInt(Matcher matcher, String group, int defaultValue) {
        String groupValue = matcher.group(group);
        return isEmpty(groupValue) ? defaultValue : Integer.parseInt(groupValue);
    }

    private static Integer getSign(Matcher matcher, String group, String positiveValue) {
        String groupValue = matcher.group(group);
        return isEmpty(groupValue) || groupValue.equals(positiveValue) ? 1 : -1;
    }

    /**
     * Static method that returns an initialized RPGDice class
     *
     * For example: "2d20" will roll two dice with 20 faces, "1d6" will roll one die with six faces.
     * At the end of the string, "+6" will add six to the final value, while "*6" will multiply the final value by six.
     * @param str The dice notation to parse
     * @return A built RPGDice class.
     * @throws IllegalArgumentException If the dice notation is bad.
     */
    public static RPGDice parse(String str) {
        Matcher matcher = DICE_PATTERN.matcher(str);
        if (matcher.matches()) {
            int rolls = getInt(matcher, "A", 1);
            int faces = getInt(matcher, "B", -1);
            int multiplier = getInt(matcher, "C", 1);
            int additive = getInt(matcher, "D", 0);
            int multiplierSign = getSign(matcher, "MULT", "x");
            int additiveSign = getSign(matcher, "ADD", "+");
            return new RPGDice(rolls, faces, multiplier * multiplierSign, additive * additiveSign);
        }
        throw new IllegalArgumentException("Invalid Expression");
    }
}
