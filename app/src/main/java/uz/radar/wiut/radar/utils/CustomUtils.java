package uz.radar.wiut.radar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CustomUtils {

    public static boolean hasNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void putSharedPrefString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSharedPreferencesString(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, "");
    }

    public static void putSharedPrefBoolean(Context context, String key, boolean state) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, state);
        editor.commit();
    }

    public static boolean getSharedPreferencesBoolean(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, false);
    }

    public static void printErrorResponse(InputStream errorStream, String urlStr, String TAG) {
        try {
            BufferedReader stream = new BufferedReader(new InputStreamReader(errorStream, Const.UTF_8));
            String line;
            StringBuilder errorResponse = new StringBuilder();
            while ((line = stream.readLine()) != null) {
                errorResponse.append(line);
            }
            StringBuilder message = new StringBuilder();
            message.append("Error received while requesting: ");
            message.append(urlStr);
            message.append("Error: ");
            message.append(errorResponse.toString());
            Log.d(TAG, message.toString());
        } catch (Exception ex) {
            Log.d(TAG, "Error while printing errorStream: " + ex);
        }
    }
}
