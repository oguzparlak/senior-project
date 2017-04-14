package utils;

import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Oguz on 14/04/2017.
 */

public class GooglePlacesNetworkUtils {

    private static final String TAG = GooglePlacesNetworkUtils.class.getSimpleName();

    private static final String ROOT = "https://maps.googleapis.com/maps/api/place/details/json?";
    private static final String PLACE_ID = "place_id";
    private static final String LANGUAGE = "language";
    private static final String KEY = "key";
    private static final String GOOGLE_WEB_API_KEY = "AIzaSyBFf-DOlrxXAQuzKZDwX1fB6Zx8RAtw_TM";

    private static String buildUrl(String placeId) {
        Uri uri = Uri
                .parse(ROOT)
                .buildUpon()
                .appendQueryParameter(PLACE_ID, placeId)
                .appendQueryParameter(LANGUAGE, "en")
                .appendQueryParameter(KEY, GOOGLE_WEB_API_KEY)
                .build();

        return uri.toString();
    }

    public static void get(String placeId, AsyncHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(buildUrl(placeId), null, handler);
    }


}
