package it.spinautomazioni.adm.crosswalkdemo;


import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.altbeacon.beacon.Beacon;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by adm on 04/11/2015.
 */

/*Classe che si occupa di inviare un JSON al server per la stima del posizionamento*/

public class PositionServer extends AsyncTask< Collection<Beacon>,Void,String> {
    private Exception exception;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected String doInBackground(Collection<Beacon>... beacons) {
        Collection<Beacon>beaconsData = beacons[0];
        // params comes from the execute() call: params[0] is the url.
        try {
            return makeRequest(beaconsData);

        } catch (Exception e) {
            this.exception = e;
            return "Unable to retrieve web page. URL may be invalid.";
        }
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {

        System.out.println("Il server ritorna " + result);
        MainActivity.posizione(result);


    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String makeRequest(Collection<Beacon> beacons) throws Exception {


        String postUrl = "http://172.16.2.34:8080/beaconData/";
        Gson gson = new Gson();

        List data = BeaconsToJSON(beacons);
        System.out.println("Invio questi dati al server" + gson.toJson(data));
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, gson.toJson(data));
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();
        Response response = null;
        try {
            response=client.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
        }




        return response.body().string();

    }






    public List BeaconsToJSON(Collection<Beacon> beacons){

        if(beacons.size()>0){

          List l1 = new LinkedList();

          for(Beacon lBeacon : beacons) {
              Map m1 = new HashMap();
              m1.put("BeaconName", lBeacon.getBluetoothName());
              m1.put("UUID",lBeacon.getId1().toString());
              m1.put("Maj", lBeacon.getId2().toString());
              m1.put("Min", lBeacon.getId3().toString());
              m1.put("TxPower",(lBeacon.getTxPower()));
              m1.put("RSSI", (lBeacon.getRssi()));
              m1.put("CalculatedDistance", (lBeacon.getDistance()));
              m1.put("CalculatedDistanceFormula",0);
              l1.add(m1);
          }
          return l1;
      }
        else return null;


    }


}
