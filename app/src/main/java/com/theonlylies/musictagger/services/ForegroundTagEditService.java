package com.theonlylies.musictagger.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.activities.ArtworkAction;
import com.theonlylies.musictagger.activities.MuchFileEditActivity;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MusicCache;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.BitmapUtils;
import com.theonlylies.musictagger.utils.edit.MediaStoreUtils;
import com.theonlylies.musictagger.utils.edit.TagManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by theonlylies on 05.01.18.
 */

public class ForegroundTagEditService extends IntentService {

    public ForegroundTagEditService() {
        super("ForegroundService");
        context = this;
    }


    Context context;
    ArtworkAction artworkAction;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final ResultReceiver resultReceiver = intent.getParcelableExtra("result");
            ArrayList<MusicFile> files = intent.getParcelableArrayListExtra("files");
            MusicFile destinationMusicFile = intent.getParcelableExtra("dest_file");
            String uri = intent.getStringExtra("bitmap");
            artworkAction = (ArtworkAction) intent.getSerializableExtra("artwork_action");
            Log.e("artworkAction", artworkAction.name());
            switch (artworkAction) {
                case CHANGED: {
                    saveChanges(destinationMusicFile, files, Uri.parse(uri), artworkAction);
                    break;
                }
                default:
                    saveChanges(destinationMusicFile, files, null, artworkAction);
            }

        }
    }

    int scannedCount = 0;
    //TODO если выбрать много файлов без альбумарта и сохранить без выбора или удаление картинки то TagManager попытается записать битмап из вектора что приведет к исключению

    public void saveChanges(final MusicFile destMusicFile, final ArrayList<MusicFile> sources, final Uri artworkUri, ArtworkAction artworkAction) {
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

        for (MusicFile f : sources) {
            boolean haveSdCardAccess = FileUtil.canWriteThisFileSAF(this, f.getRealPath());
            if (!FileUtil.fileOnSdCard(new File(f.getRealPath()))) {
                Log.d("OneFileEdit", "file on internal !all nice...");
                TagManager tagManager = new TagManager(f.getRealPath());
                tagManager.setGeneralTagsFromMusicFile(destMusicFile);
                switch (artworkAction) {
                    case CHANGED: {
                        if(bmp!=null){
                            tagManager.setArtwork(bmp);
                            //Toast.makeText(this,"Artwork wasn't loaded",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case DELETED: {
                        tagManager.deleteArtwork();
                    }
                }
                succesed++;

                tagManager.save();
                TagManager.rewriteTag(f.getRealPath());
            } else if (FileUtil.fileOnSdCard(new File(f.getRealPath())) && haveSdCardAccess) {
                Log.d("OneFileEdit", "file on sdcard ! EDITITNG!");
                MusicCache cache = new MusicCache(this);
                try {
                    File file = cache.cacheMusicFile(new File(f.getRealPath()));
                    TagManager tagManager = new TagManager(file.getPath());
                    tagManager.setGeneralTagsFromMusicFile(destMusicFile);
                    switch (artworkAction) {
                        case CHANGED: {
                            if(bmp!=null){
                                tagManager.setArtwork(bmp);
                                //Toast.makeText(this,"Artwork wasn't loaded",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case DELETED: {
                            tagManager.deleteArtwork();
                            break;
                        }

                    }
                    tagManager.save();
                    TagManager.rewriteTag(file.getAbsolutePath());
                    cache.replaceCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                succesed++;

            } else {
                Log.d("Foreground service", "fuck you sd card access");
                Toast.makeText(this, "Make sure that access to the microsd", Toast.LENGTH_SHORT).show();
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
                            Log.e("scannedSection", "start");
                            Log.e("artworkAction", artworkAction.name());

                            List<Long> albIds = StreamSupport.stream(scannedFile).map(MusicFile::getAlbum_id).distinct().collect(Collectors.toList());
                            //FIXME need to rewrite change album art logic
                            switch (artworkAction) {
                                case CHANGED: {
                                    MediaStoreUtils.setAlbumArt(finalBmp, context, scannedFile.get(0).getAlbum_id());
                                    for (Long id : albIds)
                                        MediaStoreUtils.setAlbumArt(finalBmp, context, id);
                                    break;
                                }
                                case DELETED: {
                                    for (MusicFile file : scannedFile)
                                        Log.e("deleted art", String.valueOf(MediaStoreUtils.deleteAlbumArt(context, file.getAlbum_id())));
                                    break;
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
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                            R.mipmap.ic_launcher))
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