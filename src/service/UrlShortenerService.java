package service;

public interface UrlShortenerService {
  void createIndex(String indexName, boolean deleteOldIndexIfExists);

  String shortenUrl(String originalUrl);

  String expandUrl(String shortenedUrl);
}
