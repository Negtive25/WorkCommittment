package org.com.code.im.service;

import org.com.code.im.pojo.Posts;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PostLikeService {

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    List<Posts> queryLikedPostList(long userId, int pageNum);
}
