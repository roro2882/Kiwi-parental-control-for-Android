package jeudi.kiwi.Divers;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionsManager {
    SharedPreferences appsDB;
    SharedPreferences.Editor appsDBeditor;
    static final String maxTimeID = "maxTime";
    static final String passwordID = "password";
    static final String wifiID = "wifi";
    static final String webViewID = "webview";
    static final String firstTimeID = "ft";
    static final String dangerousEventsID = "dangerousEvents";

    static final String LOGTAG = "AppPermissionManager";

    public PermissionsManager(Context ctx){
        appsDB = ctx.getSharedPreferences(
                Constants.SPIDAppPermissions, Context.MODE_MULTI_PROCESS);
        appsDBeditor = appsDB.edit();
    }

    public long getApplicationTimeLeft(String appPackage){
        long time = appsDB.getLong(appPackage + maxTimeID, 0);
        return (time==-1)?-1:time-System.currentTimeMillis();
    }

    public boolean isApplicationAllowed(String appPackage){
        if (Constants.always_authorised_packages.contains(appPackage))return true;
        long time = getApplicationTimeLeft(appPackage);
        return (time>=-1);
    }

    public void setApplicationTimeLeft(String appPackage,long timeLimit){
        appsDBeditor.putLong(appPackage+ maxTimeID,timeLimit);
        appsDBeditor.apply();
    }

    public void setWebViewsAllowed(String appPackage, boolean allowed){
        appsDBeditor.putBoolean(appPackage+ webViewID,allowed);
        appsDBeditor.apply();
    }

    public boolean areWebViewsAllowed(String appPackage){
        return appsDB.getBoolean(appPackage+webViewID, false);
    }

    public void setWifiAllowed(String appPackage, boolean allowed){
        appsDBeditor.putBoolean(appPackage+ wifiID,allowed);
        appsDBeditor.apply();
    }

    public boolean areWifiAllowed(String appPackage){
        return appsDB.getBoolean(appPackage+wifiID, false);
    }

    public boolean isFirstTime(){
        return appsDB.getBoolean(firstTimeID, true);
    }

    public void setFirstTime(boolean firstTime){
         appsDBeditor.putBoolean(firstTimeID, firstTime);
         appsDBeditor.apply();
    }

    public void setDangerousEventsAllowed(String appPackage, boolean allowed){
        appsDBeditor.putBoolean(appPackage+ dangerousEventsID,allowed);
        appsDBeditor.apply();
    }

    public boolean areDangerousEventsAllowed(String appPackage){
        return appsDB.getBoolean(appPackage+dangerousEventsID, false);
    }

    public void lockApplication(String appPackage){
        setApplicationTimeLeft(appPackage,0);
    }

    public String timeToString(long time){
        String result = "";
        if (time==-1)
            result = "forever";
        else if ((time > 0)) {
            if (((float) time) / 1000F / 60F / 60F / 24F > 1)
                result = (int) (((float) time) / 1000F / 60F / 60F / 24F) + " day(s)";
            else if (((float) time) / 1000F / 60F / 60F > 1)
                result = (int) (((float) time) / 1000F / 60F / 60F) + " hour(s)";
            else if (((float) time) / 1000F / 60F > 1)
                result = "" + (int) (((float) time) / 1000F / 60F) + " minute(s)";
            else if (((float) time) / 1000F > 1)
                result = "" + (int) (((float) time) / 1000F) + " second(s)";
        }
        return result;
    }

    public SharedPreferences getAppsDB() {
        return appsDB;
    }

    public String getPassword(){
        return appsDB.getString(passwordID,"0000");
    }

    public void setPassword(String password){
        appsDBeditor.putString(passwordID,password);
        appsDBeditor.apply();
    }

    public boolean isPasswordValid(String password){
        return (password.equals(getPassword()));
    }
}
