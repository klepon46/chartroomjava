package id.klepontech.chatroom;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import id.klepontech.chatroom.model.ChatModel;

/**
 * Created by garya on 12/01/2018.
 */

public class ChatRoom extends AppCompatActivity implements View.OnClickListener{

    private Button btnSendMsg;
    private EditText inputMsg;
    private TextView chatConverstation;
    private String userName, roomName;
    private DatabaseReference root;
    private String tempKey;
    private String chatMsg, chatUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        bindView();

        userName = getIntent().getExtras().get("user_name").toString();
        roomName = getIntent().getExtras().get("room_name").toString();
        setTitle("Room - " + roomName);

        root = FirebaseDatabase.getInstance().getReference().child(roomName);

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendChatConversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void appendChatConversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            chatMsg = (String) ((DataSnapshot) i.next()).getValue();
            chatUserName = (String) ((DataSnapshot) i.next()).getValue();

            chatConverstation.append(chatUserName + " : " + chatMsg + "\n");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button :
                sendMessage();
                break;
        }
    }

    public void sendMessage(){
        ChatModel model = new ChatModel(userName, inputMsg.getText().toString(), "");
        root.push().setValue(model);
    }

    public void bindView(){
        btnSendMsg = (Button) findViewById(R.id.button);
        btnSendMsg.setOnClickListener(this);

        inputMsg = (EditText) findViewById(R.id.editText);
        chatConverstation = (TextView) findViewById(R.id.textView);
    }
}
