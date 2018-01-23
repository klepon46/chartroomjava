package id.klepontech.chatroom;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.adapter.ChatAdapter;
import id.klepontech.chatroom.adapter.ClickListenerChatFirebase;
import id.klepontech.chatroom.model.ChatModel;
import id.klepontech.chatroom.model.FileModel;
import id.klepontech.chatroom.model.MapModel;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatRoomTwo extends AppCompatActivity implements View.OnClickListener,
        ClickListenerChatFirebase {

    static final String TAG = MainActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "chatmodel";

    //Firebase and GoogleApiClient
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();


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

    private File filePathImageCamera;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                break;
            case R.id.btnPopupMenu:
                showPopupMenu();
                break;
        }
    }

    private void sendMessage() {
        if(etMessage.getText().toString().isEmpty() || etMessage.getText() == null){
            return;
        }

        String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
        ChatModel model = new ChatModel(userName, etMessage.getText().toString(), timeStamp);
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
                    case R.id.sendPhotoCamera:
                        verifyStoragePermissions();
                        break;
                    case R.id.sendPhotoGallery:
                        photoGalleryIntent();
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

    private void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_picture_title)), 102);
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(ChatRoomTwo.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    ChatRoomTwo.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
            // we already have permission, lets go ahead and call camera intent
            photoCameraIntent();
        }
    }

    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto+"camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(ChatRoomTwo.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
        startActivityForResult(it, 103);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                .child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel =
                            new MapModel(latLng.latitude + "", latLng.longitude + "");
                    ChatModel chatModel = new ChatModel(userName, timeStamp, mapModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                }
            }
        }

        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                } else {
                    //URI IS NULL
                }
            }
        }

        if(requestCode == 103){
            if(resultCode == RESULT_OK){
                if(filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imagCameraRef =
                            storageRef.child(filePathImageCamera.getName()+"_camera");
                    sendFileFirebase(imagCameraRef,filePathImageCamera);
                }
            }
        }
    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) {
        if (storageReference != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                    ChatModel chatModel = new ChatModel(userName, timeStamp, fileModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                }
            });
        } else {
            //IS NULL
        }

    }

    private void sendFileFirebase(StorageReference storageReference, final File file) {
        if (storageReference != null){
            Uri photoURI = FileProvider.getUriForFile(ChatRoomTwo.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            UploadTask uploadTask = storageReference.putFile(photoURI);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG,"onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
                    FileModel fileModel = new FileModel("img",downloadUrl.toString()
                            ,file.getName(),file.length()+"");
                    ChatModel chatModel = new ChatModel(userName, timeStamp, fileModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                }
            });
        }else{
            //IS NULL
        }

    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
        Intent intent = new Intent(this,FullScreenImageActivity.class);
        intent.putExtra("nameUser",nameUser);
        intent.putExtra("urlPhotoClick",urlPhotoClick);
        startActivity(intent);
    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private void initializeFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child(roomName);

        Query query = mFirebaseDatabaseReference.orderByKey();

        FirebaseRecyclerOptions<ChatModel> options =
                new FirebaseRecyclerOptions.Builder<ChatModel>()
                        .setQuery(query, ChatModel.class)
                        .build();

        adapter = new ChatAdapter(options, userName, this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePos = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePos == -1
                        || (positionStart >= (messageCount - 1) &&
                        lastVisiblePos == (positionStart - 1))) {
                    rvListMessage.scrollToPosition(positionStart);
                }
            }
        });

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
