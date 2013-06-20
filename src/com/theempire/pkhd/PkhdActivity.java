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
    public HashMap<String, ImageView> player_holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        player_holder = new HashMap<String, ImageView>();
        image_map = new HashMap<String, Integer>();
        Context context = getApplicationContext();
        int image_id = 0;
        /* Can stuff in "base_p_right", "base_k_left" etc. */
        for (String base_name : new String[] { "base_p_left", "base_p_right" }) {
            for (int i = 0; i < 12; i++) {
                image_id = context.getResources().getIdentifier(base_name + "_" + i, "drawable", context.getPackageName());
                image_map.put(base_name + i, image_id);
            }
        }

        setContentView(R.layout.activity_pkhd);
        player_holder.put("player_left", (ImageView) findViewById(R.id.player_left));
        player_holder.put("player_right", (ImageView) findViewById(R.id.player_right));

        findViewById(R.id.p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new PkhdAnimator("left")).start();
            }
        });

        findViewById(R.id.d).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new PkhdAnimator("right")).start();
            }
        });
    }

    class PkhdAnimator implements Runnable {
        private String target;

        public PkhdAnimator(String target) {
            this.target = target;
        }

        @Override
        public void run() {
            int counter = 12;
            while (--counter >= 0) {
                /* sleep */
                Log.e("PkhdActivity", "Looping with counter: " + counter + " on target: " + this.target);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /* run on ui thread */
                runOnUiThread(new PkhdBlitter(this.target));
            }
        }
    }

    class PkhdBlitter implements Runnable {
        private String target;

        public PkhdBlitter(String target) {
            this.target = target;
        }

        @Override
        public void run() {
            Log.e("PkhdActivity", "Looping on target: " + this.target);

            String player = "player_" + this.target;
            int num = (Integer.parseInt((String) player_holder.get(player).getTag()) + 1) % 12;
            String action_type = "base_p_" + this.target + num;

            player_holder.get(player).setImageResource(image_map.get(action_type));
            player_holder.get(player).setTag(Integer.toString(num));
        }
    }
}