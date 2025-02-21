package org.com.code.webcommunity.service;

import org.com.code.webcommunity.pojo.Articles;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ArticleService {

    //这里是redis数据库进行的操作

    //查询最热文章或最新文章
    //1代表最热文章，2代表最新文章
    //每5个5个查,排行榜最多只有点赞量或最新的前200篇文章
    public List<Articles> selectMostLikedOrLatestArticles(int page, int size,int LikesOrLatest);


    //-------------------------------------------------
    //这都是在mysql数据库进行的操作

    //查询一个文章
    public Articles selectArticlesById(int articleId);

    //查询一个作者的所有文章
    public List<Articles> selectArticlesOfAuthor(int authorId);

    //查询一个作者的所有草稿
    public List<Articles> selectDraftsOfAuthor(int authorId);

    //模糊搜索文章
    public List<Articles> selectArticlesLikeTitle(String title);

    //创建草稿
    public int insertArticlesAsDraft(Articles articles);

    //发布文章
    public int updateArticleStatusToPublish(int authorId,int articleId);

    //修改文章或草稿
    public int updateArticles(int authorId,Articles articles);

    //删除文章或草稿
    public int deleteArticles(int authorId,int articleId);


}
