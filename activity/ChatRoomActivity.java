package id.klepontech.chatroom.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.klepontech.chatroom.BuildConfig;
import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.OwnListener;
import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.adapter.ChatAdapter;
import id.klepontech.chatroom.adapter.ClickListenerChatFirebase;
import id.klepontech.chatroom.model.ChatModel;
import id.klepontech.chatroom.model.FileModel;
import id.klepontech.chatroom.model.MapModel;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener,
        ClickListenerChatFirebase {

    static final String TAG = MainActivity.class.getSimpleName();

    private static final int LOCATION_INTENT_RC = 101;
    private static final int IMAGE_GALLERY_INTENT_RC = 102;
    private static final int CAMERA_INTENT_RC = 103;

    //Firebase and GoogleApiClient
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private Button btSendMessage;
    private ImageButton btPopupChatMenu;
    private ChatAdapter adapter;
    private EditText etMessage;
    private Toolbar toolbar;
    private ProgressDialog dialog;

    private String userName;
    private String roomName;
    private String profileKey;

    private OwnListener ownListener;

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
        setContentView(R.layout.activity_chatroom);

        ownListener = new OwnListener();
        userName = getIntent().getExtras().get("user_name").toString();
        roomName = getIntent().getExtras().get("room_name").toString();
        profileKey = getIntent().getExtras().get("profile_key").toString();
        toolbar = findViewById(R.id.toolbar_chat);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(roomName);

        if (Util.isConnectedToInternet(this)) {
            bindViews();
            initializeFirebase();
        } else {
            Toast.makeText(this, "You dont have internet Connection", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sendPhotoCamera:
                verifyStoragePermissions();
                break;
            case R.id.sendLocation:
                sendLocationIntent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSendMessage:
                sendMessage();
                break;
            case R.id.btnPopupMenu:
                //showPopupMenu();
                photoGalleryIntent();
                break;
        }
    }

    private void sendMessage() {
        if (etMessage.getText().toString().isEmpty() || etMessage.getText() == null) {
            return;
        }

        String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());

        ChatModel model = new ChatModel(profileKey, etMessage.getText().toString(),
                timeStamp);

        mFirebaseDatabaseReference.push().setValue(model);
        etMessage.setText(null);
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(ChatRoomActivity.this, btPopupChatMenu);
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
            startActivityForResult(builder.build(this), LOCATION_INTENT_RC);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_picture_title)), IMAGE_GALLERY_INTENT_RC);
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(ChatRoomActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    ChatRoomActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            // we already have permission, lets go ahead and call camera intent
            photoCameraIntent();
        }
    }

    private void photoCameraIntent() {
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                nomeFoto + "camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(ChatRoomActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(it, CAMERA_INTENT_RC);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                .child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == LOCATION_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
                    LatLng latLng = place.getLatLng();

                    MapModel mapModel =
                            new MapModel(latLng.latitude + "", latLng.longitude + "");

                    ChatModel chatModel =
                            new ChatModel(profileKey, timeStamp, mapModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                }
            }
        }

        if (requestCode == IMAGE_GALLERY_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                } else {
                    //URI IS NULL
                }
            }
        }

        if (requestCode == CAMERA_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera.exists()) {
                    StorageReference imagCameraRef =
                            storageRef.child(filePathImageCamera.getName() + "_camera");
                    sendFileFirebase(imagCameraRef, filePathImageCamera);
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
                    ChatModel chatModel = new ChatModel(profileKey, timeStamp, fileModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                    dismissDialog();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())
                            / taskSnapshot.getTotalByteCount();

                    Log.d(TAG, "onProgress: " + progress);

                    showLoadingDialog((int) progress + " %");
                }
            });
        } else {
            //IS NULL
        }

    }

    private void sendFileFirebase(StorageReference storageReference, final File file) {
        if (storageReference != null) {
            Uri photoURI = FileProvider.getUriForFile(ChatRoomActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            UploadTask uploadTask = storageReference.putFile(photoURI);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String timeStamp = String.valueOf(Calendar.getInstance().getTime().getTime());
                    FileModel fileModel = new FileModel("img", downloadUrl.toString()
                            , file.getName(), file.length() + "");

                    ChatModel chatModel = new ChatModel(profileKey, timeStamp, fileModel);
                    mFirebaseDatabaseReference.push().setValue(chatModel);
                    dismissDialog();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())
                            / taskSnapshot.getTotalByteCount();
                    showLoadingDialog((int) progress + " %");
                }
            });
        } else {
            //IS NULL
        }

    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("nameUser", nameUser);
        intent.putExtra("urlPhotoClick", urlPhotoClick);
        intent.putExtra("roomName", roomName);
        startActivity(intent);
    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude, longitude, latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private void initializeFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child("rooms").child(roomName);

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
        adapter.setProfileKey(profileKey);

        rvListMessage.setLayoutManager(new LinearLayoutManager(this));
        rvListMessage.setAdapter(adapter);
    }


    private void bindViews() {
        btSendMessage = (Button) findViewById(R.id.btnSendMessage);
        btSendMessage.setOnClickListener(this);

        btPopupChatMenu = findViewById(R.id.btnPopupMenu);
        btPopupChatMenu.setOnClickListener(this);

        etMessage = findViewById(R.id.etMessage);

        rvListMessage = (RecyclerView) findViewById(R.id.messageRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    private String getProfileUrl() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileUrlPhotoKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences(prefName, Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }

    public void showLoadingDialog(String progress) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }

        dialog.setTitle("Loading...");
        dialog.setMessage(progress);
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void getProfileKey() {

        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("profiles");
        ref.orderByChild("phoneNumber")
                .equalTo(phoneNumber)
                .addListenerForSingleValueEvent(ownListener);
    }
}
