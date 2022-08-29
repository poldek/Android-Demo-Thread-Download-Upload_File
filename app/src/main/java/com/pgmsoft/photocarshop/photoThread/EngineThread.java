package com.pgmsoft.photocarshop.photoThread;

import android.util.Log;

import com.pgmsoft.photocarshop.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EngineThread extends Thread {

    public static final List<ClientEnginePhoto>  clientEnginePhotoArrayList = new ArrayList<>();
    public static PhotoListenerAction listenerActionPhoto = null;
    private static final boolean D = false;
    private static final String TAG = "ThreadEngine";
    public static final String URL_IMAGE = "{HTTP API GET IMAGE}";

    /**
     * if add new action blocked listArray
     */
    @Override
    public void run() {
        synchronized (clientEnginePhotoArrayList) {
            try {
                clientEnginePhotoArrayList.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * IMPORTANT end of thread !
     */
    public static void quit() {
        synchronized (clientEnginePhotoArrayList) {
            clientEnginePhotoArrayList.notify();
        }
    }

    /**
     * Client send new action - send photo
     * arg = path link to photo
     */
    public class ClientEnginePhoto extends Thread {
        private final String downloadFileName;


        public ClientEnginePhoto(String downloadFileName) {
            this.downloadFileName= downloadFileName;
        }

        @Override
        public void run() {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getFileFromUri(downloadFileName);
            synchronized (clientEnginePhotoArrayList) {
                clientEnginePhotoArrayList.remove(this);
            }
        }
    }

    /**
     * Listener connect to Service and UI
     */
    public void setListenerActionPhoto(PhotoListenerAction listenerActionPhoto) {
        EngineThread.listenerActionPhoto = listenerActionPhoto;
    }

    /**
     * Listener
     */
    public interface PhotoListenerAction {
       void photoSendStatus(String photo);
       void photoGetStatusCode(int statusCode);
    }

    /**
     * Send info name photo
     */
    public void sendPhotoStatusGetImage(String photo) {
        if(listenerActionPhoto != null) {
            listenerActionPhoto.photoSendStatus(photo);
        }
    }
    /**
     * Send code status code
     */
    public void sendPhotoStatusCode(int code) {
        if(listenerActionPhoto != null) {
            listenerActionPhoto.photoGetStatusCode(code);
        }
    }


    /**
     * Add new task from UI
     */
    public void getPhotoFromUrl(String downloadFileName) {
        synchronized (clientEnginePhotoArrayList) {
            ClientEnginePhoto clientEnginePhoto = new ClientEnginePhoto(downloadFileName);
            clientEnginePhotoArrayList.add(clientEnginePhoto);
            clientEnginePhoto.start();
        }
    }


    /**
     * Get from url
     */
    private void getFileFromUri(String downloadFileName) {
        File outputFile = null;
        sendPhotoStatusGetImage(downloadFileName);
        try {
            URL url = new URL(URL_IMAGE+"/"+downloadFileName);//Create Download URl
            HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
            c.connect();//connect the URL Connection

            //If Connection response is not OK then show Logs
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("Response", "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());
                sendPhotoStatusCode(c.getResponseCode()); // send status 404/401/500 etc ...
            } else {
                Log.e("Response", "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());
                sendPhotoStatusCode(c.getResponseCode()); // send status 404/401/500 etc ...
            }

            outputFile = new File(Utils.apkStorage, downloadFileName);//Create Output file in Main File
            FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
            InputStream is = c.getInputStream();//Get InputStream for connection

            byte[] buffer = new byte[1024];//Set buffer type
            int len1 = 0;//init length
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);//Write new file
            }

            //Close all connection after doing task
            fos.close();
            is.close();

        } catch (Exception e) {

            //Read exception if something went wrong
            e.printStackTrace();
            outputFile = null;
            Log.e("Error", "Download Error Exception " + e.getMessage());
            sendPhotoStatusCode(400);
        }
    }
}
