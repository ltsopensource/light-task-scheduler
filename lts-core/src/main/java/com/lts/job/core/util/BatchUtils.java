package com.lts.job.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class BatchUtils {

    /**
     * 批量处理切分的时候，返回第index个List
     *
     * @param index
     * @param batchSize
     * @param collection
     * @return
     */
    public static <E> List<E> getBatchList(Integer index, int batchSize, Collection<E> collection) {
        List<E> list = null;
        if (collection instanceof List) {
            list = (List) collection;
        } else {
            list = new ArrayList<E>(collection);
        }

        if (index == list.size() / batchSize) {
            return list.subList(index * batchSize, list.size());
        } else {
            return list.subList(index * batchSize, (index + 1) * batchSize);
        }
    }

}



