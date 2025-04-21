package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.User;
import org.com.code.im.pojo.UserFollowing;
import org.com.code.im.pojo.userNameAndAvatar;


import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    public User selectUserById(long userId);

    public String getAuth(long userId);

    public int insertUser(User user);

    public Long selectUserIdByName(String userName);

    public Long updateUser(Map<String,Object> map);

    /**
     *  用于SpringSecurity的校验
     */
    public User findUserByName(String userName);

    public String selectUserNameById(long userId);

    public List<String> queryUserNameByManyIds(List<Long> ids);

    public String selectAvatarById(long userId);

    public List<userNameAndAvatar> selectNameAndAvatarByIds(List<Long> ids);

    public List<UserFollowing> queryUserListByManyIds(Map map);
}

