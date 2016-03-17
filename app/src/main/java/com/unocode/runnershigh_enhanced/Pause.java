package com.unocode.runnershigh_enhanced;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by apsMac1 on 3/14/16.
 */
public class Pause extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pause_menu);

    }

    public void quitGame(View view){
        Log.d("quitGame", "clicked");
    }

    public void unPauseGame(View view) {
        Log.d("unpauseGame", "clicked");
        finish();//just finishes this activity- what i want it to do
    }
}
