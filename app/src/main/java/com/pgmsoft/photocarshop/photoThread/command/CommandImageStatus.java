package com.pgmsoft.photocarshop.photoThread.command;

public class CommandImageStatus {

    public static final int PHOTO_SEND = 0;
    public static final int PHOTO_STATUS = 1;
    public static final int PHOTO_GET = 2;
    private final int target;
    private final String imageName;
    private final String imagePath;

    /**
     * Send data
     */
    public CommandImageStatus(int target, String imageName, String imagePath) {
        this.target = target;
        this.imageName = imageName;
        this.imagePath = imagePath;
    }

    public int getTarget() {
        return target;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImagePath() {
        return imagePath;
    }
}
