package org.com.code.webcommunity.service.Impl;

import org.com.code.webcommunity.dao.CommentsDao;
import org.com.code.webcommunity.exception.DatabaseException;
import org.com.code.webcommunity.pojo.Comments;
import org.com.code.webcommunity.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentImpl implements CommentService {
    @Autowired
    private CommentsDao commentsDao;

    @Override
    @Transactional
    public int insertComment(Comments comments){
        try {
            return commentsDao.insertComments(comments);
        }catch (Exception e){
            throw new DatabaseException("数据库插入评论失败");
        }
    }

    @Override
    @Transactional
    public int numberOfAllComments(int articleId){
        try {
            return commentsDao.numberOfAllComments(articleId);
        }catch (Exception e){
            throw new DatabaseException("数据库查询文章的评论数失败");
        }
    }

    @Override
    @Transactional
    public int numberOfReply(Comments comments){
        try {
            return commentsDao.numberOfReply(comments);
        }catch (Exception e){
            throw new DatabaseException("数据库查询文章的评论回复数失败");
        }
    }

    @Override
    @Transactional
    public List<Comments> selectCommentsOfOneArticle(Comments comments){
        try {
            return commentsDao.selectCommentsOfOneArticle(comments);
        }catch (Exception e){
            throw new DatabaseException("数据库查询文章的评论失败");
        }
    }

    @Override
    @Transactional
    public int deleteComment(int id){
        try {
            return commentsDao.deleteComments(id);
        }catch (Exception e){
            throw new DatabaseException("数据库删除评论失败");
        }
    }

}
