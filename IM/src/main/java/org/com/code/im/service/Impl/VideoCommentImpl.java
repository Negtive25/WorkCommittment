package org.com.code.im.service.Impl;

import org.com.code.im.exception.DatabaseException;
import org.com.code.im.mapper.VideoCommentMapper;
import org.com.code.im.pojo.VideoComments;
import org.com.code.im.service.VideoCommentService;
import org.com.code.im.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class VideoCommentImpl implements VideoCommentService {
    @Autowired
    private VideoCommentMapper videoCommentMapper;

    @Override
    @Transactional
    public void insertVideoComment(Map map) {
        try {
            long videoId= (long) map.get("videoId");
            videoCommentMapper.increaseVideoCommentCount(videoId);
            long parentId = (long) map.get("parentId");
            if(parentId!=0)
                videoCommentMapper.increaseReplyCommentCount(parentId);
            videoCommentMapper.insertVideoComment(map);
        } catch (Exception e) {
            throw new DatabaseException("评论失败");
        }
    }

    @Override
    @Transactional
    public void deleteVideoComment(Map map) {
        try {
            long videoId= (long) map.get("videoId");
            videoCommentMapper.decreaseVideoCommentCount(videoId);
            long parentId = (long) map.get("parentId");
            if(parentId!=0)
                videoCommentMapper.decreaseReplyCommentCount(parentId);
            videoCommentMapper.deleteVideoComment(map);
        } catch (Exception e) {
            throw new DatabaseException("删除评论失败");
        }
    }

    @Override
    @Transactional
    public void updateVideoComment(Map map) {
        try {
            videoCommentMapper.updateVideoComment(map);
        } catch (Exception e) {
            throw new DatabaseException("更新评论失败");
        }
    }

    @Override
    public List<VideoComments> selectVideoComment(long videoId) {
        try {
            return videoCommentMapper.selectVideoComment(videoId);
        } catch (Exception e) {
            throw new DatabaseException("查询评论失败");
        }
    }

    @Override
    public List<VideoComments> selectReplyComment(Map map) {
        try {
            return videoCommentMapper.selectReplyComment(map);
        }catch (Exception e) {
            throw new DatabaseException("查询回复评论失败");
        }
    }
}
