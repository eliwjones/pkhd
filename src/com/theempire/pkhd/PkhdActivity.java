package com.theempire.pkhd;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PkhdActivity extends Activity implements View.OnClickListener{
    public HashMap<Integer, String> name_id_map;
    public HashMap<String, Integer> image_map;
    public HashMap<String, ImageView> animateable_holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_pkhd);

        animateable_holder = new HashMap<String, ImageView>();
        image_map = new HashMap<String, Integer>();
        name_id_map = new HashMap<Integer, String>();

        int image_id = 0;
        /* Can stuff in "base_p_right", "base_k_left" etc. */
        for (String base_name : new String[] { "base_p_left", "base_p_right", "base_k_left", "base_k_right", "base_h_left", "base_h_right", "base_d_left", "base_d_right" }) {
            for (int i = 0; i < 12; i++) {
                image_id = context.getResources().getIdentifier(base_name + "_" + i, "drawable", context.getPackageName());
                image_map.put(base_name + i, image_id);
            }
        }
        String view_id_string = "";
        for (String player_name : new String[] {"left", "right"}){
            for (String action : new String[] {"p","k","h","d"}){
                view_id_string = action + "_" + player_name;
                image_id = context.getResources().getIdentifier(view_id_string, "id", context.getPackageName());
                if(image_id != 0){
                    findViewById(image_id).setOnClickListener(this);
                }
                name_id_map.put(image_id, view_id_string);
            }
        }


        animateable_holder.put("player_left", (ImageView) findViewById(R.id.player_left));
        animateable_holder.put("player_right", (ImageView) findViewById(R.id.player_right));
    }
    
    @Override
    public void onClick(View v){
        String view_name = name_id_map.get(v.getId());
        String[] parts = view_name.split("_");
        String action = parts[0];
        String player_position = parts[1];

        eventRouter("player_" + player_position, action, true);
    }
    
    public void eventRouter(String target, String action, Boolean bufferable){
        if(bufferable){
            /* If animation running, append action to buffer. */
        }
        new Thread(new PkhdAnimator(target, action)).start();
    }
    
    class PkhdAnimator implements Runnable {
        private String target;
        private String action;

        public PkhdAnimator(String target, String action) {
            this.target = target;
            this.action = action;
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
                runOnUiThread(new PkhdBlitter(this.target, this.action));
            }
        }
    }

    class PkhdBlitter implements Runnable {
        private String target;
        private String action;

        public PkhdBlitter(String target, String action) {
            this.target = target;
            this.action = action;
        }

        @Override
        public void run() {
            Log.e("PkhdActivity", "Looping on target: " + this.target);

            String[] parts = this.target.split("_");
            int num = (Integer.parseInt((String) animateable_holder.get(this.target).getTag()) + 1) % 12;
            String action_type = "base_" + this.action + "_" + parts[1] + num;

            animateable_holder.get(this.target).setImageResource(image_map.get(action_type));
            animateable_holder.get(this.target).setTag(Integer.toString(num));
        }
    }
}