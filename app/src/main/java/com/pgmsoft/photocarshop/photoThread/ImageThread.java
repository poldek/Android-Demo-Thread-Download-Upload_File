package com.pgmsoft.photocarshop.photoThread;

import android.util.Log;

import androidx.annotation.NonNull;

import com.pgmsoft.photocarshop.Utils;
import com.pgmsoft.photocarshop.photoThread.command.CommandImageStatus;
import com.pgmsoft.photocarshop.restapi.ApiRestHolder;
import com.pgmsoft.photocarshop.restapi.ImageModel;
import com.pgmsoft.photocarshop.restapi.ImageRestHandler;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ImageThread extends Thread {

    private static final List<CommandImageStatus> listCommand = new ArrayList<>();
    private static final List<CommandStatusInterface> listCommandStatus = new ArrayList<>();
    private static boolean quit = false;
    private static boolean onProgress = false;
    private static String IMAGE_NAME = null;
    private static final boolean D = true;
    public static final String ADRES = "{GET IMAGE FROM SERVER API}"; //GET IMAGE FROM SERVER API



    public ImageThread() {
        synchronized (listCommand) {
            listCommand.clear();
            listCommandStatus.clear();
            quit = false;
            onProgress = false;
            IMAGE_NAME = null;
        }
    }

    /**
     * quit to thread
     */
    public static void quit() {
        synchronized (listCommand) {
            quit = true;
            listCommand.notify();
        }
    }

    /**
     * Communication to Service add command
     */
    public void addCommandStatusListener(CommandStatusInterface commandStatusInterface) {
        synchronized (listCommandStatus) {
            listCommandStatus.add(commandStatusInterface);
        }
    }

    /**
     * Communication to Service remove command
     */
    public void removeCommandStatusListener(CommandStatusInterface commandStatusInterface) {
        synchronized (listCommandStatus) {
            listCommandStatus.remove(commandStatusInterface);
        }
    }


    /**
     * Comunication to Thread command completed - interface
     */

    private void commandOnCompletedListener(String msg) {
        synchronized (listCommandStatus) {
            int size = listCommandStatus.size();
            for (int i = 0; i < size; i++) {
                listCommandStatus.get(i).onCompleted(msg);
            }
        }
    }
    /**
     * Comunication to Thread command progress value - interface
     */

    private static void commandOnProgressListener(boolean value, String imageName) {
        synchronized (listCommandStatus) {
            int size = listCommandStatus.size();
            for (int i = 0; i < size; i++) {
                listCommandStatus.get(i).onProgress(value, imageName);
            }
        }
    }

    /**
     * Comunication to Thread command failed - interface
     */

    private void commandOnFailedListener(String msg) {
        synchronized (listCommandStatus) {
            int size = listCommandStatus.size();
            for (int i = 0; i < size; i++) {
                listCommandStatus.get(i).onFailed(msg);
            }
        }
    }

    /**
     * Comunication to Thread command get image - interface
     */

    private void commandGetImageListener(ArrayList<ImageModel> imageModels) {
        synchronized (listCommandStatus) {
            int size = listCommandStatus.size();
            for (int i = 0; i < size; i++) {
                listCommandStatus.get(i).getPhoto(imageModels);
            }
        }
    }



    /**
     * Engine thread
     */
    @Override
    public void run() {
        if (D) Log.e("@@@", "Command list, Start");
        while (!quit) {
            CommandImageStatus command =  null;
            synchronized (listCommand) {
                int size  = listCommand.size();
                if (size == 0) {
                    try {
                        listCommand.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    command = listCommand.remove(0);
                }
            }

            /*
             * Action thread load or send image file etc ...
             */
            if (command != null) {
                switch (command.getTarget()) {
                    case CommandImageStatus.PHOTO_SEND:
                        String imageName = command.getImageName();
                        String imagePath = command.getImagePath();
                        uploadFile(imagePath, imageName);
                        break;
                    case CommandImageStatus.PHOTO_STATUS:
                        //Check Api
                        break;
                    case CommandImageStatus.PHOTO_GET:
                        if (D) Log.e("@@@", "Get image thread ");
                        getImageFromServer();
                        break;
                }
            }
        }
        if (D) Log.e("@@@", "Command list, Ended");
    }

    //status
    public static void getUploadStatus(){
        synchronized (listCommand){
            commandOnProgressListener(onProgress,IMAGE_NAME);
        }
    }

    /**
     * New Job load image
     */
    public static void newJobImage(int commandTarget, String imageName, String imagePath){
        synchronized (listCommand) {
            CommandImageStatus commandImageStatus = new CommandImageStatus(commandTarget,imageName, imagePath);
            listCommand.add(commandImageStatus);
            listCommand.notify();
        }
    }


    /**
     * For Activity
     */
    public interface CommandStatusInterface {
        void onProgress(boolean value, String imageName); // status list command
        void onCompleted(String msg); // send photo
        void onFailed(String msg);
        void getPhoto(ArrayList<ImageModel> imageModels);
    }


    private void uploadFile(String path, String imageName) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image",imageName,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(path)))
                .addFormDataPart("_method","PATCH")
                .addFormDataPart("name","Zdjecie")
                .build();
        Request request = new Request.Builder()
                .url(Utils.BASE_URL + "/image")
                    .method("POST", body)
                .build();
        try {
            //Listener show dialog
            synchronized (listCommand){
                onProgress = true;
                IMAGE_NAME = imageName;
            }
            commandOnProgressListener(onProgress, IMAGE_NAME);

            Response response = client.newCall(request).execute();

            synchronized (listCommand){
                onProgress = false;
                IMAGE_NAME= imageName;
            }
            commandOnProgressListener(onProgress, IMAGE_NAME);

            if (D) Log.e("@@@", "Api Rest :" + response.code());

            /*
             * Decode JSON message
             */
            String responseData = response.body().string();
            JSONObject json = new JSONObject(responseData);
            final String code = json.getString("code");
            final String message = json.getString("message");

            switch (response.code()) {
                case 200:
                    commandOnCompletedListener(message);
                    break;
                case 404:
                    commandOnFailedListener(message);
                    break;
            }

        } catch (IOException exception) {
            commandOnFailedListener(exception.getMessage());
            exception.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getImageFromServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ADRES)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        ApiRestHolder apiRestHolder = retrofit.create(ApiRestHolder.class);
        Call<ImageRestHandler> repos = apiRestHolder.getImagePacjent();
        repos.enqueue(new Callback<ImageRestHandler>() {
            @Override
            public void onResponse(@NonNull Call<ImageRestHandler> call, @NonNull retrofit2.Response<ImageRestHandler> response) {
                if (!response.isSuccessful()) {
                    if (response.code() == 500) {
                        commandOnFailedListener("Error API 500");
                    } else if (response.code() == 401) {
                        commandOnFailedListener("Error API 401");
                    } else if (response.code() == 404) {
                        commandOnFailedListener("Error API 404");
                    }
                    return;
                }
                if( response.body() != null ) {
                    //if (D) Log.e("@@@", "File extension: " + response.body().getFiles().get(0).getExtension());
                    //if (D) Log.e("@@@", "File extension: " + response.body().getFiles().get(0).getFull());
                    ArrayList<ImageModel> imageModels = new ArrayList<>();
                    for (int i = 0; i < response.body().getFiles().size(); i++) {
                        imageModels.add(new ImageModel(
                                response.body().getFiles().get(i).getFull(),
                                response.body().getFiles().get(i).getFile(),
                                response.body().getFiles().get(i).getExtension(),
                                response.body().getFiles().get(i).getTime(),
                                response.body().getFiles().get(i).getSize()
                        ));
                        commandGetImageListener(imageModels);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ImageRestHandler> call, @NonNull Throwable t) {
                if (D) Log.e("@@@", "Rest API error");
                commandOnFailedListener(t.getMessage());
            }
        });
    }
}
