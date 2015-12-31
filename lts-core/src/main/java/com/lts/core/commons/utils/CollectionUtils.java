package com.lts.core.commons.utils;

import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && map.size() > 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return !isNotEmpty(map);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return !isNotEmpty(collection);
    }

    public static int sizeOf(Collection<?> collection) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.size();
    }

    public static int sizeOf(Map<?, ?> map) {
        if (map == null) {
            return 0;
        }
        return map.size();
    }

    /**
     * 返回第一个列表中比第二个多出来的元素
     */
    public static <T> List<T> getLeftDiff(List<T> list1, List<T> list2) {
        if (isEmpty(list2)) {
            return list1;
        }
        List<T> list = new ArrayList<T>();
        if (isNotEmpty(list1)) {
            for (T o : list1) {
                if (!list2.contains(o)) {
                    list.add(o);
                }
            }
        }
        return list;
    }

    public static <T> List<T> setToList(Set<T> set) {
        if (set == null) {
            return null;
        }
        return new ArrayList<T>(set);
    }
}
