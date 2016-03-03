package it.spinautomazioni.adm.crosswalkdemo;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Antonio on 02/03/16.
 */
public class downloadFile extends AsyncTask<String, Integer, Integer> {

    ProgressDialog progress = new ProgressDialog(MainActivity.xWalkWebView.getContext());
    private String filename;

    public downloadFile(String filename){
        this.filename = filename;

    }

    @Override
    protected void onPreExecute() {
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setTitle("Download File..");
        progress.setMessage("Please wait.");
        progress.show();

    }

    @Override
    protected Integer doInBackground(String... sUrl) {
        progress.show();
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            String completeURL = "http://172.16.2.34:8080/download/" + sUrl[0];
            URL url = new URL(completeURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return connection.getResponseCode();

            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream("/sdcard/serverfile/" + sUrl[0]);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {

            }

            if (connection != null)
                connection.disconnect();
        }
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
            return null;
    }

    @Override
    protected void onPostExecute(Integer result) {
        System.out.println("Download terminato " + result);
        if(progress.isShowing())
            progress.dismiss();
        if (result == 404)
            Toast.makeText(MainActivity.xWalkWebView.getContext(), "File non presente sul Server", Toast.LENGTH_LONG).show();
        else if (result == 200) {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(fileExt("/sdcard/serverfile/" + filename).substring(1));
            newIntent.setDataAndType(Uri.fromFile(new File("/sdcard/serverfile/" + filename)), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                MainActivity.xWalkWebView.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.xWalkWebView.getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }

        }else if (result == null){
            Toast.makeText(MainActivity.xWalkWebView.getContext(), "Errore Sconosciuto", Toast.LENGTH_LONG).show();
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
}
