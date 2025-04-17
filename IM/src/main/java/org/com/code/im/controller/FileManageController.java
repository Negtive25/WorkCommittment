package org.com.code.im.controller;

import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.FileUploadService;
import org.com.code.im.service.VideoService;
import org.com.code.im.utils.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@RestController
public class FileManageController {

    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private OSSUtil ossUtil;

    /**
     * @param url
     * @param id
     * @return
     *
     * 这是视频,图片,其他文件的统一删除接口
     */
    @RequestMapping("/api/file/delete")
    public ResponseHandler deleteFile(@RequestParam("url") String url,
                                      @RequestParam(value = "id", required = false) long id) {
        String[] urlSplit =url.split("/");
        String fileType=urlSplit[0];
        String strUserId=urlSplit[2];
        /**
         * 删除之前判断该用户是否为该文件的所有者,如果不是作者，则不允许删除
         */
        if(!strUserId.equals(SecurityContextHolder.getContext().getAuthentication().getName()))
            return new ResponseHandler(ResponseHandler.ERROR,"你没有权限删除此文件,删除失败");

        if(fileType.equals("video")){
            /**
             * 1.如果删除作者的视频除了要URL码,还需要输入id号,然后删除数据库中的视频信息
             *
             * 2.如果删除其他非视频文件，则只需要输入url即可
             */
            long userId = Long.parseLong(strUserId);
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("userId", userId);
            videoService.deleteVideo(map);
        }

        ossUtil.deleteFile(url);
        return new ResponseHandler(ResponseHandler.SUCCESS,"文件删除成功");
    }

    /**
     * 初始化分段上传
     * @param fileName 文件名包含后缀,例如: 地平线4.mp4
     * @param fileType 文件类型,只有3种: video,image,file
     * @param fileSize 单位是Byte
     * @return
     *
     * 这个包括接下来的所有接口
     * 1.初始化分段上传
     * 2.上传分段
     * 3.完成分段上传
     * 4.取消上传
     * 5.获取上传进度
     *
     * 这些接口统一用于视频,图片 和 其他文件的通用接口,
     * 前端通过这些接口上传文件后,获取到文件的url
     * 1.如果是上传视频,则调用video的 插入数据库的接口
     * 2.如果是上传image或者其他文件,则在获取到URL码之后放到messages里面的消息content中,
     *   把消息设置成对应类别,image或者file,然后发送消息
     *   因为这些文件主要用于主要用途是内容传递，不作为独立资源,所以不需要插入到数据库
     *
     *   1.文件的作者可以通过调用/api/file/delete接口主动撤回(即删除)这些文件
     *   2.这些非视频文件在OSS上会被设置过期日期,每隔一段时间会被自动删除,
     *     比如图片7天后自动删除,其他非视频文件14天后自动删除
     *
     *      ****************************************************************************
     *      * 这些自动删除文件的操作的实现是在阿里云的 OSS 控制台手动配置不同前缀的文件自动过期策略 *
     *      ****************************************************************************
     *
     *
     */
    @PostMapping("/api/file/initMultipartUpload")
    public ResponseHandler initMultipartUpload(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileType") String fileType,
            @RequestParam("fileSize") long fileSize) {

        Map uploadInfo = fileUploadService.initMultipartUpload(fileName, fileType,fileSize);
        return new ResponseHandler(ResponseHandler.SUCCESS, "初始化分段上传成功", uploadInfo);
    }

    // 上传分段
    @PostMapping("/api/file/uploadPart")
    public ResponseHandler uploadPart(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("partNumber") int partNumber,
            @RequestParam("file") MultipartFile file) throws Exception {

        /**
         * clientHash是客户端本地计算分段文件的哈希值,用于在分段文件上传后和云端OSS计算出的哈希值进行比较来校验,
         * 如果哈希值相等则文件完整传输了，否则文件传输失败
         */
        fileUploadService.uploadPart(uploadId, partNumber, file.getInputStream());
        return new ResponseHandler(ResponseHandler.SUCCESS, "分段上传成功");
    }

    // 完成分段上传
    @PostMapping("/api/file/completeMultipartUpload")
    public ResponseHandler completeMultipartUpload(@RequestParam("uploadId") String uploadId) {

        String fileUrl = fileUploadService.completeMultipartUpload(uploadId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "文件上传完成", fileUrl);
    }

    // 取消上传
    @DeleteMapping("/api/file/abortMultipartUpload")
    public ResponseHandler abortMultipartUpload(@RequestParam("uploadId") String uploadId) {

        fileUploadService.abortMultipartUpload(uploadId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "上传已取消");
    }

    // 获取上传进度
    @GetMapping("/api/file/uploadProgress")
    public ResponseHandler getUploadProgress(@RequestParam("uploadId") String uploadId) {

        Map progress = fileUploadService.getUploadProgress(uploadId);
        return new ResponseHandler(ResponseHandler.SUCCESS, "获取上传进度成功", progress);
    }
}
