package id.klepontech.chatroom.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.Util;

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

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String imageProfileName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String sharedPrefKey = getResources().getString(R.string.sharedPrefKey);
        SharedPreferences sharedPref = this.getSharedPreferences(sharedPrefKey
                , Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        imageProfileName = auth.getCurrentUser().getPhoneNumber();

        nameText = findViewById(R.id.profile_editText);
        nextButton = findViewById(R.id.profile_btnNext);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        userImage = findViewById(R.id.profile_image);

        nextButton.setOnClickListener(this);
        chooseImageButton.setOnClickListener(this);

        String profileImageUrl = getProfileUrl();
        Glide.with(this).load(profileImageUrl).fitCenter().into(userImage);

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
        String phoneNumber = auth.getCurrentUser().getPhoneNumber();

        if (requestCode == IMAGE_GALLERY_INTENT_RC) {
            if (resultCode == RESULT_OK) {
                Uri fileUri = data.getData();
                sendImageToFirebase(storageRef, fileUri);
            }
        }
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

    private String getProfileUrl() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileUrlPhotoKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences(prefName, Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);

        return profileName;
    }
}
