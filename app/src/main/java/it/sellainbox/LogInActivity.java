package it.sellainbox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import it.sellainbox.cache.SellaCache;
import it.sellainbox.connection.ServerConfig;

/**
 * Created by GodwinRoseSamuel on 04-07-2015.
 */
public class LogInActivity extends Activity {
    private static final String TAG = "LogInActivity";

    private Button register;
    private EditText employeeId;
    protected ProgressDialog progressDialog;
    public String userCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        employeeId = (EditText) findViewById(R.id.employeeId);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userCode = employeeId.getText().toString().toUpperCase();
                new RegisterThread().execute();
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(i);
            }
        });
    }

    private String getDeviceId() {
        final String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(TAG, "Android Device Id : " + android_id);
        return android_id;
    }

    private String register() {
        StringBuffer result = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userCode", userCode);
            request.put("userDeviceId", getDeviceId());
            request.put("deviceId", "9262");

            Log.e(TAG, "user String : " + request.toString());

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(ServerConfig.getRegisterURL(getApplicationContext()));

            post.setHeader("Content-Type", "application/json");

            post.setEntity(new StringEntity(request.toString()));
            HttpResponse response = client.execute(post);

            Log.e(TAG, "Response Code : " + response.getStatusLine().getStatusCode());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            Log.e(TAG, "Result : " + result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class RegisterThread extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LogInActivity.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Registering as a new user...");
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {
                Thread.sleep(1000);
                response = register();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progressDialog.dismiss();
            try {
                JSONObject userResponse = new JSONObject(response);
                JSONObject status = (JSONObject) userResponse.get("status");
                final String statusstr = (String) status.get("code");
                Log.e(TAG, "User Registered:" + statusstr);
                if (statusstr.equalsIgnoreCase("BIOK")) {

                    SellaCache.putCache("userCode", userCode, getApplicationContext());

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                } else {
                    new AlertDialog.Builder(LogInActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("OOPS..!!!")
                            .setMessage((String) status.get("description"))
                            .setCancelable(false)
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    LogInActivity.this.finish();
                                }
                            })
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
