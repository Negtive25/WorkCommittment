package org.com.code.im.service.Impl;

import org.com.code.im.exception.DatabaseException;
import org.com.code.im.mapper.VideoLikeMapper;
import org.com.code.im.mapper.VideoMapper;
import org.com.code.im.pojo.Videos;
import org.com.code.im.service.VideoLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VideoLikeImpl implements VideoLikeService {
    @Autowired
    private VideoLikeMapper videoLikeMapper;
    @Autowired
    private VideoMapper videoMapper;

    @Qualifier("objRedisTemplate")
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    private static int pageSize = 10;
    /**
     * 我点赞和取消点赞的逻辑的前提,
     * redis作为缓存层,不存储所有视频的所有点赞记录,所有点赞记录由mysql存储
     * 用户进行的点赞和取消点赞的操作暂时存储在redis中,每隔一段时间把记录统一批量同步到mysql中,然后删除redis中的暂存的记录
     * 这就有一个问题,如果一个人很久之前就给一个视频点赞了,后来他想要取消点赞,但是redis只是暂时缓存他最近的点赞或取消点赞的操作,
     * 而不是很久以前的点赞记录,所以只根据redis的记录不知道用户是否已经给该视频点赞过,
     * 所以,我此处的逻辑如下:
     *
     */

    @Override
    @Transactional
    public void insertVideoLike(long videoId, long userId) {
        try {
            /**
             * 插入点赞记录的时候, 先判断redis中是否有用户点赞的记录
             * hash: LikedVideo_videoId   userId    1/-1(表示用户是否点赞过该视频，1表示点赞过，-1表示没有点赞过)
             */
            Object obj = redisTemplate.opsForHash().get("LikedVideo_" + videoId, String.valueOf(userId));
            /**
             * 如果redis中没有任何该用户是否已经点赞或者取消点赞的记录
             * 则先查询数据库是否存在该用户对该视频的点赞记录
             */
            if(obj == null){
                Map map=new HashMap();
                map.put("videoId",videoId);
                map.put("userId",userId);
                /**
                 * 如果redis中该用户没有点赞过该视频,则插入一条点赞记录到redis中,同时把对应视频的点赞记录加1
                 */
                if(!videoLikeMapper.checkIfUserAlreadyGiveLike(map)){
                    redisTemplate.opsForHash().put("LikedVideo_" + videoId, String.valueOf(userId), 1);

                    redisTemplate.opsForHash().put("LikedVideoOfUser_"+userId, videoId, 1);
                    redisTemplate.opsForSet().add("UserIdListOfVideo", userId);

                    redisTemplate.opsForZSet().incrementScore("LikedVideoList", String.valueOf(videoId), 1);
                }
                /**
                 * 如果redis中有用户取消点赞的操作记录,则把取消点赞这个操作的记录删除,当作什么也没发生,然后把对应视频的点赞记录加1
                 */
                else if (obj.equals(-1)) {
                    redisTemplate.opsForHash().delete("LikedVideo_" + videoId, String.valueOf(userId));
                    redisTemplate.opsForHash().delete("LikedVideoOfUser_"+userId, videoId);
                    redisTemplate.opsForZSet().incrementScore("LikedVideoList", String.valueOf(videoId), 1);
                }
                /**
                 * 如果是其他情况,object为1在redis中表示该用户已经点赞过该视频,则直接返回,不需要再插入一条点赞记录到redis中,
                 */
            }
        }catch (Exception e){
            throw new DatabaseException("点赞失败");
        }
    }


    @Override
    @Transactional
    public void deleteVideoLike(long videoId, long userId) {
        try {
            /**
             * 取消点赞的逻辑同点赞的逻辑
             */
            Object obj = redisTemplate.opsForHash().get("LikedVideo_" + videoId, String.valueOf(userId));
            if(obj==null){
                Map map=new HashMap();
                map.put("videoId",videoId);
                map.put("userId",userId);
                if(videoLikeMapper.checkIfUserAlreadyGiveLike(map)){
                    redisTemplate.opsForHash().put("LikedVideo_" + videoId, String.valueOf(userId), -1);

                    redisTemplate.opsForHash().put("LikedVideoOfUser_"+userId, videoId,-1);

                    redisTemplate.opsForZSet().incrementScore("LikedVideoList", String.valueOf(videoId), -1);
                }
            }else if (obj.equals(1)) {
                redisTemplate.opsForHash().delete("LikedVideo_" + videoId, String.valueOf(userId));
                redisTemplate.opsForHash().delete("LikedVideoOfUser_"+userId, videoId);
                redisTemplate.opsForZSet().incrementScore("LikedVideoList", String.valueOf(videoId), -1);
            }
        }catch (Exception e){
            throw new DatabaseException("取消点赞失败");
        }
    }

    //pageNum从1开始,pageSize为10
    @Override
    public List<Videos> queryLikedVideoList(long userId,int pageNum) {
       try {
           /**
            * 先从数据库中查询用户喜欢的视频的id集合
            */
           List<Long> videoIdList = videoLikeMapper.queryLikedVideoList(userId);
           /**
            * 如果redis中没有缓存用户对视频点赞和删除的任何一次操作记录,则直接返回数据库中查询到的视频集合
            */
           if(!redisTemplate.opsForSet().isMember("UserIdListOfVideo", userId)){
               return getLikedVideoInPage(pageNum, videoIdList);
           }
           Map<Object, Object> videoIdMap = redisTemplate.opsForHash().entries("LikedVideoOfUser_"+userId);
           if(videoIdMap==null||videoIdMap.isEmpty())
               return getLikedVideoInPage(pageNum, videoIdList);

           /**
            * 如果redis中有缓存用户对视频点赞和删除的任何一次操作记录,则把数据库中查询到的视频集合和redis中的点赞记录进行合并
            * 先把数据库中查询到的用户的点赞过的视频Id的List集合转换为Set集合,
            * 因为添加和删除Set的元素时间复杂度为1,比List集合快多了
            */
           Set<Long> finalVideoIdSet = new HashSet<>(videoIdList);
           videoIdMap.forEach((key, value) -> {
               /**
                * 如果是1,则表示缓存的是用户给视频点赞的操作,所以把该视频Id加入到finalVideoIdSet集合中
                */
               if((long)value==1)
                   finalVideoIdSet.add((long)key);
               /**
                * 如果是-1,则表示缓存的是用户取消点赞的操作,所以把该视频Id从finalVideoIdSet集合中移除
                */
               else if((long)value==-1){
                   finalVideoIdSet.remove((long)key);
               }
           });
           /**
            * 把合并后的集合转换为List集合,并返回
            */
           return getLikedVideoInPage(pageNum,new ArrayList<>(finalVideoIdSet));
       }catch (Exception e){
           throw new DatabaseException("查询自己喜欢的视频失败");
       }
    }

    private List<Videos> getLikedVideoInPage(int pageNum, List<Long> videoIdList) {
        // 计算起始索引和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, videoIdList.size());

        List<Long> finalVideoIdList = videoIdList.subList(startIndex, endIndex);
        return videoMapper.selectVideoListByManyIds(finalVideoIdList);
    }

    /**
     * 每一个小时同步一次点赞记录到mysql中,然后删除redis中的暂存的记录
     */
    @Scheduled(fixedRate = 3600000)
    public void synchronizeRedisAndMysql() {
        /**
         * 存储的数据结构是
         * ZSet: LikedVideoList           videoId   delta(表示这个视频在这段时间内的视频点赞增量，可以为负数)
         * Hash: LikedVideo_videoId   userId    1/-1(表示用户是否点赞过该视频，1表示点赞过，-1表示没有点赞过)
         */
        Set<ZSetOperations.TypedTuple<Object>> videoIdWithScore = redisTemplate.opsForZSet().rangeWithScores("LikedVideoList", 0, -1);
        if (videoIdWithScore == null || videoIdWithScore.isEmpty()) {
            return;
        }

        List<Map<String, Long>> addList = new LinkedList<>();
        List<Map<String, Long>> deleteList = new LinkedList<>();

        List<Long> videoIdList = new LinkedList<>();
        List<Long> userIdList = new LinkedList<>();
        List<Map<String, Long>> updateVideoLikeList = new LinkedList<>();


        /**
         * 遍历ZSet集合 LikedVideoList  videoId  delta的数据
         */
        for (ZSetOperations.TypedTuple<Object> tuple : videoIdWithScore) {
            String videoId = tuple.getValue().toString();
            Double score = tuple.getScore();

            Long videoIdNumber=Long.parseLong(videoId);
            videoIdList.add(videoIdNumber);

            /**
             * 把每一个videoId和对应的delta(点赞的增量或者减量数据)放到一个map中，然后把这个map放到一个list中，
             */
            Map<String, Long> updateVideoLikeMap = new HashMap<>();
            updateVideoLikeMap.put("videoId", videoIdNumber);
            updateVideoLikeMap.put("delta", score.longValue());
            updateVideoLikeList.add(updateVideoLikeMap);

            /**
             * 遍历Hash集合 LikedVideo_videoId userId  1/-1的数据
             */
            Map<Object, Object> videoMap = redisTemplate.opsForHash().entries("LikedVideo_" + videoId);
            for (Object userId : videoMap.keySet()) {
                Long userIdNumber=Long.parseLong(userId.toString());
                userIdList.add(userIdNumber);
                /**
                 * 上面遍历了ZSet集合的数据,获取了每个VideoId,这里根据获取到的VideoId,通过
                 * LikedVideo_videoId userId  1/-1的数据,获取到每个userId,然后把这个videoId及其对应的userId放到一个map中，
                 */
                Map<String, Long> map = new HashMap<>();
                map.put("videoId",videoIdNumber);
                map.put("userId", userIdNumber);

                /**
                 * 根据每一对的videoId 和 userId,对应的值,
                 * 如果是 1 则表示userId用户要给VideoId的视频进行点赞操作,然后把map放到addList中
                 * 如果是 -1 则表示userId用户要给VideoId的视频进行取消点赞操作,然后把map放到deleteList中
                 */
                if ((Integer) videoMap.get(userId) == 1) {
                    addList.add(map);
                } else if ((Integer) videoMap.get(userId) == -1) {
                    deleteList.add(map);
                }
            }
        }
        /**
         * 最后根据addList和deleteList,把videoId和userId这对值所代表的点赞记录插入到mysql中,或者从mysql中删除
         * 然后再删除redis中的暂存的操作记录
         */
        videoLikeMapper.insertVideoLike(addList);
        videoLikeMapper.deleteVideoLike(deleteList);
        videoMapper.updateVideoLikes(updateVideoLikeList);
        redisTemplate.delete("LikedVideoList");
        redisTemplate.delete("UserIdListOfVideo");
        cleanUpRedisKeyList(videoIdList,"LikedVideo_");
        cleanUpRedisKeyList(userIdList,"LikedVideoOfUser_");

    }

    public void cleanUpRedisKeyList(List<Long> idList,String keyName){
        redisTemplate.executePipelined(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                for (Long videoId : idList) {
                    byte[] key = redisTemplate.getStringSerializer().serialize(keyName + videoId);
                    connection.del(key);
                }
                return null;
            }
        });
    }
}
