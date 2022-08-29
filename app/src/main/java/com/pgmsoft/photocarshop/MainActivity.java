package com.pgmsoft.photocarshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pgmsoft.photocarshop.databinding.ActivityMainBinding;
import com.pgmsoft.photocarshop.model_binding.StatusModel;
import com.pgmsoft.photocarshop.photoThread.EngineThread;
import com.pgmsoft.photocarshop.photoThread.ImageThread;
import com.pgmsoft.photocarshop.photoThread.ServiceThreadEngine;
import com.pgmsoft.photocarshop.photoThread.command.CommandImageStatus;
import com.pgmsoft.photocarshop.restapi.ImageModel;
import com.pgmsoft.photocarshop.ui.ImageRecyclerViewList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private ServiceThreadEngine.ListenerBinderService binder = null;
    private ActivityMainBinding activityMainBinding;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog dialog;
    private ViewHandler handler = null;
    private AlertDialog alertDialog;
    private ImageView imageView;
    private static final boolean D = true;
    private ImageRecyclerViewList imageRecyclerViewListAdapter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setTextStatus("Loading ...");
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_baseline_baby_changing_station_24);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialogMaterial(MainActivity.this);

        /*
         * Recycler view
         */
        RecyclerView recyclerView = findViewById(R.id.recycleViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerViewListAdapter = new ImageRecyclerViewList(this);
        recyclerView.setAdapter(imageRecyclerViewListAdapter);

        /*
         * Btn set click
         */
        activityMainBinding.setBtnStatus(this);
        activityMainBinding.setBtnGetImage(this);
        activityMainBinding.setBtnImageDirectory(this);

        /*
         * start service
         * start binder service
         */
        startService(new Intent(this, ServiceThreadEngine.class));
        bindService(new Intent(this, ServiceThreadEngine.class), serviceThreadConnection, 0);
        verifyStoragePermissions(this);
        handler = new ViewHandler();

        /*
        * RecyclerView click
        */
        imageRecyclerViewListAdapter.setOnItemClickListener(imageModel -> {
            if (!Utils.fileIfExist(imageModel.getFile())) {
                if (binder != null) {
                    if (Utils.isSDCardPresent()) {
                        dialog.setMessage("Photo : " + imageModel.getFile());
                        dialog.show();
                        binder.getPhotoFromUri(imageModel.getFile());
                        Toast.makeText(this, "Presents SDCARD", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Sdcard Not Presents ", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "The file already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onResume() {
        Log.e("Resume ", "Start");
        /*
         * start service
         * start binder service
         */
        startService(new Intent(this, ServiceThreadEngine.class));
        bindService(new Intent(this, ServiceThreadEngine.class), serviceThreadConnection, Context.BIND_AUTO_CREATE);
        if (imageRecyclerViewListAdapter.getItemCount() <= 0 ) {
            ImageThread.newJobImage(CommandImageStatus.PHOTO_GET, "","" );

        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        handler = null;
        if(binder != null) {
            binder.setListenerPhotoService(null);
            binder.removeStatusListener(commandStatusInterface);
        }
        binder = null;
        unbindService(serviceThreadConnection);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if(alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        alertDialog.onRestoreInstanceState(savedInstanceState.getBundle("DIALOG"));
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("DIALOG", alertDialog.onSaveInstanceState());
    }

    private final ServiceConnection serviceThreadConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (ServiceThreadEngine.ListenerBinderService) iBinder;
            binder.setListenerPhotoService(photoListenerAction);
            binder.addStatusListener(commandStatusInterface);
            ImageThread.getUploadStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }
    };

    /**
     * Get info status et ... from thread action
     * send info to Handler or HandlerView
     */
    private final EngineThread.PhotoListenerAction photoListenerAction = new EngineThread.PhotoListenerAction() {
        @Override
        public void photoSendStatus(String photo) {
            //setTextStatus(photo);
            Log.e("Image", " : " + photo);
            setTextStatus(photo);

        }

        @Override
        public void photoGetStatusCode(int statusCode) {
            setTextStatus(String.valueOf(statusCode));
            switch (statusCode) {
                case 404:
                    if(handler != null) {
                        handler.obtainMessage(3).sendToTarget();
                        setTextStatus("Api something wrong :" + String.valueOf(statusCode));
                    }
                    break;
                case 200:
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    handler.obtainMessage(5).sendToTarget();
                    break;
            }
        }
    };

    /**
     * Thread action in action
     * send info to Handler or HandlerView
     */
    private final ImageThread.CommandStatusInterface commandStatusInterface = new ImageThread.CommandStatusInterface() {

        @Override
        public void onProgress(boolean value, String imageName) {
            if(handler != null) {
                Message msg = handler.obtainMessage(1);
                Bundle data = new Bundle();
                data.putBoolean("status", value);
                data.putString("name", imageName);
                msg.setData(data);
                msg.sendToTarget();
            }
        }

        @Override
        public void onCompleted(String msg) {
            if(handler != null) {
                setTextStatus(msg);
            }
        }

        @Override
        public void onFailed(String msg) {
            if(handler != null) {
                handler.obtainMessage(3).sendToTarget();
                setTextStatus("Api something wrong :" + msg);
            }
        }

        @Override
        public void getPhoto(ArrayList<ImageModel> imageModels) {
            if (handler != null) {
                Message msg = handler.obtainMessage(4);
                Bundle data = new Bundle();
                data.putParcelableArrayList("images", imageModels);
                msg.setData(data);
                msg.sendToTarget();
            }
        }
    };

    public void btnGetImage() {
       if(binder != null) {
           Toast.makeText(this, "Get Image from server", Toast.LENGTH_SHORT).show();
           ImageThread.newJobImage(CommandImageStatus.PHOTO_GET, "","" );
       }
    }


    public void btnClickModel() {
        Toast.makeText(this, "Send photo", Toast.LENGTH_SHORT).show();
        if(binder != null) {
            //binder.addPhotoLink("New link action");
            List<String> imageName = new ArrayList<>();
            imageName.add("20210209_132956.jpg");
            imageName.add("20210515_115820.jpg");
            imageName.add("20210222_093108.jpg");
            imageName.add("20210628_154620.jpg");
            List<String> imagePath = new ArrayList<>();
            imagePath.add("/mnt/sdcard/DCIM/Camera/20210209_132956.jpg");
            imagePath.add("/mnt/sdcard/DCIM/Camera/20210515_115820.jpg");
            imagePath.add("/mnt/sdcard/DCIM/Camera/20210222_093108.jpg");
            imagePath.add("/mnt/sdcard/DCIM/Camera/20210628_154620.jpg");

            for (int i = 0; i < imageName.size(); i++) {
                ImageThread.newJobImage(CommandImageStatus.PHOTO_SEND, imageName.get(i),imagePath.get(i) );
            }
        }
    }



    private void setTextStatus(String status) {
        StatusModel statusModel = new StatusModel();
        statusModel.status = status;
        activityMainBinding.setTxtStatus(statusModel);
    }



    private class ViewHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d("Powiadamiacz", " : " + msg.what);
            switch (msg.what) {
                case 1:
                    boolean status = msg.peekData().getBoolean("status");
                    String name = msg.peekData().getString("name");
                    setTextStatus("Status image sending ... ");
                    if (status) {
                        if (!alertDialog.isShowing()) {
                            dialog.setMessage("Zdjęcie : " + name);
                            dialog.show();
                        }
                        showImage("/mnt/sdcard/DCIM/Camera/" + name);
                    } else {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                    break;
                case 2:
                    String completed = msg.peekData().getString("status");
                    setTextStatus(completed);
                    break;
                case 3:
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                    break;
                case 4:
                    List<ImageModel> imageModel = msg.peekData().getParcelableArrayList("images");
                    imageRecyclerViewListAdapter.listImage(imageModel);
                    break;
                case 5:

                    ImageThread.newJobImage(CommandImageStatus.PHOTO_GET, "","" );
                    break;
            }
        }
    }

    private void showImage(String pathImage) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap b = BitmapFactory.decodeFile(pathImage, options);
        Glide
                .with(this)
                .load(b)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(!doubleBackToExitPressedOnce){
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Click and exit.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                ServiceThreadEngine.exitService(this);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void dialogMaterial(Context context){
        alertDialog = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_baseline_baby_changing_station_24)
                .setTitle("Api Error")
                .setMessage("Api not responded !")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ServiceThreadEngine.exitService(MainActivity.this);
                        finish();
                    }
                })
                .create();
    }
}