package com.pgmsoft.photocarshop;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;

public class Utils {

    public static String BASE_URL = "{HTTP_API}";
    private static final String DIR_IMAGE = "PhotoCar";
    public static  final File apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + DIR_IMAGE);


    public static boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!apkStorage.exists()) {
                apkStorage.mkdir();
                Log.e("=>", "Directory Created.");
            }
            return true;
        }
        return false;
    }

    public static String timeConvert(int time) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(" HH:mm:ss");
        String dateString = simpleDateFormat.format(time);
        return String.format("Hours: %s", dateString);
    }



    public static String bytesIntoHumanReadable(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }

    public static boolean fileIfExist(String fileName) {
        File file = new File(apkStorage,fileName);
        if(file.exists()){
            return true;
        }
        else{
           return false;
        }
    }
}
