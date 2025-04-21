package org.com.code.im.service;

import org.com.code.im.pojo.Blocks;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BlockService {
    public void insertBlock(long userId,long targetUserId);
    public int cancelBlock(long userId,long targetUserId);
    public List<Blocks> queryBlockedUserList(long userId);
}
