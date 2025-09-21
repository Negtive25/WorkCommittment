package org.com.code.im.service.Impl;

import org.apache.poi.ss.formula.functions.T;
import org.com.code.im.ElastiSearch.Service.ESPostService;
import org.com.code.im.ElastiSearch.Service.ESVideoService;
import org.com.code.im.exception.DatabaseException;
import org.com.code.im.mapper.LearningPlanMapper;
import org.com.code.im.mapper.LearningTaskMapper;
import org.com.code.im.mapper.PostMapper;
import org.com.code.im.mapper.VideoMapper;
import org.com.code.im.pojo.LearningPlan;
import org.com.code.im.pojo.LearningTask;
import org.com.code.im.pojo.Posts;
import org.com.code.im.pojo.Videos;
import org.com.code.im.pojo.Likeable;
import org.com.code.im.service.CustomRecommendService;
import org.com.code.im.service.UpdateLatestLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomRecommendImpl implements CustomRecommendService {
    @Autowired
    private LearningPlanMapper learningPlanMapper;
    @Autowired
    private LearningTaskMapper learningTaskMapper;
    @Autowired
    private ESPostService esPostService;
    @Autowired
    private ESVideoService esVideoService;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    @Qualifier("Posts")
    UpdateLatestLikeService updateLatestLikeService;
    @Autowired
    @Qualifier("Videos")
    UpdateLatestLikeService updateLatestVideoLikeService;

    /**
     * 根据用户学习计划和任务推荐帖子
     * 1.获取所有未完成的计划,
     * 2.获取计划底下任务,要求为isCompletedToday为false并且当前时间小于任务的targetDueDay(任务的截止时间)
     * 3.获取对应计划和任务的文本描述信息,将其拼接成一个字符串
     * 4.根据字符串使用ES搜索引擎搜索相关帖子
     * 5.返回帖子
     */
    @Override
    @Transactional
    public List<Posts> queryRecommendPost(long userId, int page, int size) {
        return queryRecommendContent(
            userId,
            (keywords) -> esPostService.searchPostByKeyWords(keywords, page, size),
            postMapper::selectPostListByManyIds,
            updateLatestLikeService,
            "查询推荐帖子失败"
        );
    }

    /**
     * 根据用户学习计划和任务推荐视频
     * 1.获取所有未完成的计划,
     * 2.获取计划底下任务,要求为isCompletedToday为false并且当前时间小于任务的targetDueDay(任务的截止时间)
     * 3.获取对应计划和任务的文本描述信息,将其拼接成一个字符串
     * 4.根据字符串使用ES搜索引擎搜索相关视频
     * 5.返回视频
     */
    @Override
    @Transactional
    public List<Videos> queryRecommendVideo(long userId, int page, int size) {
        return queryRecommendContent(
            userId,
            (keywords) -> esVideoService.searchVideoByKeyWords(keywords, page, size),
            videoMapper::selectVideoListByManyIds,
            updateLatestVideoLikeService,
            "查询推荐视频失败"
        );
    }

    /**
     * 通用的推荐内容查询方法
     * 通过函数式接口处理不同类型内容的搜索和查询逻辑
     */
    private <T extends Likeable> List<T> queryRecommendContent(
            long userId,
            Function<String, List<Long>> searchFunction,
            Function<List<Long>, List<Object>> mapperFunction,
            UpdateLatestLikeService likeService,
            String errorMessage) {
        try {
            // 获取所有未完成的计划
            List<LearningPlan> unCompletedPlanlanList = learningPlanMapper.findAllActivePlans(userId);

            if (unCompletedPlanlanList == null || unCompletedPlanlanList.isEmpty())
                return new ArrayList<>();

            // 构建搜索关键词
            StringJoiner joiner = new StringJoiner(" ");
            List<Long> planIds = unCompletedPlanlanList.stream()
                    .map(LearningPlan::getId)
                    .collect(Collectors.toList());
            
            for (LearningPlan plan : unCompletedPlanlanList)
                joiner.add(plan.getTitle()).add(plan.getGoal());

            List<LearningTask> taskList = learningTaskMapper.selectTodayOnGoingTask(userId, planIds);
            for (LearningTask task : taskList)
                joiner.add(task.getDescription());

            // 使用传入的搜索函数进行ES搜索
            List<Long> contentIds = searchFunction.apply(joiner.toString());

            if (contentIds == null || contentIds.isEmpty())
                return new ArrayList<>();

            // 使用传入的mapper函数获取对象列表
            List<Object> contentList = mapperFunction.apply(contentIds);

            // 使用传入的点赞服务更新点赞数并保持顺序
            return likeService.updateObjectLikeCountList(preserveOrder(contentList, contentIds));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(errorMessage);
        }
    }

    /**
     * 通用的保持ES查询结果顺序的排序方法
     * 适用于所有继承自Likeable的对象（Posts、Videos等）
     */
    private <T extends Likeable> List<T> preserveOrder(List<Object> objects, List<Long> orderedIds) {
        Map<Long, T> objectMap = objects.stream()
                .map(obj -> (T) obj)
                .collect(Collectors.toMap(T::getId, obj -> obj));
        
        return orderedIds.stream()
                .map(objectMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
