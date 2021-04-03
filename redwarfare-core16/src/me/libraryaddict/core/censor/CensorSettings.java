package me.libraryaddict.core.censor;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

public class CensorSettings {
    private static HashMap<Pattern, String> _censored = new HashMap<Pattern, String>();

    private static HashMap<String, String> _correctWords = new HashMap<String, String>();

    private static Vector<String> _ignoreCaps = new Vector();

    static {
        ignoreCaps("xd", "DX", ":D", ":p", ";P", ":s", ":o", ";o", "d:", "d;", "o.o", "0.o", "o.0", "=d", "d=", "=p", "p=", "=o",
                "gg");

        correctWords("im", "I'm", "i'm", "I'm", "i", "I", "u", "you", "sry", "sorry", "ur",
                "your", "il", "I'll", "i'l", "I'll", "i'll", "I'll", "cant", "can't", "wasnt", "wasn't", "yh", "yeah", "dont",
                "don't", "every1", "everyone", "evry1", "everyone", "youre", "you're", "suk", "suck", "didnt", "didn't", "r",
                "are", "u", "you", "any1", "anyone", "guna", "gonna", "cyka",
                "kisses", "blyat", "huggies", "bleach", "my love", "kys", "be strong", "kill yourself", "be strong", "nigger", "honorable black man");

        censor();
    }

    private static void censor(String... strings) {
        for (int i = 0; i < strings.length; i += 2) {
            StringBuilder pattern = new StringBuilder("(?i)");

            char[] chars = strings[i].toCharArray();

            for (int pos = 0; pos < chars.length; pos++) {
                pattern.append(Pattern.quote("" + chars[pos])).append(pos + 1 == chars.length ? "+" : "+[^A-Za-z0-9]*");
            }

            _censored.put(Pattern.compile(pattern.toString()), strings[i + 1]);
        }
    }

    private static void correctWords(String... strings) {
        for (int i = 0; i < strings.length; i += 2)
            _correctWords.put(strings[i], strings[i + 1]);
    }

    public static HashMap<Pattern, String> getCensored() {
        return _censored;
    }

    public static HashMap<String, String> getCorrectWords() {
        return _correctWords;
    }

    public static Vector<String> getIgnoreCaps() {
        return _ignoreCaps;
    }

    private static void ignoreCaps(String... strings) {
        for (String string : strings)
            _ignoreCaps.add(string);
    }
}
