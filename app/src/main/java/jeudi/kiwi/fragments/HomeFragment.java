package jeudi.kiwi.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import jeudi.kiwi.Dialogs.PasswordView;
import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.Divers.PermissionsManager;
import jeudi.kiwi.Divers.AppsListManager;
import jeudi.kiwi.adapters.CustomAppItem;
import jeudi.kiwi.adapters.BaseAppGridAdapter;
import jeudi.kiwi.R;


public class HomeFragment extends Fragment {
    GridView authAppsGridView = null;
    Button buttonSettings = null;
    ConstraintLayout rootLayout;

    Context context;
    private BaseAppGridAdapter appViewAdapter;
    private PermissionsManager permissionsManager;
    private AppsListManager appsListManager;

    static final String LOGTAG = "HomeFragment";

    ArrayList<CustomAppItem> apps;
    String allowedAppsIndex="";

    private int mInterval = 1000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    // newInstance constructor for creating fragment with arguments
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            context = getActivity();
            authAppsGridView = rootView.findViewById(R.id.apps_list);
            buttonSettings = rootView.findViewById(R.id.button_settings);
            rootLayout = rootView.findViewById(R.id.rootView);

            permissionsManager = new PermissionsManager(context);
            appsListManager = new AppsListManager(context);

            apps = new ArrayList<>();
            appViewAdapter = new BaseAppGridAdapter(context, apps);
            authAppsGridView.setAdapter(appViewAdapter);
            initListeners();
            mHandler = new Handler();
            startRepeatingTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void initListeners() {
        authAppsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String packageName = appViewAdapter.getItem(position).getPackageName();

                try {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launchIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        authAppsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String packageName = apps.get(i).getPackageName();
                    permissionsManager.lockApplication(packageName);
                    updateApps();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentOrientation = getResources().getConfiguration().orientation;
                PasswordView passwordView = new PasswordView(context,true, Constants.adminsettings+"/", currentOrientation == Configuration.ORIENTATION_LANDSCAPE);
                passwordView.addToConstraintLayout(rootLayout);
                passwordView.setOnExitSettingsListener(new PasswordView.onExitListener() {
                    @Override
                    public void onExit() {

                    }

                    @Override
                    public void onUnlock() {
                        getActivity().finish();
                    }
                });
            }
        });
    }


    void updateApps() {
        try {
            String[] appsPackages = appsListManager.getAppsIndex();
            for (String packageName : appsPackages) {
                long time = permissionsManager.getApplicationTimeLeft(packageName);
                if (time >= -1 && !allowedAppsIndex.contains(packageName)) {
                    apps.add(new CustomAppItem(permissionsManager.timeToString(time), packageName, context));
                    allowedAppsIndex = allowedAppsIndex + packageName + "/";
                }
            }
            for (final CustomAppItem appItem : apps) {
                long time = permissionsManager.getApplicationTimeLeft(appItem.getPackageName());
                if (time < -1) {
                    allowedAppsIndex = allowedAppsIndex.replace(appItem.getPackageName() + "/", "");
                    apps.remove(appItem);
                } else {
                    appItem.setInfo(permissionsManager.timeToString(time));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appItem.updateInfoText();
                            }
                            });
                                                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        authAppsGridView.invalidateViews();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateApps();
                    }
                }).start();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}


