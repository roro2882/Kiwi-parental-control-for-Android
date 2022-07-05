package jeudi.kiwi.Divers;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;

import jeudi.kiwi.Dialogs.DialogManager;
import jeudi.kiwi.Dialogs.LockView;
import jeudi.kiwi.activity.MissingPermissionsActivity;


public class KiwiAccessibility extends AccessibilityService {
    private static final String LOGTAG = "KiwiAccessibility";
    private static final String appName = "Kiwi";
    private static final int depth = 5;
    PackageManager pm;
    Context context;
    LockView lockDialog;
    LayoutInflater layoutInflater = null;

    WifiManager wifiManager;

    PermissionsManager permissionsManager;

    RightsTools rightsTools;
    boolean MainStarted = false;
    long lastTimeRightschecked = 0;
    static final long intervalChecking = 1000;
    DialogManager dialogManager;

    String lastApp="";

    BroadcastReceiver mReceiver;

    // use this as an inner class like here or as a top-level class
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constants.ResumeReceiverName)) {
                try {
                    lockDialog.unlock();
                    MainStarted = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                MainStarted = false;
            }
        }

        // constructor
        public MyReceiver(){

        }
    }

    public void init() {
        try {
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            rightsTools = new RightsTools(context);
            pm = this.getPackageManager();
            permissionsManager = new PermissionsManager(context);
            dialogManager = new DialogManager(context);
            lockDialog = new LockView(context, dialogManager);
            wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            // get an instance of the receiver in your service
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ResumeReceiverName);
            filter.addAction(Constants.PauseReceiverName);
            mReceiver = new MyReceiver();
            registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getApplicationName(String appPackage) {
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( appPackage, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }


    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.e(LOGTAG, "Accessibility service connected");
        context = this;
        init();

    }

    public void checkRights() {
            lastTimeRightschecked = System.currentTimeMillis();
            if (!rightsTools.weHaveAllPermissions() && !permissionsManager.isApplicationAllowed(Constants.adminsettings)) {
                Intent in = new Intent(this, MissingPermissionsActivity.class);
                startActivity(in);
            }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {


            if (event.getPackageName() != null && rightsTools.isOptionEnabled(rightsTools.MONITORING)) {


                Log.i(LOGTAG, "new event !");
                Log.i(LOGTAG, "event package : " + event.getPackageName().toString());
                Log.i(LOGTAG, "event class name : " + event.getClassName().toString());
                Log.i(LOGTAG, "event class type : " + event.getEventType());
                Log.i(LOGTAG, "event class text : " + event.getText().toString());
                Log.i(LOGTAG, "last app:"+lastApp);
                if(MainStarted){
                    Log.e(LOGTAG, "MAIN STARTED");
                    lockDialog.unlock();
                    return;
                }

                if(!permissionsManager.areWifiAllowed(lastApp)&& rightsTools.isOptionEnabled(rightsTools.WIFIMANAGEMENT)){
                    if(wifiManager.isWifiEnabled())wifiManager.setWifiEnabled(false);
                }

                if (permissionsManager.isApplicationAllowed(Constants.adminsettings)) {
                    return;
                }




                if(rightsTools.isOptionEnabled(rightsTools.RECENTTASKSLOCK)&&event.getClassName().toString().contains("com.android.systemui.recents")){
                    Log.e(LOGTAG, "recents; locking");
                    lockDialog.lock(Constants.security_alert, "Task list");
                }
                String packageName = event.getPackageName().toString();
                //on vérifie que l'event vient d'une activitée
                if (pm.getLaunchIntentForPackage(packageName)==null) {
                    Log.i(LOGTAG, "ignoring event cause package doesn't have any launch intent");
                    return;
                }

                if (event.getPackageName().toString().contains(Constants.packageName)) {
                    return;
                }

                lastApp = packageName;

                    /*On verifie la presence d'elements dangereux*/
                    int rdepth = depth;
                    if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)rdepth+=2;
                    if (scanNode(event.getSource(),(rightsTools.isOptionEnabled(rightsTools.WIFIMANAGEMENT)&&!permissionsManager.areWebViewsAllowed(packageName)), rightsTools.isOptionEnabled(rightsTools.TEXTSEARCH),rdepth)) {
                        Log.e(LOGTAG, "dangerous event");
                        lockDialog.lock(Constants.security_alert, "This page");
                        return;
                    }
                if (!permissionsManager.isApplicationAllowed(packageName)) {
                    Log.e(LOGTAG, "unauthorized event : " + packageName + getApplicationName(packageName));
                    lockDialog.lock(packageName, getApplicationName(packageName));
                    return;
                }

                if (System.currentTimeMillis() - lastTimeRightschecked > intervalChecking) {
                    checkRights();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onInterrupt() {
        Log.e(LOGTAG, "interrupted");
        checkRights();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(LOGTAG, "unbinded");
        checkRights();
        return true;
    }

    @Override
    public void onDestroy() {
        checkRights();
        super.onDestroy();
    }

    public boolean scanNode(AccessibilityNodeInfo nodeInfo,boolean searchForWebView, boolean searchForText, int profondeur) {
        if (nodeInfo == null) return false;
        if (profondeur == 0) return false;
        CharSequence textc = nodeInfo.getText();
        if(textc!=null && searchForText&&textc.toString().contains(appName))return true;
        if(searchForWebView && nodeInfo.getClassName()!=null && nodeInfo.getClassName().toString().contains(WebView.class.getSimpleName()))return true;
        if(profondeur!=1)
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            if(scanNode(nodeInfo.getChild(i),searchForWebView, searchForText, profondeur - 1))return true;
        }
        if(scanNode(nodeInfo.getParent(), searchForWebView, searchForText, 1))return true;
        return false;
    }

}