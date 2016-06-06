package service.impl;

import service.UrlShortenerService;
import service.controller.ElasticsearchController;

public class UrlShortenerServiceImpl implements UrlShortenerService {

  private final ElasticsearchController elasticsearchController = ElasticsearchController.getInstance();

  @Override
  public void createIndex(String indexName, boolean deleteOldIndexIfExists) {
    try {
      elasticsearchController.createIndex(indexName, deleteOldIndexIfExists);
    } catch (Exception e) {
      System.err.println("Error in createIndex");
    }
  }

  @Override
  public String shortenUrl(String originalUrl) {
    try {
      return elasticsearchController.generateUrl(originalUrl);
    } catch (Exception e) {
      System.err.println("Error in shortenUrl");
      return null;
    }
  }

  @Override
  public String expandUrl(String shortenedUrl) {
    try {
      return elasticsearchController.expandUrl(shortenedUrl);
    } catch (Exception e) {
      System.err.println("Error in expandUrl");
      return null;
    }
  }
}
