package org.com.code.im.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BloomFilters {
    private static BloomFilter<String> bloomFilter;
    private static CopyOnWriteArrayList<BloomFilter<String>> bloomFilterList;
    /**
     * 读写锁多个线程可以同时进行读操作，只有写操作会阻塞其他线程。
     */
    private static ReentrantReadWriteLock lockForUpdateAndIterator = new ReentrantReadWriteLock ();
    private static final int BLOOM_FILTER_LIST_SIZE = 5;
    private static final int EXPECTED_INSERTIONS = 10000;
    private static final double FPP = 0.01;

    public static BloomFilter<String> getBloomFilter() {
        /**
         * 之所以在synchronized里面还要再判断一次是否为null，是因为如果有两个线程
         * 线程1和线程2同时发现bloomFilter为null，那么两个线程都会进入if语句，
         * 然后线程1拿到锁.线程21在外面等待.线程1执行完synchronized代码块后,
         * 此时bloomFilter已经有一个实例的引用了,如果这时候线程2拿到锁,那么线程2又会再创建一个实例
         * 因此我们在synchronized里面还要再判断一次是否为null
         */
        if (bloomFilter == null) {
            synchronized (BloomFilters.class) {
                if (bloomFilter == null) {
                    bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), EXPECTED_INSERTIONS, FPP);
                }
            }
        }
        return bloomFilter;
    }

    /**
     * 静态初始化,确保只初始化一次。
     */
    static {
        bloomFilterList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < BLOOM_FILTER_LIST_SIZE; i++) {
            bloomFilterList.add(getBloomFilter());
        }
    }


    /**
     * 分片布隆过滤器将一个大的布隆过滤器分成多个小的布隆过滤器，
     * 每个小布隆过滤器负责一部分数据。通过轮换这些分片，可以实现类似滑动窗口的效果
     * 每隔一个小时，把队列开头最旧的布隆过滤器的分片移除掉，并添加一个新的分片到队列末尾
     */
    @Scheduled(fixedRate = 1,timeUnit = TimeUnit.HOURS)
    public static void updateBloomFilterList() {
        synchronized (lockForUpdateAndIterator) {
            bloomFilterList.remove(0);
            bloomFilterList.add(getBloomFilter());
        }
    }

    public static boolean checkIfDuplicatedMessage(String message) {
        synchronized (lockForUpdateAndIterator) {
            for(int i = 0; i < BLOOM_FILTER_LIST_SIZE; i++){
                if (bloomFilterList.get(i).mightContain(message)) {
                    return true;
                }
            }
            bloomFilterList.get(BLOOM_FILTER_LIST_SIZE - 1).put(message);
            return false;
        }
    }
}
