package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.Messages;

import java.util.List;

@Mapper
public interface MessageMapper {
    public void insertBatchMsg(List<Messages> messages);
    public List<Messages> queryUnreadMessages(List<Long> unreadMessageIds);
    public int deleteGroupMessages(long sessionId);
}
