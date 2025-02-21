package org.com.code.webcommunity.pojo;

import java.time.LocalDateTime;

public class ArticleLikes {
    private int id;  // 主键ID

    private int articleId;  // 文章ID
    private int userId;  // 喜欢这篇文章的用户ID

    private LocalDateTime createdAt;  // 点赞时间

    public ArticleLikes(){}
    public ArticleLikes(int usrId, int articleId){
        this.userId = usrId;
        this.articleId = articleId;
    }

    public ArticleLikes(int id, int articleId, int userId, LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ArticleLikes{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}