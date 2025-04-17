package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.PrivateMembers;

@Mapper
public interface PrivateMemberMapper {
    public void insertPrivateMember(PrivateMembers privateMember);
}
