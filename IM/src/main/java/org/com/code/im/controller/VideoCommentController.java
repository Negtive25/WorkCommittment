package org.com.code.im.controller;

import org.com.code.im.pojo.VideoComments;
import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.VideoCommentService;
import org.com.code.im.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class VideoCommentController {
    @Autowired
    private VideoCommentService videoCommentService;

    /**
     * 插入评论
     * 前端的参数:
     * videoId: 视频id
     * content: 评论内容
     * parentId: 父评论id
     */
    @PostMapping("/api/videoComment/insertVideoComment")
    public ResponseHandler insertVideoComment(@RequestBody VideoComments videoComments){
        Map map = videoComments.toMap();
        map.put("id", SnowflakeIdUtil.commentIdWorker.nextId());
        map.put("userId",Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));

        videoCommentService.insertVideoComment(map);
        return new ResponseHandler(ResponseHandler.SUCCESS,"评论成功",null);
    }

    /**
     * 删除评论
     * 前端参数:
     * id: 评论id
     */
    @DeleteMapping("/api/videoComment/deleteVideoComment/{id}")
    public ResponseHandler deleteVideoComment(@PathVariable long id){
        long userId=Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Map<String,Long> map = new HashMap();
        map.put("id",id);
        map.put("userId",userId);

        videoCommentService.deleteVideoComment(map);
        return new ResponseHandler(ResponseHandler.SUCCESS,"删除评论成功",null);
    }

    /**
     * 更新评论
     * 前端参数:
     * id: 评论id
     * content: 评论内容
     */
    @PutMapping("/api/videoComment/updateVideoComment")
    public ResponseHandler updateVideoComment(@RequestBody VideoComments videoComments){
        Map<String,Object> map = new HashMap();
        long userId=Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        map.put("id",videoComments.getId());
        map.put("content",videoComments.getContent());
        map.put("userId",userId);

        videoCommentService.updateVideoComment(map);
        return new ResponseHandler(ResponseHandler.SUCCESS,"更新评论成功",null);
    }

    /**
     * 查询视频的所有评论
     * 前端参数:
     * videoId: 视频id
     */
    @GetMapping("/api/videoComment/selectVideoComment/{videoId}")
    public ResponseHandler selectVideoComment(@PathVariable long videoId){
        return new ResponseHandler(ResponseHandler.SUCCESS,"查询评论成功",videoCommentService.selectVideoComment(videoId));
    }

    /**
     * 查询评论的回复
     * 前端参数:
     * videoId: 视频id
     * parentId: 父评论id
     */
    @GetMapping("/api/videoComment/selectReplyComment")
    public ResponseHandler selectReplyComment(@RequestParam("videoId") long videoId,@RequestParam("parentId") long parentId){
        Map map = new HashMap();
        map.put("videoId",videoId);
        map.put("parentId",parentId);
        return new ResponseHandler(ResponseHandler.SUCCESS,"查询评论成功",videoCommentService.selectReplyComment(map));
    }
}
