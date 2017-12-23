package com.theonlylies.musictagger.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.theonlylies.musictagger.R;

/**
 * Created by linuxoid on 22.12.17.
 */

public class SimpleTagActivity extends AppCompatActivity {

    TextView textView,textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tageditorsimple);
        textView=findViewById(R.id.textView2);
        textView1=findViewById(R.id.textView3);
        textView.setText(getIntent().getCharSequenceExtra("title"));
        textView1.setText(getIntent().getCharSequenceExtra("other"));
    }
}
