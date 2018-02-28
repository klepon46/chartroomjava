package id.klepontech.chatroom.model;

/**
 * Created by garya on 17/01/2018.
 */

public class ChatModel {

    private String name;
    private String message;
    private String urlPhoto;
    private String timeStamp;
    private String profileKey;
    private MapModel mapModel;
    private FileModel fileModel;

    public ChatModel() {

    }

    public ChatModel(String name, String message, String urlPhoto, String timeStamp) {
        this.name = name;
        this.message = message;
        this.urlPhoto = urlPhoto;
        this.timeStamp = timeStamp;
    }

    public ChatModel(String profileKey, String timeStamp, MapModel mapModel) {
        this.profileKey = profileKey;
        this.mapModel = mapModel;
        this.timeStamp = timeStamp;
    }

    public ChatModel(String profileKey, String timeStamp, FileModel fileModel) {
        this.profileKey = profileKey;
        this.fileModel = fileModel;
        this.timeStamp = timeStamp;
    }

    public ChatModel(String profileKey, String message, String timeStamp) {
        this.profileKey = profileKey;
        this.message = message;
        this.timeStamp = timeStamp;
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

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getProfileKey() {
        return profileKey;
    }
}
