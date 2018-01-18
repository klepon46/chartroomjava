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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

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

    public ChatAdapter(FirebaseRecyclerOptions<ChatModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(ChatViewHolder holder, int position, ChatModel model) {
        holder.setTxtMessage(model.getMessage());
        holder.tvIsLocation(View.GONE);

        if(model.getMapModel() != null){

            holder.setIvChatPhoto(Util.local(model.getMapModel().getLatitude()
                    ,model.getMapModel().getLongitude()));
            holder.tvIsLocation(View.VISIBLE);
        }

        //holder.tvName.setText(model.getName());
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == RIGHT_MSG) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right, parent, false);
            return new ChatViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right_img, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getMapModel() != null) {
            return RIGHT_MSG_IMG;
        } else {
            return RIGHT_MSG;
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvMessage, tvLocation;
        ImageView  ivChatPhoto;

//        TextView tvTimeStamp, tvLocation;


        public ChatViewHolder(View itemView) {
            super(itemView);

            //tvName = itemView.findViewById(R.id.tvName_test);
            tvMessage = itemView.findViewById(R.id.txtMessage);

//            tvTimeStamp = (TextView) itemView.findViewById(R.id.timestamp);
            tvLocation = (TextView)itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView)itemView.findViewById(R.id.img_chat);
//            ivUser = (ImageView) itemView.findViewById(R.id.ivUserChat);

        }

        public void setTxtMessage(String message) {
            if (tvMessage == null) return;
            tvMessage.setText(message);
        }

//
//        public void setTvTimestamp(String timestamp) {
//            if (tvTimeStamp == null) return;
//            tvTimeStamp.setText(converteTimestamp(timestamp));
//        }
//
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

    private CharSequence converteTimestamp(String mileSegundos) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

}
