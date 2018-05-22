package com.theonlylies.musictagger.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.PreferencesManager;

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
        ConstraintLayout layout = findViewById(R.id.splashLayout);
        layout.animate().alpha(1).setDuration(900).withEndAction(this::initPermissions);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void openSdCardTree() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(R.layout.sd_dialog)
                .setTitle(R.string.attention_string);
        builder.setPositiveButton("Ok", (dialog, id) -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, OPEN_TREE_REQUEST_CODE);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void initPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.write_access_string)
                    .setTitle(R.string.attention_string);
            builder.setPositiveButton("Ok", (dialog, id) -> ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 5));

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
            if (uri == null) {
                Toast.makeText(this, R.string.null_uri_string, Toast.LENGTH_LONG).show();
                PreferencesManager.putValue(this, "sdcard_uri", null);
            } else {
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                Log.d("nonpersisturi", resultData.getData().toString());
                Log.d("persisturi", uri.toString());

                PreferencesManager.putValue(this, "sdcard_uri", uri.toString());
            }



            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }else if(requestCode == OPEN_TREE_REQUEST_CODE){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sd_access_attention_string)
                    .setTitle(R.string.attention_string);
            builder.setPositiveButton("Ok", (dialog, id) -> openSdCardTree());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
