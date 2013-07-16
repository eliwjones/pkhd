package com.theempire.pkhd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PkhdActivity extends Activity implements View.OnClickListener{
    public HashMap<Integer, String> name_id_map;
    public HashMap<String, List<Integer>> image_map;
    public HashMap<String, ImageView> animateable_holder;
    public HashMap<String, Boolean> animateable_state;
    public HashMap<String, List<String>> animateable_buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_pkhd);

        animateable_holder = new HashMap<String, ImageView>();
        animateable_state = new HashMap<String, Boolean>();
        animateable_buffer = new HashMap<String, List<String>>();
        image_map = new HashMap<String, List<Integer>>();
        name_id_map = new HashMap<Integer, String>();

        int image_id = 0;
        int view_id = 0;
        /* Can stuff in "base_p_right", "base_k_left" etc. */
        for (String base_name : new String[] { "base_p_left", "base_p_right", "base_k_left", "base_k_right", "base_h_left", "base_h_right", "base_d_left", "base_d_right" }) {
            /* key must be base_p_left -> player_left_p */
            /* ultimately, just rename image assets. */
            String[] parts = base_name.split("_");
            String image_map_key = "player_" + parts[2] + "_" + parts[1];
            image_map.put(image_map_key, new ArrayList<Integer>());
            for (int i = 0; i < 12; i++) {
                image_id = context.getResources().getIdentifier(base_name + "_" + i, "drawable", context.getPackageName());
                image_map.get(image_map_key).add(i, image_id);
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
            /* Build out animateable HashMaps */
            view_id = context.getResources().getIdentifier("player_" + player_name, "id", context.getPackageName());
            animateable_holder.put("player_" + player_name, (ImageView) findViewById(view_id));
            animateable_state.put("player_" + player_name, false);
            animateable_buffer.put("player_" + player_name, new ArrayList<String>());
        }

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
        if(bufferable && animateable_state.get(target)){
            /* If animation running, append action to buffer. */
            if(animateable_buffer.get(target).size() < 3){
                //Log.e("Animateable Buffer", "Adding to buffer for target: " + target + " action: " + action);
                animateable_buffer.get(target).add(action);
            } else {
                Log.e("Animateable Buffer", "Buffer Full!!!!");
            }
            return;
        }
        animateable_state.put(target, true);
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
                //Log.e("PkhdActivity", "Looping with counter: " + counter + " on target: " + this.target);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /* run on ui thread */
                runOnUiThread(new PkhdBlitter(this.target, this.action));
            }
            /* Once done with looping, check animateable_buffer.get(target) for actions. */
            if(!animateable_buffer.get(target).isEmpty()){
                Log.e("Animateable Buffer","Found stuff in buffer!  Should be animating it!! List: " +  TextUtils.join(", ", animateable_buffer.get(target)));
                new Thread(new PkhdAnimator(target, animateable_buffer.get(target).remove(0))).start();
            } else {
                animateable_state.put(target, false);
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
            //Log.e("PkhdActivity", "Looping on target: " + this.target);

            String action_type = this.target + "_" + this.action;
            int num = (Integer.parseInt((String) animateable_holder.get(this.target).getTag()) + 1) % image_map.get(action_type).size();

            animateable_holder.get(this.target).setImageResource(image_map.get(action_type).get(num));
            animateable_holder.get(this.target).setTag(Integer.toString(num));
        }
    }
}