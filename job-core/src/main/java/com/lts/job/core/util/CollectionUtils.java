package com.lts.job.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isNotEmpty(Map map) {
        return map != null && map.size() > 0;
    }

    public static boolean isEmpty(Map map) {
        return !isNotEmpty(map);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return !isNotEmpty(collection);
    }

    /**
     * 返回第一个列表中比第二个多出来的元素
     *
     * @param list1
     * @param list2
     * @return
     */
    public static List getLeftDiff(List list1, List list2) {
        if (isEmpty(list2)) {
            return list1;
        }
        List list = new ArrayList();
        if (isNotEmpty(list1)) {
            for (Object o : list1) {
                if (!list2.contains(o)) {
                    list.add(o);
                }
            }
        }
        return list;
    }
}
