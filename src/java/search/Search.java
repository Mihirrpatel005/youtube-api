package java.search;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Prints a list of videos based on a search term.
 *
 * @author Mihir Patel
 */
@ManagedBean(name = "search")
@SessionScoped
public class Search {

    public String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private static String PROPERTIES_FILENAME = "youtube.properties";

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final long NUMBER_OF_VIDEOS_RETURNED = 20;

    private static YouTube youtube;

    public String transform() {

        Properties properties = new Properties();
        StringBuffer result = new StringBuffer("");
        try {
            InputStream in = Search.class.getResourceAsStream(PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause() + " : " + e.getMessage());
            System.exit(1);
        }

        try {

            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            if (!keyword.equalsIgnoreCase("null")) {
                String queryTerm = keyword;//getInputQuery();

                YouTube.Search.List search = youtube.search().list("id,snippet");

                String apiKey = properties.getProperty("youtube.apikey");
                search.setKey(apiKey);
                search.setQ(queryTerm);

                search.setType("video");

                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                SearchListResponse searchResponse = search.execute();

                List<SearchResult> searchResultList = searchResponse.getItems();

                System.out.println("\n=============================================================");
                System.out.println(
                        "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + queryTerm + "\".");
                System.out.println("=============================================================\n");

                if (searchResultList != null) {
                    //prettyPrint(searchResultList.iterator(), queryTerm);

                    Iterator<SearchResult> it = searchResultList.iterator();
                    int count = 0;
                    while (it.hasNext()) {
                        String list_name = null;
                        SearchResult singleVideo = it.next();
                        ResourceId rId = singleVideo.getId();
                        if (rId.getKind().equals("youtube#video")) {
                            Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                            String link = "https://www.youtube.com/embed/" + rId.getVideoId();
                            String anchor = "<a href='" + link + "' target='youtube_load'><img src='" + thumbnail.getUrl() + "' alt=''/></a>";
                            System.out.println(anchor + "\n");
                            result.append(anchor);
//                            result.append("<a href='https://www.youtube.com/embed/'"+rId.getVideoId()).append("'><img src='").append(thumbnail.getUrl()).append("' alt=''/></a>");
//                           
                            //                        result.append("<br/> Video Id " + rId.getVideoId());
//                        result.append("<br/>Title: " + singleVideo.getSnippet().getTitle());
//                        result.append("<br/>Thumbnail: " + thumbnail.getUrl());
                        }
                        count++;
                    }

                }
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result.toString();
    }

    private static String getInputQuery() throws IOException {

        String inputQuery = "";

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {

            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            if (rId.getKind().equals("youtube#video")) {

                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
//                result.append("<br/> Video Id " + rId.getVideoId());
//                result.append("<br/>Title: " + singleVideo.getSnippet().getTitle());
//                result.append("<br/>Thumbnail: " + thumbnail.getUrl());

                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}
