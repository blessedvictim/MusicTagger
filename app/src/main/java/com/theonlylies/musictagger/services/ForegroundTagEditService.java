package com.theonlylies.musictagger.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MusicCache;
import com.theonlylies.musictagger.utils.ParcelableMusicFile;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.MediaStoreUtils;
import com.theonlylies.musictagger.utils.edit.TagManager;

import java.io.File;
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
            if(!uri.equals("delete") && !uri.equals("nothing")){
                saveChanges(destinationMusicFile, files, Uri.parse(uri),false);
            }else if(!uri.equals("delete")){
                saveChanges(destinationMusicFile, files, null,true);
            } else { // equals nothing
                saveChanges(destinationMusicFile, files, null,false);
            }

            stopSelf();
        }
    }

    //TODO если выбрать много файлов без альбумарта и сохранить без выбора или удаление картинки то TagManager попытается записать битмап из вектора что приведет к исключению

    public void saveChanges(final ParcelableMusicFile destMusicFile, final ArrayList<ParcelableMusicFile> sources,final Uri bitmap,boolean deleteArt) {
        int succesed = 0;

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

        ArrayList<Long> album_ids = new ArrayList<>();

        for (ParcelableMusicFile f : sources) {
            boolean haveSdCardAccess = FileUtil.canWriteThisFileSAF(this, f.getRealPath());
            if (!FileUtil.fileOnSdCard(new File(f.getRealPath()))) {
                Log.d("OneFileEdit", "file on internal !all nice...");
                TagManager tagManager = new TagManager(f.getRealPath());
                tagManager.setGeneralTagsFromMusicFile(
                        destMusicFile);
                if (bitmap != null) {
                    tagManager.setArtwork(bmp);
                } else if (deleteArt) {
                    tagManager.deleteArtwork();
                }
                succesed++;
            } else if (FileUtil.fileOnSdCard(new File(f.getRealPath())) && haveSdCardAccess) {
                Log.d("OneFileEdit", "file on sdcard ! EDITITNG!");
                MusicCache cache = new MusicCache(this);
                try {
                    File file = cache.cacheMusicFile(new File(f.getRealPath()));
                    TagManager tagManager = new TagManager(file.getPath());
                    tagManager.setTagsFromMusicFile(
                            destMusicFile);
                    if (bitmap != null) {
                        tagManager.setArtwork(bmp);
                    } else if (deleteArt) {
                        tagManager.deleteArtwork();
                    }

                    cache.replaceCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                succesed++;
            } else {
                Log.d("OneFileEdit", "fuck you sd card access");
                Toast.makeText(this, "fuck you sd card access", Toast.LENGTH_SHORT).show();
            }
        }


        Bitmap finalBmp = bmp;
        int finalSuccesed = succesed;
        MediaStoreUtils.updateMuchFilesMediaStore(sources, getApplicationContext(), new MediaScannerConnection.OnScanCompletedListener() {
            int loop=0;
            @Override
            public void onScanCompleted(String path, Uri uri) {
                loop++;
                if(loop==sources.size()){
                    MusicFile musicFile = MediaStoreUtils.getMusicFileByPath(sources.get(0).getRealPath(),getApplicationContext());
                    boolean albumEditMust=false;
                    for(MusicFile f:sources){
                        if(f.getAlbum_id()==musicFile.getAlbum_id()){
                            if(bitmap!=null){
                                Bitmap bmp = null;
                                try {
                                    bmp = GlideApp.with(getApplicationContext())
                                            .asBitmap()
                                            .load(destMusicFile.getArtworkUri())
                                            .signature(new MediaStoreSignature("lolers",System.currentTimeMillis(),3))
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .centerCrop()
                                            .submit()
                                            .get();
                                    Log.d("artUri",destMusicFile.getArtworkUri().toString());
                                    Log.d("result=",String.valueOf( MediaStoreUtils.setAlbumArt(bmp, context, f.getAlbum_id()) )+"  "+f.getAlbum_id());
                                    Log.d("group","set album art for group");
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }else if(deleteArt) {
                                MediaStoreUtils.deleteAlbumArt(getApplicationContext(),musicFile.getAlbum_id());
                            }
                            break;
                        }
                    }

                    //end edit album art
                    Log.d("ForegroundService","Scan completed!");
                    Intent i = new Intent("CHANGE_TAG_FINISHED_ADMT");
                    sendBroadcast(i);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        int notifyID = 1;
                        String CHANNEL_ID = "my_channel_01";// The id of the channel.
                        CharSequence name = "General";// The user-visible name of the channel.
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                        notificationManager.createNotificationChannel(mChannel);
                    }



                    Notification notification = new NotificationCompat.Builder(context, "my_channel_01")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Update tags finshed!")
                            .setContentText("Successed : " + finalSuccesed + " Failed : " + (sources.size() - finalSuccesed))
                            .setLargeIcon(finalBmp)
                            .setChannelId("my_channel_01")
                            .build();

                    notificationManager.notify(1,notification);

                    MediaStoreUtils.dumpAlbums(context);

                }

            }
        });

    }
}