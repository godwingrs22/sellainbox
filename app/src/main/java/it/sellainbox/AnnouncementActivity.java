package it.sellainbox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import it.sellainbox.cache.SellaCache;
import it.sellainbox.cache.SellaImageLoader;
import it.sellainbox.connection.ServerConfig;
import it.sellainbox.fragment.announcement.Announcement;
import it.sellainbox.fragment.announcement.AnnouncementListAdapter;
import it.sellainbox.service.BeaconsMonitoringService;

/**
 * Created by GodwinRoseSamuel on 11-08-2015.
 */
public class AnnouncementActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementActivity";
    private ListView announcementListView;
    private AnnouncementListAdapter announcementListAdapter;
    private List<Announcement> announcementList;
    private Cache cache;
    private String notification_url;
    private TextView welcomeUser;
    private SellaImageLoader locationMapView;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        String userCode = SellaCache.getCache("userCode", "0", getApplicationContext());
        if (userCode != null) {
            notification_url = ServerConfig.getNotificationURL(getApplicationContext()) + "/" + BeaconsMonitoringService.getBeaconId() + "?userCode=" + userCode;
        }
        Log.e(TAG, "<----Notification URL---->" + notification_url);

        welcomeUser = (TextView) findViewById(R.id.welcomeUser);
        announcementListView = (ListView) findViewById(R.id.announcementListView);
        locationMapView = (SellaImageLoader) findViewById(R.id.locationMap);

        announcementList = new ArrayList<Announcement>();

        announcementListAdapter = new AnnouncementListAdapter(this, R.layout.activity_announcement_listrow, announcementList);
        announcementListView.setAdapter(announcementListAdapter);
        cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(notification_url);
        if (entry != null) {
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    Log.e(TAG, "cache response" + data);
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, notification_url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    VolleyLog.d(TAG, "Response: " + response.toString());
                    if (response != null) {
                        Log.e(TAG, "new response" + response.toString());
                        parseJsonFeed(response);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(jsonReq);
        }
    }

    private void parseJsonFeed(JSONObject response) {
        try {
            Log.e(TAG, "response" + response.toString());
            final JSONObject user = response.getJSONObject("user");
            final String name = user.getString("name");
            welcomeUser.setText(name);

            final JSONObject device = response.getJSONObject("device");
            final String location = device.getString("location");
            final String locationMap = ServerConfig.getServerURL(getApplicationContext()) + device.getString("locationMap");

            locationMapView.setImageUrl(locationMap, imageLoader);
            locationMapView.setVisibility(View.VISIBLE);
            locationMapView.setResponseObserver(new SellaImageLoader.ResponseObserver() {
                @Override
                public void onError() {
                }

                @Override
                public void onSuccess() {
                }
            });

            JSONArray announcements = response.getJSONArray("announcements");

            if (announcements != null) {

                postNotification(location);

                for (int i = 0; i < announcements.length(); i++) {
                    JSONObject announcementJSON = (JSONObject) announcements.get(i);
                    JSONObject createdBy = (JSONObject) announcementJSON.get("createdBy");
                    Announcement announcement = new Announcement();
                    announcement.setId(announcementJSON.getInt("id"));
                    announcement.setCreatedByName(createdBy.getString("name"));
                    announcement.setProfileImage(ServerConfig.getServerURL(getApplicationContext()) + createdBy.getString("image"));
                    announcement.setStartTimestamp(announcementJSON.getString("startTimeStamp"));
                    String url = announcementJSON.isNull("url") ? null : announcementJSON.getString("url");
                    announcement.setUrl(url);
                    JSONArray messages = announcementJSON.getJSONArray("messages");

                    for (int j = 0; j < messages.length(); j++) {
                        JSONObject messagesJSON = (JSONObject) messages.get(j);

                        Announcement messageAnnouncement = new Announcement();
                        messageAnnouncement.setId(announcement.getId());
                        messageAnnouncement.setCreatedByName(announcement.getCreatedByName());
                        messageAnnouncement.setProfileImage(announcement.getProfileImage());
                        messageAnnouncement.setStartTimestamp(announcement.getStartTimestamp());
                        messageAnnouncement.setUrl(announcement.getUrl());
                        String content = messagesJSON.isNull("content") ? null : messagesJSON.getString("content");
                        messageAnnouncement.setMessage(content);
                        String image = messagesJSON.isNull("image") ? null : ServerConfig.getServerURL(getApplicationContext()) + messagesJSON.getString("image");
                        messageAnnouncement.setImage(image);
                        announcementList.add(messageAnnouncement);
                    }
//                announcementList.add(announcement);
                }
            }

            announcementListAdapter.notifyDataSetChanged();

            SellaCache.putCache("beaconId", String.valueOf(BeaconsMonitoringService.getBeaconId()), getApplicationContext());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postNotification(final String location) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Welcome To BancaSella")
                .setContentText("You're at " + location + "!")
                .setSmallIcon(R.drawable.bancasella)
                .setContentIntent(intent)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        notificationManager.notify(1, notification);

        Toast toast = Toast.makeText(this, "You have entered into " + location, Toast.LENGTH_LONG);
        View view = toast.getView();
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setBackgroundColor(getResources().getColor(R.color.md_white_1000));
        text.getBackground().setAlpha(0);
        toast.setView(view);
        toast.show();
    }
}
