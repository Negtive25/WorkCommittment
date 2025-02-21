package org.com.code.webcommunity.pojo;



import java.time.LocalDateTime;

public class Articles {

    private int id;  // 文章ID（主键）

    private String title;  // 文章标题
    private String content;  // 文章内容（支持Markdown 格式）
    private int authorId=0;  // 作者ID
    private String authorName;//作者名

    private int viewCount;  // 文章浏览量
    private int likeCount;  // 文章点赞量
    private int status;  // 文章状态（0=草稿，1=已发布）

    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    public Articles() {
    }

    //为从数据库中选出部分类属性赋给Articles而写的构造函数
    public Articles(int id, String title,String authorName, int viewCount, int likeCount, int status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Articles(int id, String title, String content, int authorId, String authorName, int viewCount, int likeCount, int status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Articles{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}