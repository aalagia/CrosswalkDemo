package it.spinautomazioni.adm.crosswalkdemo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
//Commit su GITHUB
// Adapter for holding devices found through scanning.
class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<Beacon> mLeDevices;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter() {
        super();
        mLeDevices = new ArrayList<Beacon>();
        //mInflator = BeaconList.this.getLayoutInflater();
    }

    public void addDevice(Beacon device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public void init(Collection<Beacon> data) {

        mLeDevices.clear();
        mLeDevices.addAll(data);


    }

    public Beacon getDevice(int position) {
        return mLeDevices.get(position);
    }


    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


}


public class MainActivity extends AppCompatActivity implements BeaconConsumer{


    static XWalkView xWalkWebView;
    XWalkNavigationHistory xWalkHistory;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BeaconManager beaconManager;
    private boolean EnableSendData;

    private ArrayList<Double> meanRSSI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xWalkWebView=(XWalkView)findViewById(R.id.xwalkWebView);
        xWalkWebView.addJavascriptInterface(new JsInterface(),"NativeInterface");

        xWalkWebView.load("https://crosswalk-project.org", null);
        xWalkHistory=xWalkWebView.getNavigationHistory();
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=ff54,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setForegroundScanPeriod(2500l);
        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);

        beaconManager.bind(this);

        setNeverSleepPolicy();
        PositionServer.ServerUrl="http://172.16.2.44:8000/Positioning/beacon_seen/";
        PositionServer.TerminalName="AndreaP";
        meanRSSI = new ArrayList<Double> (Collections.<Double>nCopies(20, 0.0));
        System.out.println("Size è " + meanRSSI.size());
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
                Codice di gestione della voce MENU_2
             */

                xWalkWebView.load("https://opentokrtc.com/qwerta", null);

                break;
            case R.id.Webview_JavaCall:
            /*
                Codice di gestione della voce MENU_2
             */
                //http://zhangwenli.com/blog/2014/08/25/crosswalk-calling-java-methods-with-javascript/

                xWalkWebView.load("file:///android_asset/index.html", null);

                break;
            case R.id.Webview_Map:

                xWalkWebView.load("http://172.16.2.33:8080/SpinMap2.html", null);
                xWalkWebView.load("javascript:androidtoJS(NativeInterface.position())", null);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                mLeDeviceListAdapter.init(beacons);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
                if (EnableSendData)
                    if (isConnected()) {
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

    public void onClickRefresh(View v) {
        // Perform action on click

        mLeDeviceListAdapter.notifyDataSetChanged();
    }

    public void onClickSendData(View v) {
        Button btn= (Button) findViewById(R.id.btnSend);
        //System.out.println("Ho questi beacon" + bea);
        // Perform action on click
        if (EnableSendData==false){
            btn.setText("Send Disable");
            EnableSendData=true;
        }
        else        {
            btn.setText("Send Enable");
            EnableSendData=false;
        }


    }

    public void onClickSendTest(View v) {
        Button btn= (Button) findViewById(R.id.btnSendTest);
        // Perform action on click
        Collection<Beacon> beacons ;
        beacons = null;

        new PositionServer().execute(beacons);
    }

    public static void posizione(String coordinate){
        System.out.println("Sono Main ho ricevuto " + coordinate);
        xWalkWebView.load("javascript:androidtoJS("+ coordinate +")", null);
    }
}
