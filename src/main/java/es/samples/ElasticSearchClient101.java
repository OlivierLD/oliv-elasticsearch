package es.samples;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

import java.util.Iterator;
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

            System.out.println("-- 20 retrieved --");

            // Create document
            esClient.index(new IndexRequest("test-cases").id("40").source("{ \"suite\": 1, \"id\": 40, \"name\": \"Created from Java\", \"value\": \"I want a pizza with pickles\" }", XContentType.JSON), RequestOptions.DEFAULT);
            System.out.println("-- 40 Created --");

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

            // Trying SQL query
            RestClient lowLevelClient = esClient.getLowLevelClient();
            Request sqlRequest = new Request("POST", "/_sql"); // /translate");
            JSONObject json = new JSONObject();
            json.put("query", "SELECT * FROM \"test-cases\" WHERE suite = 1 "); // double-quotes around the index, because of the '-' in the name
//            String jsonPayload = "{ \"query\": \"SELECT suite, name, id, value FROM \\\"test-cases\\\" \" }";
//            String jsonPayload = "{ \"query\": \"SELECT * FROM \\\"test-cases\\\" \" }";
            String jsonPayload = json.toString();
            sqlRequest.setJsonEntity(jsonPayload);
            Response sqlResponse = lowLevelClient.performRequest(sqlRequest);
            // Use Jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(sqlResponse.getEntity().getContent());

            ArrayNode columns = (ArrayNode)tree.get("columns");
            ArrayNode rows = (ArrayNode)tree.get("rows");

            // Columns
            System.out.println("-- SQL Query, columns --");
            columns.elements().forEachRemaining(jsonNode -> {
                System.out.println(String.format("%s, as %s", jsonNode.get("name").asText(), jsonNode.get("type").asText()));
            });
            // Rows
            System.out.println("-- SQL Query, rows --");
            Iterator<JsonNode> elements = rows.elements();
            elements.forEachRemaining(jsonNode -> {
                System.out.println(jsonNode.toString());
            });
            lowLevelClient.close();

            // Done
            esClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
