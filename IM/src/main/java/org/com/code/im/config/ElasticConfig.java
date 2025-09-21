package org.com.code.im.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticConfig {

    public static final String VIDEO_INDEX = "video";
    public static final String POST_INDEX = "post";
    public static final String USER_INDEX = "user";

    @Value("${spring.elasticsearch.host}")
    String hostAddress;
    private RestHighLevelClient restHighLevelClient;
    
    @Bean("node1")
    public RestHighLevelClient createRestClient() {
        restHighLevelClient= new RestHighLevelClient(RestClient.builder(
                HttpHost.create(hostAddress)
        ));
        return restHighLevelClient;
    }
    @PreDestroy
    public void shutdownScheduler() throws IOException {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndices() throws IOException {
        createIndexIfNotExists(VIDEO_INDEX, VIDEO_MAPPING);
        createIndexIfNotExists(POST_INDEX, POST_MAPPING);
        createIndexIfNotExists(USER_INDEX, USER_MAPPING);
        System.out.println("Indices initialized.");
    }

    public void createIndexIfNotExists(String indexName, String mapping) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        if(!restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT)){
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            createIndexRequest.source(mapping, XContentType.JSON);
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
    }

    public static final String VIDEO_MAPPING="{\n" +
            "  \"settings\": {\n" +
            "    \"number_of_shards\": 1,\n" +
            "    \"number_of_replicas\": 0,\n" +
            "    \"analysis\": {\n" +
            "      \"analyzer\": {\n" +
            "        \"ik_analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"ik_max_word\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"title\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\",\n" +
            "        \"fields\": {\n" +
            "          \"keyword\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"tags\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"category\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"description\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"createdAt\": {\n" +
            "        \"type\": \"date\",\n" +
            "        \"format\": \"yyyy-MM-dd'T'HH:mm:ss\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String POST_MAPPING="{\n" +
            "  \"settings\": {\n" +
            "    \"number_of_shards\": 1,\n" +
            "    \"number_of_replicas\": 0,\n" +
            "    \"analysis\": {\n" +
            "      \"analyzer\": {\n" +
            "        \"ik_analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"ik_max_word\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"title\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\",\n" +
            "        \"fields\": {\n" +
            "          \"keyword\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"tags\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"content\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"createdAt\": {\n" +
            "        \"type\": \"date\",\n" +
            "        \"format\": \"yyyy-MM-dd'T'HH:mm:ss\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String USER_MAPPING="{\n" +
            "  \"settings\": {\n" +
            "    \"number_of_shards\": 1,\n" +
            "    \"number_of_replicas\": 0,\n" +
            "    \"analysis\": {\n" +
            "      \"analyzer\": {\n" +
            "        \"ik_analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"ik_max_word\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"userName\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"bio\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"createdAt\": {\n" +
            "        \"type\": \"date\",\n" +
            "        \"format\": \"yyyy-MM-dd'T'HH:mm:ss\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
}
