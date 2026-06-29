package com.example.pbl;

import java.util.HashMap;
import java.util.Map;

public class NumberUtils {
    private static final Map<String, String> NUMBER_WORDS = new HashMap<>();

    static {
        NUMBER_WORDS.put("0", "zero");
        NUMBER_WORDS.put("1", "one");
        NUMBER_WORDS.put("2", "two");
        NUMBER_WORDS.put("3", "three");
        NUMBER_WORDS.put("4", "four");
        NUMBER_WORDS.put("5", "five");
        NUMBER_WORDS.put("6", "six");
        NUMBER_WORDS.put("7", "seven");
        NUMBER_WORDS.put("8", "eight");
        NUMBER_WORDS.put("9", "nine");
        NUMBER_WORDS.put("10", "ten");
    }

    public static String normalizeNumbers(String text) {
        if (text == null) return null;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (NUMBER_WORDS.containsKey(word)) {
                sb.append(NUMBER_WORDS.get(word));
            } else {
                sb.append(word);
            }
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
