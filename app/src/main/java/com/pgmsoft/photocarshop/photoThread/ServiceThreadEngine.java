package com.pgmsoft.photocarshop.photoThread;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ServiceThreadEngine extends Service {

    private static final String EXIT_ACTION_SERVICE = " com.pgmsoft.photocarshop.photo";
    private EngineThread engineThread = null;
    private ImageThread imageThread = null;
    private final ListenerBinderService listenerBinderService = new ListenerBinderService();

    /**
     * Start Root Thread
     */
    @Override
    public void onCreate() {

        if(engineThread != null){
            EngineThread.quit();
        }
        engineThread = new EngineThread();
        engineThread.start();


        if (imageThread != null) {
            ImageThread.quit();
        }
        imageThread = new ImageThread();
        imageThread.start();

    }

    /**
     * Action Exit Service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(EXIT_ACTION_SERVICE)){
                    stopSelf();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EngineThread.quit();
        ImageThread.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return listenerBinderService;
    }

    public class ListenerBinderService extends Binder {

        public void getPhotoFromUri(String downloadFileName){
            if(engineThread != null){
                engineThread.getPhotoFromUrl(downloadFileName);
            }
        }

        public void setListenerPhotoService(EngineThread.PhotoListenerAction listenerPhotoService){
            if(engineThread != null){
               engineThread.setListenerActionPhoto(listenerPhotoService);

            }
        }


        public void addStatusListener(ImageThread.CommandStatusInterface imageThreadListener) {

            if (imageThreadListener != null) {
                imageThread.addCommandStatusListener(imageThreadListener);
            }
        }
        public void removeStatusListener(ImageThread.CommandStatusInterface imageThreadListener) {

            if (imageThreadListener != null) {
                imageThread.removeCommandStatusListener(imageThreadListener);
            }
        }
    }

    /**
     * End of Service after exit APP
     */
    public static void exitService(Context context) {
        Intent intentSerwisu = new Intent(context, ServiceThreadEngine.class);
        intentSerwisu.setAction(EXIT_ACTION_SERVICE);
        context.startService(intentSerwisu);
    }
}