package org.com.code.im.ElastiSearch.Service;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ElasticUtil {
    @Autowired
    @Qualifier("node1")
    private RestHighLevelClient client;

    public void createIndex(Map data, String indexName) throws IOException {
        LocalDateTime createdAt = (LocalDateTime) data.get("createdAt");
        // 解析并截断到秒级，存储为字符串以避免JSON序列化时的格式问题
        String truncatedTime = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        data.put("createdAt", truncatedTime);

        IndexRequest request = new IndexRequest(indexName).id(data.get("id").toString());
        request.source(data, XContentType.JSON);

        client.index(request, RequestOptions.DEFAULT);
    }

    public SearchRequest getSearchRequest(String startTime, String endTime, int page, int size,String indexName,SortOrder sortOrder) {
        LocalDate startDate = LocalDate.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 转换为LocalDateTime并格式化为字符串，与ES中存储的格式保持一致
        String startDateTime = startDate.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String endDateTime = endDate.atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source().query(QueryBuilders.rangeQuery("createdAt").gte(startDateTime).lte(endDateTime))
                .sort("createdAt", sortOrder).from(page * size).size(size);
        return searchRequest;
    }

    public List<Long> getIds(SearchRequest searchRequest) throws IOException {
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        List<Long> ids = new ArrayList<>();
        for  (SearchHit hit : hits.getHits()){
            ids.add(Long.parseLong(hit.getId()));
        }
        return ids;
    }
}
