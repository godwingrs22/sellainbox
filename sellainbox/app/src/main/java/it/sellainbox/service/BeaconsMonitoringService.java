package it.sellainbox.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import it.sellainbox.AnnouncementActivity;
import it.sellainbox.AppController;
import it.sellainbox.R;


public class BeaconsMonitoringService extends Service {
    private static final String TAG = "BeaconMonitoringService";
    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
    private BeaconManager beaconManager;
    private static int beaconId;
    int enterCount = 0;
    int exitCount = 0;

    public double getDistance(Beacon beacon) {
        return Math.pow(10d, ((double) beacon.getMeasuredPower() - beacon.getRssi()) / (10 * 2));
    }

//    protected double getDistance(int txPower, double rssi) {
//        if (rssi == 0) {
//            return -1.0; // if we cannot determine accuracy, return -1.
//        }
//        double ratio = rssi * 1.0 / txPower;
//        if (ratio < 1.0) {
//            return Math.pow(ratio, 10);
//        } else {
//            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
//            return accuracy;
//        }
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "<---Beacons monitoring service created--->");
        EstimoteSDK.initialize(this, "sellainbox", "a3a31c5ea67310d8dc70f5e45b23c735");
        EstimoteSDK.enableDebugLogging(true);
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
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(5), 5);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    Log.d(TAG, "<---Beacons monitoring service Ready--->");
//                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.e(TAG, "<---region--->" + region.getMinor());
                if (!beacons.isEmpty()) {
                    for (Beacon beacon : beacons) {
//                        Double distance1=getDistance(beacon.getMeasuredPower(),beacon.getRssi());
                        double distance=getDistance(beacon);
//                        double distance = Math.min(Utils.computeAccuracy(beacon), 1.0);
                        Log.e(TAG, "<---Beacon Rssi--->" + distance);

                        if (distance < 0.1 && enterCount == 0) {
                            enterCount = 1;
                            exitCount = 0;
                            Log.e(TAG, "<---you have entered into beacon region--->" + beacon.getMinor());
                            setBeaconId(beacon.getMinor());
                            Intent notificationIntent = new Intent(BeaconsMonitoringService.this, AnnouncementActivity.class);
                            notificationIntent.putExtra("BEACON_ID", beacon.getMinor());
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(notificationIntent);
                        } else if (distance >= 0.1 && exitCount == 0) {
                            enterCount = 0;
                            exitCount = 1;
                            Log.e(TAG, "<---You have left from beacon region--->" + region.getMinor());
                            Toast.makeText(BeaconsMonitoringService.this, "You have left from beacon region", Toast.LENGTH_LONG).show();
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);

                            Notification notification = new Notification.Builder(BeaconsMonitoringService.this)
                                    .setContentTitle("Thank you for your visit")
                                    .setContentText("You've left Beacon Region!")
                                    .setSmallIcon(R.drawable.bancasella)
                                    .build();

                            notification.defaults |= Notification.DEFAULT_SOUND;
                            notification.defaults |= Notification.DEFAULT_LIGHTS;

                            notificationManager.notify(2, notification);
                        }
                    }
                }
            }
        });

//        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
//            @Override
//            public void onEnteredRegion(Region region, List<Beacon> beacons) {
//                if (!beacons.isEmpty()) {
//                    for (Beacon beacon : beacons) {
//                        Log.e(TAG, "<---you have entered into beacon region--->" + beacon.getMinor());
////                        if (beacon.getMinor() == 9262) {
//                        setBeaconId(beacon.getMinor());
//                        Intent notificationIntent = new Intent(BeaconsMonitoringService.this, AnnouncementActivity.class);
//                        notificationIntent.putExtra("BEACON_ID", beacon.getMinor());
//                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
////                            PendingIntent intent = PendingIntent.getActivity(BeaconsMonitoringService.this, 0, notificationIntent, 0);
//
////                        Notification notification = new Notification.Builder(BeaconsMonitoringService.this)
////                                .setContentTitle("Welcome To BancaSella")
////                                .setContentText("You're at Beacon(" + beacon.getMinor() + ") Region!")
////                                .setSmallIcon(R.drawable.bancasella)
////                                .setContentIntent(intent)
////                                .build();
////
////                        notification.defaults |= Notification.DEFAULT_SOUND;
////                        notification.defaults |= Notification.DEFAULT_LIGHTS;
////
////                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////                        notificationManager.cancel(2);
////                        notificationManager.notify(1, notification);
//
//                        startActivity(notificationIntent);
////                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onExitedRegion(Region region) {
//                Log.e(TAG, "<---You have left from beacon region--->" + region.getMinor());
//                Toast.makeText(getApplicationContext(), "You have left from beacon region " + region.getMinor(), Toast.LENGTH_LONG).show();
//                Toast.makeText(BeaconsMonitoringService.this, "You have left from beacon region", Toast.LENGTH_LONG).show();
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(1);
//
//                Notification notification = new Notification.Builder(BeaconsMonitoringService.this)
//                        .setContentTitle("Thank you for your visit")
//                        .setContentText("You've left Beacon Region!")
//                        .setSmallIcon(R.drawable.bancasella)
//                        .build();
//
//                notification.defaults |= Notification.DEFAULT_SOUND;
//                notification.defaults |= Notification.DEFAULT_LIGHTS;
//
//                notificationManager.notify(2, notification);
//            }
//        });

        return START_STICKY;
    }

    public void setBeaconId(int beaconId) {
        this.beaconId = beaconId;
    }

    public static int getBeaconId() {
        return beaconId;
    }
}
