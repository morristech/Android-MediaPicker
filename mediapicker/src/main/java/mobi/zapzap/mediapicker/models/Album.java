package mobi.zapzap.mediapicker.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * Created by MyInnos on 03-11-2016.
 */
public class Album implements Parcelable {

    private String name;
    private String coverPath;
    private String displayDate;
    private int iconResId;

    public Album(@NonNull String name, @NonNull String cover, String displayDate, @DrawableRes int iconResId) {

        this.name = name;
        this.coverPath = cover;
        this.displayDate = displayDate;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    @DrawableRes
    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(@DrawableRes int iconResId) {
        this.iconResId = iconResId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.name);
        dest.writeString(this.coverPath);
        dest.writeString(this.displayDate);
        dest.writeInt(this.iconResId);
    }

    protected Album(Parcel in) {

        this.name = in.readString();
        this.coverPath = in.readString();
        this.displayDate = in.readString();
        this.iconResId = in.readInt();
    }

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {

        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}

