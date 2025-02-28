package org.com.code.webcommunity.dao;

import org.com.code.webcommunity.mapper.ArticlesMapper;
import org.com.code.webcommunity.pojo.Articles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ArticleDao {
    @Autowired
    private ArticlesMapper articlesMapper;
    @Autowired
    private RedisTemplate<String,Integer> redisTemplate;
    @Autowired
    private RedisDao redisDao;

    //这里是redis数据库进行的操作

    //如果LikesOrLiked=0，则按点赞量降序，分页查询，每5个5个查
    //如果LikesOrLatest=1，则按发布时间降序，分页查询，每5个5个查
    public List<Articles> selectMostLikedOrLatestArticles(int page, int size,int LikesOrLatest) {
        int start = (page - 1) * size;
        int end = start + size - 1;

        Set<Integer> articleIds;
        if(LikesOrLatest==0) articleIds=redisTemplate.opsForZSet().reverseRange("article_likes", start, end);
        else
            articleIds= redisTemplate.opsForZSet().reverseRange("article_latest", start, end);
        List<Articles> articlesList=articlesMapper.selectArticlesByManyIds(articleIds);
        //如果有人给这些文章点赞的话，可能点赞数还在redis数据库中缓存，还没有同步到mysql数据库中
        //所以直接由articlesMapper.selectArticlesByManyIds(articleIds)访问mysql数据库获取的文章点赞数可能不准
        //所以这里访问redis数据库，对这些文章的点赞数进行可能的更新
        return redisDao.queryRedisToUpdateLikeCountForArticleList(articlesList);

    }
    //---------------------------------------------------------------------------------------

    //这都是在mysql数据库进行的操作

    //通过作者id和文章状态查询文章或它的草稿

    //判断作者id是否和文章id匹配，防止有人恶意修改他人文章
    public int checkIfAuthorIdMatchArticleId(int authorId, int articleId) {
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", articleId);
        map.put("authorId", authorId);
        //判断作者id是否和文章id匹配，防止有人恶意修改他人文章
        if (articlesMapper.checkIfAuthorIdMatchArticleId(map) == 0)
            return 0;
        return 1;
    }

    public List<Articles> selectArticlesByAuthorIdAndStatusId(Map<String, Object> map) {
        return articlesMapper.selectArticlesByAuthorIdAndStatus(map);
    }

    //通过文章id查询文章,相当于点击加载一个文章
    public Articles selectArticlesById(int articleId) {
        //文章被点击一次，浏览量加一
        articlesMapper.articleViewIncrease(articleId);
        Articles articles = articlesMapper.selectArticlesById(articleId);
        //如果有人给这篇文章点赞的话，可能点赞数还在redis数据库中缓存，还没有同步到mysql数据库中
        //所以直接由articlesMapper.selectArticlesById(articleId)访问mysql数据库获取的文章点赞数可能不准
        //所以这里访问redis数据库，对这篇文章的点赞数进行可能的更新
        articles.setLikeCount(redisDao.getLikesCount(articleId));
        return articles;
    }


    //模糊搜索文章
    //这里返回除了文章内容以外的所有属性的对象列表，旨在节省性能，如果点击某个特定的文章后
    //才会调用查询该文章id加载整个文章内容
    public List<Articles> selectArticlesLikeTitle(String title) {
        List<Articles> articlesList=articlesMapper.selectArticlesLikeTitle(title);

        //如果有人给这些文章点赞的话，可能点赞数还在redis数据库中缓存，还没有同步到mysql数据库中
        //所以直接由articlesMapper.selectArticlesLikeTitle(title)访问mysql数据库获取的文章点赞数可能不准
        //所以这里访问redis数据库，对这些文章的点赞数进行可能的更新
        return redisDao.queryRedisToUpdateLikeCountForArticleList(articlesList);
    }

    //创建草稿
    public int insertArticlesAsDraft(Articles articles) {
        return articlesMapper.insertArticlesAsDraft(articles);
    }

    //发布文章
    public int updateArticleStatusToPublish(int articleId) {
        redisTemplate.opsForZSet().add("article_likes", articleId, 0);
        redisTemplate.opsForZSet().add("article_latest", articleId, articlesMapper.selectArticlesCreatedAt(articleId));
        System.out.println("article_latest:");
        return articlesMapper.updateArticleStatusToPublish(articleId);
    }

    //修改文章或草稿
    public int updateArticles(Map<String, Object> articles) {
        return articlesMapper.updateArticles(articles);
    }

    public int updateAuthorNameOfArticles(Map<String, Object> articles) {
        return articlesMapper.updateAuthorNameOfArticles(articles);
    }

    //删除文章或草稿
    public int deleteArticles(int articleId) {
        redisTemplate.opsForZSet().remove("article_likes", articleId);
        redisTemplate.opsForZSet().remove("article_latest", articleId);
        return articlesMapper.deleteArticles(articleId);
    }

    public void setArticleLikes(int likeCount,int articleId) {
        Map<String, Object> likeCountAndArticleId =new HashMap<>();
        likeCountAndArticleId.put("likeCount",likeCount);
        likeCountAndArticleId.put("articleId",articleId);
        articlesMapper.setArticleLikes(likeCountAndArticleId);
    }
}
