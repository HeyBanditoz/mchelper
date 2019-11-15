package io.banditoz.mchelper.utils;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPGDice {
    private static final Pattern DICE_PATTERN = Pattern.compile("(?<A>\\d*)d(?<B>\\d+)(?>(?<MULT>[x/])(?<C>\\d+))?(?>(?<ADD>[+-])(?<D>\\d+))?");
    private static final SecureRandom random = new SecureRandom();

    private int rolls = 0;
    private int faces = 0;
    private int multiplier = 1;
    private int additive = 0;

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

    public String roll() {
        int[] rolls = new int[this.rolls];
        int total = 0;
        for (int i = 0; i < this.rolls; i++) {
            rolls[i] = random.nextInt(faces)+1;
            rolls[i] = rolls[i] * multiplier + additive;
            total += rolls[i];
        }
        StringBuilder rollsStringBuilder = new StringBuilder();
        for (int i = 0; i < rolls.length; i++) {
            rollsStringBuilder.append(rolls[i]);
            if (i < rolls.length - 1) {
                rollsStringBuilder.append(", ");
            }
        }
        if (rolls.length != 1) {
            rollsStringBuilder.append(" -> ");
            rollsStringBuilder.append(total);
        }
        if (rollsStringBuilder.length() >= 2000) {
            // just show total
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

    @Override
    public String toString() {
        return String.format("{\"rolls\": %s, \"faces\": %s, \"multiplier\": %s, \"additive\": %s}", rolls, faces, multiplier, additive);
    }

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
