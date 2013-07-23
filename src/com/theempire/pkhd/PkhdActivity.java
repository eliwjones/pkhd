package com.theempire.pkhd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PkhdActivity extends Activity implements View.OnClickListener {
    public HashMap<Integer, String> name_id_map;
    public HashMap<String, List<Integer>> image_map;
    public HashMap<String, View> animatable_holder;
    public HashMap<String, Boolean> animatable_state;
    public HashMap<String, List<String>> animatable_buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_pkhd);

        animatable_holder = new HashMap<String, View>();
        animatable_state = new HashMap<String, Boolean>();
        animatable_buffer = new HashMap<String, List<String>>();
        image_map = new HashMap<String, List<Integer>>();
        name_id_map = new HashMap<Integer, String>();

        int image_id = 0;
        int view_id = 0;
        String view_id_string = "";

        for (String player_name : new String[] { "player_left", "player_right", "health_left", "health_right" }) {
            /* Build out animatable HashMaps */
            view_id = context.getResources().getIdentifier(player_name, "id", context.getPackageName());
            animatable_holder.put(player_name, findViewById(view_id));
            animatable_state.put(player_name, false);
            animatable_buffer.put(player_name, new ArrayList<String>());

            String[] parts = player_name.split("_");
            if (parts[0].equals("health")) {
            }
            if (parts[0].equals("player")) {
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
        
        new Thread(new Nhpk("player_right", new String[] { "p", "k", "h", "d" })).start();
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
        if (bufferable && animatable_state.get(target)) {
            /* If animation running, append action to buffer. */
            if (animatable_buffer.get(target).size() < 3) {
                //Log.e("Animateable Buffer", "Adding to buffer for target: " + target + " action: " + action);
                animatable_buffer.get(target).add(action);
            } else {
                Log.e("Animateable Buffer", "Buffer Full!!!!");
            }
            return;
        }
        animatable_state.put(target, true);
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
                /* Expiremental Handler approach.  Don't really like it though. Only fun to do if you are changing a single image property.
                Message msg = blitter_handler.obtainMessage();
                msg.obj = new String[]{this.target, this.action};
                blitter_handler.sendMessage(msg);
                */
                
            }

            /* Reduce health bar at this point if necessary. */
            String[] right_left = this.target.split("_");
            String health_bar = right_left[1].equals("left") ? "right" : "left";
            health_bar = "health_" + health_bar;
            LinearLayout health_bar_layout = (LinearLayout) animatable_holder.get(health_bar);
            int health = Integer.parseInt((String) health_bar_layout.getTag());
            if (health > 0) {
                Message msg = health_handler.obtainMessage();
                /* Should add child ImageViews starting at 0 so don't need wonky 10 - health. */
                msg.obj = health_bar_layout.getChildAt(10 - health);
                health_handler.sendMessage(msg);
                health = health - 1;
                animatable_holder.get(health_bar).setTag("" + health);
            }

            /* Once done with looping, check animatable_buffer.get(target) for actions. */
            if (!animatable_buffer.get(target).isEmpty()) {
                Log.e("Animateable Buffer", "Found stuff in buffer!  Should be animating it!! List: " + TextUtils.join(", ", animatable_buffer.get(target)));
                new Thread(new PkhdAnimator(target, animatable_buffer.get(target).remove(0))).start();
            } else {
                animatable_state.put(target, false);
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
            int num = (Integer.parseInt((String) animatable_holder.get(this.target).getTag()) + 1) % image_map.get(action_type).size();

            ((ImageView) animatable_holder.get(this.target)).setImageResource(image_map.get(action_type).get(num));
            animatable_holder.get(this.target).setTag(Integer.toString(num));
        }
    }
    
    class Nhpk implements Runnable {
        private String target;
        private String[] actions;
        
        public Nhpk(String target, String[] actions){
            this.target = target;
            this.actions = actions;
        }
        
        @Override
        public void run(){
            /* Fires off events for "player_right" */
            /* sleeps variable amount of time between 800 and 1500 */
            Random rand_sleep = new Random();
            Random rand_action = new Random();
            int max = 2000;
            int min = 1000;
            
            while(true){
                int index = rand_action.nextInt(actions.length);
                String action = actions[index];
                
                eventRouter(this.target, action, true);
                
                int sleep = rand_sleep.nextInt(max - min + 1) + min;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    final Handler blitter_handler = new Handler() {
        public void handleMessage(Message msg){
            String target = ((String[])msg.obj)[0];
            String action = ((String[])msg.obj)[1];
            int num = (Integer.parseInt((String) animatable_holder.get(target).getTag()) + 1) % image_map.get(target + "_" + action).size();
            ((ImageView) animatable_holder.get(target)).setImageResource(image_map.get(target + "_" + action).get(num));
            animatable_holder.get(target).setTag(Integer.toString(num));
        }
    };

    /* Implicitly runs on UI Thread. */
    static final Handler health_handler = new Handler() {
        public void handleMessage(Message msg) {
            ((ImageView) msg.obj).setVisibility(View.INVISIBLE);
        }
    };
}