package org.com.code.webcommunity.service;

import org.com.code.webcommunity.pojo.Comments;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {

    public int insertComment(Comments comments);

    public int numberOfAllComments(int articleId);

    public int numberOfReply(Comments articleIdAndParentCommentId);

    public List<Comments> selectCommentsOfOneArticle(Comments comments);

    public int deleteComment(int commentId);
}
