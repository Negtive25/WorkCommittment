package org.com.code.webcommunity.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.webcommunity.pojo.Comments;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentsMapper {
    public int insertComments(Comments comments);

    public int numberOfAllComments(int articleId);

    public int numberOfReply(Map<String, Object> map);

    public List<Comments> selectCommentsOfOneArticle(Map<String, Object> map);

    public int deleteComment(int id);
}
