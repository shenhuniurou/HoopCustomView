package com.xx.hoopcustomview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements HoopView.OnClickButtonListener {

    private HoopView hoopview1;
    private HoopView hoopview2;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hoopview1 = (HoopView) findViewById(R.id.hoopview1);
        hoopview1.setOnClickButtonListener(this);

        hoopview2 = (HoopView) findViewById(R.id.hoopview2);
        hoopview2.setOnClickButtonListener(this);
    }


    @Override public void clickButton(View view, int num) {
        if (view.getId() == R.id.hoopview1) {
            Toast.makeText(this, "hoopview1增加" + num, Toast.LENGTH_SHORT).show();
            hoopview1.setCount(num);
        } else if (view.getId() == R.id.hoopview2) {
            Toast.makeText(this, "hoopview2增加" + num, Toast.LENGTH_SHORT).show();
            hoopview2.setCount(num);
        }
    }

}
