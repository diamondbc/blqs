package com.iwdnb.blqs.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSpecialUtils {

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern pattern = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = pattern.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
