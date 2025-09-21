package org.com.code.im.ElastiSearch.Service.Impl;

import org.com.code.im.ElastiSearch.Service.ESUserService;
import org.com.code.im.ElastiSearch.Service.ElasticUtil;
import org.com.code.im.config.ElasticConfig;
import org.com.code.im.exception.ElasticSearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ESUserImpl implements ESUserService {

    @Autowired
    @Qualifier("node1")
    private RestHighLevelClient client;
    @Autowired
    private ElasticUtil elasticUtil;

    @Override
    public void createUserIndex(Map userMap) {
        try {
            elasticUtil.createIndex(userMap, ElasticConfig.USER_INDEX);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("创建ES用户索引失败"+e.getMessage());
        }
    }

    @Override
    public void deleteUserIndex(Long userId) {
        try {
            DeleteRequest request = new DeleteRequest(ElasticConfig.USER_INDEX, userId.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ElasticSearchException("删除ES视频索引失败");
        }
    }

    @Override
    public void updateUserIndex(Map userMap) {
        try {
            UpdateRequest request = new UpdateRequest(ElasticConfig.POST_INDEX, userMap.get("id").toString());

            Map<String,Object> map=new HashMap<>();
            map.put("userName",userMap.get("userName"));
            map.put("bio",userMap.get("bio"));
            request.doc(map);

            client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ElasticSearchException("更新ES用户索引失败");
        }
    }

    @Override
    public List<Long> searchUserByName(String userName, int page, int size) {
        try {
            SearchRequest searchRequest = new SearchRequest(ElasticConfig.USER_INDEX);
            searchRequest.source().query(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.termQuery("userName", userName))
                            .should(QueryBuilders.prefixQuery("userName", userName))
            ).sort("createdAt", SortOrder.DESC).from(page*size).size(size);

            return elasticUtil.getIds(searchRequest);
        }catch (Exception e) {
            e.printStackTrace();
            throw new ElasticSearchException("搜索帖子失败");
        }
    }
}
