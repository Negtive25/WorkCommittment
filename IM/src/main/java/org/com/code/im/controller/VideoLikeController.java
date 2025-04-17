package org.com.code.im.controller;

import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.VideoLikeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoLikeController {
    private VideoLikeService videoLikeService;

    @PostMapping("api/videoLike/giveLikeToVideo/{videoId}")
    public ResponseHandler giveLikeToVideo(@PathVariable long videoId) {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        videoLikeService.insertVideoLike(videoId, userId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "点赞成功");
    }

    @PostMapping("api/videoLike/cancelLikeToVideo/{videoId}")
    public ResponseHandler cancelLikeToVideo(@PathVariable long videoId) {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        videoLikeService.deleteVideoLike(videoId, userId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "取消点赞成功");
    }

    @PostMapping("api/videoLike/queryLikedVideoList/{pageNum}")
    public ResponseHandler queryLikedVideoList(@PathVariable int pageNum) {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        return new ResponseHandler(ResponseHandler.SUCCESS, "查询自己喜欢的视频列表成功", videoLikeService.queryLikedVideoList(userId, pageNum));
    }
}
