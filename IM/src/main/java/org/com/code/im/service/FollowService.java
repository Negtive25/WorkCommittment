package org.com.code.im.service;

import org.com.code.im.pojo.FanListPageQuery;
import org.com.code.im.pojo.UserFanListResponse;
import org.com.code.im.pojo.UserFollowing;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FollowService {
    //userId指用户要关注的人的id
    public void insertFollow(long userId,long fanId);
    public void cancelFollow(long userId,long fanId);
    public UserFanListResponse queryFanList(FanListPageQuery fanListPageQuery);
    public List<UserFollowing> queryFollowList(long userId);
    public List<UserFollowing> queryFriendList(long userId);
}
