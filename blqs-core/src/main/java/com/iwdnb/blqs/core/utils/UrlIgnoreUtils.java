package com.iwdnb.blqs.core.utils;

import org.apache.commons.lang3.StringUtils;

import com.iwdnb.blqs.core.Options;

import java.util.Arrays;

public class UrlIgnoreUtils {

    public static boolean validClassUrl(String path) {
        String urls = Options.urls;
        if (StringUtils.isBlank(urls)) {
            return true;
        }
        String[] urlArray = urls.split(",");
        if (WildcardUtils.match(path, Arrays.asList(urlArray))) {
            return true;
        }
        return false;
    }
}
