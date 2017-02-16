package utils;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;


public class ZomatoNetworkUtils {

    private static final String ZOMATO_ROOT_URL = "http://www.zomato.com/asda";

    /**
     * Creates a URL based on the query
     * @param query Query can be address, street, location ..etc
     * @return URL
     */
    public static URL buildURL(String query) {
        Uri searchUri = Uri.parse(ZOMATO_ROOT_URL).buildUpon().appendQueryParameter("", query).build();
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
    public static URL buildURL(double lat, double lng) {
        Uri searchUri = Uri.parse(ZOMATO_ROOT_URL).
                buildUpon().
                appendQueryParameter("", "").
                appendQueryParameter("", "").build();
        URL url = null;
        try {
            url = new URL(searchUri.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return url;
    }
}
