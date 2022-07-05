package jeudi.kiwi.Divers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jeudi.kiwi.adapters.CustomAppItem;

import static jeudi.kiwi.Divers.Tools.drawableToBitmap;
import static jeudi.kiwi.Divers.Tools.getResizedBitmap;
import static jeudi.kiwi.Divers.Tools.saveToInternalStorage;

public class AppsListManager {
    Context context;
    SharedPreferences appsList;
    SharedPreferences.Editor appsListEditor;
    PackageManager packageManager;

    static final String indexID = "index";

    static final String LOGTAG = "AppsListManager";

    public AppsListManager(Context context) {
        this.context = context;
        appsList = context.getSharedPreferences(Constants.SPIDAppList, Context.MODE_MULTI_PROCESS);
        appsListEditor = appsList.edit();
        packageManager = context.getPackageManager();
    }

    public String[] getAppsIndex() {
        String appsIndex = appsList.getString("index", "");
        return appsIndex.split("/");
    }

    public String ArrayToString(String[] array) {
        StringBuilder result = new StringBuilder();
        for (String s1 : array) {
            result.append(s1).append("/");
        }
        return result.toString();
    }

    private String[] StringToArray(String array) {
        return array.split("/");
    }

    private void saveIcon(ApplicationInfo app){
        Drawable icon = packageManager.getApplicationIcon(app);
        saveToInternalStorage(getResizedBitmap(drawableToBitmap(icon), 80), app.packageName, context);
    }

    public File getIcon(String packageName){
        return new File(context.getFilesDir(),packageName+".jpg");
    }

    public void updateAppList() {
        Log.e(LOGTAG,"updating App List...");
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);
        String index = appsList.getString(indexID, "");
        for (int i = 0; i < packages.size(); i++) {
            ApplicationInfo app = packages.get(i);
            String packageName = app.packageName;
            if (index.contains(packageName)||packageManager.getLaunchIntentForPackage(packageName) == null) continue;
            saveIcon(app);
            index = index + packageName + "/";
        }
        String[] apps = StringToArray(index);
        for (String app : apps) {
            if (packageManager.getLaunchIntentForPackage(app) == null) {
                index = index.replace(app + "/", "");
            }
        }
        apps = StringToArray(index);
        sortApplicationsByName(apps);
        appsListEditor.putString(indexID, ArrayToString(apps));
        appsListEditor.apply();
    }

    public String getApplicationName(String appPackage) {
        ApplicationInfo ai;
        try {
            ai = packageManager.getApplicationInfo( appPackage, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }

    private void sortApplicationsByName(String[] appPackages) {
        Comparator<String> localeComparator = new Comparator<String>() {
            public int compare(String s1, String s2) {
                return getApplicationName(s1).compareToIgnoreCase(getApplicationName(s2));
            }
        };
        Arrays.sort(appPackages, localeComparator);
    }

    public ArrayList<CustomAppItem> getAppsItems() {
        ArrayList<CustomAppItem> apps = new ArrayList<>();
        String[] appsPackages = getAppsIndex();
        for (String packageName : appsPackages) {
            if (!packageName.equals("")) {
                apps.add(new CustomAppItem(getApplicationName(packageName), packageName, context));
            }
        }
        return apps;
    }

    public ArrayList<CustomAppItem> getAppsItems(String index) {
        ArrayList<CustomAppItem> apps = new ArrayList<>();
        String[] appsPackages = StringToArray(index);
        for (String packageName : appsPackages) {
            if (!packageName.equals("")) {
                apps.add(new CustomAppItem(getApplicationName(packageName), packageName, context));
            }
        }
        return apps;
    }
}
