package it.sellainbox;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import it.sellainbox.cache.SellaCache;

/**
 * Created by GodwinRoseSamuel on 14-06-2015.
 */
public class SplashScreen extends Activity {

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        spinner.setVisibility(View.VISIBLE);
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    final String userCode = SellaCache.getCache("userCode", "0", getApplicationContext());
                    if (userCode != null && !userCode.equalsIgnoreCase("") && !userCode.equalsIgnoreCase("0")) {
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(getApplicationContext(), LogInActivity.class);
                        startActivity(i);
                    }

                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
