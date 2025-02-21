package org.com.code.webcommunity.service.Impl;


import org.com.code.webcommunity.dao.ArticleLikesDao;
import org.com.code.webcommunity.dao.RedisDao;
import org.com.code.webcommunity.exception.DatabaseException;
import org.com.code.webcommunity.pojo.ArticleLikes;
import org.com.code.webcommunity.pojo.Articles;
import org.com.code.webcommunity.service.ArticleLikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ArticleLikesImpl implements ArticleLikesService {
    @Autowired
    private ArticleLikesDao articleLikesDao;
    @Autowired
    private RedisDao redisDao;

    //这是点赞的方法
    @Override
    @Transactional
    public void insertArticleLike(ArticleLikes articleLikes){
        try {
            //这里调用redis，先把点赞的动作，即增的记录存到redis中，然后等redis和mysql定时同步更新
            redisDao.articleLikeIncrease(articleLikes.getArticleId(),articleLikes.getUserId());
        }catch (Exception e){
            throw new DatabaseException("数据库点赞失败，每个用户一个文章只能点赞一次");
        }
    }

    //这是取消赞的方法
    @Override
    @Transactional
    public void deleteArticleLike(ArticleLikes articleLikes){
        try {
            //这里调用redis，先把取消点赞的动作，即减的记录存到redis中，然后等redis和mysql定时同步更新
            redisDao.articleLikeDecrease(articleLikes.getArticleId(),articleLikes.getUserId());
        }catch (Exception e){
            throw new DatabaseException("数据库取消点赞失败");
        }
    }

    @Override
    @Transactional
    public List<Articles> selectLikedArticlesOfUser(int userId){
        try {
            return articleLikesDao.selectLikedArticlesOfUser(userId);
        }catch (Exception e){
            throw new DatabaseException("数据库查询点赞后的文章失败");
        }
    }

    @Override
    @Transactional
    public boolean ifUserEverLikesTheArticle(int userId,int articleId){
        try {
            return articleLikesDao.ifUserEverLikesTheArticle(userId,articleId);
        }catch (Exception e){
            throw new DatabaseException("数据库查询点赞记录失败");
        }
    }
}
