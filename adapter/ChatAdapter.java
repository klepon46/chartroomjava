package id.klepontech.chatroom.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.microedition.khronos.opengles.GL;

import de.hdodenhof.circleimageview.CircleImageView;
import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.model.ChatModel;
import id.klepontech.chatroom.model.UserModel;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatAdapter extends FirebaseRecyclerAdapter<ChatModel, ChatAdapter.ChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private ClickListenerChatFirebase mClickListenerChatFirebase;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private String userName;
    private String profileKey;

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public ChatAdapter(FirebaseRecyclerOptions<ChatModel> options, String userName,
                       ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(options);
        this.userName = userName;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    protected void onBindViewHolder(final ChatViewHolder holder, int position, ChatModel model) {
        holder.setTxtUsername(model.getName());
        holder.setTxtMessage(model.getMessage());
        holder.setIvUserChat(model.getUrlPhoto());
        holder.setTvTimestamp(model.getTimeStamp());
        holder.tvIsLocation(View.GONE);

        if (model.getMapModel() != null) {
            holder.setIvChatPhoto(Util.local(model.getMapModel().getLatitude()
                    , model.getMapModel().getLongitude()));
            holder.tvIsLocation(View.VISIBLE);
        }

        if (model.getFileModel() != null) {
            holder.setIvChatPhoto(model.getFileModel().getUrl_file());
            holder.tvIsLocation(View.GONE);
        }

        if (model.getProfileKey() != null) {
            mFirebaseDatabase
                    .getReference("profiles")
                    .child(model.getProfileKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            holder.setIvUserChat(userModel.getUrlPhoto());
                            holder.setTxtUsername(userModel.getName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == RIGHT_MSG) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right, parent, false);
            return new ChatViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left, parent, false);
            return new ChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right_img, parent, false);
            return new ChatViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left_img, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        String modelProfileKey = model.getProfileKey();

        if (model.getMapModel() != null) {

            if (modelProfileKey.equals(profileKey)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }

        } else if (model.getFileModel() != null) {
            if (modelProfileKey.equals(profileKey)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else {
            if (modelProfileKey.equals(profileKey)) {
                return RIGHT_MSG;
            } else {
                return LEFT_MSG;
            }
        }
    }

    private CharSequence converteTimestamp(String ms) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(ms),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvUsername, tvMessage, tvLocation, tvTimeStamp;
        ImageView ivChatPhoto;
        CircleImageView ivUserChat;

        public ChatViewHolder(View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.txtUserName);
            tvMessage = itemView.findViewById(R.id.txtMessage);

            tvTimeStamp = (TextView) itemView.findViewById(R.id.timestamp);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            ivUserChat = itemView.findViewById(R.id.ivUserChat);
        }

        public void setTxtUsername(String username) {
            if (tvUsername == null) return;
            tvUsername.setText(username);
        }


        public void setTxtMessage(String message) {
            if (tvMessage == null) return;
            tvMessage.setText(message);
        }


        public void setTvTimestamp(String timestamp) {
            if (tvTimeStamp == null) return;
            tvTimeStamp.setText(converteTimestamp(timestamp));
        }

        public void setIvUserChat(String urlPhotoUser) {
            if (ivUserChat == null) return;
            Glide.with(ivUserChat.getContext())
                    .load(urlPhotoUser)
                    .centerCrop()
                    .override(40, 40)
                    .into(ivUserChat);
        }

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }

        @Override
        public void onClick(View view) {
            ChatModel model = getItem(getAdapterPosition());
            if (model.getFileModel() != null) {
                mClickListenerChatFirebase.clickImageChat(view, getAdapterPosition(),
                        userName, "", model.getFileModel().getUrl_file());
            } else {
                mClickListenerChatFirebase.clickImageMapChat(view, getAdapterPosition(),
                        model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
            }

        }
    }

}
