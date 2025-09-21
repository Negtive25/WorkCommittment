package org.com.code.im.ElastiSearch.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ESPostService {
    /**
     * 延迟创建帖子索引
     */
    void createPostIndexDelayed(Map postData);
    /**
     * 删除帖子索引
     */
    void deletePostIndex(Long postId);
    /**
     * 更新帖子索引中的文档
     */
    void updatePostIndex(Map postData);

    /**
     * 关键词搜索帖子
     */
    List<Long> searchPostByKeyWords(String keyword,int page,int size);

    /**
     * 根据时间搜索帖子
     */
    List<Long> searchPostsByTime(String startTime, String endTime, int page, int size);
}
