package it.spinautomazioni.adm.crosswalkdemo;

import org.xwalk.core.JavascriptInterface;

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
    public String tablet() {
        return "Sono il Tablet";
    }

    @JavascriptInterface
    public int position() {
        return 150;
    }
}
