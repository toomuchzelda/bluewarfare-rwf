package me.libraryaddict.core.utils;

import me.libraryaddict.mysql.operations.MysqlSaveLog;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class UtilString {
    public static int count(String string, String lookingFor) {
        int number = 0;
        int index = -lookingFor.length();

        while ((index = string.indexOf(lookingFor, index + lookingFor.length())) != -1) {
            number++;
        }

        return number;
    }

    public static int countCapitalization(String string) {
        int amount = 0;

        for (char c : string.toCharArray()) {
            if (Character.isUpperCase(c))
                amount++;
        }

        return amount;
    }

    public static String join(Collection<String> strings, String seperator) {
        return String.join(seperator, strings);
    }

    public static String join(int ignoreArgs, String[] args, String seperator) {
        args = Arrays.copyOfRange(args, Math.min(ignoreArgs, args.length), args.length);

        return String.join(seperator, args);
    }

    public static String join(String[] args, String seperator) {
        return String.join(seperator, args);
    }

    public static void log(String string) {
        new Thread() {
            public void run() {
                new MysqlSaveLog(UtilError.getServer(), string);
            }
        }.start();
    }

    public static String repeat(char character, int times) {
        return repeat(String.valueOf(character), times);
    }

    public static String repeat(String string, int times) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < times; i++)
            s.append(string);

        return s.toString();
    }
}
