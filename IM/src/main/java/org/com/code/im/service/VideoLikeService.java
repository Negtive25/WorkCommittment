package org.com.code.im.service;

import org.com.code.im.pojo.Videos;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface VideoLikeService {

    public void insertVideoLike(long videoId,long userId);
    public void deleteVideoLike(long videoId,long userId);
    public List<Videos> queryLikedVideoList(long userId,int pageNum);
}
