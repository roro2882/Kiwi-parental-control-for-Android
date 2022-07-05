package jeudi.kiwi.Dialogs;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.Divers.Tools;
import jeudi.kiwi.R;

import static android.content.Context.ACTIVITY_SERVICE;

public class LockView {
    Context context;
    DialogManager dialogManager;
    private static final String LOGTAG = "LockView";
    PasswordView passwordView;

    List<ActivityManager.RunningAppProcessInfo> processes;

    long timeOfLock=0;
    View screenBlockView;
    ConstraintLayout rootLayout;
    private Button unlock,home;
    TextView textViewInfos;
    String appLocked = "";
    private String appName;
    ObjectAnimator colorFade;

    public LockView(Context ctx, DialogManager dialogManager){
        context = ctx;
        this.dialogManager = dialogManager;
        initBlockView();

    }

    public void lock(String lockedAppPackage, String lockedAppName){

        if(!appLocked.equals(lockedAppPackage)){
            colorFade = ObjectAnimator.ofObject(rootLayout, "backgroundColor", new ArgbEvaluator(), Color.argb(0,0,0,0), 0xff000000);
            colorFade.setDuration(10000);
            colorFade.start();
            appLocked = lockedAppPackage;
            appName = lockedAppName;
            textViewInfos.setText(appName + " is locked");
            if (appLocked.equals(Constants.security_alert)) unlock.setVisibility(View.GONE);
            else {
                unlock.setVisibility(View.VISIBLE);
            }

            rootLayout.setVisibility(View.VISIBLE);
            timeOfLock = System.currentTimeMillis();
            screenBlockView.setVisibility(View.VISIBLE);

        }

    }


    public void askForPassword(){
        passwordView = new PasswordView(context, false, appLocked+"/", getView().getWidth()>getView().getHeight());
        passwordView.setOnExitSettingsListener(unlockListener);
        passwordView.addToConstraintLayout(rootLayout, dialogManager.getScreenSize());

    }

    public void unlock(){
        colorFade.cancel();
        rootLayout.setBackgroundColor(Color.TRANSPARENT);
        screenBlockView.setVisibility(View.GONE);
        passwordView = null;
        appLocked = "";

    }
    public void initBlockView(){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        FrameLayout wrapper = new FrameLayout(context) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    Log.e(LOGTAG,"BAAACKKKK");
                    return false;
                }
                return super.dispatchKeyEvent(event);
            }

            //if pressed home key,
            public void onCloseSystemDialogs(String reason) {
                //The Code Want to Perform.
                System.out.println("System dialog " + reason);
                if (reason.equals("homekey")) {
                    // handle home button
                }
            }

        };
        screenBlockView = layoutInflater.inflate(R.layout.view_lock, wrapper);

        rootLayout = screenBlockView.findViewById(R.id.password_c);
        textViewInfos = screenBlockView.findViewById(R.id.locked_text_infos);
        home = screenBlockView.findViewById(R.id.button_home);
        home.setOnClickListener(buttonHomePressed);
        unlock = screenBlockView.findViewById(R.id.button_unlock);
        unlock.setOnClickListener(buttonUnlockPressed);
        screenBlockView.setVisibility(View.GONE);
        dialogManager.showView(screenBlockView);
        dialogManager.setOrientationListener(new DialogManager.onScreenRotate() {
            @Override
            public void onOrientationChanged() {
                if(passwordView!=null) {
                    passwordView.exit();
                    passwordView = new PasswordView(context, false, appLocked + "/", dialogManager.getScreenSize().x > dialogManager.getScreenSize().y);
                    passwordView.setOnExitSettingsListener(unlockListener);
                    passwordView.addToConstraintLayout(rootLayout, dialogManager.getScreenSize());
                }
            }
        });

    }

    public View getView(){
        return screenBlockView;
    }

    View.OnClickListener buttonHomePressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Tools.goHome(context);
        }
    };

    View.OnClickListener buttonUnlockPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            askForPassword();
        }
    };

    PasswordView.onExitListener unlockListener = new PasswordView.onExitListener(){

        @Override
        public void onExit() {
            passwordView = null;
        }

        @Override
        public void onUnlock() {
            unlock();
        }
    };

}
