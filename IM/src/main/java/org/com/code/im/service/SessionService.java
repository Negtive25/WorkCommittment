package org.com.code.im.service;

import org.com.code.im.pojo.Sessions;
import org.springframework.stereotype.Service;

@Service
public interface SessionService {
    public Long createOrGetCurrentPrivateConversation(long ownerId,long userId);
    public Long createGroupChat(long ownerId,Long[] userIds, Sessions sessions);
    public Long addGroupMember(Long sessionId,Long[] userId);
    public String queryGroupRole(long sessionId,long userId);
    public void MuteOrCancelMuteGroupMember(long sessionId, long currentUserId, long targetUserId,String operationType);
    public void dismissGroup(long sessionId);
    public void kickOutGroupMember(long sessionId,long targetUserId);
    public void updateGroupInfo(Sessions sessions);
    public void updateMemberRole(long sessionId,long userId,String role);
}
