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

public class PkhdActivity extends Activity implements View.OnClickListener {
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
        String view_id_string = "";
        for (String player_name : new String[] { "player_left", "player_right" }) {
            /* Build out animateable HashMaps */
            view_id = context.getResources().getIdentifier(player_name, "id", context.getPackageName());
            animateable_holder.put(player_name, (ImageView) findViewById(view_id));
            animateable_state.put(player_name, false);
            animateable_buffer.put(player_name, new ArrayList<String>());

            for (String action : new String[] { "p", "k", "h", "d" }) {
                view_id_string = player_name + "_" + action;
                view_id = context.getResources().getIdentifier(view_id_string, "id", context.getPackageName());
                if (view_id != 0) {
                    findViewById(view_id).setOnClickListener(this);
                    name_id_map.put(view_id, view_id_string);
                }
                image_map.put(view_id_string, new ArrayList<Integer>());
                for (int i = 0; i < 12; i++) {
                    image_id = context.getResources().getIdentifier(view_id_string + "_" + i, "drawable", context.getPackageName());
                    image_map.get(view_id_string).add(i, image_id);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        String view_name = name_id_map.get(v.getId());
        Log.e("onClick", "view_name: " + view_name);
        String[] parts = view_name.split("_");
        String action = parts[2];
        String target = parts[0] + "_" + parts[1];

        eventRouter(target, action, true);
    }

    public void eventRouter(String target, String action, Boolean bufferable) {
        if (bufferable && animateable_state.get(target)) {
            /* If animation running, append action to buffer. */
            if (animateable_buffer.get(target).size() < 3) {
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
        private int animation_length;

        public PkhdAnimator(String target, String action) {
            this.target = target;
            this.action = action;
            this.animation_length = 1000;
        }

        @Override
        public void run() {
            int image_map_size = image_map.get(this.target + "_" + this.action).size();
            int counter = image_map_size;
            while (--counter >= 0) {
                /* sleep */
                //Log.e("PkhdActivity", "Looping with counter: " + counter + " on target: " + this.target);
                try {
                    Thread.sleep(animation_length / image_map_size);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /* run on ui thread */
                runOnUiThread(new PkhdBlitter(this.target, this.action));
            }
            /* Reduce health bar at this point if necessary. */

            /* Once done with looping, check animateable_buffer.get(target) for actions. */
            if (!animateable_buffer.get(target).isEmpty()) {
                Log.e("Animateable Buffer", "Found stuff in buffer!  Should be animating it!! List: " + TextUtils.join(", ", animateable_buffer.get(target)));
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