package it.sellainbox.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.sellainbox.AnnouncementActivity;
import it.sellainbox.AppController;
import it.sellainbox.R;
import it.sellainbox.cache.SellaCache;
import it.sellainbox.connection.ServerConfig;


public class BeaconsMonitoringService extends Service {
    private static final String TAG = "BeaconMonitoringService";
    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
    private BeaconManager beaconManager;
    private static int beaconId = 0;
    private static int count = 0;
    private static Boolean isInRegion = false;
    private Utils.Proximity checkIn1 = Utils.Proximity.IMMEDIATE;
    private Utils.Proximity checkIn2 = Utils.Proximity.NEAR;
    private Utils.Proximity checkOut = Utils.Proximity.FAR;
    HttpClient client = new DefaultHttpClient();
    HttpGet httpGet;

    private static final Region[] SELLA_BEACONS_REGION = new Region[]{
            new Region("SellaEntrance", ESTIMOTE_PROXIMITY_UUID, 30143, 9262),
            new Region("SellaWealthManagament", ESTIMOTE_PROXIMITY_UUID, 30143, 9263),
            new Region("SellaArchitecture", ESTIMOTE_PROXIMITY_UUID, 30143, 9264)
    };

    public static double getDistance(Beacon beacon) {
        return Math.pow(10d, ((double) beacon.getMeasuredPower() - beacon.getRssi()) / (10 * 2));
    }

    private static double computeAccuracy(int rssi, int measuredPower) {
        if (rssi == 0) {
            return -1.0D;
        } else {
            double ratio = (double) rssi / (double) measuredPower;
            double rssiCorrection = 0.96D + Math.pow((double) Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D ? Math.pow(ratio, 9.98D) * rssiCorrection : (0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }

    public static Utils.Proximity proximityFromAccuracy(double accuracy, Context context) {
        double checkIn = Double.valueOf(SellaCache.getCache("beacon1CheckIn", "1.0", context));
        double checkOut1 = Double.valueOf(SellaCache.getCache("beacon1CheckOut1", "2.0", context));
        double checkOut2 = Double.valueOf(SellaCache.getCache("beacon1CheckOut2", "3.0", context));

        Log.e(TAG, "<---count :" + count + " isInRegion:" + isInRegion + " accuracy: " + accuracy + " Checkin:" + checkIn + " Checkout1:" + checkOut1 + " CheckOut2:" + checkOut2 + "--->");

        if (accuracy <= checkIn) {
            return Utils.Proximity.NEAR;
        } else if (accuracy > checkOut1 && accuracy <= checkOut2) {
            return Utils.Proximity.FAR;
        }
        return Utils.Proximity.UNKNOWN;
//        return accuracy < 0.0D ? Utils.Proximity.UNKNOWN : (accuracy < 0.5D ? Utils.Proximity.IMMEDIATE : (accuracy <3.0D ? Utils.Proximity.NEAR : Utils.Proximity.FAR));
    }


    public static Utils.Proximity computeProximity(Beacon beacon, Context context) {
        return proximityFromAccuracy(computeAccuracy(beacon.getRssi(), beacon.getMeasuredPower()), context);
//        return proximityFromAccuracy(getDistance(beacon));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "<---Beacons monitoring service created--->");
        EstimoteSDK.initialize(this, "sellainbox", "a3a31c5ea67310d8dc70f5e45b23c735");
        EstimoteSDK.enableDebugLogging(true);
        isInRegion = false;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "<---Beacons monitoring service destroyed--->");
        Toast.makeText(this, "Beacons monitoring Stopped", Toast.LENGTH_SHORT).show();
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
        beaconManager.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "<---Beacons monitoring service Starting--->");
        Toast.makeText(this, "Beacons monitoring service starting", Toast.LENGTH_SHORT).show();

        beaconManager = AppController.getInstance().getBeaconManager();
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 1);
//        beaconManager.setForegroundScanPeriod(TimeUnit.SECONDS.toMillis(5), 1);

        isInRegion = false;

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    Log.d(TAG, "<---Beacons monitoring service Ready--->");
                    for (Region region : SELLA_BEACONS_REGION) {
                        beaconManager.startRanging(region);
//                        beaconManager.startMonitoring(region);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                final int checkCount = Integer.valueOf(SellaCache.getCache("beacon1Count", "2", getApplicationContext()));

                if (beacons != null && !beacons.isEmpty()) {
                    Beacon beacon = beacons.get(0);
                    String userCode = SellaCache.getCache("userCode", "0", getApplicationContext());
                    if (userCode != null && !userCode.equalsIgnoreCase("") && !userCode.equalsIgnoreCase("0")) {
//                        Utils.Proximity proximity = Utils.computeProximity(beacon);
                        Utils.Proximity proximity = computeProximity(beacon, getApplicationContext());
                        if ((proximity == checkIn1 || proximity == checkIn2) && !isInRegion) {
                            if (count >= checkCount) {
                                Log.e(TAG, "<---you have entered into beacon region--->" + beacon.getMinor());
                                Toast.makeText(BeaconsMonitoringService.this, "you have entered into beacon region:" + beacon.getMinor(), Toast.LENGTH_SHORT).show();

                                httpGet = new HttpGet(ServerConfig.getCheckInOutURL(getApplicationContext()) + "/" + userCode + "/" + beacon.getMinor() + "?check=in");

                                try {
                                    client.execute(httpGet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (beaconId != 0) {
                                    httpGet = new HttpGet(ServerConfig.getCheckInOutURL(getApplicationContext()) + "/" + userCode + "/" + beaconId + "?check=out");
                                    try {
                                        client.execute(httpGet);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                setBeaconId(beacon.getMinor());
                                Intent notificationIntent = new Intent(BeaconsMonitoringService.this, AnnouncementActivity.class);
                                notificationIntent.putExtra("BEACON_ID", beacon.getMinor());
                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(notificationIntent);
                                count = 0;
                            } else if (beaconId != beacon.getMinor()) {
                                count++;
                            }
                        }

                    /*else if (proximity == checkOut && isInRegion) {
                        if (count >= checkCount) {
                            Log.e(TAG, "<---You have left from beacon region--->" + beacon.getMinor());
                            isInRegion = false;
                            Toast.makeText(BeaconsMonitoringService.this, "You have left from beacon region:" + beacon.getMinor(), Toast.LENGTH_LONG).show();
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);

                            Notification notification = new Notification.Builder(BeaconsMonitoringService.this)
                                    .setContentTitle("Thank you for your visit")
                                    .setContentText("You've left Beacon Region:"+ beacon.getMinor())
                                    .setSmallIcon(R.drawable.bancasella)
                                    .build();

                            notification.defaults |= Notification.DEFAULT_SOUND;
                            notification.defaults |= Notification.DEFAULT_LIGHTS;

                            notificationManager.notify(2, notification);
                            count = 0;
                        } else {
                            Toast.makeText(BeaconsMonitoringService.this, "Exit Count" + count, Toast.LENGTH_SHORT).show();
                            count++;
                        }

                    } else {
                        count = 0;
                    }*/
                    }
//                else{
//                    Log.e(TAG, "<---You have left from beacon outer region--->");
//                    isInRegion = false;
//                    Toast.makeText(BeaconsMonitoringService.this, "You have left from beacon outer region:" , Toast.LENGTH_LONG).show();
//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    notificationManager.cancel(1);
//
//                    Notification notification = new Notification.Builder(BeaconsMonitoringService.this)
//                            .setContentTitle("Thank you for your visit")
//                            .setContentText("You've left Beacon outer Region")
//                            .setSmallIcon(R.drawable.bancasella)
//                            .build();
//
//                    notification.defaults |= Notification.DEFAULT_SOUND;
//                    notification.defaults |= Notification.DEFAULT_LIGHTS;
//
//                    notificationManager.notify(2, notification);
//                    count = 0;
//                }
                }
            }
        });

        return START_STICKY;
    }

    public void setBeaconId(int beaconId) {
        this.beaconId = beaconId;
    }

    public static int getBeaconId() {
        return beaconId;
    }
}
