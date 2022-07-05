package jeudi.kiwi.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import jeudi.kiwi.Dialogs.DialogManager;
import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.Divers.PermissionsManager;
import jeudi.kiwi.Divers.RightsTools;
import jeudi.kiwi.Divers.Tools;
import jeudi.kiwi.R;
import jeudi.kiwi.fragments.UnlockFragment;
import jeudi.kiwi.fragments.UsageStatsFragment;
import jeudi.kiwi.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity{
    private static final String LOGTAG = "MainActivity";

    RightsTools rightsTools;
    ViewPager vpPager;
    ImageView mainBackground;
    ConstraintLayout rootLayout;
    PermissionsManager permissionsManager;
    DialogManager dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
        setContentView(R.layout.activity_main);
        rootLayout = findViewById(R.id.root_layout_main);
        vpPager = findViewById(R.id.vpPager);


        mainBackground = findViewById(R.id.mainbackground);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap background  = Tools.loadImageFromStorage(Constants.backgroundname, getApplicationContext());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(background!=null)
                                mainBackground.setImageBitmap(background);
                                else mainBackground.setImageResource(R.drawable.sun);

                            }
                        });
                }
            }).start();

        dialogManager = new DialogManager(this);
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
            rightsTools = new RightsTools(this);
            MyPagerAdapter adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        adapterViewPager.addFragment(new UnlockFragment(), "Unlock");
        adapterViewPager.addFragment(new HomeFragment(), "Home");

        if(rightsTools.isOptionEnabled(rightsTools.USAGESTATS)){
            adapterViewPager.addFragment(new UsageStatsFragment(), "Stats");
        }

        vpPager.setAdapter(adapterViewPager);
        vpPager.setOffscreenPageLimit(2);
        vpPager.setCurrentItem(1);
        permissionsManager = new PermissionsManager(this);
        if(permissionsManager.isFirstTime()){

            new AlertDialog.Builder(this)
                    .setTitle("Infos")
                    .setMessage("Hi you ! The default password 0000 \n There are awesome options in the settings, you should try them ;)")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            permissionsManager.setFirstTime(false);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
        }catch (Exception e){e.printStackTrace();}

    }
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        Log.e(LOGTAG, "onResume");
        permissionsManager.setApplicationTimeLeft(Constants.adminsettings,0);
        if(!rightsTools.weHaveAllPermissions())lockBcausePermissions();
        Intent i = new Intent(Constants.ResumeReceiverName);
        sendBroadcast(i);
        super.onResume();
    }

    public void lockBcausePermissions(){
        Intent in = new Intent(this, MissingPermissionsActivity.class);
        startActivity(in);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent i = new Intent(Constants.PauseReceiverName);
        sendBroadcast(i);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        public ArrayList<Fragment> pages;
        public ArrayList<String> names;


        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            pages = new ArrayList<>();
            names = new ArrayList<>();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return pages.size();
        }


        public void addFragment(Fragment f, String name){
            pages.add(f);
            names.add(name);
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            try{
                if(position<getCount())
           return pages.get(position);
                else return null;
            }catch (Exception e){e.printStackTrace();}
return null;
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return names.get(position);
        }

    }

}
