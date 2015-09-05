package it.sellainbox;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.BeaconManager;

import it.sellainbox.service.BeaconsMonitoringService;
import it.sellainbox.volley.LruBitmapCache;

/**
 * Created by GodwinRoseSamuel on 09-08-2015.
 */
public class AppController extends Application {

    public static final String TAG = "AppController";

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    LruBitmapCache mLruBitmapCache;
    private BeaconManager beaconManager = null;

    private static AppController appController;

    @Override
    public void onCreate() {
        super.onCreate();
        appController = this;
        setBeaconManager(new BeaconManager(this));
        startService(new Intent(getApplicationContext(), BeaconsMonitoringService.class));
    }

    public static synchronized AppController getInstance() {
        return appController;
    }

    public BeaconManager getBeaconManager() {
        if (beaconManager == null) {
            beaconManager = new BeaconManager(this);
        }
        return beaconManager;
    }

    public void setBeaconManager(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}