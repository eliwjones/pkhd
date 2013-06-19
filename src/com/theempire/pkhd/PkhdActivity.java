package com.theempire.pkhd;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class PkhdActivity extends Activity {
    public HashMap<Integer, Integer> image_map;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        image_map = new HashMap<Integer, Integer>();
        Context context = getApplicationContext();
        int image_id = 0;
        for(int i=0; i<12; i++){
            image_id = context.getResources().getIdentifier("base_p_left_" + i, "drawable", context.getPackageName());
            image_map.put(i, image_id);
        }
        
        setContentView(R.layout.activity_pkhd);
        findViewById(R.id.player_left).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /* Redraw image. */
                ImageView player_left = (ImageView) v;
                Log.e("PkhdActivity", "Player Left was clicked. Tag: " + player_left.getTag());
                int num = (Integer.parseInt((String)player_left.getTag()) + 1 ) % 12;
                
                player_left.setImageResource(image_map.get(num));
                player_left.setTag(Integer.toString(num));
            }
        });
    }
}