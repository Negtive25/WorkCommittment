package org.com.code.im.controller;

import org.com.code.im.pojo.Posts;
import org.com.code.im.pojo.Videos;
import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.CustomRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomRecommendController {

    @Autowired
    private CustomRecommendService customRecommendService;

    private long getCurrentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }
    @GetMapping("/api/recommend/getPost")
    public ResponseHandler getPost(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        long userId = getCurrentUserId();
        List<Posts> posts = customRecommendService.queryRecommendPost(userId, page, size);
        return new ResponseHandler(ResponseHandler.SUCCESS, "推荐的内容",posts);
    }

    @GetMapping("/api/recommend/getVideo")
    public ResponseHandler getVideo(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        long userId = getCurrentUserId();
        List<Videos> videos = customRecommendService.queryRecommendVideo(userId, page, size);
        return new ResponseHandler(ResponseHandler.SUCCESS, "推荐的视频",videos);
    }
}
