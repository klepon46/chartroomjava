package id.klepontech.chatroom.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import id.klepontech.chatroom.R;
import id.klepontech.chatroom.adapter.RoomGridAdapter;

public class MainActivity extends AppCompatActivity {


    private static final int SIGN_IN_RC = 46;
    private ListView listView;
    private GridView gridView;

    private String name;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference root = database.getReference().getRoot();
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        gridView = findViewById(R.id.room_grid);

        name = getCurrentProfileName();
        final List<String> rooms = new ArrayList<>();

        showLoadingDialog();
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    rooms.add(item.getKey());
                }

                gridView.setAdapter(new RoomGridAdapter(MainActivity.this, rooms));
                dismissDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TextView room = view.findViewById(R.id.room_name);
                String roomName = room.getText().toString();

                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("room_name", roomName);
                intent.putExtra("user_name", name);
                startActivity(intent);

            }
        });

    }


    private String getCurrentProfileName() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileNameKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }

    public void showLoadingDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setTitle("Loading...");
        }

        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
