package com.theonlylies.musictagger.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.PreferencesManager;

import dalvik.system.DexFile;

/**
 * Created by theonlylies on 30.12.17.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int OPEN_TREE_REQUEST_CODE = 6 ;


    ImageView image;
    TextView text;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //text = findViewById(R.id.splashText);
        //image = findViewById(R.id.splashImage);
        ConstraintLayout layout = findViewById(R.id.splashLayout);
        layout.animate().alpha(1).setDuration(900).withEndAction(() -> initPermissions());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void openSdCardTree() {
        // ACTION_OPEN_DOCUMENT_TREE is the intent to choose a file TREE via the system's
        // file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, OPEN_TREE_REQUEST_CODE);
    }

    private void initPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOL", "QweQQW");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("You must provide storage write permissons for get this app work")
                    .setTitle("Attention");
            builder.setPositiveButton("understand", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET}, 5);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else{
            if(!FileUtil.haveSdCardWriteAccess(this) && FileUtil.hasRomovableDeivce(this)){
                Log.d("SplashActivity","havent sdcard rights !!!");
                openSdCardTree();
            }else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("PERMISSONS:", "START");
        if (5 == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSONS:", "GRANTED!!!");
                if(!FileUtil.haveSdCardWriteAccess(this)){
                    Log.d("SplashActivity","havent sdcard rights !!!");
                    openSdCardTree();
                }
            } else {
                Log.d("PERMISSONS:", "UNGRANTED!!!");
                initPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.d("onActivityResult:", "onActivityResult");
        if (requestCode == OPEN_TREE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri uri = resultData.getData();

            final int takeFlags = resultData.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri,takeFlags);

            Log.d("nonpersisturi",resultData.getData().toString());
            Log.d("persisturi",uri.toString());

            PreferencesManager.putValue(this,"sdcard_uri",uri.toString());

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }else if(requestCode == OPEN_TREE_REQUEST_CODE){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must provide ds storage write permissons for get this app work\nPlease select root sd-card directory")
                    .setTitle("Attention");
            builder.setPositiveButton("setup", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    openSdCardTree();
                }
            });
            builder.setNegativeButton("next without this", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    openSdCardTree();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show(); //TODO repeat question about sdcardtree!
        }
    }
}
