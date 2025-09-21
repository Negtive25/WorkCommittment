package org.com.code.im.service;

import org.com.code.im.pojo.Blocks;
import org.springframework.stereotype.Service;

import java.util.List;

public interface BlockService {
    void insertBlock(long userId,long targetUserId);
    int cancelBlock(long userId,long targetUserId);
    List<Blocks> queryBlockedUserList(long userId);
}
