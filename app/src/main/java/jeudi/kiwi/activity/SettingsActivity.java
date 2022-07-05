package jeudi.kiwi.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import jeudi.kiwi.Divers.Constants;
import jeudi.kiwi.Divers.PermissionsManager;
import jeudi.kiwi.Divers.Tools;
import jeudi.kiwi.R;
import jeudi.kiwi.Divers.RightsTools;

public class SettingsActivity extends AppCompatActivity {
    private static final String LOGTAG = "Settings";
    Context context;
    RightsTools rightsTools;
    PermissionsManager permissionsManager;
    LinearLayout lay_permissions;
    Button accessibility,admin,overlay,launcher,battery, usageStats, actualizeBtn;
    CheckBox accessibility_checkbox,admin_checkbox,launcher_checkbox,battery_checkbox, usageStats_checkbox, webview_lock_checkbox, recents_checkbox, textsearch_checkbox;
    Button buttonChangePassword, buttonChangeWallpaper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context=this;

        permissionsManager = new PermissionsManager(this);
        rightsTools = new RightsTools(this);

        Log.e(LOGTAG, "eonCreate");
        accessibility = findViewById(R.id.button_accessibility);
        battery = findViewById(R.id.button_battery);
        admin = findViewById(R.id.button_admin);
        launcher = findViewById(R.id.button_defaultlauncher);
        overlay = findViewById(R.id.button_overlay);
        usageStats = findViewById(R.id.button_activity_infos);

        buttonChangePassword = findViewById(R.id.button_change_password);
        buttonChangeWallpaper = findViewById(R.id.button_change_wallpaper);
        actualizeBtn = findViewById(R.id.actualize_btn);
        lay_permissions = findViewById(R.id.layout_missing_permissions);

        accessibility_checkbox = findViewById(R.id.checkBox_accessibility);
        battery_checkbox = findViewById(R.id.checkBox_battery);
        admin_checkbox = findViewById(R.id.checkBox_admin);
        launcher_checkbox = findViewById(R.id.checkBox_defaultlauncher);
        usageStats_checkbox = findViewById(R.id.checkBox_activity_infos);
        webview_lock_checkbox = findViewById(R.id.checkBox_webviewlock);
        recents_checkbox = findViewById(R.id.checkBox_recentlock);
        textsearch_checkbox = findViewById(R.id.checkBox_textSearch);

        accessibility_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.MONITORING,b);
                actualizePermissions();
            }
        });
        battery_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.BATTERIE_OPTI,b);
                actualizePermissions();
            }
        });admin_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.DEVICE_ADMIN,b);
                actualizePermissions();
            }
        });launcher_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.DEFAULTLAUNCHER,b);
                actualizePermissions();
            }
        });usageStats_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.USAGESTATS,b);
                actualizePermissions();
            }
        });
        textsearch_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.TEXTSEARCH,b);
                actualizePermissions();
            }
        });
        webview_lock_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.WIFIMANAGEMENT,b);
                actualizePermissions();
            }
        });
        recents_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rightsTools.setOptionEnabled(rightsTools.RECENTTASKSLOCK,b);
                actualizePermissions();
            }
        });
        actualizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizePermissions();
            }
        });
        accessibility.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        battery.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightsTools.requestBatteryNonoptimization();
            }
        });
        admin.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightsTools.requestForAdmin();
            }
        });
        launcher.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightsTools.requestDefaultLauncher();
            }
        });
        overlay.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightsTools.requestSystemAlertPermission();
            }
        });
        usageStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightsTools.requestActivityInfosPermission();
            }
        });
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForPassword();
            }
        });
        buttonChangeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    //do your check here
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");

                        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");

                        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                        startActivityForResult(chooserIntent, PICK_IMAGE);
                    }else{
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }

            }
        });
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOGTAG,"Permission is granted");
                return true;
            } else {

                Log.v(LOGTAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOGTAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(LOGTAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    public static final int PICK_IMAGE = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            decodeUri(data.getData());
        }
    }

    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD.getFileDescriptor();

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(imageSource, null, o);

            // the new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
        /*    while (true) {
                if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }*/

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);
            Tools.saveToInternalStorage(bitmap,Constants.backgroundname,context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parcelFD != null)
                try {
                    parcelFD.close();
                } catch (IOException e) {
                    // ignored
                }
        }
    }
    public void askForPassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the new password");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String password1 = input.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter again the password");

// Set up the input
                final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String password2 = input.getText().toString();

                        if(password1.equals(password2)) {
                            permissionsManager.setPassword(password1);
                            Toast.makeText(context,"Ok : Password changed", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context,"Error : the 2 passwords are different", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }


private void actualizePermissions(){
        lay_permissions.setVisibility(rightsTools.weHaveAllPermissions()?View.GONE:View.VISIBLE);
    admin.setVisibility((!rightsTools.isAdminEnabled()&&rightsTools.isOptionEnabled(rightsTools.DEVICE_ADMIN))?View.VISIBLE:View.GONE);
    overlay.setVisibility((!rightsTools.isSystemAlertPermissionGranted()&&rightsTools.isOptionEnabled(rightsTools.MONITORING))?View.VISIBLE:View.GONE);
    launcher.setVisibility((!rightsTools.isDefautLauncher()&&rightsTools.isOptionEnabled(rightsTools.DEFAULTLAUNCHER))?View.VISIBLE:View.GONE);
    battery.setVisibility((!rightsTools.isBatteryUnoptimised()&&rightsTools.isOptionEnabled(rightsTools.BATTERIE_OPTI))?View.VISIBLE:View.GONE);
    accessibility.setVisibility((!rightsTools.isAccessibilityEnabled()&&rightsTools.isOptionEnabled(rightsTools.MONITORING))?View.VISIBLE:View.GONE);
    usageStats.setVisibility((!rightsTools.isUsageStatsAllowed()&&rightsTools.isOptionEnabled(rightsTools.USAGESTATS))?View.VISIBLE:View.GONE);
}

    private void actualizeOptions(){
        admin_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.DEVICE_ADMIN));
        launcher_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.DEFAULTLAUNCHER));
        battery_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.BATTERIE_OPTI));
        accessibility_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.MONITORING));
        usageStats_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.USAGESTATS));
        webview_lock_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.WIFIMANAGEMENT));
        recents_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.RECENTTASKSLOCK));
        textsearch_checkbox.setChecked(rightsTools.isOptionEnabled(rightsTools.TEXTSEARCH));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
    actualizePermissions();
    actualizeOptions();
    super.onResume();
}

@Override
    public void onDestroy(){
        super.onDestroy();
}
}
