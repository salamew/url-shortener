package service.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import model.ShortUrl;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import utils.AppConstants;
import elasticsearch.ElasticsearchUnavailableException;
import elasticsearch.TransportClientFactory;

public class ElasticsearchController {
  private final static String HOST_ADRESS = "127.0.0.1";
  private final static int PORT = 9300;
  private static Client client;
  private final static ElasticsearchController instance = new ElasticsearchController();
  static {
    try {
      client = new TransportClientFactory(HOST_ADRESS, PORT).build();
    } catch (ElasticsearchUnavailableException e) {
      System.err.println("Error in initClient\n" + e.toString());
    }
  }

  private ElasticsearchController() {

  }

  public static ElasticsearchController getInstance() {
    return instance;
  }

  public Client getClient() {
    return client;
  }

  public void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException {
    final IndicesExistsResponse res = client.admin().indices().prepareExists(indexName).execute().actionGet();
    if (res.isExists() && deleteOldIndexIfExists) {
      deleteIndex(indexName);
    }

    if (!res.isExists() || (res.isExists() && deleteOldIndexIfExists)) {
      XContentBuilder settingsBuilder = null;
      settingsBuilder = XContentFactory.jsonBuilder().startObject();
      settingsBuilder.startObject("index").field("number_of_shards", 1).field("number_of_replicas", 1);
      settingsBuilder.endObject();
      CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName).setSettings(settingsBuilder.string());

      CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
      if (!response.isAcknowledged()) {
        System.err.println("Could not create index");
        throw new RuntimeException();
      }
    }
  }

  private void deleteIndex(String indexName) {
    DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(indexName).execute().actionGet();
    if (!deleteIndexResponse.isAcknowledged()) {
      System.err.println("Could not delete index");
      throw new RuntimeException();
    }
  }

  public String generateUrl(String originalUrl) throws JsonProcessingException {
    String shortenedUrl = buildShortenedUrl(originalUrl);

    String documentAsJson = toJson(buildShortenedUrlDocument(originalUrl, shortenedUrl));
    client.prepareIndex(AppConstants.INDEX_NAME, AppConstants.TYPE_NAME, shortenedUrl).setSource(documentAsJson).execute();

    return shortenedUrl;
  }

  private String buildShortenedUrl(String originalUrl) {
    return Hashing.murmur3_32().hashString(originalUrl, java.nio.charset.Charset.defaultCharset()).toString();
  }

  public String expandUrl(String shortenedUrl) {
    if (shortenedUrl.length() < AppConstants.URL_PREFIX.length()) {
      return null;
    }
    String shortenedUniqueId = shortenedUrl.substring(AppConstants.URL_PREFIX.length()); // User will enter the url with http://cl.ip/ at the beginning

    GetResponse getResponse = getDocumentGetResponse(AppConstants.INDEX_NAME, AppConstants.TYPE_NAME, shortenedUniqueId);
    if (getResponse.isExists()) {
      return getResponse.getSourceAsMap().get(ShortUrl.Field.ORIGINAL_URL.getJson()).toString();
    } else {
      return null;
    }
  }

  private GetResponse getDocumentGetResponse(String indexName, String type, String id) {
    return client.prepareGet(indexName, type, id).execute().actionGet();
  }

  private static Map<String, Object> buildShortenedUrlDocument(String originalUrl, String shortenedUrl) {
    Map<String, Object> document = new HashMap<>();
    document.put(ShortUrl.Field.ORIGINAL_URL.getJson(), originalUrl);
    document.put(ShortUrl.Field.SHORT_URL.toString(), shortenedUrl);

    return document;
  }

  public static String toJson(Map<String, Object> document) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(document);
  }
}
