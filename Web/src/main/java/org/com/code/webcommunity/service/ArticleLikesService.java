package org.com.code.webcommunity.service;

import org.com.code.webcommunity.pojo.ArticleLikes;
import org.com.code.webcommunity.pojo.Articles;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ArticleLikesService {

    //服务层的insertArticleLike方法其实是把 赞 缓存到redis中，然后定时同步到mysql中
    public void insertArticleLike(ArticleLikes articleLikes);

    //服务层的deleteArticleLike方法其实是把 取消赞 缓存到redis中，然后定时同步到mysql中
    public void deleteArticleLike(ArticleLikes articleLikes);

    public List<Articles> selectLikedArticlesOfUser(int userId);

    public boolean ifUserEverLikesTheArticle(int userId,int articleId);

}
