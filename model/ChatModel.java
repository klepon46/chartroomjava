package id.klepontech.chatroom.model;

import id.klepontech.chatroom.ChatRoom;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatModel {

    private String name;
    private String message;
    private MapModel mapModel;

    public ChatModel(){

    }

    public ChatModel(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public ChatModel(String name, MapModel mapModel) {
        this.name = name;
        this.mapModel = mapModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }
}
