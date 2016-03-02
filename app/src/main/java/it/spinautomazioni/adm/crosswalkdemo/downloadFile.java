package it.spinautomazioni.adm.crosswalkdemo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Antonio on 02/03/16.
 */
public class downloadFile extends AsyncTask<String, Integer, String> {

    ProgressDialog progress = new ProgressDialog(MainActivity.xWalkWebView.getContext());

    @Override
    protected void onPreExecute() {
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setTitle("Download File..");
        progress.setMessage("Please wait.");
        progress.show();
    }

    @Override
    protected String doInBackground(String... sUrl) {
        progress.show();
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream("/sdcard/file_name.pdf");

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
            return e.toString();
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
        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        if(progress.isShowing())
            progress.dismiss();
            System.out.println("Download terminato");

    }
}
