package org.com.code.webcommunity.dao;

import org.com.code.webcommunity.mapper.CommentsMapper;
import org.com.code.webcommunity.pojo.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommentsDao {
    @Autowired
    private CommentsMapper commentsMapper;

    public int insertComments(Comments comments) {
        return commentsMapper.insertComments(comments);
    }

    public int numberOfAllComments(int articleId) {
        return commentsMapper.numberOfAllComments(articleId);
    }

    public int numberOfReply(Comments articleIdAndParentCommentId){
        Map<String,Object> map=new HashMap<>();
        map.put("articleId",articleIdAndParentCommentId.getArticleId());
        map.put("parentCommentId",articleIdAndParentCommentId.getParentCommentId());
        return commentsMapper.numberOfReply(map);
    }

    public List<Comments> selectCommentsOfOneArticle(Comments comments){
        Map<String,Object> map=new HashMap<>();
        map.put("articleId",comments.getArticleId());
        map.put("parentCommentId",comments.getParentCommentId());
        return commentsMapper.selectCommentsOfOneArticle(map);
    }

    public int deleteComments(int id) {
        return commentsMapper.deleteComment(id);
    }

}
