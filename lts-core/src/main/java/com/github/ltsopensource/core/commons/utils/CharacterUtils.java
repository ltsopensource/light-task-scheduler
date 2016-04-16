package com.github.ltsopensource.core.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class CharacterUtils {

    /**
     * 下划线格式字符串转换成驼峰格式字符串
     * eg: player_id -> playerId;<br>
     * player_name -> playerName;
     */
    public static String underScore2CamelCase(String strs) {

        if (strs == null || !strs.contains("_")) {
            return strs;
        }
        StringBuilder sb = new StringBuilder("");
        String[] elems = strs.split("_");
        for (int i = 0; i < elems.length; i++) {
            String elem = elems[i].toLowerCase();
            if (i != 0) {
                char first = elem.toCharArray()[0];
                sb.append((char) (first - 32)).append(elem.substring(1));
            } else {
                sb.append(elem);
            }
        }
        return sb.toString();
    }

    /**
     * @param param 待转换字符串
     *              驼峰格式字符串 转换成 下划线格式字符串
     *              eg: playerId -> player_id;<br>
     *              playerName -> player_name;
     */
    public static String camelCase2Underscore(String param) {
        Pattern p = Pattern.compile("[A-Z]");
        if (param == null || param.equals("")) {
            return "";
        }
        StringBuilder builder = new StringBuilder(param);
        Matcher mc = p.matcher(param);
        int i = 0;
        while (mc.find()) {
            builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase());
            i++;
        }
        if ('_' == builder.charAt(0)) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }
}
