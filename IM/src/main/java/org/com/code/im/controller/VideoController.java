package org.com.code.im.controller;

import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.FileUploadService;
import org.com.code.im.service.VideoService;
import org.com.code.im.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@RestController
public class VideoController {
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private FileUploadService fileUploadService;

    @Qualifier("objRedisTemplate")
    @Autowired
    private RedisTemplate redisTemplateObj;

    /**
     * 前端先把视频文件上传到OSS后,获得视频的URL码，再调用此接口，将视频信息保存到数据库中
     * 调用此接口的时候要记得带上之前上传文件的uploadId参数，后端会根据uploadId从redis中获取视频的时长(单位:分钟)，并保存到数据库中
     */
    @PostMapping("/api/video/insertVideo")
    public ResponseHandler insertVideo(@RequestParam("title") String title,
                                       @RequestParam(value = "tags", required = false) String tags,
                                       @RequestParam(value = "category", required = false) String category,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam("uploadId") String uploadId,
                                       @RequestParam("url") String url) {

        double durationMinutes = (double) redisTemplateObj.opsForHash().get("upload:" + uploadId,"duration");
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // 构建插入视频的参数
        Map<String, Object> videoParams = new HashMap<>();
        videoParams.put("id", SnowflakeIdUtil.videoIdWorker.nextId());
        videoParams.put("userId", userId);
        videoParams.put("title", title);
        videoParams.put("url", url);
        videoParams.put("duration", durationMinutes);

        // 添加可选参数
        if (tags != null && !tags.isEmpty()) {
            videoParams.put("tags", tags);
        }
        if (category != null && !category.isEmpty()) {
            videoParams.put("category", category);
        }
        if (description != null && !description.isEmpty()) {
            videoParams.put("description", description);
        }

        // 调用service插入视频记录
        videoService.insertVideo(videoParams);
        // 删除redis中保存的上传信息
        redisTemplateObj.delete("upload:" + uploadId);

        return new ResponseHandler(ResponseHandler.SUCCESS, "视频上传成功");
    }

    /**
     * 我的设计思路是前端通过调用任何除了/api/video/queryVideoDetail以外的其他接口,
     * 获取到的视频列表数据是不包括这些视频的URL,tags,description,我是想要懒加载和节省带宽
     * 所以只有用户在真正想要看某个视频的时候,前端需要通过/api/video/queryVideoDetail接口获取视频的URL码观看
     * 同时获取该视频的tags,description
     *
     * 所以这个视频用于从视频列表中获取某个视频的详细信息,用于观看视频，
     * 这个接口只能用于查询非
     */

    @GetMapping("/api/video/queryVideoDetail")
    public ResponseHandler queryVideoDetail(@RequestParam("id") long id) {
        Map videoInfo = videoService.queryVideoDetail(id);
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoInfo);
    }

    /**
     * 这个接口用于获取视频URL码，然后调用/api/file/delete接口,带上视频URL码和视频id删除视频
     */
    @GetMapping("/api/video/getVideoURL")
    public ResponseHandler getVideoURL(@RequestParam("id") long id) {
        String url = videoService.selectVideoURL(id);
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", url);
    }


    @GetMapping("/api/video/searchVideoByKeyWords")
    public ResponseHandler searchVideoByKeyWords(@RequestParam("keyWords") String keyWords) {
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.searchVideoByKeyWords(keyWords));
    }

    @GetMapping("/api/video/searchVideoByTime")
    public ResponseHandler searchVideoByYear(@RequestParam("startTime") String startTime,
                                            @RequestParam("endTime") String endTime) {

        LocalDateTime startDateTime= null;
        LocalDateTime endDateTime= null;
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            // 将字符串转换为LocalDate
            LocalDate startDate = LocalDate.parse(startTime, formatter);
            LocalDate endDate = LocalDate.parse(endTime, formatter);

            // 后续需要 LocalDateTime，结合默认时间进行转换
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atStartOfDay();
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseHandler(ResponseHandler.BAD_REQUEST, "日期格式错误");
        }

        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.searchVideoByTime(startDateTime,endDateTime));
    }

    /**
     * 当作者要查询自己的视频列表，哪些过审了，哪些在等待过审，哪些没有过审
     * 如果遇到上述任何一种情况，当遇到查询视频详细信息的时候，必须只能用这个接口查询
     *
     * /api/video/queryVideoDetail 接口和它不一样，如果是正常在视频主页看视频，查询视频详情
     * 就用这个接口
     *
     * 当如果是在用户主页查询自己视频的过审情况，则只能用/api/video/querySelfVideoDetail 查询自己视频的详情
     */
    @GetMapping("/api/video/querySelfVideoDetail")
    public ResponseHandler querySelfVideoDetail(@RequestParam("id") long id) {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Map<String, Long> map= new HashMap();
        map.put("id",id);
        map.put("userId",userId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.querySelfVideoDetail(map));
    }

    @GetMapping("/api/video/selectSelfVideoWaitToReview")
    public ResponseHandler selectSelfVideoWaitToReview() {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.selectSelfVideoWaitToReview(userId));
    }

    @GetMapping("/api/video/selectSelfApprovedVideo")
    public ResponseHandler selectSelfApprovedVideo() {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.selectSelfApprovedVideo(userId));
    }

    @GetMapping("/api/video/selectSelfRejectedVideo")
    public ResponseHandler selectSelfRejectedVideo() {
        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.selectSelfRejectedVideo(userId));
    }

    @GetMapping("/api/video/selectAllVideoWaitToReview")
    public ResponseHandler selectAllVideoWaitToReview() {
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.selectAllVideoWaitToReview());
    }

    @PutMapping("/api/video/updateVideoReviewStatus")
    public ResponseHandler updateVideoReviewStatus(@RequestParam("id") long id,
                                                  @RequestParam("status") String status,
                                                  @RequestParam("reviewNotes") String reviewNotes) {
        long reviewerId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        videoService.updateVideoReviewStatus(id, status, reviewerId, reviewNotes);
        return new ResponseHandler(ResponseHandler.SUCCESS, "更新成功");
    }

    /**
     * 获取最新的视频列表,最多100条
     */
    @GetMapping("/api/video/queryLatestVideos")
    public ResponseHandler queryLatestVideos() {
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.queryLatestVideos());
    }

    /**
     * 获取播放量最高的视频列表,最多100条
     */
    @GetMapping("/api/video/queryMostViewedVideos")
    public ResponseHandler queryMostViewedVideos() {
        return new ResponseHandler(ResponseHandler.SUCCESS, "查询成功", videoService.queryMostViewedVideos());
    }
}
