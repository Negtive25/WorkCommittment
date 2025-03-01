package org.com.code.webcommunity.controller;

import org.com.code.webcommunity.exception.BadRequestException;
import org.com.code.webcommunity.pojo.Articles;
import org.com.code.webcommunity.pojo.User;
import org.com.code.webcommunity.service.ArticleService;
import org.com.code.webcommunity.service.UserService;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
public class ArticleController {

    static int pageSize = 5;
    @Autowired
    ArticleService articleService;
    @Autowired
    UserService userService;

    //按点击量降序，分页查询，每5个5个查
    @GetMapping("/api/articles/mostLikedArticles")
    public ResponseEntity<List<Articles>> selectArticlesDescByLikes(@RequestParam(value = "page", defaultValue = "1") int page) throws BadRequestException {
        if (page <1) {
            throw new BadRequestException("起始页面page参数不合理,page需要大于等于1");
        }
        List<Articles> articles = articleService.selectMostLikedOrLatestArticles(page, pageSize,0);
        if (articles == null || articles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @GetMapping("/api/articles/mostLatestArticles")
    public ResponseEntity<List<Articles>> selectArticlesDescByLatest(@RequestParam(value = "page", defaultValue = "1") int page) throws BadRequestException {
        if (page <1) {
            throw new BadRequestException("起始页面page参数不合理,page需要大于等于1");
        }
        List<Articles> articles = articleService.selectMostLikedOrLatestArticles(page, pageSize,1);
        if (articles == null || articles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @GetMapping("/api/articles/selectArticlesById")
    public ResponseEntity<Articles> selectArticlesById(@RequestParam(value = "articleId") int articleId) throws BadRequestException {
        if (articleId <1) {
            throw new BadRequestException("文章id参数不合理,articleId需要大于等于1");
        }
        Articles articles = articleService.selectArticlesById(articleId);
        if (articles == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @GetMapping("/api/articles/articlesOfAuthor")
    public ResponseEntity<List<Articles>> selectArticlesOfAuthor() throws BadRequestException {

        int authorId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if (authorId <1) {
            throw new BadRequestException("作者id参数不合理,authorId需要大于等于1");
        }
        List<Articles> articles = articleService.selectArticlesOfAuthor(authorId);
        if (articles == null || articles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @GetMapping("/api/articles/draftsOfAuthor")
    public ResponseEntity<List<Articles>> selectDraftsOfAuthor() throws BadRequestException {
        int authorId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Articles> articles = articleService.selectDraftsOfAuthor(authorId);
        if (articles == null || articles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @GetMapping("/api/articles/selectArticlesLikeTitle")
    public ResponseEntity<List<Articles>> selectArticlesLikeTitle(@RequestParam(value = "title") String title) throws BadRequestException {
        List<Articles> articles = articleService.selectArticlesLikeTitle(title);
        if (articles == null || articles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    //如果要发布文章的话，前提是要把文章保存
   @PostMapping("/api/articles/saveArticlesAsDraft")
   public ResponseEntity<Articles> insertArticlesAsDraft(@RequestBody Articles article) throws BadRequestException {
        if (article.getTitle() == null|| article.getContent() == null) {
            throw new BadRequestException("文章不能为空");
        }
       int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        article.setAuthorId(userId);
       User result = userService.selectUserById(userId);
       if (result == null) {
           throw new BadRequestException("用户不存在");
       }
       article.setAuthorName(result.getUserName());

       try{
           articleService.insertArticlesAsDraft(article);
       }catch (Exception e){
           throw new BadRequestException("创建草稿失败");
       }
        return new ResponseEntity<>(HttpStatus.OK);
   }

   @PutMapping("/api/articles/publishArticles")
    public ResponseEntity<Articles> updateArticleStatusToPublish(@RequestParam(value = "articleId") int articleId) throws BadRequestException {

       int authorId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        int result = articleService.updateArticleStatusToPublish(authorId ,articleId);
        if (result == 0)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/api/articles/updateArticles")
    public ResponseEntity<Articles> updateArticles(@RequestBody Articles article) throws BadRequestException {

        int authorId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if (article == null) {
            throw new BadRequestException("文章为空，更新失败");
        }

        int result = articleService.updateArticles(authorId,article);
        if (result == 0)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/articles/deleteArticles")
    public ResponseEntity<Articles> deleteArticles(@RequestParam(value = "articleId") int articleId) throws BadRequestException {

        int authorId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        int result = articleService.deleteArticles(authorId,articleId);
        if (result == 0)
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
