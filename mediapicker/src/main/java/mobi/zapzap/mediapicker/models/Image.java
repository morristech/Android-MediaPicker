package mobi.zapzap.mediapicker.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zapper Development on 03-11-2016.
 */
public class Image implements Parcelable {

    private long id;
    private String name;
    private String headerDate;
    private String contentPath;
    private String path;
    private String caption;
    private long timestamp;
    private boolean isSelected;

    public Image(long id, String name, String headerDate, String contentPath, String path, String caption, long timestamp, boolean isSelected) {

        this.id = id;
        this.name = name;
        this.headerDate = headerDate;
        this.contentPath = contentPath;
        this.path = path;
        this.caption = caption;
        this.timestamp = timestamp;
        this.isSelected = isSelected;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeaderDate() {
        return headerDate;
    }

    public void setHeaderDate(String headerDate) {
        this.headerDate = headerDate;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.headerDate);
        dest.writeString(this.contentPath);
        dest.writeString(this.path);
        dest.writeString(this.caption);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }

    protected Image(Parcel in) {

        this.id = in.readLong();
        this.name = in.readString();
        this.headerDate = in.readString();
        this.contentPath = in.readString();
        this.path = in.readString();
        this.caption = in.readString();
        this.timestamp = in.readLong();
        this.isSelected = in.readByte() != 0;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {

        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }

    };

}
