package id.klepontech.chatroom.Utility;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by garya on 18/01/2018.
 */

public class Util {

    public static final String URL_STORAGE_REFERENCE = "gs://chatroom-303e6.appspot.com";
    public static final String FOLDER_STORAGE_IMG = "images";
    public static final String FOLDER_STORAGE_IMG_PROFILE = "profiles";

    public static boolean isConnectedToInternet(Context context){
        boolean isConnected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        isConnected = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return isConnected;
    }


    public static String local(String lat, String lot){
        return "https://maps.googleapis.com/maps/api/staticmap?center="+lat+","
                +lot+"&zoom=18&size=280x280&markers=color:red|"
                +lat+","+lot;
    }

}
