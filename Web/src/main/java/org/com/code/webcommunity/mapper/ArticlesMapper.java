package org.com.code.webcommunity.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.webcommunity.pojo.Articles;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ArticlesMapper {

    //这都是在mysql数据库进行的操作


    //判断作者id是否和文章id匹配，防止有人恶意修改他人文章
    public int checkIfAuthorIdMatchArticleId(Map<String, Object> map);
    //通过作者id和文章状态status来查询该作者的草稿和文章有哪些
    public List<Articles> selectArticlesByAuthorIdAndStatus(Map<String, Object> map);

    //获取文章创建日期
    public int selectArticlesCreatedAt(int articleId);

    //通过文章id查询文章
    //这里返回文章的全部属性，用来展示文章
    public Articles selectArticlesById(int articleId);

    //这个方法批量查询大量的文章，所以返回值是List<Articles>
    //这里返回除了文章内容以外的所有属性的对象列表，旨在节省性能，
    //如果点击某个特定的文章后，才会调用查询该文章id加载整个文章内容
    public List<Articles> selectArticlesByManyIds(Set<Integer> articleIds);

    //模糊搜索文章
    //这里返回除了文章内容以外的所有属性的对象列表，旨在节省性能，如果点击某个特定的文章后
    //才会调用查询该文章id加载整个文章内容
    public List<Articles> selectArticlesLikeTitle(String title);

    //创建草稿
    public int insertArticlesAsDraft(Articles articles);

    //发布文章
    public int updateArticleStatusToPublish(int articleId);

    //修改文章或草稿
    public int updateArticles(Map<String, Object> map);

    public int updateAuthorNameOfArticles(Map<String, Object> map);

    //删除文章
    public int deleteArticles(int articleId);

    public void setArticleLikes(Map<String, Object> likeCountAndArticleId);

    public int  articleViewIncrease(int articleId);
}
