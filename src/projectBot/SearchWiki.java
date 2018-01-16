package projectBot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SearchWiki {
	private final String USER_AGENT = "Mozilla/5.0";
	
	public void searchWiki (String word) throws Exception {
		String urlToRead = "https://en.wikipedia.org/wiki/" + word;		
		System.out.print(urlToRead);
		StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      System.out.print(result.toString());
	}
}
