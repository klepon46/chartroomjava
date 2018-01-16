package id.klepontech.chatroom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

/**
 * Created by garya on 15/01/2018.
 */

public class SignInActivity extends AppCompatActivity {

    private static final int SIGN_IN_RC = 46;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(), SIGN_IN_RC);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            if(isProfileNameEmpty()){
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }

            if(!isProfileNameEmpty()){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_RC) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        }


    }

    private boolean isProfileNameEmpty() {

        String prefName = getResources().getString(R.string.sharedPrefKey);
        String key = getResources().getString(R.string.profileNameKey);

        SharedPreferences sharedRef =
                this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        String profileName = sharedRef.getString(key, null);


        return profileName == null ? true : false;
    }
}
