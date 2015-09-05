package it.sellainbox.fragment.announcement;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import it.sellainbox.AppController;
import it.sellainbox.R;
import it.sellainbox.cache.SellaImageLoader;

/**
 * Created by GodwinRoseSamuel on 09-08-2015.
 */
public class AnnouncementListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Announcement> announcementList;
    private int layoutid;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public AnnouncementListAdapter(Activity activity, int layoutid, List<Announcement> announcementList) {
        this.activity = activity;
        this.announcementList = announcementList;
        this.layoutid = layoutid;
    }

    @Override
    public int getCount() {
        return announcementList.size();
    }

    @Override
    public Object getItem(int location) {
        return announcementList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(layoutid, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        TextView createdByName = (TextView) convertView.findViewById(R.id.createdByName);
        TextView startTimestamp = (TextView) convertView.findViewById(R.id.startTimestamp);
        TextView message = (TextView) convertView.findViewById(R.id.message);
        TextView url = (TextView) convertView.findViewById(R.id.url);
        NetworkImageView profileImage = (NetworkImageView) convertView.findViewById(R.id.profileImage);
        SellaImageLoader sellaImageLoader = (SellaImageLoader) convertView.findViewById(R.id.imageView);

        Announcement item = announcementList.get(position);

        createdByName.setText(item.getCreatedByName());

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(Long.parseLong(item.getStartTimestamp()), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        startTimestamp.setText(timeAgo);

        if (!TextUtils.isEmpty(item.getMessage())) {
            message.setText(item.getMessage());
            message.setVisibility(View.VISIBLE);
        } else {
            message.setVisibility(View.GONE);
        }

        if (item.getUrl() != null) {
            url.setText(Html.fromHtml("<a href=\"" + item.getUrl() + "\">" + item.getUrl() + "</a> "));
            url.setMovementMethod(LinkMovementMethod.getInstance());
            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }

        profileImage.setImageUrl(item.getProfileImage(), imageLoader);

        if (item.getImage() != null) {
            sellaImageLoader.setImageUrl(item.getImage(), imageLoader);
            sellaImageLoader.setVisibility(View.VISIBLE);
            sellaImageLoader.setResponseObserver(new SellaImageLoader.ResponseObserver() {
                @Override
                public void onError() {
                }

                @Override
                public void onSuccess() {
                }
            });
        } else {
            sellaImageLoader.setVisibility(View.GONE);
        }

        return convertView;
    }
}
