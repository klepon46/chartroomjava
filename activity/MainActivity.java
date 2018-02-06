package id.klepontech.chatroom.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.adapter.RoomGridAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_GALLERY_INTENT_RC = 102;

    private GridView gridView;
    private CircleImageView userImage;
    private ImageButton chooseImageButton;
    private Button btnUpdate;
    private EditText etProfileName;
    private SharedPreferences.Editor editor;
    private ImageView imgBannerTop;
    private ImageView imgBannerBottom;

    private String name;
    private String imageProfileName;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference root = database.getReference().getRoot();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        gridView = findViewById(R.id.room_grid);
        userImage = findViewById(R.id.profile_image);
        etProfileName = findViewById(R.id.profile_editText);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        btnUpdate = findViewById(R.id.profile_btnNext);
        imgBannerTop = findViewById(R.id.img_banner_top);
        imgBannerBottom = findViewById(R.id.img_banner_bottom);

        chooseImageButton.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);



        String sharedPrefKey = getResources().getString(R.string.sharedPrefKey);
        SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefKey
                , Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        imageProfileName = auth.getCurrentUser().getPhoneNumber();

        name = getCurrentProfileName();

        final List<String> rooms = new ArrayList<>();

        showLoadingDialog();
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
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

        etProfileName.setText(name);
        String profileImageUrl = getPhotoProfileUrl();

        String urlBanner = "http://www.limocart.com/img/banner_img1.jpg";

        Glide.with(this).load(profileImageUrl).fitCenter().into(userImage);
        Glide.with(this).load(urlBanner).centerCrop().into(imgBannerTop);
        Glide.with(this).load(urlBanner).centerCrop().into(imgBannerBottom);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseImageButton:
                onClickChooseImgBtn();
                break;
            case R.id.profile_btnNext:
                onClickNextButton();
                break;
        }
    }

    private void onClickChooseImgBtn() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                IMAGE_GALLERY_INTENT_RC);
    }

    private void onClickNextButton() {

        String key = getResources().getString(R.string.profileNameKey);
        editor.putString(key, etProfileName.getText().toString());
        editor.commit();

        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                .child(Util.FOLDER_STORAGE_IMG_PROFILE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String phoneNumber = auth.getCurrentUser().getPhoneNumber();

        if (requestCode == IMAGE_GALLERY_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();
                sendImageToFirebase(storageRef, fileUri);
            }
        }
    }

    private void sendImageToFirebase(StorageReference storageReference, final Uri file) {

        showLoadingDialog();
        StorageReference profileGalleryRef = storageReference.child(imageProfileName);
        UploadTask uploadTask = profileGalleryRef.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Glide.with(MainActivity.this)
                        .load(file)
                        .fitCenter()
                        .into(userImage);

                Uri uri = taskSnapshot.getDownloadUrl();

                String key = getResources().getString(R.string.profileUrlPhotoKey);
                editor.putString(key, uri.toString());
                editor.commit();

                dismissDialog();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dismissDialog();
            }
        });


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

    private String getPhotoProfileUrl() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileUrlPhotoKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences(prefName, Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }

    private String getCurrentProfileName() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileNameKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }


}
