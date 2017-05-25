package utils;

import android.net.Uri;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;


public class ZomatoNetworkUtils {

    private static AsyncHttpClient mClient = new AsyncHttpClient();

    /**
     * Keys
     */
    private static final String ZOMATO_ROOT_URL = "https://developers.zomato.com/api/v2.1/search";
    private static final String RES_ID = "res_id";
    private static final String QUERY = "q";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_ID = "entity_id";
    private static final String COLLECTION_ID = "collection_id";

    // API KEY, should be stored in somewhere else, or enable ProGuard
    private static final String API_KEY = "8b000e3bd6d2a4434c926aeca9a040d0";

    /**
     * Values
     */
    private static final String CITY_ENTITY_TYPE = "city";

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        mClient.addHeader("user-key", API_KEY);
        mClient.get(url, params, responseHandler);
    }

    /**
     * Creates a URL based on the query
     * @param query Query can be address, street, location ..etc
     * @return URL
     */
    public static URL buildURL(String query) {
        Uri searchUri = Uri.parse(ZOMATO_ROOT_URL).
                buildUpon().
                appendQueryParameter("", query).
                build();
        URL url = null;
        try {
            url = new URL(searchUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Creates URL based on the coordinates
     * @param lat Latitude
     * @param lng Longitude
     * @return URL
     */
    public static String buildURL(double lat, double lng) {
        Uri searchUri = Uri.parse(ZOMATO_ROOT_URL).
                buildUpon().
                appendQueryParameter(LAT, String.valueOf(lat)).
                appendQueryParameter(LON, String.valueOf(lng)).
                build();
        return searchUri.toString();
    }

    public static String buildTrendingRestaurantsURL(String entityID) {
        Uri searchUri = Uri.parse(ZOMATO_ROOT_URL).
                buildUpon().
                appendQueryParameter(ENTITY_ID, entityID).
                appendQueryParameter(ENTITY_TYPE, CITY_ENTITY_TYPE).
                appendQueryParameter(COLLECTION_ID, "1").
                build();
        return searchUri.toString();
    }

    public static String getZomatoReviewReference(String rootRef) {
        switch (rootRef) {
            case "new-york-city":
                return "nyc-zomato-reviews";
            case "london":
                return "london-zomato-reviews";
            case "istanbul":
                return "istanbul-zomato-reviews";
            default:
                throw new IllegalArgumentException("Unknown reference");
        }
    }

    public static String getZomatoRestaurantReference(String rootRef) {
        switch (rootRef) {
            case "new-york-city":
                return "nyc-zomato-external";
            case "london":
                return "london-zomato-external";
            case "istanbul":
                return "istanbul-zomato-external";
            default:
                throw new IllegalArgumentException("Unknown reference");
        }
    }


}
