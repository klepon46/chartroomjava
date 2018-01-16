package id.klepontech.chatroom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by garya on 15/01/2018.
 */

public class ProfileActivity extends AppCompatActivity {

    private EditText nameText;
    private Button nextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.profile_editText);
        nextButton = findViewById(R.id.profile_btnNext);

        String sharedPrefKey = getResources().getString(R.string.sharedPrefKey);
        SharedPreferences sharedPref =
                this.getSharedPreferences(sharedPrefKey,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= sharedPref.edit();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = getResources().getString(R.string.profileNameKey);
                editor.putString(key, nameText.getText().toString());
                editor.commit();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        });

    }




}
