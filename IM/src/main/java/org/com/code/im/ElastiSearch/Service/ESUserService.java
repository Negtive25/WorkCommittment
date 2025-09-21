package org.com.code.im.ElastiSearch.Service;

import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

public interface ESUserService {
    void createUserIndex(Map userMap);
    /**
     * 删除帖子索引
     */
    void deleteUserIndex(Long userId);
    /**
     * 更新帖子索引中的文档
     */
    void updateUserIndex(Map userMap);

    /**
     *  根据用户名模糊搜索用户
     */
    List<Long> searchUserByName(String userName, int page, int size);
}
