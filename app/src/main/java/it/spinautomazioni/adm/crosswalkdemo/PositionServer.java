package it.spinautomazioni.adm.crosswalkdemo;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by adm on 04/11/2015.
 */
public class PositionServer extends AsyncTask< Collection<Beacon>,Void,String> {
    private Exception exception;
    static public String ServerUrl;
    static public String TerminalName = "Term1";
    private static final String DEBUG_TAG = "HttpExample";
    static public Context mContext;
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
        // textView.setText(result);
        System.out.println("Il server ritorna " + result);
        MainActivity.posizione(result);
        /*JSONObject coordinate = null;
        try {
            coordinate = new JSONObject(result);

            //System.out.println("Coordinate x " + coordinate.get("pos_X"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String makeRequest(Collection<Beacon> beacons) throws Exception {

        //DefaultHttpClient httpclient = new DefaultHttpClient();

        //System.out.println("Ho questi beacon post" + beacons.);
        //BeaconSeen[] pojo = new BeaconSeen[3];

        String postUrl = "http://172.16.2.34:8080/beaconData/";
        Gson gson = new Gson();
        /*pojo[0] = new BeaconSeen();
        pojo[0].device="Device1";
        pojo[0].beacon="01_03";
        pojo[0].RSSI=34;

        pojo[1] = new BeaconSeen();
        pojo[1].device="Device1";
        pojo[1].beacon="01_04";
        pojo[1].RSSI=34;

        pojo[2] = new BeaconSeen();
        pojo[2].device="Device1";
        pojo[2].beacon="01_03";
        pojo[2].RSSI=34;

        System.out.println(gson.toJson(pojo));
        */

        List data = BeaconsToJSON(beacons);
        System.out.println("Invio questi dati al server" + gson.toJson(data));
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, gson.toJson(data));
        Request request = new Request.Builder()
                //.addHeader("Accept", "application/json")
                //.addHeader("Content-type", "application/json")
                .url(postUrl)
                .post(body)
                .build();
        Response response = null;
        try {
            response=client.newCall(request).execute();
            //String tmp = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //


        return response.body().string();


        /*
        HttpClient httpclient = new DefaultHttpClient();
        //url with the post data
        HttpPost httpost = new HttpPost(postUrl);



        //passes the results to a string builder/entity
        StringEntity se = new StringEntity(gson.toJson(data));

        //sets the post request as the resulting string
        httpost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");

        //Handles what is returned from the page
        ResponseHandler responseHandler = new BasicResponseHandler();
        return  httpclient.execute(httpost, responseHandler).toString();

        //return"";*/
    }





    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public List BeaconsToJSON(Collection<Beacon> beacons){
        //Gson gson = new Gson();
        if(beacons.size()>0){

          List l1 = new LinkedList();
          Map m2 = new HashMap();
          //m2.put("TerminalName",TerminalName);
          //l1.add(m2);
          for(Beacon lBeacon : beacons) {
              Map m1 = new HashMap();
              m1.put("BeaconName", lBeacon.getBluetoothName());
              m1.put("UUID",lBeacon.getId1().toString());
              m1.put("Maj", lBeacon.getId2().toString());
              //System.out.println("MAJOR è: " + lBeacon.getId2().toString());
              m1.put("Min", lBeacon.getId3().toString());
              m1.put("TxPower",Integer.toString(lBeacon.getTxPower()));
              m1.put("RSSI", Integer.toString(lBeacon.getRssi()));
              m1.put("CalculatedDistance", (lBeacon.getDistance()));
              double formula = BeaconList.calculateDistance(lBeacon.getTxPower(), lBeacon.getRssi());
              m1.put("CalculatedDistanceFormula",formula);
              l1.add(m1);
          }
          //gson.toJson(l1);
          return l1;
      }
        else return null;


    }








    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    class BeaconSeen
    {
        String device;
        String beacon;
        int RSSI;
//generate setter and getters
    }

}
