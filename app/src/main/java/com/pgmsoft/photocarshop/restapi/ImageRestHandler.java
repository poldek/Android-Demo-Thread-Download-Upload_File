package com.pgmsoft.photocarshop.restapi;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImageRestHandler {

    @SerializedName("files")
    @Expose
    private List<ImageModel> files = null;
    public List<ImageModel> getFiles() {
        return files;
    }

}


