package com.pgmsoft.photocarshop.restapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImageModel implements Parcelable {

    @SerializedName("full")
    @Expose
    private final String full;
    @SerializedName("file")
    @Expose
    private final String file;
    @SerializedName("extension")
    @Expose
    private final String extension;
    @SerializedName("time")
    @Expose
    private final Integer time;
    @SerializedName("size")
    @Expose
    private final Integer size;

    public ImageModel(String full, String file, String extension, Integer time, Integer size) {
        this.full = full;
        this.file = file;
        this.extension = extension;
        this.time = time;
        this.size = size;
    }

    protected ImageModel(Parcel in) {
        full = in.readString();
        file = in.readString();
        extension = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readInt();
        }
        if (in.readByte() == 0) {
            size = null;
        } else {
            size = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(full);
        dest.writeString(file);
        dest.writeString(extension);
        if (time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(time);
        }
        if (size == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(size);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    public String getFull() {
        return full;
    }


    public String getFile() {
        return file;
    }


    public String getExtension() {
        return extension;
    }


    public Integer getTime() {
        return time;
    }


    public Integer getSize() {
        return size;
    }


}
