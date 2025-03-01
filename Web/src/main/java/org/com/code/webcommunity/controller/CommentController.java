package org.com.code.webcommunity.controller;

import jakarta.websocket.server.PathParam;
import org.com.code.webcommunity.exception.BadRequestException;
import org.com.code.webcommunity.pojo.Comments;
import org.com.code.webcommunity.service.CommentService;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/api/comments/insertComment")
    public ResponseEntity<Comments> insertComment(@RequestHeader String token,@RequestBody Comments comment) throws BadRequestException {
        int userId =  Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        if (comment == null)
            throw new BadRequestException("评论为空");

        comment.setUserId(userId);

        int result = commentService.insertComment(comment);
        if (result == 0)
            throw new BadRequestException("插入评论失败");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/comments/numberOfAllComments")
    public ResponseEntity<Integer> numberOfAllComments(@PathParam("id") int articleId) throws BadRequestException {
        if (articleId < 1)
            throw new BadRequestException("文章id必须大于等于1");
        return new ResponseEntity<>(commentService.numberOfAllComments(articleId), HttpStatus.OK);
    }

    //查询一个评论底下的回复 数量 ，parentCommentId代表这个评论它的父评论的id号
    //举个例子，如果一个评论的parentCommentId为5,那这个评论就是评论id为5的子评论
    //而parentCommentId为0表示它为这篇文章的评论，不是回复给任何人的，就只是在文章底下留言而已，
    @GetMapping("/api/comments/numberOfReply")
    public ResponseEntity<Integer> numberOfCommentsFromOneArticleOrOneComment(@PathParam("articleId") int articleId, @PathParam("parentCommentId") int parentCommentId) throws BadRequestException {
        if (articleId < 1)
            throw new BadRequestException("查询文章的评论，文章id必须大于等于0");
        Comments articleIdAndParentCommentId = new Comments(articleId, parentCommentId);
        return new ResponseEntity<>(commentService.numberOfReply(articleIdAndParentCommentId), HttpStatus.OK);
    }

    //查询一个评论底下的全部回复 ,parentCommentId代表这个评论它的父评论的id号
    //举个例子，如果一个评论的parentCommentId为5,那这个评论就是评论id为5的子评论
    //而parentCommentId为0表示它为这篇文章的评论，不是回复给任何人的，就只是在文章底下留言而已，
    @GetMapping("/api/comments/selectCommentsOfOneArticle")
    public ResponseEntity<List<Comments>> selectCommentsOfOneArticle(@PathParam("articleId") int articleId, @PathParam("parentCommentId") int parentCommentId) throws BadRequestException {
        if (articleId <1)
            throw new BadRequestException("文章id数必须大于等于1");
        Comments comments=new Comments(articleId,parentCommentId);
        return new ResponseEntity<>(commentService.selectCommentsOfOneArticle(comments), HttpStatus.OK);
    }

    @DeleteMapping("/api/comments/deleteComment")
    public ResponseEntity<Comments> deleteComment(@PathParam("commentId") int commentId) throws BadRequestException {
        if (commentId < 1)
            throw new BadRequestException("评论id必须大于等于1");
        int result = commentService.deleteComment(commentId);
        if (result == 0)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
