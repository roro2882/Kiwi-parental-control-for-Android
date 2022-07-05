package jeudi.kiwi.Divers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import jeudi.kiwi.R;


public class DeviceAdmin extends DeviceAdminReceiver {

    SharedPreferences spApps;
    @Override
    public void onEnabled(Context context, Intent intent) {


    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.e(getClass().getSimpleName(),"onDisableRequested");
        spApps = context.getSharedPreferences(
                context.getString(R.string.spapps), Context.MODE_MULTI_PROCESS);

        return "Are you sure?";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    }

}