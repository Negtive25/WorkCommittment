package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.Videos;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper {
    public void insertVideo(Map map);
    public void deleteVideo(Map map);
    public Videos queryVideoDetail(long id);
    public void increaseViewCount(long id);
    public List<Videos> searchVideoByKeyWords(String keyWords);
    public List<Videos> searchVideoByTime(Map map);

    public Videos querySelfVideoDetail(Map map);
    public List<Videos> selectSelfVideoWaitToReview(long userId);
    public List<Videos> selectSelfApprovedVideo(long userId);
    public List<Videos> selectSelfRejectedVideo(long userId);

    public List<Videos> selectAllVideoWaitToReview();
    public void updateVideoReviewStatus(Map map);

    public String selectVideoURL(long id);

    public List<Videos> selectVideoListByManyIds(List<Long> ids);

    public void updateVideoLikes(List<Map<String,Long>> mapList);

    /**
     * 获取最新的视频，按创建时间倒序，最多100条
     * @return 视频列表
     */
    List<Videos> selectLatestVideo();

    /**
     * 获取播放量最高的视频，按观看数倒序，最多100条
     * @return 视频列表
     */
    List<Videos> selectMostViewedVideo();
}
