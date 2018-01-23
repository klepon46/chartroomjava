package id.klepontech.chatroom.adapter;

import android.view.View;

/**
 * Created by garya on 17/01/2018.
 */

public interface ClickListenerChatFirebase {

    void clickImageChat(View view, int position, String nameUser
            , String urlPhotoUser, String urlPhotoClick);

    void clickImageMapChat(View view, int position, String latitude, String longitude);
}
