package org.com.code.webcommunity.service.Impl;

import org.com.code.webcommunity.dao.ArticleDao;
import org.com.code.webcommunity.exception.DatabaseException;
import org.com.code.webcommunity.pojo.Articles;
import org.com.code.webcommunity.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleImpl implements ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Transactional
    @Override
    public List<Articles> selectMostLikedOrLatestArticles(int page, int size, int LikesOrLatest) {
        try {
           return articleDao.selectMostLikedOrLatestArticles(page, size, LikesOrLatest);
        } catch (Exception e) {
            throw new DatabaseException("似乎文库里一篇文章都没有");
        }
    }

    @Transactional
    @Override
    public Articles selectArticlesById(int articleId) {
        try {
            return articleDao.selectArticlesById(articleId);
        } catch (Exception e) {
            throw new DatabaseException("数据库查询文章发生错误");
        }
    }

    @Transactional
    @Override
    public List<Articles> selectArticlesOfAuthor(int authorId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("authorId", authorId);
            map.put("status", 1);
            return articleDao.selectArticlesByAuthorIdAndStatusId(map);
        } catch (Exception e) {
            throw new DatabaseException("数据库查询用户自己发布的文章发生错误");
        }
    }

    @Transactional
    @Override
    public List<Articles> selectDraftsOfAuthor(int authorId){
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("authorId", authorId);
            map.put("status", 0);
            return articleDao.selectArticlesByAuthorIdAndStatusId(map);
        } catch (Exception e) {
            throw new DatabaseException("数据库查询用户自己的草稿发生错误");
        }
    }

    @Transactional
    @Override
    public List<Articles> selectArticlesLikeTitle(String title) {
        try {
            return articleDao.selectArticlesLikeTitle(title);
        } catch (Exception e) {
            throw new DatabaseException("数据库标题模糊查询文章发生错误");
        }
    }

    @Transactional
    @Override
    public int insertArticlesAsDraft(Articles articles) {
        try {
            return articleDao.insertArticlesAsDraft(articles);
        } catch (Exception e) {
            throw new DatabaseException("数据库保存草稿发生错误");
        }
    }

    @Transactional
    @Override
    public int updateArticleStatusToPublish(int authorId, int articleId) {
        try {
            if(articleDao.checkIfAuthorIdMatchArticleId(authorId, articleId)==0)
                throw new DatabaseException("文章id和作者id不匹配");
            return articleDao.updateArticleStatusToPublish(articleId);
        } catch (Exception e) {
            throw new DatabaseException("数据库修改发布文章发生错误");
        }
    }

    @Transactional
    @Override
    public int updateArticles(int authorId,Articles articles) {
        try {

            int articleId = articles.getId();

            if(articleDao.checkIfAuthorIdMatchArticleId(authorId, articleId)==0)
                throw new DatabaseException("文章id和作者id不匹配");

            Map<String, Object> map = new HashMap<>();
            map.put("id", articles.getId());
            map.put("title", articles.getTitle());
            map.put("content", articles.getContent());

            return articleDao.updateArticles(map);
        } catch (Exception e) {
            throw new DatabaseException("数据库更新文章发生错误");
        }
    }

    @Transactional
    @Override
    public int deleteArticles(int authorId,int articleId) {
        try {
            if(articleDao.checkIfAuthorIdMatchArticleId(authorId, articleId)==0)
                throw new DatabaseException("文章id和作者id不匹配");

            return articleDao.deleteArticles(articleId);
        } catch (Exception e) {
            throw new DatabaseException("数据库删除文章发生错误");
        }
    }
}
