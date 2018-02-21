package id.klepontech.chatroom.Utility;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by garya on 16/02/2018.
 */

public class OwnListener implements ValueEventListener {

    private String key;

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        String lkey = null;
        for (DataSnapshot item : dataSnapshot.getChildren()) {
            lkey = item.getKey();
        }
        Log.d("ARYA", "onDataChange: " + lkey) ;
        setKey(lkey);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public String getKey() {
        Log.i("ARYA", "getKey: " + key);
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
