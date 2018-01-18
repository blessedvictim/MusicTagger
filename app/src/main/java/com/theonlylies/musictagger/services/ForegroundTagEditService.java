package com.theonlylies.musictagger.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.GlideRequest;
import com.theonlylies.musictagger.utils.MediaStoreUtils;
import com.theonlylies.musictagger.utils.ParcelableMusicFile;
import com.theonlylies.musictagger.utils.TagManager;
import com.theonlylies.musictagger.utils.adapters.MusicFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by theonlylies on 05.01.18.
 */

public class ForegroundTagEditService extends IntentService {

    public ForegroundTagEditService() {
        super("petuch");
        context = this;
    }

    Context context;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            ArrayList<ParcelableMusicFile> files = intent.getParcelableArrayListExtra("files");
            ParcelableMusicFile destinationMusicFile = intent.getParcelableExtra("dest_file");
            String uri = intent.getStringExtra("bitmap");
            if(!uri.equals("delete")){
                saveChanges(destinationMusicFile, files, Uri.parse(uri));
            }else{
                saveChanges(destinationMusicFile, files, null);
            }

            stopSelf();
        }
    }

    //TODO если выбрать мого файлов без альбумарта и сохранить без выбора или удаление картинки то TagManager попытается записать юитмаз из вектора что приведет к исключению

    public void saveChanges(ParcelableMusicFile destMusicFile, ArrayList<ParcelableMusicFile> sources, Uri bitmap) {
        Bitmap bmp = null;
        if (bitmap != null) {
        try {
            bmp = GlideApp.with(this)
                    .asBitmap()
                    .load(bitmap)
                    .centerCrop()
                    .submit()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


        for (ParcelableMusicFile f : sources) {
            TagManager tagManager = new TagManager(f.getRealPath());
            tagManager.setGeneralTagsFromMusicFile(destMusicFile);

            if (bitmap != null) {
                tagManager.setArtwork(bmp);
                Log.d("setuping bitmap","...");
            } else {
                Log.d("deletingg bitmap","...");
                tagManager.deleteArtwork();
            }
        }

        MediaStoreUtils.updateMuchFilesMediaStore(sources, this);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int notifyID = 1;
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "sfsdf";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }



        Notification notification = new NotificationCompat.Builder(context, "my_channel_01")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("All finished")
                .setContentText("Update tags finshed!")
                .setLargeIcon(bmp)
                .setChannelId("my_channel_01")
                .build();

        notificationManager.notify(1,notification);

        MediaStoreUtils.dumpAlbums(context);

    }
}