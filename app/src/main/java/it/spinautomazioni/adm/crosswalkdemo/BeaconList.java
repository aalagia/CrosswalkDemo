package it.spinautomazioni.adm.crosswalkdemo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;

import java.lang.Double;import java.lang.Exception;import java.lang.Integer;import java.lang.Math;import java.lang.Object;import java.lang.Override;import java.lang.Runnable;import java.lang.String;import java.lang.System;import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import it.spinautomazioni.adm.crosswalkdemo.PositionServer;
import it.spinautomazioni.adm.crosswalkdemo.R;
//import it.spinautomazioni.adm.altbeaconlist2.TimedBeaconSimulator;

public class BeaconList extends ListActivity  implements BeaconConsumer {
     //

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BeaconManager beaconManager;
    private boolean EnableSendData;
    //private TimedBeaconSimulator timedBeaconSimulator;
    private ArrayList<Double> meanRSSI;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
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
        //timedBeaconSimulator = new TimedBeaconSimulator(this);
        //timedBeaconSimulator.createTimedSimulatedBeacons();


    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_list, menu);
        return true;
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
    @Override
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


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<Beacon> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<Beacon>();
            mInflator = BeaconList.this.getLayoutInflater();
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            return view;
        }
        /*@Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.row_layout, null);
                viewHolder = new ViewHolder();
                viewHolder.txtViewDeviceName = (TextView) view.findViewById(R.id.txtViewDeviceName);
                viewHolder.textViewUUID = (TextView) view.findViewById(R.id.textViewUUID);
                viewHolder.textViewMaj = (TextView) view.findViewById(R.id.textViewMaj);
                viewHolder.textViewMin = (TextView) view.findViewById(R.id.textViewMin);
                viewHolder.textViewPower = (TextView) view.findViewById(R.id.textViewPower);
                viewHolder.textViewRSSI = (TextView) view.findViewById(R.id.textViewRSSI);
                viewHolder.textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
                viewHolder.textViewDistanceFormula = (TextView) view.findViewById(R.id.textViewDistanceFormula);
                viewHolder.textViewDistancePathModel = (TextView) view.findViewById(R.id.textViewDistancePathModel);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Beacon device = mLeDevices.get(i);
            double distance = calculateDistance(device.getTxPower(), device.getRssi());
            double distanceModel = calculateDistancePathModel(device.getRssi());
            final String deviceName = device.getBluetoothName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.txtViewDeviceName.setText(deviceName);
            else
                viewHolder.txtViewDeviceName.setText("unknown_device");

            viewHolder.textViewUUID.setText(device.getId1().toString());
            viewHolder.textViewMaj.setText(device.getId2().toString());
            viewHolder.textViewMin.setText(device.getId3().toString());
            viewHolder.textViewPower.setText(Integer.toString(device.getTxPower()));
            viewHolder.textViewRSSI.setText(Integer.toString(device.getRssi()));
            viewHolder.textViewDistance.setText("Distance " + Double.toString(device.getDistance()));
            viewHolder.textViewDistanceFormula.setText("Distance Formula " + Double.toString(distance));
            viewHolder.textViewDistancePathModel.setText("Distance Path Model " + Double.toString(distanceModel));
            return view;
        }*/
    }

    static class ViewHolder {
        TextView txtViewDeviceName;
        TextView textViewUUID;
        TextView textViewMaj;
        TextView textViewMin;
        TextView textViewPower;
        TextView textViewRSSI;
        TextView textViewDistance;
        TextView textViewDistanceFormula;
        TextView textViewDistancePathModel;
        String key;
        int position;
    }

    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
    protected double calculateDistancePathModel(double rssi){
        meanRSSI.remove(0);
        //double index = (-53.6800 - rssi - 8.7763)/(10*2);
        double index = (-53.6800 - rssi - 3.3703)/(10*2.3);
        System.out.println("L'indice è " + index + "RSSI è " + rssi);
        double distanceModel = 1*Math.pow(10,index);
        meanRSSI.add(distanceModel);
        double sum = 0;
        double n=0;
        for(Double d : meanRSSI) {
            if (d != 0) {
                sum += d;
                n++;
            }
        }
        return sum/n;


    }

}
