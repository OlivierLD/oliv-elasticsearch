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
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Map;

/**
 * See https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-initialization.html
 * Good sample: https://github.com/dadoonet/elasticsearch-java-client-demo/blob/master/src/test/java/fr/pilato/test/elasticsearch/hlclient/EsClientTest.java
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

            RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
//            org.elasticsearch.action.search.SearchRequestBuilder requestBuilder = client.prepareSearch(indexName);
            // Get version!
            MainResponse info = esClient.info(RequestOptions.DEFAULT);
            System.out.println("---- A D M I N -----");
            System.out.println(String.format("Version     : %s", info.getVersion().getNumber()));
            System.out.println(String.format("Cluster Name: %s", info.getClusterName()));
            System.out.println(String.format("Cluster UUID: %s", info.getClusterUuid()));
            System.out.println(String.format("Node Name   : %s", info.getNodeName()));
            System.out.println("------ host(s) -----");
            esClient.getLowLevelClient().getNodes().forEach(node -> System.out.println(String.format("%s://%s:%d",
                        node.getHost().getSchemeName(),
                        node.getHost().getHostName(),
                        node.getHost().getPort())));
            System.out.println("--------------------");

            // Get Source
            GetSourceRequest request = new GetSourceRequest("test-cases", "20");
            GetSourceResponse response = esClient.getSource(request, RequestOptions.DEFAULT);

            Map<String, Object> source = response.getSource();

            source.forEach((key, value) -> System.out.println(String.format("%s: %s", key, value)));

            System.out.println("---------------------");

            // Search request
            SearchRequest searchRequest = new SearchRequest("test-cases");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.sort("id", SortOrder.DESC); // a sort!
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
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
            esClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
