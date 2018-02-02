package com.theonlylies.musictagger.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.theonlylies.musictagger.R;

/**
 * Created by theonlylies on 24.01.18.
 */

public class PreferencesActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    EditTextPreference sdcardEditText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.preference_headers);
        sdcardEditText = (EditTextPreference) findPreference("sdcard_uri");

        sdcardEditText.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("sdcard_uri")) {
            openSdCardTree();
        }
        return true;
    }

    private final int OPEN_TREE_REQUEST_CODE = 44;

    private void openSdCardTree() {
        // ACTION_OPEN_DOCUMENT_TREE is the intent to choose a file TREE via the system's
        // file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, OPEN_TREE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.d("onActivityResult:", "onActivityResult");
        if (requestCode == OPEN_TREE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri uri = resultData.getData();

            final int takeFlags = resultData.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            Log.d("PrefActivity", uri.toString());
            sdcardEditText.setText(uri.toString());
        }
    }

    /*@Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }*/
}
