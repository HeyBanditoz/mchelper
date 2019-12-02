package io.banditoz.mchelper.utils;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class MorseUtils {
    private static DualHashBidiMap<Character, String> morseMap = new DualHashBidiMap<>();

    static {
        morseMap.put('A', ".-");
        morseMap.put('B', "-...");
        morseMap.put('C',  "-.-.");
        morseMap.put('D',  "-..");
        morseMap.put('E',    ".");
        morseMap.put('F', "..-.");
        morseMap.put('G',  "--.");
        morseMap.put('H', "....");
        morseMap.put('I',   "..");
        morseMap.put('J', ".---");
        morseMap.put('K',   "-.-");
        morseMap.put('L', ".-..");
        morseMap.put('M',   "--");
        morseMap.put('N',   "-.");
        morseMap.put('O',  "---");
        morseMap.put('P', ".--.");
        morseMap.put('Q', "--.-");
        morseMap.put('R', ".-.");
        morseMap.put('S',  "...");
        morseMap.put('T',   "-");
        morseMap.put('U',  "..-");
        morseMap.put('V', "...-");
        morseMap.put('W',  ".--");
        morseMap.put('X', "-..-");
        morseMap.put('Y', "-.--");
        morseMap.put('Z', "--..");
        morseMap.put('1', ".----");
        morseMap.put('2',"..---");
        morseMap.put('3', "...--");
        morseMap.put('4', "....-");
        morseMap.put('5', ".....");
        morseMap.put('6', "-....");
        morseMap.put('7', "--...");
        morseMap.put('8', "---..");
        morseMap.put('9', "----.");
        morseMap.put('0', "-----");
        morseMap.put(',', "--..--");
        morseMap.put('.', ".-.-.-");
        morseMap.put('?', "..--..");
        morseMap.put('\'', ".----.");
        //morseMap.put('\"', ".-..-.");
        morseMap.put('@', ".--.-.");
        morseMap.put(' ', "/");
    }

    public static String toMorse(String message) {
        String toMorse = message.toUpperCase().replaceAll("[^A-Z0-9?.,\"@ ]", "");
        StringBuilder morse = new StringBuilder();

        for (Character c : toMorse.toCharArray()) {
            morse.append(morseMap.get(c)).append(" ");
        }

        return morse.toString();
    }

    public static String fromMorse(String morse) {
        String[] letters = morse.split("\\s");
        StringBuilder text = new StringBuilder();

        for (String letter : letters) {
            if (morseMap.inverseBidiMap().get(letter) == null) {
                text.append("■");
            } else {
                text.append(morseMap.inverseBidiMap().get(letter));
            }
        }
        return text.toString();
    }
}
