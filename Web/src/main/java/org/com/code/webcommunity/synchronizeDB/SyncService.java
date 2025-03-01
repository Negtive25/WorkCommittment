package org.com.code.webcommunity.synchronizeDB;


import org.com.code.webcommunity.dao.ArticleDao;
import org.com.code.webcommunity.dao.ArticleLikesDao;
import org.com.code.webcommunity.dao.RedisDao;
import org.com.code.webcommunity.pojo.ArticleLikes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;


@Service
public class SyncService {
    @Autowired
    private RedisDao redisDao;
    @Autowired
    private ArticleLikesDao articleLikesDao;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @Autowired
    private ArticleDao articleDao;


    //每隔3分钟同步一次Redis中的点赞记录到ArticleLikes表和Article表
    @Scheduled(fixedRate = 180000)
    public void syncLikesCountToRedis() {
        Map<Object, Object> entriesAdd = redisTemplate.opsForHash().entries("add");
        Map<Object, Object> entriesDelete = redisTemplate.opsForHash().entries("delete");

        //记得把add和delete中的记录清空，否则会重复同步
        for (Map.Entry<Object, Object> entry : entriesAdd.entrySet()) {
            redisTemplate.opsForHash().delete("add", entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Object, Object> entry : entriesDelete.entrySet()) {
            redisTemplate.opsForHash().delete("delete", entry.getKey(), entry.getValue());
        }

        //更新ArticleLikes表的操作，把Redis中的点赞记录同步到ArticleLikes表
        //先插入点赞记录
        for (Map.Entry<Object, Object> entry : entriesAdd.entrySet()) {
            String articleIdStr = (String) entry.getKey();
            String userIdStr = (String) entry.getValue();

            int userId = Integer.parseInt(userIdStr);
            int articleId = Integer.parseInt(articleIdStr);

            //这里根据 add 的操作记录，直接mysql插入数据
            ArticleLikes articleLikes = new ArticleLikes(userId, articleId);
            articleLikesDao.insertArticleLike(articleLikes);

            //因为任何点赞或者取消赞的动作都是第一时间发生在Redis中的，所以Redis的记录永远是最新的
            //更新Article表的操作，把Redis中的点赞记录同步到Article表
            int likesCount = redisDao.getLikesCount(articleId);
            articleDao.setArticleLikes(likesCount, articleId);
        }
        //再删除点赞记录
        for (Map.Entry<Object, Object> entry : entriesDelete.entrySet()) {
            String articleIdStr = (String) entry.getKey();
            String userIdStr = (String) entry.getValue();

            int userId = Integer.parseInt(userIdStr);
            int articleId = Integer.parseInt(articleIdStr);

            ArticleLikes articleLikes = new ArticleLikes(userId, articleId);
            articleLikesDao.deleteArticleLike(articleLikes);

            //因为任何点赞或者取消赞的动作都是第一时间发生在Redis中的，所以Redis的记录永远是最新的
            //更新Article表的操作，把Redis中的点赞记录同步到Article表
            int likesCount = redisDao.getLikesCount(articleId);
            articleDao.setArticleLikes(likesCount, articleId);
        }

    }
}
