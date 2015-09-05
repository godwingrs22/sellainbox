package it.sellainbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import it.sellainbox.AppController;
import it.sellainbox.R;
import it.sellainbox.connection.ServerConfig;
import it.sellainbox.fragment.announcement.Announcement;
import it.sellainbox.fragment.announcement.AnnouncementListAdapter;
import it.sellainbox.service.BeaconsMonitoringService;

/**
 * Created by GodwinRoseSamuel on 09-08-2015.
 */
public class AnnouncementFragment extends Fragment {
    private static final String TAG = "DashBoardFragment";
    Context context;
    private ListView announcementListView;
    private AnnouncementListAdapter announcementListAdapter;
    private List<Announcement> announcementList;
    private Cache cache;
    private TextView welcomeUser;
    private String notification_url;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh: {
//                cache.invalidate(URL_FEED, true);
//                cache.initialize();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);
        context = view.getContext();
        notification_url = ServerConfig.getNotificationURL() + "/" + BeaconsMonitoringService.getBeaconId() + "?userCode=GBS02286";
//      notification_url= ServerConfig.getNotificationURL()+"/9262?userCode=GBS02286";
        Log.e(TAG, "<----Notification URL---->" + notification_url);

        String notification_url= ServerConfig.getNotificationURL();
        welcomeUser = (TextView) view.findViewById(R.id.welcomeUser);
        announcementListView = (ListView) view.findViewById(R.id.announcementListView);

        announcementList = new ArrayList<Announcement>();

        announcementListAdapter = new AnnouncementListAdapter(getActivity(), R.layout.announcement_listrow, announcementList);
        announcementListView.setAdapter(announcementListAdapter);

        cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(notification_url);
        if (entry != null) {
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            // making fresh volley request and getting json
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                    notification_url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    VolleyLog.d(TAG, "Response: " + response.toString());
                    if (response != null) {
                        parseJsonFeed(response);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);
        }
        return view;
    }

    private void parseJsonFeed(JSONObject response) {
        try {
            final JSONObject user = response.getJSONObject("user");
            final String name = user.getString("name");
            welcomeUser.setText(name);

            final JSONObject device = response.getJSONObject("device");
            final String location = device.getString("location");

            JSONArray announcements = response.getJSONArray("announcements");
            for (int i = 0; i < announcements.length(); i++) {
                JSONObject announcementJSON = (JSONObject) announcements.get(i);
                JSONObject createdBy = (JSONObject) announcementJSON.get("createdBy");
                Announcement announcement = new Announcement();
                announcement.setId(announcementJSON.getInt("id"));
                announcement.setCreatedByName(createdBy.getString("name"));
                announcement.setProfileImage(ServerConfig.getServerURL() + createdBy.getString("image"));
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
                    String image = messagesJSON.isNull("image") ? null : ServerConfig.getServerURL()+messagesJSON.getString("image");
                    messageAnnouncement.setImage(image);
                    announcementList.add(messageAnnouncement);
                }
//                announcementList.add(announcement);
            }
            announcementListAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
