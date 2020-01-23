package com.iwdnb.blqs.core.utils;

import org.apache.commons.lang3.StringUtils;

import com.iwdnb.blqs.core.Options;

public class UrlIgnoreUtils {

    public static boolean validClassUrl(String path) {
        String urls = Options.urls;
        if (StringUtils.isBlank(urls)) {
            return true;
        }
        String[] urlArray = urls.split(",");
        for (String url : urlArray) {
            if (path.contains(url)) {
                return true;
            }
        }
        return false;
    }
}
