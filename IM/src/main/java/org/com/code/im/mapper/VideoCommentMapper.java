package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.VideoComments;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoCommentMapper {
    public void insertVideoComment(Map map);
    public void deleteVideoComment(Map map);
    public void updateVideoComment(Map map);
    public List<VideoComments> selectVideoComment(long videoId);
    public List<VideoComments> selectReplyComment(Map map);
    public void increaseVideoCommentCount(long videoId);
    public void decreaseVideoCommentCount(long videoId);
    public void increaseReplyCommentCount(long id);
    public void decreaseReplyCommentCount(long id);
}
