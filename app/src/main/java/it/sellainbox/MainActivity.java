package it.sellainbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.EstimoteSDK;

import it.sellainbox.fragment.AnnouncementFragment;
import it.sellainbox.fragment.EventsFragment;
import it.sellainbox.fragment.InBoxFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int NAVIGATION_VIEW_ACTION = 201;
    private static final int REQUEST_ENABLE_BT = 202;
    private static final int NOTIFICATION_ID = 203;

    private Boolean doubleBackToExitPressedOnce = false;
    private Boolean isSavedInstance = false;

    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.sella.BancaSella";
    private static final String FACEBOOK_URL = "https://www.facebook.com/bancasella";
    private static final String TWITTER_URL = "https://twitter.com/bancasella";
    private static final String WEBSITE_URL = "https://www.sella.it";
    private static final String CONTACT_US_URL = "https://www.sella.it/ita/contatti/index.jsp";


    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private NavigationView navigationView;

    private static TextView userName;
    private static TextView userEmail;
    private static ImageView userPhoto;
    private static ImageView coverLayout;

    AnnouncementFragment announcementFragment = new AnnouncementFragment();
    EventsFragment eventsFragment = new EventsFragment();
    InBoxFragment inBoxFragment = new InBoxFragment();

//    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
//    private BeaconManager beaconManager;
//    private NotificationManager notificationManager;
//    private Region region;

    public double getDistance(Beacon beacon) {
        return Math.pow(10d, ((double) beacon.getMeasuredPower() - beacon.getRssi()) / (10 * 2));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EstimoteSDK.initialize(this, "sellainbox", "a3a31c5ea67310d8dc70f5e45b23c735");

        EstimoteSDK.enableDebugLogging(true);


//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        beaconManager = new BeaconManager(this);

//        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
//        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
//            @Override
//            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
//                Log.e(TAG, "Ranged beacons: " + beacons);
//                if (!beacons.isEmpty()) {
//                    for (Beacon beacon : beacons) {
//                        Log.e(TAG, "Distance:" + getDistance(beacon));
//                        if (getDistance(beacon) <= 0.4) {
//                            postNotification("You have entered into Beacon1 region");
//                        }
//                    }
//                }
//            }
//        });

        TypedValue typedValueColorPrimaryDark = new TypedValue();
        MainActivity.this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueColorPrimaryDark, true);
        final int colorPrimaryDark = typedValueColorPrimaryDark.data;
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(colorPrimaryDark);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>SellaInbox</font>"));

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupNavigationDrawerContent(navigationView);
        }

        userName = (TextView) navigationView.findViewById(R.id.userName);
        userEmail = (TextView) navigationView.findViewById(R.id.userEmail);
        userPhoto = (ImageView) navigationView.findViewById(R.id.userPhoto);
        coverLayout = (ImageView) navigationView.findViewById(R.id.coverPhoto);

        setupNavigationDrawerContent(navigationView);

        navigationView.getMenu().performIdentifierAction(R.id.item_navigation_drawer_Announcement, 0);

        if (savedInstanceState == null) {
            isSavedInstance = true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if device supports Bluetooth Low Energy.
        if (!AppController.getInstance().getBeaconManager().hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }
        // If Bluetooth is not enabled, let user enable it.
        if (!AppController.getInstance().getBeaconManager().isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
//            connectToService();
        }
    }

    @Override
    protected void onStop() {
//        try {
//            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
//        } catch (RemoteException e) {
//            Log.e(TAG, "Error while stopping ranging", e);
//        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
//        notificationManager.cancel(NOTIFICATION_ID);
//        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
//                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth not enabled");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private void connectToService() {
//        Log.d(TAG, "Scanning...");
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                try {
//                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
//                } catch (RemoteException e) {
//                    Toast.makeText(getApplicationContext(), "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
//                    Log.e(TAG, "Cannot start ranging", e);
//                }
//            }
//        });
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        notificationManager.cancel(NOTIFICATION_ID);
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                try {
//                    beaconManager.startMonitoring(region);
//                } catch (RemoteException e) {
//                    Log.d(TAG, "Error while starting monitoring");
//                }
//            }
//        });
//    }

//    private void postNotification(String msg) {
//        Intent notifyIntent = new Intent(MainActivity.this, MainActivity.class);
//        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivities(MainActivity.this, 0, new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new Notification.Builder(MainActivity.this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Welcome to SellaInbox")
//                .setContentText(msg)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
//                .build();
////        notification.defaults |= Notification.DEFAULT_SOUND;
//        notification.defaults |= Notification.DEFAULT_LIGHTS;
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            case R.id.action_help: {
                try {
                    Intent i = new Intent("android.intent.action.MAIN");
                    i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                    i.addCategory("android.intent.category.LAUNCHER");
                    i.setData(Uri.parse(WEBSITE_URL));
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                break;
            }
            case R.id.action_about: {
                new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setTitle("About SellaInbox")
                        .setMessage("SellaInbox\n------------------------\nVersion:1.0.2\n\nDeveloped By:\nBanca Sella-Chennai Branch\nwww.sella.it"
                                + "\n\nSupport by Email:\ninfo@sella.it"
                                + "\n\nDISCLAIMER:\n"
                                + "The user uses the application it on own and sole responsibility."
                                + "The information and datas appearing in the application serve exclusively "
                                + "as guidance and the creator is not liable for their correctness"
                                + "\n\nGood Luck!!\nBanca Sella")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    Handler navigationViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NAVIGATION_VIEW_ACTION: {
                    MenuItem menuItem = (MenuItem) msg.obj;
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    switch (menuItem.getItemId()) {
                        case R.id.item_navigation_drawer_Announcement:
                            fragmentTransaction.replace(R.id.content_frame, announcementFragment).commit();
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            break;
//                        case R.id.item_navigation_drawer_dashboard:
//                            fragmentTransaction.replace(R.id.content_frame, dashBoardFragment).commit();
//                            drawerLayout.closeDrawer(GravityCompat.START);
//                            menuItem.setChecked(true);
//                            break;
                        case R.id.item_navigation_drawer_Inbox:
                            fragmentTransaction.replace(R.id.content_frame, inBoxFragment).commit();
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            break;
                        case R.id.item_navigation_drawer_Events:
                            fragmentTransaction.replace(R.id.content_frame, announcementFragment).commit();
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            break;
                        case R.id.item_navigation_drawer_about:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                                    .setTitle("About SellaInbox")
                                    .setMessage("SellaInbox\n------------------------\nVersion:1.0.2\n\nDeveloped By:\nBanca Sella-Chennai Branch\nwww.sella.it"
                                            + "\n\nSupport by Email:\ninfo@sella.it"
                                            + "\n\nDISCLAIMER:\n"
                                            + "The user uses the application it on own and sole responsibility."
                                            + "The information and datas appearing in the application serve exclusively "
                                            + "as guidance and the creator is not liable for their correctness"
                                            + "\n\nGood Luck!!\nBanca Sella")
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .show();
                            menuItem.setChecked(true);
                            break;
                        case R.id.item_navigation_drawer_share:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            share.putExtra(Intent.EXTRA_SUBJECT, navigationView.getContext().getString(R.string.app_name));
                            share.putExtra(Intent.EXTRA_TEXT, navigationView.getContext().getString(R.string.app_description) + "\n" +
                                    "Website : " + WEBSITE_URL + "\n" +
                                    "FaceBook Page : " + FACEBOOK_URL + "\n" +
                                    "Download from: " + PLAY_STORE_URL);
                            navigationView.getContext().startActivity(Intent.createChooser(share, navigationView.getContext().getString(R.string.app_name)));
                            break;
                        case R.id.item_navigation_drawer_rateus:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            try {
                                Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
                                navigationView.getContext().startActivity(rateIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(navigationView.getContext(), "No browser is installed", Toast.LENGTH_LONG);
                            }
                            break;
                        case R.id.item_navigation_drawer_account_settings:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            break;
                        case R.id.item_navigation_drawer_contact_us:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            try {
                                Intent contactusIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CONTACT_US_URL));
                                navigationView.getContext().startActivity(contactusIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(navigationView.getContext(), "No browser is installed", Toast.LENGTH_LONG);
                            }
                            break;
                        case R.id.item_navigation_drawer_help_and_feedback:
                            drawerLayout.closeDrawer(GravityCompat.START);
                            menuItem.setChecked(true);
                            try {
                                Intent i = new Intent("android.intent.action.MAIN");
                                i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                                i.addCategory("android.intent.category.LAUNCHER");
                                i.setData(Uri.parse(WEBSITE_URL));
                                navigationView.getContext().startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                navigationView.getContext().startActivity(i);
                            }
                            break;
                    }
                }
            }
        }
    };

    private void setupNavigationDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (this == null)
                                        return;
                                    navigationViewHandler.obtainMessage(NAVIGATION_VIEW_ACTION, menuItem).sendToTarget();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit", Toast.LENGTH_SHORT).show();
            doubleBackToExitPressedOnce = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

}

