package it.sellainbox;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import it.sellainbox.cache.SellaCache;

/**
 * Created by PremKumar on 03/09/15.
 */
public class SettingsActivity extends AppCompatActivity {

    private EditText beacon1CheckIn;
    private EditText beacon1CheckOut1;
    private EditText beacon1CheckOut2;
    private EditText beacon1Count;
    private EditText ipAddress;
    private Button saveSettings;
    private Toolbar toolbar;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TypedValue typedValueColorPrimaryDark = new TypedValue();
        SettingsActivity.this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueColorPrimaryDark, true);
        final int colorPrimaryDark = typedValueColorPrimaryDark.data;
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(colorPrimaryDark);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>Settings</font>"));

        beacon1CheckIn = (EditText) findViewById(R.id.beacon1CheckIn);
        beacon1CheckOut1 = (EditText) findViewById(R.id.beacon1CheckOut1);
        beacon1CheckOut2 = (EditText) findViewById(R.id.beacon1CheckOut2);
        beacon1Count = (EditText) findViewById(R.id.beacon1Count);
        ipAddress = (EditText) findViewById(R.id.ipAddressView);
        saveSettings = (Button) findViewById(R.id.saveSettings);

        beacon1CheckIn.setText(SellaCache.getCache("beacon1CheckIn", "1.0", getApplicationContext()));
        beacon1CheckOut1.setText(SellaCache.getCache("beacon1CheckOut1", "2.2", getApplicationContext()));
        beacon1CheckOut2.setText(SellaCache.getCache("beacon1CheckOut2", "7.0", getApplicationContext()));
        beacon1Count.setText(SellaCache.getCache("beacon1Count", "2", getApplicationContext()));
        ipAddress.setText(SellaCache.getCache("ipAddress", "192.168.0.105", getApplicationContext()));

        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SellaCache.putCache("beacon1CheckIn", beacon1CheckIn.getText().toString(), getApplicationContext());
                SellaCache.putCache("beacon1CheckOut1", beacon1CheckOut1.getText().toString(), getApplicationContext());
                SellaCache.putCache("beacon1CheckOut2", beacon1CheckOut2.getText().toString(), getApplicationContext());
                SellaCache.putCache("beacon1Count", beacon1Count.getText().toString(), getApplicationContext());
                SellaCache.putCache("ipAddress", ipAddress.getText().toString(), getApplicationContext());

                Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_LONG).show();
            }
        });
    }
}
