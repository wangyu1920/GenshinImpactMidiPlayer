package com.example.genshinimpactmidiplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.autoclick.AutoService;
import com.autoclick.FloatingView;
import com.leff.midi.MidiFile;
import com.leff.midi.examples.EventsCollection;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //FrameLayout mLayout;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    MidiFile midiFile;
    EventsCollection ec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewInit();

    }

    private void setViewInit() {
        //打开和关闭悬浮窗按钮的初始化
        findViewById(R.id.startFloat).setOnClickListener(this);
        findViewById(R.id.stopFloat).setOnClickListener(this);
        //读midi文件按钮的初始化 以及 展示midi文件细节的TextView的初始化
        findViewById(R.id.button_ReadMidi).setOnClickListener(v -> {
            try {
                verifyStoragePermissions(this);
                File inputMid=new File(((EditText)findViewById(R.id.editText_MidiPath)).getText().toString());
                midiFile = new MidiFile(inputMid);
                ec = new EventsCollection(midiFile);
                Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
                ((TextView)findViewById(R.id.textView_MidiDetails)).setText("共"+ec.getTotalValueNum()+"个音符；可以弹奏最多"+(int)(ec.getCanPlayRatio()*100)+"%个音符。低于80%将自动调整");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
                Toast.makeText(this, "失败，请检查文件路径是否正确", Toast.LENGTH_LONG).show();
            }

        });
        //两个checkBox的初始化
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
            ((CheckBox)findViewById(R.id.checkbox_permission_window)).setChecked(true);
        }
        if(isAccessibilitySettingsOn(getApplicationContext())) {
            ((CheckBox)findViewById(R.id.checkbox_permission_ally)).setChecked(true);
        }
        ((CheckBox)findViewById(R.id.checkbox_permission_window)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))) {
                    askPermission();
                }
            }
        });
        ((CheckBox)findViewById(R.id.checkbox_permission_ally)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isAccessibilitySettingsOn(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        //对联系开发者文字做的监听器，跳转到我的QQ资料页
        findViewById(R.id.intent).setOnClickListener(v -> {
            Intent i = new Intent();
            i.setPackage("com.tencent.mobileqq");
            i.setData(Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&nim=3095598652"));
            startActivity(i);
        });


    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AutoService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
        }

        return false;
    }


    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    public static void verifyStoragePermissions(Activity activity) {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, new String[]{"android.permission.MOUNT_UNMOUNT_FILESYSTEMS"},REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startFloat) {
            stopService(new Intent(MainActivity.this, FloatingView.class));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                startService(new Intent(MainActivity.this, FloatingView.class).putExtra("midiPath",((EditText)findViewById(R.id.editText_MidiPath)).getText().toString()));
            } else if (Settings.canDrawOverlays(this)) {
                startService(new Intent(MainActivity.this, FloatingView.class).putExtra("midiPath",((EditText)findViewById(R.id.editText_MidiPath)).getText().toString()));
            } else {
                askPermission();
                Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.stopFloat) {
            stopService(new Intent(MainActivity.this, FloatingView.class));
        }

    }
}
