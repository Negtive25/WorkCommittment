package org.com.code.im.service;


import org.com.code.im.pojo.VideoComments;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface VideoCommentService {
    public void insertVideoComment(Map map);
    public void deleteVideoComment(Map map);
    public void updateVideoComment(Map map);
    public List<VideoComments> selectVideoComment(long videoId);
    public List<VideoComments> selectReplyComment(Map map);
}
