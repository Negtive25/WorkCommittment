package org.com.code.im.ElastiSearch.Service.Impl;

import jakarta.annotation.PreDestroy;
import org.com.code.im.ElastiSearch.Service.ESVideoService;
import org.com.code.im.ElastiSearch.Service.ElasticUtil;
import org.com.code.im.config.ElasticConfig;
import org.com.code.im.exception.ElasticSearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ESVideoImpl implements ESVideoService {
    @Autowired
    @Qualifier("node1")
    private RestHighLevelClient client;
    @Autowired
     private ElasticUtil elasticUtil;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Override
    public void createVideoIndexDelayed(Map videoData) {
        scheduler.schedule(() -> {
            try {
                elasticUtil.createIndex(videoData, ElasticConfig.VIDEO_INDEX);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ElasticSearchException("创建ES视频索引失败");
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
    public void deleteVideoIndex(Long videoId) {
        try {
            DeleteRequest request = new DeleteRequest(ElasticConfig.VIDEO_INDEX, videoId.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ElasticSearchException("删除ES视频索引失败");
        }
    }

    @Override
    public List<Long> searchVideoByKeyWords(String keyword, int page, int size) {
        try {
            SearchRequest searchRequest = new SearchRequest(ElasticConfig.VIDEO_INDEX);
            searchRequest.source().query(
                    QueryBuilders.multiMatchQuery(keyword, "title", "description", "tags", "category")
            ).sort("createdAt", SortOrder.DESC).from(page*size).size(size);

            return elasticUtil.getIds(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("搜索视频失败");
        }
    }

    @Override
    public List<Long> searchVideosByTime(String startTime, String endTime, int page, int size) {
        try {
            SearchRequest searchRequest =
                    elasticUtil.getSearchRequest(startTime, endTime, page, size,ElasticConfig.VIDEO_INDEX,SortOrder.DESC);
            return elasticUtil.getIds(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("搜索视频失败");
        }
    }
}
