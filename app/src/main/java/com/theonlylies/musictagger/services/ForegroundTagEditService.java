package com.theonlylies.musictagger.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.activities.MuchFileEditActivity;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MusicCache;
import com.theonlylies.musictagger.utils.ParcelableMusicFile;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.BitmapUtils;
import com.theonlylies.musictagger.utils.edit.MediaStoreUtils;
import com.theonlylies.musictagger.utils.edit.TagManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by theonlylies on 05.01.18.
 */

public class ForegroundTagEditService extends IntentService {

    public ForegroundTagEditService() {
        super("ForegroundService");
        context = this;
    }

    Context context;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final ResultReceiver resultReceiver = intent.getParcelableExtra("result");
            ArrayList<ParcelableMusicFile> files = intent.getParcelableArrayListExtra("files");
            ParcelableMusicFile destinationMusicFile = intent.getParcelableExtra("dest_file");
            String uri = intent.getStringExtra("bitmap");
            if (!uri.equals("delete") && !uri.equals("nothing")) {
                saveChanges(destinationMusicFile, files, Uri.parse(uri), false);
            } else if (!uri.equals("delete")) {
                saveChanges(destinationMusicFile, files, null, true);
            } else { // equals nothing
                saveChanges(destinationMusicFile, files, null, false);
            }


        }
    }

    int scannedCount = 0;
    //TODO если выбрать много файлов без альбумарта и сохранить без выбора или удаление картинки то TagManager попытается записать битмап из вектора что приведет к исключению

    public void saveChanges(final ParcelableMusicFile destMusicFile, final ArrayList<ParcelableMusicFile> sources, final Uri artworkUri, boolean deleteArt) {
        int succesed = 0;
        Bitmap bmp = null;
        if (artworkUri != null) {
            try {
                bmp = GlideApp.with(this)
                        .asBitmap()
                        .load(artworkUri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .submit()
                        .get();
                bmp = BitmapUtils.getCenterCropedBitmap(bmp);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Log.e("ForegroundService", "bitmap wasn't loaded");
            }
        }

        for (ParcelableMusicFile f : sources) {
            boolean haveSdCardAccess = FileUtil.canWriteThisFileSAF(this, f.getRealPath());
            if (!FileUtil.fileOnSdCard(new File(f.getRealPath()))) {
                Log.d("OneFileEdit", "file on internal !all nice...");
                TagManager tagManager = new TagManager(f.getRealPath());
                tagManager.setGeneralTagsFromMusicFile(
                        destMusicFile);
                if (artworkUri != null) {
                    tagManager.setArtwork(bmp);
                } else if (deleteArt) {
                    tagManager.deleteArtwork();
                }
                succesed++;
                tagManager.save();
            } else if (FileUtil.fileOnSdCard(new File(f.getRealPath())) && haveSdCardAccess) {
                Log.d("OneFileEdit", "file on sdcard ! EDITITNG!");
                MusicCache cache = new MusicCache(this);
                try {
                    File file = cache.cacheMusicFile(new File(f.getRealPath()));
                    TagManager tagManager = new TagManager(file.getPath());
                    tagManager.setGeneralTagsFromMusicFile(
                            destMusicFile);
                    if (artworkUri != null) {
                        tagManager.setArtwork(bmp);
                    } else if (deleteArt) {
                        tagManager.deleteArtwork();
                    }
                    tagManager.save();
                    cache.replaceCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                succesed++;

            } else {
                Log.d("Foreground service", "fuck you sd card access");
                Toast.makeText(this, "fuck you sd card access", Toast.LENGTH_SHORT).show();
            }
        }


        Bitmap finalBmp = bmp;//just copy of bmp link for ...
        List<MusicFile> scannedFile = new ArrayList<>();
        MediaStoreUtils.updateMuchFilesMediaStore(sources, getApplicationContext(), new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        scannedCount++;
                        MusicFile ddd = MediaStoreUtils.getMusicFileByPath(path, context);
                        scannedFile.add(ddd);
                        Log.e("Scanned file album_id ", String.valueOf(ddd.getAlbum_id()));

                        if (scannedCount == sources.size()) {
                            long tmpAlbumId = scannedFile.get(0).getAlbum_id();
                            if (deleteArt) {
                                for (MusicFile file : scannedFile) {
                                    Log.e("deleted art", String.valueOf(MediaStoreUtils.deleteAlbumArt(context, file.getAlbum_id())));
                                }
                            } else if (artworkUri != null) {
                                MediaStoreUtils.setAlbumArt(finalBmp, context, scannedFile.get(0).getAlbum_id());
                                for (MusicFile file : scannedFile) {
                                    if (tmpAlbumId != file.getAlbum_id()) {
                                        MediaStoreUtils.setAlbumArt(finalBmp, context, file.getAlbum_id());
                                    }
                                    tmpAlbumId = file.getAlbum_id();
                                }
                            }


                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                String CHANNEL_ID = "my_channel_01";// The id of the channel.
                                CharSequence name = "General";// The user-visible name of the channel.
                                int importance = NotificationManager.IMPORTANCE_HIGH;
                                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                                notificationManager.createNotificationChannel(mChannel);
                            }

                            Notification notification = new NotificationCompat.Builder(context, "my_channel_01")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Update tags finshed!")
                                    .setContentText("Files was edited")
                                    .setLargeIcon(finalBmp)
                                    .setChannelId("my_channel_01")
                                    .build();

                            notificationManager.notify(1, notification);

                            Intent finishIntent = new Intent(MuchFileEditActivity.BROADCAST_ACTION);
                            sendBroadcast(finishIntent);

                            stopSelf();
                        }


                    }
                }
        );

    }
}