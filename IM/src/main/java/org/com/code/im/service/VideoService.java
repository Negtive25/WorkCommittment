package org.com.code.im.service;

import org.com.code.im.pojo.Videos;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface VideoService {
    public void insertVideo(Map map);
    public void deleteVideo(Map map);

    public Map queryVideoDetail(long id);
    public List<Videos> searchVideoByKeyWords(String keyWords);
    public List<Videos> searchVideoByTime(LocalDateTime startTime, LocalDateTime endTime);

    public Map querySelfVideoDetail(Map map);
    public List<Videos> selectSelfVideoWaitToReview(long userId);
    public List<Videos> selectSelfApprovedVideo(long userId);
    public List<Videos> selectSelfRejectedVideo(long userId);

    public List<Videos> selectAllVideoWaitToReview();
    public void updateVideoReviewStatus(long id, String status,long reviewerId,String reviewNotes);
    public String selectVideoURL(long id);

    List<Videos> queryLatestVideos();

    List<Videos> queryMostViewedVideos();
}
