package com.lach.common.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static String[] findMatches(String regex, String source, boolean multiLined) {
        Pattern pattern;
        if (multiLined) {
            pattern = Pattern.compile(regex, Pattern.DOTALL);
        } else {
            pattern = Pattern.compile(regex);
        }

        return findMatches(pattern, source);
    }

    public static String[] findMatches(Pattern pattern, String source) {
        ArrayList<String> regexValues = new ArrayList<>();
        Matcher matcher = pattern.matcher(source);

        while (matcher.find()) {
            try {
                regexValues.add(matcher.group(1));
            } catch (Exception e) {
                regexValues.add(matcher.group());
            }
        }

        return regexValues.toArray(new String[regexValues.size()]);
    }
}
