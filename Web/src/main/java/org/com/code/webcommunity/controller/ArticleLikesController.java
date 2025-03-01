package org.com.code.webcommunity.controller;

import org.com.code.webcommunity.exception.BadRequestException;
import org.com.code.webcommunity.pojo.ArticleLikes;
import org.com.code.webcommunity.pojo.Articles;
import org.com.code.webcommunity.service.ArticleLikesService;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ArticleLikesController {
    @Autowired
    private ArticleLikesService articleLikesService;

    //服务层的insertArticleLike方法其实是把 赞 缓存到redis中，然后定时同步到mysql中
    @PostMapping("/api/articleLikes/insertArticleLike")
    public ResponseEntity<ArticleLikes> insertArticleLike(@RequestBody ArticleLikes articleLikes) throws BadRequestException {
        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if(articleLikes==null)
            throw new BadRequestException("点赞参数为空");

        articleLikes.setUserId(userId);

        articleLikesService.insertArticleLike(articleLikes);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    //服务层的insertArticleLike方法其实是把 取消赞 缓存到redis中，然后定时同步到mysql中
    @DeleteMapping("/api/articleLikes/deleteArticleLike")
    public ResponseEntity<ArticleLikes> deleteArticleLike(@RequestBody ArticleLikes articleLikes) throws BadRequestException {
        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if(articleLikes==null)
            throw new BadRequestException("取消点赞参数为空");

        articleLikes.setUserId(userId);

        articleLikesService.deleteArticleLike(articleLikes);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/articleLikes/selectLikedArticlesOfUser")
    public ResponseEntity<List<Articles>> selectLikedArticlesOfUser() throws BadRequestException {
        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Articles> result = articleLikesService.selectLikedArticlesOfUser(userId);
        if(result==null||result.isEmpty())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @GetMapping("/api/articleLikes/ifUserEverLikesTheArticle")
    public ResponseEntity<Boolean> ifUserEverLikesTheArticle(@RequestParam("articleId") int articleId) throws BadRequestException {
        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if(articleId < 1)
            throw new BadRequestException("查询用户点赞的文章，文章id必须大于等于1");
        boolean result = articleLikesService.ifUserEverLikesTheArticle(userId,articleId);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }
}
