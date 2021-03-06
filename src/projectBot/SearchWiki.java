package projectBot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SearchWiki {
	private final String USER_AGENT = "Mozilla/5.0";
	
	public void searchWiki (String word) throws Exception {
		String urlToRead = "https://en.wikipedia.org/wiki/" + word.toUpperCase();		
		/*System.out.print(urlToRead);
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
	    System.out.print(result.toString());*/
	 
	    URL urlObject = new URL(urlToRead);
        URLConnection urlConnection = urlObject.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");       
	    
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
	      
		String inputLine;
		StringBuilder stringBuilder = new StringBuilder();
		while ((inputLine = bufferedReader.readLine()) != null)
		{
			stringBuilder.append(inputLine);
		}
		
		System.out.print(stringBuilder.toString());
	}
}
