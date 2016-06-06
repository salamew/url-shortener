package urlclx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import service.UrlShortenerService;
import service.impl.UrlShortenerServiceImpl;
import utils.AppConstants;

public class UrlShortenerApp {

  private static UrlShortenerService urlShortenerService = new UrlShortenerServiceImpl();
  private final static BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

  public void start() throws IOException {
    urlShortenerService.createIndex(AppConstants.INDEX_NAME, false);

    showMenu();
  }

  private void showMenu() throws IOException {
    String text = null;

    Option pickedAction = null;
    while (pickedAction != Option.EXIT) {
      printMenu();

      text = console.readLine();

      pickedAction = Option.valueOfCustom(text);
      if (pickedAction != null) {
        pickedAction.execute();
      } else {
        System.out.println("Invalid choice");
      }
    }
  }

  private void printMenu() {
    System.out.println();
    System.out.println("Pick an action (enter the number) !");
    for (Option o : Option.values()) {
      System.out.println(o.getKey() + " - " + o.getTitle());
    }
    System.out.println();
  }

  public enum Option implements Action {
    SHORTEN("1", "Shorten that URL !") {
      @Override
      public void execute() {
        System.out.println("");
        System.out.println("Enter URL to shorten");
        try {
          String originalUrl = console.readLine();
          String shortenedUrl = urlShortenerService.shortenUrl(originalUrl);
          System.out.println("Shortened to --> " + AppConstants.URL_PREFIX + shortenedUrl);
        } catch (Exception e) {
          System.err.println("Error while shortened your URL !");
        }
      }
    },
    EXPAND("2", "Expand that URL !") {
      @Override
      public void execute() {
        System.out.println("");
        System.out.println("Enter URL to expand");
        try {
          String shortenedUrl = console.readLine();
          String originalUrl = urlShortenerService.expandUrl(shortenedUrl);
          if (originalUrl != null) {
            System.out.println("Expanded URL --> " + originalUrl);
          } else {
            System.out.println("Shortened URL not found !");
          }
        } catch (Exception e) {
          System.err.println("Error while expanding your URL !");
        }
      }
    },
    EXIT("3", "Exit program") {
      @Override
      public void execute() {
        System.out.println("Bye bye!");
      }
    };

    private String key;
    private String title;
    private static Map<String, Option> keyMap = new HashMap<>();
    static {
      for (Option o : Option.values()) {
        keyMap.put(o.key, o);
      }
    }

    private Option(String key, String title) {
      this.key = key;
      this.title = title;
    }

    public String getKey() {
      return this.key;
    }

    public String getTitle() {
      return this.title;
    }

    public static Option valueOfCustom(String key) {
      return keyMap.get(key);
    }
  }

  public interface Action {
    void execute();
  }
}
