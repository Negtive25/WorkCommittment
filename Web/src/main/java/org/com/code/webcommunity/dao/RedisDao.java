package org.com.code.webcommunity.dao;

import org.com.code.webcommunity.pojo.Articles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RedisDao {
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    //获取某篇文章点赞数
    public int getLikesCount(int articleId){
        Double likesCount = redisTemplate.opsForZSet().score("article_likes",articleId);
        return likesCount.intValue();
    }


    //传入从数据库中查询到的很多文章的列表
    //根据列表的批量的文章各自对应id，从redis中获取对应的批量的点赞数
    //由于批量的文章id是随机的，不能通过zrange来根据数据范围获取对应某个范围的点赞数
    //为了性能，不能一次一次查询redis数据库，所以Redis Pipeline将需要查询的命令id一次性发送给redis查询
    //查询完后则把最新的文章对应的点赞数赋值给文章对象，并返回这个新的文章列表
    public List<Articles> queryRedisToUpdateLikeCountForArticleList(List<Articles> articlesList){

        //从数据库中查询到的很多文章的列表提取批量的文章id
        List<String> articleIds = articlesList.stream()
                .map(Articles::getId)
                .map(String::valueOf)
                .collect(Collectors.toList());
        
        //通过Redis Pipeline将多个命令一次性发送给Redis执行,获取对应的批量文章的各自的点赞数
        List<Object> LikesCounts = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize("article_likes");
                for (String articleId : articleIds) {
                    byte[] articleIdBytes = redisTemplate.getStringSerializer().serialize(articleId);
                    connection.zScore(key, articleIdBytes); // 发送ZSCORE命令
                }
                // 返回值被忽略，结果通过管道收集
                return null;
            }
        });
        //将点赞数转换为Integer类型
        List<Integer> result = LikesCounts.stream()
                                .map(likesCount -> (Double) likesCount)
                                .map(Double::intValue)
                                .collect(Collectors.toList());
        
        //遍历articlesList，将result的点赞数依次赋值给articlesList的每个对象
        for (int i = 0; i < articlesList.size(); i++) {
            articlesList.get(i).setLikeCount(result.get(i));
        }
        return articlesList;
    }

    public boolean checkIfExists(String key, String field, String value) {
        Object val = redisTemplate.opsForHash().get(key, field);
        if (val == null) {
            return false;
        }
        return value.equals(val.toString());
    }

    //给文章点赞的方法
    public void articleLikeIncrease(int articleId,int  userId){

        String field=articleId+"";
        String value=userId+"";

        //article_likes为文章点赞数，user_likes_为用户点赞过的文章
        //举例子，user_likes_1表格的value值为用户1点赞过的文章id，score值为1但无含义
        //article_likes表格的value值为文章id，score值为点赞数

        //因为是点赞的方法，所以往Redis里增加用户对这篇文章的点赞记录
        Boolean added =redisTemplate.opsForZSet().add("user_likes_"+userId,articleId,1);

        //如果用户没有点赞过，added为true，则增加文章点赞数，否则不增加点赞数
        if(added){
            redisTemplate.opsForZSet().incrementScore("article_likes",articleId,1);
            //记录增操作，和记录下来的删操作做对比，如果是对同一组数据的增删操作，则两个操作抵消掉
            //如果不是是对同一组数据的增删操作，则继续记录增操作
            if (checkIfExists("delete", field, value))
                redisTemplate.opsForHash().delete("delete", field, value);
            else
                redisTemplate.opsForHash().put("add",field, value);
        }
    }

    //取消点赞的方法
    public void articleLikeDecrease(int articleId,int userId){

        //Redis的哈希表的key, field , value 都为 String类型，所以这里需要把userId和articleId转为String类型!!!!
        //要不然类型强转转化会报错
        String field=articleId+"";
        String value=userId+"";

        //开始，因为是取消点赞，所以把redis里面的用户对应某个文章的点赞记录删除掉
        Long deleted = redisTemplate.opsForZSet().remove("user_likes_"+userId,articleId);

        //如果删除成功，则把文章点赞数减一
        //如果删除失败，则说明用户没有点赞过，则不减一
        if(deleted>0){
            redisTemplate.opsForZSet().incrementScore("article_likes",articleId,-1);
            //记录删操作，和记录下来的增操作做对比，如果是对同一组数据的增删操作，则两个操作抵消掉
            //如果不是是对同一组数据的增删操作，则继续记录删操作
            if(checkIfExists("add", field, value))
                redisTemplate.opsForHash().delete("add", field, value);
            else
                redisTemplate.opsForHash().put("delete", field, value);
        }
    }

    //获取用户点赞过的文章
    public Set<Integer> selectLikedArticlesOfUser(int userId){
        return redisTemplate.opsForZSet().range("user_likes_"+userId, 0, -1);
    }

    //获取所有文章的id
    public Set<Integer> getAllArticleIds() {
        return redisTemplate.opsForZSet().range("article_likes", 0, -1);
    }

    public boolean checkIfUserLikesTheArticle(int userId,int articleId){
        Double score = redisTemplate.opsForZSet().score("user_likes_" + userId, articleId);
        return score != null;
    }
}
