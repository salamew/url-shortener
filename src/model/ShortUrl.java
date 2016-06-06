package model;

public class ShortUrl {

  private String originalUrl;
  private String shortUrl;

  public String getOriginalUrl() {
    return this.originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public String getShortUrl() {
    return this.shortUrl;
  }

  public void setShortUrl(String shortUrl) {
    this.shortUrl = shortUrl;
  }

  public enum Field {
    ORIGINAL_URL("ou"),
    SHORT_URL("su");

    private String json;

    private Field(String json) {
      this.json = json;
    }

    public String getJson() {
      return this.json;
    }

    @Override
    public String toString() {
      return getJson();
    }
  }
}