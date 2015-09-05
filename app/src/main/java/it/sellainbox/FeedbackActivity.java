package it.sellainbox;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.sellainbox.cache.SellaCache;
import it.sellainbox.connection.HttpClientManager;
import it.sellainbox.connection.ServerConfig;

/**
 * Created by PremKumar on 05/09/15.
 */
public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    private Button submit;
    private RatingBar ratingBar1;
    private RatingBar ratingBar2;
    private RatingBar ratingBar3;
    private RatingBar ratingBar4;

    private TextView question1;
    private TextView question2;
    private TextView question3;
    private TextView question4;
    private TextView question5;

    private EditText othercomments;

    private Toolbar toolbar;
    private ActionBar actionBar;
    protected ProgressDialog progressDialog;

    private static final HttpClientManager httpClientManager = new HttpClientManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        TypedValue typedValueColorPrimaryDark = new TypedValue();
        FeedbackActivity.this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueColorPrimaryDark, true);
        final int colorPrimaryDark = typedValueColorPrimaryDark.data;

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(colorPrimaryDark);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>FeedBack</font>"));

        ratingBar1 = (RatingBar) findViewById(R.id.ratingBar1);
        ratingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
        ratingBar3 = (RatingBar) findViewById(R.id.ratingBar3);
        ratingBar4 = (RatingBar) findViewById(R.id.ratingBar4);

        question1 = (TextView) findViewById(R.id.question1);
        question2 = (TextView) findViewById(R.id.question2);
        question3 = (TextView) findViewById(R.id.question3);
        question4 = (TextView) findViewById(R.id.question4);
        question5 = (TextView) findViewById(R.id.question5);

        othercomments = (EditText) findViewById(R.id.othercomments);

        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FeedBackAsyncThread().execute();
            }
        });
    }

    public String getFeedback() {
        JSONObject feedback = new JSONObject();
        try {
            JSONObject feedback1 = new JSONObject();
            feedback1.put("question", question1.getText().toString());
            feedback1.put("answer", ratingBar1.getRating());

            JSONObject feedback2 = new JSONObject();
            feedback2.put("question", question2.getText().toString());
            feedback2.put("answer", ratingBar2.getRating());

            JSONObject feedback3 = new JSONObject();
            feedback3.put("question", question3.getText().toString());
            feedback3.put("answer", ratingBar3.getRating());

            JSONObject feedback4 = new JSONObject();
            feedback4.put("question", question4.getText().toString());
            feedback4.put("answer", ratingBar4.getRating());

            JSONObject feedback5 = new JSONObject();
            feedback5.put("question", question5.getText().toString());
            feedback5.put("answer", othercomments.getText().toString());

            JSONArray feedbacks = new JSONArray();
            feedbacks.put(feedback1);
            feedbacks.put(feedback2);
            feedbacks.put(feedback3);
            feedbacks.put(feedback4);
            feedbacks.put(feedback5);

            feedback.put("feedback", feedbacks);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return feedback.toString();
    }

    private class FeedBackAsyncThread extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(FeedbackActivity.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Submitting your feedback...!");
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {
                Thread.sleep(2000);
                final String request = getFeedback();
                Log.e(TAG, "<---FeedBack Received--->" + request);
                final String userCode = SellaCache.getCache("userCode", "", getApplicationContext());
                if (userCode != null) {
                    final String feedbackUrl = ServerConfig.getFeedbackURL(getApplicationContext()) + "/" + userCode + "/feedback";
                    Log.e(TAG, "<---feedbackUrl--->" + feedbackUrl);

                    response = httpClientManager.getHttpPostResponse(feedbackUrl, request);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progressDialog.dismiss();
            new AlertDialog.Builder(FeedbackActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("SUCCESS..!!!")
                    .setMessage("Thank you for your feedback.!!!")
                    .setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FeedbackActivity.this.finish();
                        }
                    })
                    .show();
        }
    }
}

