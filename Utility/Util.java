package id.klepontech.chatroom.Utility;

/**
 * Created by garya on 18/01/2018.
 */

public class Util {

    public static String local(String lat, String lot){
        return "https://maps.googleapis.com/maps/api/staticmap?center="+lat+","
                +lot+"&zoom=18&size=280x280&markers=color:red|"
                +lat+","+lot;
    }

}
