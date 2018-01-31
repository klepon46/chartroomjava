package id.klepontech.chatroom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import id.klepontech.chatroom.R;

/**
 * Created by garya on 31/01/2018.
 */

public class RoomGridAdapter extends BaseAdapter {

    private Context context;
    private List<String> rooms;

    public RoomGridAdapter(Context context, List<String> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.item_menu_room, viewGroup, false);
        }

        assignAndSetViewValue(view, i);

        return view;
    }

    private void assignAndSetViewValue(View convertView, int position){
        TextView textView = convertView.findViewById(R.id.room_name);
        String roomName = rooms.get(position);

        textView.setText(roomName);

    }

}
