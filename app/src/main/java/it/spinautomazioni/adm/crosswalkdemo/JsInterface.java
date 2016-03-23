package it.spinautomazioni.adm.crosswalkdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkActivity;

import java.io.File;

/**
 * Created by adm on 17/11/2015.
 */
/*Interfaccia tra il codice javascript e la parte Android*/
public class JsInterface {

    public JsInterface() {
    }


    @JavascriptInterface
    public String loadFile(String sURL) {


        File file = new File("/sdcard/serverfile/" + sURL);
        if(!file.exists())
            new downloadFile(sURL).execute(sURL);

        else {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(fileExt("/sdcard/serverfile/" + sURL).substring(1));
            newIntent.setDataAndType(Uri.fromFile(new File("/sdcard/serverfile/" + sURL)), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                MainActivity.xWalkWebView.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.xWalkWebView.getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        }
        return "File caricato";
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
