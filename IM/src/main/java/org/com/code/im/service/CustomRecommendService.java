package org.com.code.im.service;

import org.com.code.im.pojo.Posts;
import org.com.code.im.pojo.Videos;

import java.util.List;

public interface CustomRecommendService {
    List<Posts> queryRecommendPost(long userId, int page, int size);
    List<Videos> queryRecommendVideo(long userId, int page, int size);
}
