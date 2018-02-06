package id.klepontech.chatroom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import id.klepontech.chatroom.R;

/**
 * Created by garya on 06/02/2018.
 */

public class VerifySignInCodeActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    private EditText fieldVerify;
    private Button btnVerify;

    private String mVerificationID;
    private String mToken;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_custom);

        fieldVerify = findViewById(R.id.field_verify_code);
        btnVerify = findViewById(R.id.btn_verify);

        mVerificationID = getIntent().getExtras().get("verification_id").toString();
        mToken = getIntent().getExtras().get("token").toString();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthCredential credential = PhoneAuthProvider
                        .getCredential(mVerificationID, fieldVerify.getText().toString());

                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            startActivity(new Intent(VerifySignInCodeActivity.this,
                                    ProfileActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(VerifySignInCodeActivity.this,
                                        "Invalid Code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
