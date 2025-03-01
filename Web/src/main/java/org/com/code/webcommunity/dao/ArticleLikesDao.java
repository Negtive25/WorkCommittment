package org.com.code.webcommunity.dao;

import org.com.code.webcommunity.mapper.ArticleLikesMapper;
import org.com.code.webcommunity.mapper.ArticlesMapper;
import org.com.code.webcommunity.pojo.ArticleLikes;
import org.com.code.webcommunity.pojo.Articles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class ArticleLikesDao {
    @Autowired
    private ArticleLikesMapper articleLikesMapper;
    @Autowired
    private RedisDao redisDao;
    @Autowired
    private ArticlesMapper articlesMapper;

    public int insertArticleLike(ArticleLikes articleLikes) {
        return articleLikesMapper.insertArticleLike(articleLikes);
    }

    public int deleteArticleLike(ArticleLikes articleLikes) {

        return articleLikesMapper.deleteArticleLike(articleLikes);
    }

    //这里返回除了文章内容以外的所有属性的对象列表，旨在节省性能，
    //如果点击某个特定的文章后，才会调用查询该文章id加载整个文章内容
    public List<Articles> selectLikedArticlesOfUser(int userId) {
        Set<Integer> articleIdSet =redisDao.selectLikedArticlesOfUser(userId);

        List<Articles> articlesList=articlesMapper.selectArticlesByManyIds(articleIdSet);
        //如果有人给这些文章点赞的话，可能点赞数还在redis数据库中缓存，还没有同步到mysql数据库中
        //所以直接由articlesMapper.selectArticlesByManyIds(articleIds)访问mysql数据库获取的文章点赞数可能不准
        //所以这里访问redis数据库，对这些文章的点赞数进行可能的更新
        return redisDao.queryRedisToUpdateLikeCountForArticleList(articlesList);
    }

    public boolean ifUserEverLikesTheArticle(int userId,int articleId){
        //在redis数据库查询用户是否喜欢过这篇文章
        return redisDao.checkIfUserLikesTheArticle(userId,articleId);
    }
}
