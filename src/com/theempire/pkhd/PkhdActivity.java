package com.theempire.pkhd;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PkhdActivity extends Activity {
    public HashMap<String, Integer> image_map;
    public ImageView player_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        image_map = new HashMap<String, Integer>();
        Context context = getApplicationContext();
        int image_id = 0;
        /* Can stuff in "base_p_right", "base_k_left" etc. */
        for (String base_name : new String[] { "base_p_left" }) {
            for (int i = 0; i < 12; i++) {
                image_id = context.getResources().getIdentifier(base_name + "_" + i, "drawable", context.getPackageName());
                image_map.put(base_name + i, image_id);
            }
        }

        setContentView(R.layout.activity_pkhd);
        player_left = (ImageView) findViewById(R.id.player_left);
        findViewById(R.id.player_left).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /* Redraw image. */
                Log.e("PkhdActivity", "Player Left was clicked. Tag: " + player_left.getTag());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int counter = 12;
                        while (--counter >= 0) {
                            /* sleep */
                            Log.e("PkhdActivity", "Looping with counter: " + counter);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            /* run on ui thread */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int num = (Integer.parseInt((String) player_left.getTag()) + 1) % 12;
                                    player_left.setImageResource(image_map.get("base_p_left" + num));
                                    player_left.setTag(Integer.toString(num));
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}