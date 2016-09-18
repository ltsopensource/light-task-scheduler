package com.github.ltsopensource.core.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class BatchUtils {

    /**
     * 批量处理切分的时候，返回第index个List
     */
    private static <E> List<E> getBatchList(Integer index, int batchSize, Collection<E> collection) {
        List<E> list = null;
        if (collection instanceof List) {
            list = (List<E>) collection;
        } else {
            list = new ArrayList<E>(collection);
        }

        if (index == list.size() / batchSize) {
            return list.subList(index * batchSize, list.size());
        } else {
            return list.subList(index * batchSize, (index + 1) * batchSize);
        }
    }

    public static <E> void batchExecute(int totalSize, int batchSize, Collection<E> collection, Executor<E> executor) {

        for (int i = 0; i <= totalSize / batchSize; i++) {
            List<E> list = BatchUtils.getBatchList(i, batchSize, collection);

            if (CollectionUtils.isNotEmpty(list)) {
                if (!executor.execute(list)) {
                    break;
                }
            }
        }
    }

    public interface Executor<E> {

        // 返回是否需要继续
        boolean execute(List<E> list);
    }
}



