package jeudi.kiwi.Dialogs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jeudi.kiwi.Divers.PermissionsManager;
import jeudi.kiwi.Divers.AppsListManager;
import jeudi.kiwi.Divers.RightsTools;
import jeudi.kiwi.R;
import jeudi.kiwi.activity.SettingsActivity;
import jeudi.kiwi.adapters.CustomAppItem;
import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.adapters.PasswordViewListAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PasswordView {
    private AppsListManager appsListManager;
    private PermissionsManager permissionsManager;
    private RightsTools rightsTools;
    Context context;
    onExitListener exitListener;

    View view;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, buttonOK, buttonCancel;
    private ImageButton btnDEL;
    TextView password_text, input_text, info_text, timeText, alertText, textViewInfo1, textViewInfo2;
    CheckBox checkbox_allowwebviews, checkBox_wifi;
    RecyclerView appList;

    String currentPassword = "", nhours = "0005",selAppsIndex="";

    boolean timeSelection = false;

    boolean launch;

    private static String LOGTAG = "PasswordView";

    public interface onExitListener {
        void onExit();
        void onUnlock();
    }

    public void setOnExitSettingsListener(onExitListener listener) {
        exitListener = listener;
    }

    private PasswordViewListAdapter selViewAdapter;

    public void onOKPressed() {
        if (timeSelection) {
            stopTimeSelection();
        } else {
            if (permissionsManager.isPasswordValid(currentPassword)) onPasswordMatch();
            else {
                Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show();
            }
        }
    }




    public void startTimeSelection() {
        Log.i(LOGTAG, "Starting time selection");
        timeSelection = true;
        nhours = "";
        input_text.setVisibility(View.VISIBLE);
        input_text.setText(getTextHour());
        info_text.setText("Enter time and click OK to continue");

    }



    public void stopTimeSelection() {
        Log.i(LOGTAG, "Stopping time selection");
        timeSelection = false;
        input_text.setVisibility(View.GONE);
        info_text.setText("Enter password and click OK to validate");
    }

    public void changeOrientation(boolean landscape){
        LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        //pour prendre en compte la return key
        FrameLayout wrapper = new FrameLayout(context) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    exit();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

        };
    }


    public PasswordView(Context ctx, boolean lauch, String selAppsIndex, boolean landscape) {
        try {
            this.launch = lauch;
            this.selAppsIndex = selAppsIndex;
            context = ctx;

            LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

            //pour prendre en compte la return key
            FrameLayout wrapper = new FrameLayout(context) {
                @Override
                public boolean dispatchKeyEvent(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        exit();
                        return true;
                    }
                    return super.dispatchKeyEvent(event);
                }

            };

            wrapper.setFocusableInTouchMode(true);

            view = li.inflate((landscape)?R.layout.view_password_landscape :R.layout.view_password, wrapper);
            Log.i(LOGTAG, "new Dialog");
            wrapper.requestFocus();
            password_text = view.findViewById(R.id.password_text);
            input_text = view.findViewById(R.id.input_text);
            info_text = view.findViewById(R.id.textViewInfos);
            alertText = view.findViewById(R.id.textViewAlert);
            textViewInfo1 = view.findViewById(R.id.txtview_infos1);
            textViewInfo2 = view.findViewById(R.id.txtview_infos2);


            stopTimeSelection();
            permissionsManager = new PermissionsManager(context);
            appsListManager = new AppsListManager(context);
            rightsTools = new RightsTools(context);
            timeText = view.findViewById(R.id.hour);
            timeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startTimeSelection();
                }
            });
            timeText.setText(getTextHour());
            checkbox_allowwebviews = view.findViewById(R.id.checkBox_allowwebview);
            checkbox_allowwebviews.setVisibility(rightsTools.isOptionEnabled(rightsTools.WIFIMANAGEMENT)?View.VISIBLE:View.INVISIBLE);
            checkbox_allowwebviews.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)checkbox_allowwebviews.setBackgroundColor(Color.RED);
                    else checkbox_allowwebviews.setBackgroundColor(Color.BLACK);
                }
            });
            checkBox_wifi = view.findViewById(R.id.checkBox_allowwifi);
            checkBox_wifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) checkBox_wifi.setBackgroundColor(Color.RED);
                    else checkBox_wifi.setBackgroundColor(Color.BLACK);
                }
            });
            checkBox_wifi.setVisibility(rightsTools.isOptionEnabled(rightsTools.WIFIMANAGEMENT)?View.VISIBLE:View.INVISIBLE);
            btn0 = view.findViewById(R.id.button0);
            btn1 = view.findViewById(R.id.button1);
            btn2 = view.findViewById(R.id.button2);
            btn3 = view.findViewById(R.id.button3);
            btn4 = view.findViewById(R.id.button4);
            btn5 = view.findViewById(R.id.button5);
            btn6 = view.findViewById(R.id.button6);
            btn7 = view.findViewById(R.id.button7);
            btn8 = view.findViewById(R.id.button8);
            btn9 = view.findViewById(R.id.button9);
            btnDEL = view.findViewById(R.id.button_del);
            buttonOK = view.findViewById(R.id.buttonOK);
            buttonCancel = view.findViewById(R.id.buttoncancel);
            btn0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("0");
                }
            });
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("1");
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("2");
                }
            });
            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("3");
                }
            });
            btn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("4");
                }
            });
            btn5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("5");
                }
            });
            btn6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("6");
                }
            });
            btn7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("7");
                }
            });
            btn8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("8");
                }
            });
            btn9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnPasswordPressed("9");
                }
            });
            btnDEL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (timeSelection && nhours.length() > 0) {
                        nhours = nhours.substring(0, nhours.length() - 1);
                        timeText.setText(getTextHour());
                        input_text.setText(getTextHour());
                    } else {
                        if (currentPassword.length() > 0)
                            currentPassword = currentPassword.substring(0, currentPassword.length() - 1);
                        password_text.setText(currentPassword);
                    }
                }
            });
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exit();
                }
            });
            buttonOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOKPressed();

                }
            });
            textViewInfo1.setText("Unlock "+selAppsIndex.split("/").length+" apps for : ");
            if (selAppsIndex.contains(Constants.adminsettings)) {
                appList = view.findViewById(R.id.sel_list);
                appList.setVisibility(View.INVISIBLE);
                alertText.setVisibility(View.VISIBLE);
                textViewInfo1.setVisibility(View.INVISIBLE);
                textViewInfo2.setVisibility(View.INVISIBLE);
                timeText.setVisibility(View.INVISIBLE);
                checkbox_allowwebviews.setVisibility(View.INVISIBLE);
                checkBox_wifi.setVisibility(View.INVISIBLE);
            } else {
// set up the list of apps
                setAppList();
            }
            Log.i(LOGTAG," setup done !");
        }catch (Exception e){
            e.printStackTrace();
            exit();
            Toast.makeText(context,"an error occured", Toast.LENGTH_LONG).show();
        }
    }

    public void exit(){
        getView().setVisibility(View.GONE);
        if(exitListener!=null)
        exitListener.onExit();
    }
    public void unlock(){
        getView().setVisibility(View.GONE);
        if(exitListener!=null)
            exitListener.onUnlock();
    }
    private void setAppList(){
        Log.i(LOGTAG,"Setting up app list");
        appList = view.findViewById(R.id.sel_list);
        final ArrayList<CustomAppItem> apps = appsListManager.getAppsItems(selAppsIndex);
        LinearLayoutManager horizontalLayoutManager
            = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        appList.setLayoutManager(horizontalLayoutManager);
        selViewAdapter = new PasswordViewListAdapter(context, apps);
        appList.setAdapter(selViewAdapter);
        Log.i(LOGTAG,"done !");
    }


    public void onPasswordMatch() {
        try {
            Log.e(LOGTAG,"valid password entered");
            int time = Integer.parseInt(getTextHourWithZeros().substring(0, 2)) * 60 * 60 * 1000 + Integer.parseInt(getTextHour().substring(3, 5)) * 60 * 1000;

            String[] appsPackages = selAppsIndex.split("/");
            for (String appPackage : appsPackages) {
                if (time == 0)
                    permissionsManager.setApplicationTimeLeft(appPackage,-1);
                else {
                    permissionsManager.setApplicationTimeLeft(appPackage,time+System.currentTimeMillis());
                }
                permissionsManager.setWebViewsAllowed(appPackage, checkbox_allowwebviews.isChecked());
                permissionsManager.setWifiAllowed(appPackage, checkBox_wifi.isChecked());
            }
                String appPackage = appsPackages[0];
            Log.e(LOGTAG,appPackage+"  launch ?"+launch);
                Intent launchIntent;
                if (appPackage.equals(Constants.adminsettings))
                    launchIntent = new Intent(context, SettingsActivity.class);
                else
                    launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackage);
                if (launch) {
                    if (launchIntent != null) {
                        Log.e(LOGTAG, "Launching...");
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                    }
                }

            unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public View getView(){
        return view;
    }

    public void addToConstraintLayout(ConstraintLayout constraintLayout){
        Log.e(LOGTAG, "adding view to Layout...");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        constraintLayout.addView(getView(),params);
    }
    public void addToConstraintLayout(ConstraintLayout constraintLayout, Point size){
        Log.e(LOGTAG, "adding view to Layout...");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size.x, size.y);
        constraintLayout.addView(getView(),params);
        Log.e(LOGTAG,"DONE!");
    }

    public void btnPasswordPressed(String id) {
        if (timeSelection) {
            nhours = nhours + id;
            if (nhours.length() == 5) {
                nhours = (nhours).substring(0, 4);
            }
            timeText.setText(getTextHour());
            input_text.setText(getTextHour());
        } else {
            currentPassword = currentPassword + id;
            password_text.setText(currentPassword);
        }
    }

    String getTextHour() {
        if (nhours.length() == 4) {
            return (nhours.substring(0, 2) + "h" + nhours.substring(2, 4));
        } else if (nhours.length() == 3) {
            return (nhours.substring(0, 2) + "h" + nhours.substring(2, 3) + "_");
        } else if (nhours.length() == 2) {
            return (nhours.substring(0, 2) + "h__");
        } else if (nhours.length() == 1) {
            return (nhours.substring(0, 1) + "_h__");
        } else if (nhours.length() == 0) {
            return ("__h__");
        }
        return "__h__";
    }
    String getTextHourWithZeros() {
        if (nhours.length() == 4) {
            return (nhours.substring(0, 2) + "h" + nhours.substring(2, 4));
        } else if (nhours.length() == 3) {
            return (nhours.substring(0, 2) + "h" + nhours.substring(2, 3) + "0");
        } else if (nhours.length() == 2) {
            return (nhours.substring(0, 2) + "h00");
        } else if (nhours.length() == 1) {
            return (nhours.substring(0, 1) + "0h00");
        } else if (nhours.length() == 0) {
            return ("00h00");
        }
        return "00h00";
    }
}
