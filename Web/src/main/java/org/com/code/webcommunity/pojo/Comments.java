package org.com.code.webcommunity.pojo;

import java.time.LocalDateTime;

public class Comments {
    private int id;  // 评论ID（主键）

    private int articleId;  // 关联的文章ID
    private int userId;  // 发表评论的用户ID
    private String content;  // 评论内容


    private int parentCommentId=0;  // 父评论ID（如果是根评论，则为0）

    private LocalDateTime createdAt;// 创建时间

    public Comments(int id, int articleId, int userId, String content, int parentCommentId, LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
    }

    public Comments(){
    }
    public Comments(int articleId, int parentCommentId) {
        this.articleId = articleId;
        this.parentCommentId = parentCommentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(int parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}