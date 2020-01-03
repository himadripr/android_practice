package com.himadri.triangularcustomviewsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.shuvam.triangleindicator.TriangularIndicator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TriangularIndicator mCustomView = (TriangularIndicator) findViewById(R.id.triangle);
        final int [] res = { R.drawable.ic_down,
                R.drawable.ic_battery,
                R.drawable.ic_clip,
                R.drawable.ic_rotate,
                R.drawable.ic_timer};
        mCustomView.setResources(res);
        mCustomView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float section = (int)Math.floor(event.getX()*(res.length)/mCustomView.getWidth());
                Toast.makeText(MainActivity.this, ""+section, Toast.LENGTH_SHORT).show();
                Log.d("Touch event value",""+section);
                return false;
            }
        });
    }
}
