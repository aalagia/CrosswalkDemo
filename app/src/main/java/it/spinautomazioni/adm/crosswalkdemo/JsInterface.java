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

public class JsInterface {

    public JsInterface() {
    }

    @JavascriptInterface
    public String sayHello() {
        return "Hello World!";
    }

    @JavascriptInterface
    public String tablet(String sURL) {

        System.out.println("CAZZZOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        new downloadFile().execute(sURL);
        Uri data = Uri.parse("file://" + "/sdcard/IMG_20160114_172015.jpg");
        //Uri data = Uri.parse("http://www.google.it");
        Intent playIntent = new Intent(Intent.ACTION_VIEW, data);
        //MainActivity.xWalkWebView.getContext().startActivity(playIntent);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt("/sdcard/IMG_20160114_172015.jpg").substring(1));
        newIntent.setDataAndType(Uri.fromFile(new File("/sdcard/IMG_20160114_172015.jpg")),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            MainActivity.xWalkWebView.getContext().startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText( MainActivity.xWalkWebView.getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
        return "Sono il Tablet";
    }

    @JavascriptInterface
    public int position() {
        return 150;
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
