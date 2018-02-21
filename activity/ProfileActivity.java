package id.klepontech.chatroom.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.User;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.OwnListener;
import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.model.UserModel;

/**
 * Created by garya on 15/01/2018.
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_GALLERY_INTENT_RC = 102;

    private EditText nameText;
    private Button nextButton;
    private ImageButton chooseImageButton;
    private CircleImageView userImage;
    private SharedPreferences.Editor editor;
    private OwnListener ownListener;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String imageProfileName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String sharedPrefKey = getResources().getString(R.string.sharedPrefKey);
        SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefKey
                , Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        imageProfileName = auth.getCurrentUser().getPhoneNumber();

        ownListener = new OwnListener();
        nameText = findViewById(R.id.profile_editText);
        nextButton = findViewById(R.id.profile_btnNext);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        userImage = findViewById(R.id.profile_image);

        nextButton.setOnClickListener(this);
        chooseImageButton.setOnClickListener(this);

        sendDefaultImageToFirebase();
        getProfileKey();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_btnNext:
                onClickNextButton();
                break;
            case R.id.chooseImageButton:
                onClickChooseImgBtn();
                break;
        }
    }


    private void onClickNextButton() {

        if (TextUtils.isEmpty(nameText.getText().toString())) {
            Toast.makeText(this, "Name cannot be null", Toast.LENGTH_SHORT).show();
            return;
        }

        final UserModel userModel = new UserModel();
        userModel.setName(nameText.getText().toString());
        userModel.setPhoneNumber(auth.getCurrentUser().getPhoneNumber());
        userModel.setUrlPhoto(getPhotoProfileUrl());

        final DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("profiles");

        Query query = ref.orderByChild("phoneNumber")
                .equalTo(auth.getCurrentUser().getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    FirebaseDatabase.getInstance().getReference().child("profiles")
                            .child(ownListener.getKey()).setValue(userModel);

                } else {
                    ref.push().setValue(userModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //ref.push().setValue(userModel);

        String key = getResources().getString(R.string.profileNameKey);
        editor.putString(key, nameText.getText().toString());
        editor.commit();

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);

        finish();

    }

    private void onClickChooseImgBtn() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                IMAGE_GALLERY_INTENT_RC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                .child(Util.FOLDER_STORAGE_IMG_PROFILE);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (requestCode == IMAGE_GALLERY_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();
                sendImageToFirebase(storageRef, fileUri);
            }
        }
    }

    private void sendDefaultImageToFirebase() {

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE)
                .child(Util.FOLDER_STORAGE_IMG_PROFILE);

        Uri uri = Uri.parse("android.resource://id.klepontech.chatroom/drawable/ic_account_circle");

        StorageReference profileGalleryRef = storageRef.child(imageProfileName);
        UploadTask uploadTask = profileGalleryRef.putFile(uri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();

                String key = getResources().getString(R.string.profileUrlPhotoKey);
                editor.putString(key, uri.toString());
                editor.commit();
            }
        });
    }

    private void sendImageToFirebase(StorageReference storageReference, final Uri file) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Set Picture");
        progressDialog.show();

        StorageReference profileGalleryRef = storageReference.child(imageProfileName);
        UploadTask uploadTask = profileGalleryRef.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Glide.with(ProfileActivity.this)
                        .load(file)
                        .fitCenter()
                        .into(userImage);

                Uri uri = taskSnapshot.getDownloadUrl();

                String key = getResources().getString(R.string.profileUrlPhotoKey);
                editor.putString(key, uri.toString());
                editor.commit();

                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private String getPhotoProfileUrl() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileUrlPhotoKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences(prefName, Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }

    private void getProfileKey() {

        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("profiles");
        ref.orderByChild("phoneNumber")
                .equalTo(phoneNumber)
                .addListenerForSingleValueEvent(ownListener);
    }
}
