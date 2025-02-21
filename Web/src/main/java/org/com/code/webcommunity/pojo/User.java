package org.com.code.webcommunity.pojo;

import java.time.LocalDateTime;


public class User {
    private int id;  // 用户ID（主键）
    private String userName;  // 用户名
    private String password;  // 密码
    private String avatar;  // 头像地址（URL）
    private String bio;  // 个人简介
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间

    public User() {
    }
    public User(String userName,String password){
        this.userName = userName;
        this.password = password;
    }

    public User(int id,String userName,String avatar,String bio,LocalDateTime createdAt,LocalDateTime updatedAt) {
        this.id = id;
        this.userName = userName;
        this.avatar = avatar;
        this.bio = bio;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bio='" + bio + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getBio() {
        return bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User(int id, String userName, String password, String avatar, String bio, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.avatar = avatar;
        this.bio = bio;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}