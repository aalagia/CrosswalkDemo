package it.spinautomazioni.adm.crosswalkdemo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Button;


import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    /*Classe per la gestire l'upload da tablet*/
    class UIClient extends XWalkUIClient {

        public UIClient(XWalkView xwalkView) {

            super(xwalkView);

        }

        public void openFileChooser(XWalkView view,

                                    ValueCallback<Uri> uploadFile, String acceptType, String capture) {

            super.openFileChooser(view, uploadFile, acceptType, capture);

            mFilePathCallback = uploadFile;

            Log.d("fchooser", "Opened file chooser.");

        }

    }

    static XWalkView xWalkWebView;
    XWalkNavigationHistory xWalkHistory;
    private BeaconManager beaconManager;
    private boolean EnableSendData;
    private ValueCallback mFilePathCallback;
    private boolean SendServer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn= (Button) findViewById(R.id.btnSend);
        btn.setVisibility(View.INVISIBLE);
        xWalkWebView=(XWalkView)findViewById(R.id.xwalkWebView);
        xWalkWebView.addJavascriptInterface(new JsInterface(), "NativeInterface");
        xWalkWebView.setUIClient(new UIClient(xWalkWebView));
        xWalkWebView.load("https://www.google.it", null);
        xWalkHistory=xWalkWebView.getNavigationHistory();
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));


        try {
            beaconManager.setForegroundScanPeriod(300l); // 300 mS
            beaconManager.setForegroundBetweenScanPeriod(0l); // 0ms
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);
        //BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);

        beaconManager.bind(this);
        Timer m = new Timer();
        m.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SendServer = true;
            }
        },0, 1100);

        setNeverSleepPolicy();


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);

        } else {
            setContentView(R.layout.activity_main);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (xWalkWebView != null) {

            if (mFilePathCallback != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                        : intent.getData();
                if (result != null) {
                    String path = MediaUtility.getPath(xWalkWebView.getContext(), result);
                    try {
                        Uri uri = Uri.fromFile(new File(path));
                        mFilePathCallback.onReceiveValue(uri);
                    }catch (Exception e){
                        mFilePathCallback.onReceiveValue(null);
                        Toast.makeText(MainActivity.xWalkWebView.getContext(), "Non riesco a caricare il file", Toast.LENGTH_LONG).show();
                    }

                } else {
                    mFilePathCallback.onReceiveValue(null);
                }
            }

            mFilePathCallback = null;
        }
        //xWalkWebView.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Button btn= (Button) findViewById(R.id.btnSend);
        switch(id)
        {
            case R.id.Webview_Back:
            /*
                Codice di gestione della voce MENU_1
             */

                if(xWalkHistory.canGoBack()) {
                    xWalkHistory.navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                }
                break;
            case R.id.Webview_Forward:
            /*
                Codice di gestione della voce MENU_2
             */
                if(xWalkHistory.canGoForward()) {
                    xWalkHistory.navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
                }
                break;
            case R.id.Webview_WebRtc:
            /*
                Codice di gestione della voce MENU_3
             */
                xWalkWebView.load("https://opentokrtc.com/qwerta", null);

                break;

            case R.id.Webview_Map:
                /*
                Codice di gestione della voce MENU_4
             */

                btn.setVisibility(View.VISIBLE);
                xWalkWebView.load("http://172.16.2.34:8080/", null);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (EnableSendData)
                    if (isConnected() && SendServer) {
                        SendServer = false;
                        new PositionServer().execute(beacons);
                    }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }


    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    private void setNeverSleepPolicy() {
        try {
            ContentResolver cr = getContentResolver();
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                int set = android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER;
                android.provider.Settings.System.putInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, set);
            } else {
                int set = android.provider.Settings.Global.WIFI_SLEEP_POLICY_NEVER;
                android.provider.Settings.System.putInt(cr, android.provider.Settings.Global.WIFI_SLEEP_POLICY, set);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }



    public void onClickSendData(View v) {
        Button btn= (Button) findViewById(R.id.btnSend);
        // Perform action on click
        if (EnableSendData==false){
            btn.setText("Disattiva Posizionamento");
            EnableSendData=true;
        }
        else        {
            btn.setText("Attiva Posizionamento");
            EnableSendData=false;
        }


    }

    public static void posizione(String coordinate){
        System.out.println("Sono Main ho ricevuto " + coordinate);
        xWalkWebView.load("javascript:androidtoJS("+ coordinate +")", null);
    }


}
