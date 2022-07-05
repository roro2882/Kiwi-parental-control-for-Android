package jeudi.kiwi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import jeudi.kiwi.Dialogs.PasswordView;
import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.Divers.RightsTools;
import jeudi.kiwi.Divers.Tools;
import jeudi.kiwi.R;

public class MissingPermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_permissions);
        context = this;
        initBlockView();
    }
    Context context;
    RightsTools rightsTools;
    private static final String LOGTAG = "MissingPermissionsView";

    boolean quit;
    ConstraintLayout rootLayout;
    private Button SettingsBtn;


    public void initBlockView(){
        try {

            rightsTools = new RightsTools(context);

            rootLayout = findViewById(R.id.password_c);
            SettingsBtn = findViewById(R.id.button_access_settings);
            SettingsBtn.setOnClickListener(buttonSettingsPressed);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!quit){
                        if (rightsTools.weHaveAllPermissions()) {
                            unlock();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        if(!rightsTools.weHaveAllPermissions()&&!quit&&isScreenOn(this)) {
            Intent me = new Intent(this, MissingPermissionsActivity.class);
            me.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(me);
        }
        super.onPause();
    }

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    View.OnClickListener buttonSettingsPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            askForPassword();
        }
    };

    public void askForPassword(){
        try {
            int currentOrientation = getResources().getConfiguration().orientation;
            PasswordView passwordView = new PasswordView(context, false, Constants.adminsettings, currentOrientation== Configuration.ORIENTATION_LANDSCAPE);
            passwordView.setOnExitSettingsListener(new PasswordView.onExitListener() {
                @Override
                public void onExit() {

                }

                @Override
                public void onUnlock() {
                    quit = true;
                    Intent intent = new Intent(context,SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            passwordView.addToConstraintLayout(rootLayout);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unlock(){
        quit=true;
        Tools.goHome(this);
    }


}
