package jeudi.kiwi.Dialogs;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.SENSOR_SERVICE;
import static android.support.constraint.Constraints.TAG;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

public class DialogManager {
    Context context;
    WindowManager windowManager;
    LayoutInflater layoutInflater;
    Rect screenBounds;
    Point screenSize;
    View currentView;

    int lastOrientation = 0;
    static final String LOGTAG = "DialogManager";
    private int lastWidth=0;
    onScreenRotate orientationListener;
    public interface onScreenRotate {
        void onOrientationChanged();
    }

    public DialogManager(Context ctx){
        context=ctx;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        initOrientationReceiver();
    }

    public void initOrientationReceiver(){
        SensorEventListener m_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if(lastOrientation == 1)                    onScreenActualized();
                    lastOrientation = 0;
                } else {
                    if(lastOrientation == 0)                    onScreenActualized();
                    lastOrientation=1;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorManager sm = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(sensor!=null)
        sm.registerListener(m_sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        else{
            OrientationEventListener orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
                public void onOrientationChanged(int orientation) {
                    getDisplayDimens();
                    if(screenBounds.width()!=lastWidth) {
                        onScreenActualized();
                        Log.e(LOGTAG, "Orientation changed");
                        lastWidth=screenBounds.width();
                    }
                }
            };
            orientationListener.enable();
        }
        Log.e(LOGTAG,"orientation receiver started");
    }

    public boolean viewIsShowing(){
        return currentView==null;
    }

    public void getDisplayDimens(){
        Display display = null;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
        screenBounds = new Rect();
        screenSize = new Point();
        if (display != null) {
            display.getRectSize(screenBounds);
            display.getRealSize(screenSize);
        }

    }

    public Point getScreenSize(){
        Display display = null;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
        Point screenSize = new Point();
        if (display != null) {
            display.getSize(screenSize);
        }
        return screenSize;
    }

    public void onScreenActualized(){ ///to call when screen rotates
        View v = currentView;
        showView(v);
        if(orientationListener!=null)orientationListener.onOrientationChanged();
    }

    public void setOrientationListener(onScreenRotate orientationListener) {
        this.orientationListener = orientationListener;
    }

    public void showView(View view){
        if(view!=null) {

            getDisplayDimens();
            WindowManager.LayoutParams params;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | FLAG_NOT_TOUCH_MODAL |FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
            } else {
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |FLAG_NOT_TOUCH_MODAL | FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
            }

            params.x = screenBounds.left;
            params.y = screenBounds.top;
            params.width = screenSize.x;
            params.height = screenSize.y;
            try {
                clearScreen();
                windowManager.addView(view, params);
            }catch (Exception e){
                e.printStackTrace();
            }
            currentView = view;
        }
    }



    public void clearScreen(){
        if(currentView!=null) {
            try {
                windowManager.removeView(currentView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentView = null;
    }
}
