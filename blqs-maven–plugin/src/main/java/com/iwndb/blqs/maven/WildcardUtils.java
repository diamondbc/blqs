package com.iwndb.blqs.maven;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: diamondbc
 */
public class WildcardUtils {

    /**
     * 将通配符表达式转化为正则表达式
     * 
     * @param path
     * @return
     */
    private static String getRegPath(String path) {
        char[] chars = path.toCharArray();
        int len = chars.length;
        StringBuilder sb = new StringBuilder();
        boolean preX = false;
        for (int i = 0; i < len; i++) {
            // 遇到*字符
            if (chars[i] == '*') {
                if (preX) {
                    // 如果是第二次遇到*，则将**替换成.*
                    sb.append(".*");
                    preX = false;
                } else if (i + 1 == len) {
                    // 如果是遇到单星，且单星是最后一个字符，则直接将*转成[^/]*
                    sb.append("[^:]*");
                } else {// 否则单星后面还有字符，则不做任何动作，下一把再做动作
                    preX = true;
                    continue;
                }
            } else {// 遇到非*字符
                // 如果上一把是*，则先把上一把的*对应的[^/]*添进来
                if (preX) {
                    sb.append("[^:]*");
                    preX = false;
                }
                // 接着判断当前字符是不是?，是的话替换成.
                if (chars[i] == '?') {
                    sb.append('.');
                } else {// 不是?的话，则就是普通字符，直接添进来
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 通配符模式
     * 
     * @param path - 地址
     * @param reqPaths - 匹配地址
     * @return
     */
    public static boolean match(String path, List<String> reqPaths) {
        for (String reqPath : reqPaths) {
            String regPath = getRegPath(reqPath);
            if (Pattern.compile(regPath).matcher(path).matches()) {
                System.out.println(regPath);
                return true;
            }
        }
        return false;

    }

    public static void main(String[] args) {
        List<String> whiteList = Arrays.asList("com.wdnb.*:test**", "/abc/df/dfd", "/abc:**/dfd", "/abc:*", "abd:**",
                                               "/g??gle", "/*.do", "/ttt");
        boolean r = match("/abc:asd", whiteList);
        System.out.println(r);
        r = match("com.wdnb.wdboom:testAbc", whiteList);
        System.out.println(r);
    }
}
