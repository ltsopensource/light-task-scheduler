package com.github.ltsopensource.core.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性hash算法
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public class ConsistentHashSelector<S> { // S类封装了机器节点的信息

    private TreeMap<Long, S> nodes; // 虚拟节点
    private final int NODE_NUM = 100; // 每个机器节点关联的虚拟节点个数

    public ConsistentHashSelector(List<S> shards) {
        super();
        nodes = new TreeMap<Long, S>();
        for (int i = 0; i != shards.size(); ++i) { // 每个真实机器节点都需要关联虚拟节点
            final S shardInfo = shards.get(i);
            for (int n = 0; n < NODE_NUM; n++)
                // 一个真实机器节点关联NODE_NUM个虚拟节点
                nodes.put(hash("SHARD-" + i + "-NODE-" + n), shardInfo);
        }
    }

    public S selectForKey(String key) {
        SortedMap<Long, S> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
        if (tail.size() == 0) {
            return nodes.get(nodes.firstKey());
        }
        return tail.get(tail.firstKey()); // 返回该虚拟节点对应的真实机器节点的信息
    }

    /**
     * MurMurHash算法，是非加密HASH算法，性能很高，
     * 比传统的CRC32,MD5，SHA-1（这两个算法都是加密HASH算法，复杂度本身就很高，带来的性能上的损害也不可避免）
     * 等HASH算法要快很多，而且据说这个算法的碰撞率很低.
     * http://murmurhash.googlepages.com/
     */
    private Long hash(String key) {

        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(
                    ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

}
