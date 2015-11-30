package com.lach.common.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    @SuppressWarnings("SameParameterValue")
    public static String[] findMatches(String regex, String source, boolean multiLined) {
        ArrayList<String> regexValues = new ArrayList<>();

        Pattern pattern;
        if (multiLined) {
            pattern = Pattern.compile(regex, Pattern.DOTALL);
        } else {
            pattern = Pattern.compile(regex);
        }

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
