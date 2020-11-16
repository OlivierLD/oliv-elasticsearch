package es.samples;

import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Map;

/**
 * See https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-initialization.html
 *
 * Uses the data manipulated by insert-master.sh
 */
public class ElasticSearchClient101 {

    private final static String ELASTIC_SEARCH_INSTANCE = "localhost:9200";

    public static void main(String... args) {
        try {
            String elasticsearchInstance = System.getProperty("elastic.search.instance", ELASTIC_SEARCH_INSTANCE);
            String host = elasticsearchInstance.substring(0, elasticsearchInstance.indexOf(":"));
            int port = Integer.parseInt(elasticsearchInstance.substring(elasticsearchInstance.indexOf(":") + 1));

            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
//            org.elasticsearch.action.search.SearchRequestBuilder requestBuilder = client.prepareSearch(indexName);

            // Get Source
            GetSourceRequest request = new GetSourceRequest("test-cases", "20");
            GetSourceResponse response = client.getSource(request, RequestOptions.DEFAULT);

            Map<String, Object> source = response.getSource();

            source.forEach((key, value) -> System.out.println(String.format("%s: %s", key, value)));

            System.out.println("---------------------");

            // Search request
            SearchRequest searchRequest = new SearchRequest("test-cases");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.sort("id", SortOrder.DESC); // a sort!
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            RestStatus status = searchResponse.status();
            System.out.println(String.format("Search Request status: %d", status.getStatus()));

            SearchHits hits = searchResponse.getHits();
            TotalHits totalHits = hits.getTotalHits();
            System.out.println(String.format("Nb hits: %d", totalHits.value));
            System.out.println("---- S O R T E D ----");

            for (SearchHit hit : hits) {
                // do something with the SearchHit
                // System.out.println(hit);
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                sourceAsMap.forEach((key, value) -> System.out.println(String.format("%s: %s", key, value)));
                System.out.println("---------------------");
            }

            // Done
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
