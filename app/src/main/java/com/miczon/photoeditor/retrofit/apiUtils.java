package com.miczon.photoeditor.retrofit;

import android.app.Activity;
import android.util.Log;

import com.miczon.photoeditor.utils.Constants;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class apiUtils {

    public static String TAG = "apiUtils";

    public static apiInterface getAPIService(Activity activity, String from) {
        String Url = "";
        if (from.equalsIgnoreCase("filter")) {
            Url = Constants.baseURL;
        } else if (from.equalsIgnoreCase("bgRemover")) {
            Url = Constants.bgRemoverBaseUrl;
        }
        Log.e(TAG, "getAPIService: url: " + Url);
        return apiClient.getClient(activity, Url).create(apiInterface.class);
    }
}
