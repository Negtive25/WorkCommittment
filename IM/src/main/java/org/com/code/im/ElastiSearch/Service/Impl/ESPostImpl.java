package org.com.code.im.ElastiSearch.Service.Impl;

import com.alibaba.fastjson.JSON;
import org.com.code.im.ElastiSearch.Service.ElasticUtil;
import org.com.code.im.utils.TimeConverter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import jakarta.annotation.PreDestroy;
import org.apache.http.client.config.RequestConfig;
import org.com.code.im.ElastiSearch.Service.ESPostService;
import org.com.code.im.config.ElasticConfig;
import org.com.code.im.exception.ElasticSearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ESPostImpl implements ESPostService {
    @Autowired
    @Qualifier("node1")
    private RestHighLevelClient client;
    @Autowired
    private ElasticUtil elasticUtil;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Override
    public void createPostIndexDelayed(Map postData) {
        scheduler.schedule(() -> {
            try {
                elasticUtil.createIndex(postData, ElasticConfig.POST_INDEX);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ElasticSearchException("创建ES帖子索引失败");
            }
        }, 3, TimeUnit.MINUTES);
    }



    @PreDestroy
    public void shutdownScheduler() throws IOException {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public void deletePostIndex(Long PostId) {
        try {
            DeleteRequest request = new DeleteRequest(ElasticConfig.POST_INDEX, PostId.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ElasticSearchException("删除ES帖子索引失败");
        }
    }

    @Override
    public void updatePostIndex(Map PostData){
        try {
           UpdateRequest request = new UpdateRequest(ElasticConfig.POST_INDEX, PostData.get("id").toString());

           Map<String,Object> map=new HashMap<>();
           map.put("title",PostData.get("title"));
           map.put("content",PostData.get("content"));
           map.put("tags",PostData.get("tags"));
           request.doc(map);

            client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ElasticSearchException("更新ES帖子索引失败");
        }
    }
    @Override
    public List<Long> searchPostByKeyWords(String keyword,int page,int size) {
        try {
            SearchRequest searchRequest = new SearchRequest(ElasticConfig.POST_INDEX);
            searchRequest.source().query(
                    QueryBuilders.multiMatchQuery(keyword, "title", "content", "tags")
            ).sort("createdAt",SortOrder.DESC).from(page*size).size(size);

           return elasticUtil.getIds(searchRequest);
        }catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("搜索帖子失败");
        }
    }

    @Override
    public List<Long> searchPostsByTime(String startTime, String endTime, int page, int size) {
        try {
           SearchRequest searchRequest =
                   elasticUtil.getSearchRequest(startTime,endTime,page,size,ElasticConfig.POST_INDEX,SortOrder.DESC);

            return elasticUtil.getIds(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("搜索帖子失败");
        }
    }


}
