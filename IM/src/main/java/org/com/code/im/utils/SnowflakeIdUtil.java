package org.com.code.im.utils;

import org.com.code.im.pojo.SnowflakeIdWorker;

public class SnowflakeIdUtil {
    public static SnowflakeIdWorker userIdWorker = new SnowflakeIdWorker(0, 0);
    public static SnowflakeIdWorker messageIdWorker = new SnowflakeIdWorker(1, 0);
    public static SnowflakeIdWorker sessionIdWorker = new SnowflakeIdWorker(2, 0);
    public static SnowflakeIdWorker videoIdWorker = new SnowflakeIdWorker(3, 0);
    public static SnowflakeIdWorker commentIdWorker = new SnowflakeIdWorker(4, 0);
}
