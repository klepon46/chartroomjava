package id.klepontech.chatroom.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import id.klepontech.chatroom.R;
import id.klepontech.chatroom.Utility.Util;
import id.klepontech.chatroom.model.ChatModel;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatAdapter extends FirebaseRecyclerAdapter<ChatModel, ChatAdapter.ChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private String userName;

    public ChatAdapter(FirebaseRecyclerOptions<ChatModel> options, String userName) {
        super(options);
        this.userName = userName;
    }

    @Override
    protected void onBindViewHolder(ChatViewHolder holder, int position, ChatModel model) {
        holder.setTxtUsername(model.getName());
        holder.setTxtMessage(model.getMessage());
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
        String modelUserName = model.getName();

        if (model.getMapModel() != null) {

            if (modelUserName.equals(userName)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }

        } else if (model.getFileModel() != null) {
            if(modelUserName.equals(userName)){
                return RIGHT_MSG_IMG;
            }else{
                return LEFT_MSG_IMG;
            }
        } else {
            if (modelUserName.equals(userName)) {
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

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername, tvMessage, tvLocation, tvTimeStamp;
        ImageView ivChatPhoto;

        public ChatViewHolder(View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.txtUserName);
            tvMessage = itemView.findViewById(R.id.txtMessage);

            tvTimeStamp = (TextView) itemView.findViewById(R.id.timestamp);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
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

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            //ivChatPhoto.setOnClickListener(this);
        }

        public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }
    }

}
