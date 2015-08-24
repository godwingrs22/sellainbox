package it.sellainbox.fragment;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import it.sellainbox.R;

/**
 * Created by GodwinRoseSamuel on 06-08-2015.
 */
public class InBoxFragment  extends Fragment {

    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private Region region;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        return view;
    }
}
