package org.com.code.webcommunity.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.webcommunity.pojo.ArticleLikes;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleLikesMapper {
    int insertArticleLike(ArticleLikes articleLikes);
    int deleteArticleLike(ArticleLikes articleLikes);
}
