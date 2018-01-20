package id.klepontech.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import id.klepontech.chatroom.adapter.ChatAdapter;
import id.klepontech.chatroom.model.ChatModel;
import id.klepontech.chatroom.model.MapModel;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatRoomTwo extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = MainActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "chatmodel";

    //Firebase and GoogleApiClient
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;


    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private Button btSendMessage;
    private ImageButton btPopupChatMenu;
    private View contentRoot;
    private ChatAdapter adapter;
    private EditText etMessage;

    private String userName;
    private String roomName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_2);

        userName = getIntent().getExtras().get("user_name").toString();
        roomName = getIntent().getExtras().get("room_name").toString();

        bindViews();
        initializeFirebase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSendMessage:

                sendMessage();
                //sendLocationIntent();
                break;
            case R.id.btnPopupMenu:
                showPopupMenu();
                break;
        }
    }

    private void sendMessage() {
        ChatModel model = new ChatModel(userName, etMessage.getText().toString());
        mFirebaseDatabaseReference.push().setValue(model);
        etMessage.setText(null);
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(ChatRoomTwo.this, btPopupChatMenu);
        popupMenu.getMenuInflater().inflate(R.menu.menu_chat, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.sendPhotoGallery:
                        break;
                    case R.id.sendLocation:
                        sendLocationIntent();
                        break;
                }

                return false;
            }
        });

        popupMenu.show();
    }

    private void sendLocationIntent() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), 101);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel =
                            new MapModel(latLng.latitude + "", latLng.longitude + "");
                    ChatModel chatModel = new ChatModel(userName, mapModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                }
            }
        }
    }

    private void initializeFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child(roomName);

        Query query = mFirebaseDatabaseReference.orderByKey();

        FirebaseRecyclerOptions<ChatModel> options =
                new FirebaseRecyclerOptions.Builder<ChatModel>()
                        .setQuery(query, ChatModel.class)
                        .build();

        adapter = new ChatAdapter(options, userName);

        rvListMessage.setLayoutManager(new LinearLayoutManager(this));
        rvListMessage.setAdapter(adapter);
    }


    private void bindViews() {
        contentRoot = findViewById(R.id.contentRoot);

        btSendMessage = (Button) findViewById(R.id.btnSendMessage);
        btSendMessage.setOnClickListener(this);

        btPopupChatMenu = findViewById(R.id.btnPopupMenu);
        btPopupChatMenu.setOnClickListener(this);

        etMessage = findViewById(R.id.etMessage);

        rvListMessage = (RecyclerView) findViewById(R.id.messageRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }


}
