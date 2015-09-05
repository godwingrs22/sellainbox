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

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import it.sellainbox.AppController;
import it.sellainbox.MainActivity;
import it.sellainbox.R;
import it.sellainbox.cache.SellaCache;
import it.sellainbox.connection.ServerConfig;
import it.sellainbox.fragment.announcement.Announcement;
import it.sellainbox.cache.SellaImageLoader;
import it.sellainbox.fragment.announcement.AnnouncementListAdapter;
import it.sellainbox.service.BeaconsMonitoringService;

/**
 * Created by GodwinRoseSamuel on 09-08-2015.
 */
public class AnnouncementFragment extends Fragment {
    private static final String TAG = "AnnouncementFragment";
    Context context;
    private ListView announcementListView;
    private AnnouncementListAdapter announcementListAdapter;
    private List<Announcement> announcementList;
    private Cache cache;
    private String notification_url;
    private NetworkImageView reward1ImageView;
    private NetworkImageView reward2ImageView;
    private NetworkImageView reward3ImageView;
    private NetworkImageView reward4ImageView;
    private NetworkImageView reward5ImageView;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

//    private TextView welcomeUser;

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

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        final String userCode = SellaCache.getCache("userCode", "0", context);
        final Integer beaconId = Integer.valueOf(SellaCache.getCache("beaconId", "9262", context));

        if (userCode != null && BeaconsMonitoringService.getBeaconId() != 0) {
            notification_url = ServerConfig.getNotificationURL(getContext()) + "/" + BeaconsMonitoringService.getBeaconId() + "?userCode=" + userCode;
        } else {
            if (userCode != null && beaconId != null) {
                notification_url = ServerConfig.getNotificationURL(getContext()) + "/" + beaconId + "?userCode=" + userCode;
            }
        }

        Log.e(TAG, "<----Notification URL---->" + notification_url);

//        String notification_url = ServerConfig.getNotificationURL();
//        welcomeUser = (TextView) view.findViewById(R.id.welcomeUser);
        announcementListView = (ListView) view.findViewById(R.id.announcementListView);
        reward1ImageView = (NetworkImageView) view.findViewById(R.id.reward1);
        reward2ImageView = (NetworkImageView) view.findViewById(R.id.reward2);
        reward3ImageView = (NetworkImageView) view.findViewById(R.id.reward3);
        reward4ImageView = (NetworkImageView) view.findViewById(R.id.reward4);
        reward5ImageView = (NetworkImageView) view.findViewById(R.id.reward5);

        announcementList = new ArrayList<Announcement>();

        announcementListAdapter = new AnnouncementListAdapter(getActivity(), R.layout.announcement_listrow, announcementList);
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
        return view;
    }

    private void parseJsonFeed(JSONObject response) {
        try {
            Log.e(TAG, "response" + response.toString());
            final JSONObject user = response.getJSONObject("user");
            final String name = user.getString("name");
            final String userProfileImage = ServerConfig.getServerURL(getContext()) + user.getString("image");
            SellaCache.putCache("userName", name, getContext());
            SellaCache.putCache("userProfileImage", userProfileImage, getContext());

            MainActivity.LoadUserProfileHandler.obtainMessage();

            final JSONObject device = response.getJSONObject("device");
            final String location = device.getString("location");

            JSONArray announcements = response.getJSONArray("announcements");

            if (announcements != null) {

                for (int i = 0; i < announcements.length(); i++) {
                    JSONObject announcementJSON = (JSONObject) announcements.get(i);
                    JSONObject createdBy = (JSONObject) announcementJSON.get("createdBy");
                    Announcement announcement = new Announcement();
                    announcement.setId(announcementJSON.getInt("id"));
                    announcement.setCreatedByName(createdBy.getString("name"));
                    announcement.setProfileImage(ServerConfig.getServerURL(getContext()) + createdBy.getString("image"));
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
                        String image = messagesJSON.isNull("image") ? null : ServerConfig.getServerURL(getContext()) + messagesJSON.getString("image");
                        messageAnnouncement.setImage(image);
                        announcementList.add(messageAnnouncement);
                    }
//                announcementList.add(announcement);
                }
            }

            JSONArray rewards = response.getJSONArray("rewards");
            Log.e(TAG, "<--rewards--->" + rewards.toString());
            getRewards(rewards);

            announcementListAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRewards(final JSONArray rewards) throws JSONException {

        if (rewards.getJSONObject(0) != null) {
            reward1ImageView.setImageUrl(ServerConfig.getServerURL(getContext()) + rewards.getJSONObject(0).getString("image"), imageLoader);
            reward1ImageView.setVisibility(View.VISIBLE);
        } else {
            reward1ImageView.setVisibility(View.GONE);
        }
        if (rewards.getJSONObject(1) != null) {
            reward2ImageView.setImageUrl(ServerConfig.getServerURL(getContext()) + rewards.getJSONObject(1).getString("image"), imageLoader);
            reward2ImageView.setVisibility(View.VISIBLE);
        } else {
            reward2ImageView.setVisibility(View.GONE);
        }
        if (rewards.getJSONObject(2) != null) {
            reward3ImageView.setImageUrl(ServerConfig.getServerURL(getContext()) + rewards.getJSONObject(2).getString("image"), imageLoader);
            reward3ImageView.setVisibility(View.VISIBLE);
        } else {
            reward3ImageView.setVisibility(View.GONE);
        }
        if (rewards.getJSONObject(3) != null) {
            reward4ImageView.setImageUrl(ServerConfig.getServerURL(getContext()) + rewards.getJSONObject(3).getString("image"), imageLoader);
            reward4ImageView.setVisibility(View.VISIBLE);
        } else {
            reward4ImageView.setVisibility(View.GONE);
        }
        if (rewards.getJSONObject(4) != null) {
            reward5ImageView.setImageUrl(ServerConfig.getServerURL(getContext()) + rewards.getJSONObject(4).getString("image"), imageLoader);
            reward5ImageView.setVisibility(View.VISIBLE);
        } else {
            reward5ImageView.setVisibility(View.GONE);
        }
    }
}
