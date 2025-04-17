package org.com.code.im.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.im.pojo.Videos;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoLikeMapper {

    public void insertVideoLike(List<Map<String,Long>> addList);
    public boolean checkIfUserAlreadyGiveLike(Map map);
    public void deleteVideoLike(List<Map<String,Long>> deleteList);
    public List<Long> queryLikedVideoList(long userId);
}
