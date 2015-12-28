package com.lts.kv.index;

import com.lts.kv.Entry;
import com.lts.kv.iterator.DBIterator;
import com.lts.kv.txlog.StoreTxLogPosition;

/**
 * @author Robert HG (254963746@qq.com) on 12/18/15.
 */
public interface Index<K, V> {

    IndexItem<K> getIndexItem(K key);

    IndexItem<K> removeIndexItem(StoreTxLogPosition txLogResult, K key);

    void putIndexItem(StoreTxLogPosition txLogResult, K key, IndexItem<K> indexItem);

    public int size();

    public boolean containsKey(K key);

    DBIterator<Entry<K, V>> iterator();

    StoreTxLogPosition lastTxLog();
}
