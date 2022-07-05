package jeudi.kiwi.fragments;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import java.util.ArrayList;

import jeudi.kiwi.Dialogs.PasswordView;
import jeudi.kiwi.Divers.AppsListManager;
import jeudi.kiwi.adapters.CustomAppItem;
import jeudi.kiwi.adapters.BaseAppGridAdapter;
import jeudi.kiwi.R;

public class UnlockFragment extends Fragment {
    Context context;
    GridView appsList;
    View rootView;
    ConstraintLayout rootLayout;
    PasswordView passView;

    private static final String LOGTAG = "UnlockFragment";

    private BaseAppGridAdapter appViewAdapter;


    AppsListManager appsListManager;

    ArrayList<CustomAppItem> apps;

    ArrayList<Integer> selectedAppsId = new ArrayList<>();
    String selAppsIndex = "";
    ConstraintLayout selectionLayout;


    public static UnlockFragment newInstance() {
        return new UnlockFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        appsListManager = new AppsListManager(context);

        rootView = inflater.inflate(R.layout.fragment_unlock, container, false);
        try {
            selectionLayout = rootView.findViewById(R.id.selection_layout);
            appsList = rootView.findViewById(R.id.apps_list);
            rootLayout = rootView.findViewById(R.id.rootView1);
            //You need to add the following line for this solution to work; thanks skayred
            rootLayout.setFocusableInTouchMode(true);
            rootLayout.requestFocus();
            rootLayout.setOnKeyListener( new View.OnKeyListener()
            {
                @Override
                public boolean onKey( View v, int keyCode, KeyEvent event )
                {
                    if( keyCode == KeyEvent.KEYCODE_BACK )
                    {   unselectAll();
                    }
                    return false;
                }
            } );

            initButtons();
            updateAppListInBackground();
            makeList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        updateAppListInBackground();
    }

    void updateAppListInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appsListManager.updateAppList();
                makeList();
            }
        }).start();
    }

    void askForPassword() {
        int currentOrientation = getResources().getConfiguration().orientation;
        final PasswordView passwordView = new PasswordView(context, false, selAppsIndex,currentOrientation == Configuration.ORIENTATION_LANDSCAPE);
        passwordView.setOnExitSettingsListener(new PasswordView.onExitListener() {
            @Override
            public void onExit() {
                rootLayout.removeView(passwordView.getView());
            }

            @Override
            public void onUnlock() {
                rootLayout.removeView(passwordView.getView()); unselectAll();
            }
        });
        passwordView.addToConstraintLayout(rootLayout);
    }

    void initButtons() {
        ImageButton unlock = rootView.findViewById(R.id.unlock_button);
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForPassword();
            }
        });

        ImageButton back = rootView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselectAll();
            }
        });
    }

    public void unselectAll(){
        selAppsIndex = "";
        selectionLayout.setVisibility(View.INVISIBLE);
        for (int id : selectedAppsId) {
            apps.get(id).setSelected(false);
            apps.get(id).updateView();
        }
        selectedAppsId = new ArrayList<>();
        appsList.invalidateViews();
    }

    void selectApp(int position) {
        selectionLayout.setVisibility(View.VISIBLE);
        selectedAppsId.add(position);
        apps.get(position).setSelected(true).updateView();
        selAppsIndex = selAppsIndex + apps.get(position).getPackageName() + "/";
    }

    void deselectApp(int position) {
        apps.get(position).setSelected(false).updateView();
        selAppsIndex = selAppsIndex.replace(apps.get(position).getPackageName() + "/", "");
        selectedAppsId.remove((Integer) position);
        if (selAppsIndex.equals("")) {
            selectionLayout.setVisibility(View.INVISIBLE);
        }
    }

    void makeList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    apps = appsListManager.getAppsItems();
                    appViewAdapter = new BaseAppGridAdapter(context, apps);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appsList.setAdapter(appViewAdapter);
                            appsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View v,
                                                        int position, long id) {
                                    if (apps.get(position).isLocked()) {
                                        deselectApp(position);
                                    } else {
                                        selectApp(position);
                                    }
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}