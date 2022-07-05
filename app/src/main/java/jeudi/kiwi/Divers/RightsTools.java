package jeudi.kiwi.Divers;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

import jeudi.kiwi.R;

import static android.provider.Settings.canDrawOverlays;

public class RightsTools {
    Context context;
    ComponentName mDeviceAdmin;
    DevicePolicyManager mDPM;
    SharedPreferences permissionsDB;
    SharedPreferences.Editor permissionsDBEditor;
    public final String DEFAULTLAUNCHER = "defaultlauncher", DEVICE_ADMIN = "DeviceAdmin", BATTERIE_OPTI = "batopti",
            MONITORING = "accessibi", USAGESTATS = "usagestats", WIFIMANAGEMENT ="webviewlock", RECENTTASKSLOCK="recentslock", TEXTSEARCH="textsearch";

    public RightsTools(Context ctx){
        context = ctx;
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(Constants.packageName,"jeudi.kiwi.Divers.DeviceAdmin");
        permissionsDB = context.getSharedPreferences(Constants.SPIDAppList,Context.MODE_MULTI_PROCESS);
        permissionsDBEditor = permissionsDB.edit();
    }

    public boolean isOptionEnabled(String id){
        return permissionsDB.getBoolean(id, false);
    }

    public void setOptionEnabled(String id, boolean enabled){
        permissionsDBEditor.putBoolean(id, enabled).apply();
    }

    public boolean weHaveAllPermissions(){
        if(isOptionEnabled(MONITORING)&&!isSystemAlertPermissionGranted())return false;
        if(isOptionEnabled(DEFAULTLAUNCHER)&&!isDefautLauncher())return false;
        if(isOptionEnabled(DEVICE_ADMIN)&&!isAdminEnabled())return false;
        if(isOptionEnabled(BATTERIE_OPTI)&&!isBatteryUnoptimised())return false;
        if(isOptionEnabled(MONITORING)&&!isAccessibilityEnabled())return false;
        if(isOptionEnabled(USAGESTATS)&&!isUsageStatsAllowed())return false;
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void requestActivityInfosPermission(){
        context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    public void requestSystemAlertPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        final String packageName = context == null ? "" : Constants.packageName;
        final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        if (context != null)
            context.startActivity(intent);
    }

    public void requestBatteryNonoptimization() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assert pm != null;
            if (!pm.isIgnoringBatteryOptimizations(Constants.packageName)) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + Constants.packageName));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void requestDefaultLauncher() {
        try{
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(selector);

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }catch (Exception e){e.printStackTrace();}

    }

    public void requestForAdmin() {
        // Launch the activity to have the user enable our admin.

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.add_admin_extra_app_text));
        context.startActivity(intent);
        // return false - don't update checkbox until we're really active
    }

    ////////////////////////CHECKING////////////////////////////////////

    public boolean isUsageStatsAllowed(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppOpsManager appOps = (AppOpsManager) context
                    .getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (appOps != null) {
                mode = appOps.checkOpNoThrow("android:get_usage_stats",
                        android.os.Process.myUid(), Constants.packageName);
            }
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            return granted;
        }else return true;
    }

    public boolean isDefautLauncher() {
        try{
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
            if (res.activityInfo == null) {
                return false;
            } else if ("android".equals(res.activityInfo.packageName)) {
                return false;
            } else {
                try {
                    return res.activityInfo.packageName.equals(Constants.packageName);
                } catch (Exception ignored) {

                }

            }
        }catch (Exception e){e.printStackTrace();}

        return false;
    }
    public boolean isBatteryUnoptimised() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        else {
            assert pm != null;
            return pm.isIgnoringBatteryOptimizations(Constants.packageName);
        }
    }

    public boolean isAdminEnabled() {
        try{
            assert mDPM != null;
            return mDPM.isAdminActive(mDeviceAdmin);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }


    public boolean isAccessibilityEnabled() {
        try{
            AccessibilityManager am = (AccessibilityManager) context
                    .getSystemService(Context.ACCESSIBILITY_SERVICE);

            assert am != null;
            List<AccessibilityServiceInfo> runningServices = am
                    .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
            for (AccessibilityServiceInfo service : runningServices) {
                Log.e("tag",service.getId());
                if (service.getId().contains("kiwi")) {
                    return true;
                }
            }
        }catch (Exception e){e.printStackTrace();}

        return false;
    }

    public boolean isSystemAlertPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return canDrawOverlays(context);
        }else{
            return true;
        }
    }
}
