package org.com.code.im.service.Impl;

import org.com.code.im.exception.DatabaseException;
import org.com.code.im.mapper.UserMapper;
import org.com.code.im.mapper.VideoMapper;
import org.com.code.im.pojo.Videos;
import org.com.code.im.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private UserMapper userMapper;
    @Qualifier("objRedisTemplate")
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void insertVideo(Map map) {
        try {
            videoMapper.insertVideo(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("视频记录插入失败");
        }
    }

    @Override
    @Transactional
    public void deleteVideo(Map map) {
        try {
            videoMapper.deleteVideo(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("视频记录删除失败");
        }
    }

    @Override
    @Transactional
    public Map queryVideoDetail(long id) {
        try {
            Videos video =videoMapper.queryVideoDetail(id);
            if (video==null){
                throw new DatabaseException("查询视频详情失败");
            }
            videoMapper.increaseViewCount(id);
            long likeCountDelta = (long) redisTemplate.opsForHash().get("LikedVideoList", String.valueOf(video.getId()));
            video.setLikes(likeCountDelta+video.getLikes());

            Map map = video.toMap();
            map.put("AuthorAvatarUrl",userMapper.selectAvatarById(video.getUserId()));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询视频详情失败");
        }
    }

    @Override
    public List<Videos> searchVideoByKeyWords(String keyWords) {
        try {
            List<Videos> videos = videoMapper.searchVideoByKeyWords(keyWords);
            return updateVideoLikeCount(videos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("根据关键字查询视频失败");
        }
    }

    @Override
    public List<Videos> searchVideoByYear(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String,LocalDateTime> map = new HashMap<>();
            map.put("startTime",startTime);
            map.put("endTime",endTime);
            List<Videos> videos = videoMapper.searchVideoByYear(map);
            return updateVideoLikeCount(videos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("按时间查询视频失败");
        }
    }

    @Override
    public List<Videos> selectSelfVideoWaitToReview(long userId) {
        try {
            List<Videos> videos = videoMapper.selectSelfVideoWaitToReview(userId);
            return updateVideoLikeCount(videos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询等待审核视频失败");
        }
    }

    @Override
    public List<Videos> selectSelfApprovedVideo(long userId) {
        try {
            List<Videos> videos = videoMapper.selectSelfApprovedVideo(userId);
            return updateVideoLikeCount(videos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询已经过审视频失败");
        }
    }

    @Override
    public List<Videos> selectSelfRejectedVideo(long userId) {
        try {
            List<Videos> videos = videoMapper.selectSelfRejectedVideo(userId);
            return updateVideoLikeCount(videos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询未过审视频失败");
        }
    }

    private List<Videos> updateVideoLikeCount(List<Videos> videos) {
        List<Long> idList = videos.stream().map(Videos::getId).toList();
        List<Long> likeCountDelta=getLatestVideoLikeCount(idList);
        for (int i = 0; i < videos.size(); i++) {
            videos.get(i).setLikes(likeCountDelta.get(i)+videos.get(i).getLikes());
        }
        return videos;
    }

    @Override
    public List<Videos> selectAllVideoWaitToReview() {
        try {
            return videoMapper.selectAllVideoWaitToReview();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询所有待审核视频失败");
        }
    }

    @Override
    @Transactional
    public void updateVideoReviewStatus(long id, String status,long reviewerId,String reviewNotes) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("id",id);
            map.put("status",status);
            map.put("reviewerId",reviewerId);
            map.put("reviewNotes",reviewNotes);
            videoMapper.updateVideoReviewStatus(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("更新视频失败");
        }
    }

    @Override
    public String selectVideoURL(long id) {
        try {
            return videoMapper.selectVideoURL(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("查询视频URL失败");
        }
    }

    public List<Long> getLatestVideoLikeCount(List<Long> ids){
        List<Object> countList =redisTemplate.executePipelined(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize("LikedVideoList");
                for (Long id : ids) {
                    byte[] idSerialized = redisTemplate.getStringSerializer().serialize(id.toString());
                    connection.hGet(key, idSerialized);
                }
                return null;
            }
        });
        return countList.stream().map(count -> Long.parseLong(count.toString())).toList();
    }
}
