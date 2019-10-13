package fr.onecraft.halloween.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringUtils {

    /**
     * Join arguments of a list (for sql query)
     *
     * @param separator String
     * @param strings   List
     * @return String
     */
    public static String join(String separator, List<String> strings) {
        return removeLastChars(
                strings.stream()
                        .map(string -> string + separator)
                        .collect(Collectors.joining()), separator.length()
        );
    }

    /**
     * Join two lists of strings with separators and connectors (for sql query)
     *
     * @param argsSeparator String
     * @param argsLink      String
     * @param args          Map
     * @return String
     */
    public static String join(String argsSeparator, String argsLink, Map<String, String> args) {
        StringBuilder result = new StringBuilder();
        for (String arg : args.keySet()) {
            result.append(arg)
                    .append(argsLink)
                    .append(args.get(arg))
                    .append(argsSeparator);
        }

        // remove last separator
        result = new StringBuilder(removeLastChars(result.toString(), argsSeparator.length()));
        return result.toString();
    }

    /**
     * Remove x last chars of a string
     *
     * @param str    String
     * @param amount int
     * @return String
     */
    public static String removeLastChars(String str, int amount) {
        if (str.length() <= amount) return "";
        return str.substring(0, str.length() - amount);
    }
}
