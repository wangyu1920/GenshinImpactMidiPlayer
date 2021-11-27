package com.example.genshinimpactmidiplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.autoclick.AutoService;
import com.autoclick.FloatingView;
import com.leff.midi.MidiFile;
import com.leff.midi.examples.EventsCollection;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    MidiFile midiFile;
    EventsCollection ec;
    String path = "";
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toast=Toast.makeText(this, null, Toast.LENGTH_SHORT);
        setContentView(R.layout.activity_main);
        setViewInit();

    }

    @SuppressLint("SetTextI18n")
    private void setViewInit() {
        //打开和关闭悬浮窗按钮的初始化
        findViewById(R.id.startFloat).setOnClickListener(this);
        findViewById(R.id.stopFloat).setOnClickListener(this);
        //读midi文件按钮的初始化 以及 展示midi文件细节的TextView的初始化
        findViewById(R.id.button_ReadMidi).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
            try {
                File inputMid;
                if (((EditText) findViewById(R.id.editText_MidiPath)).getText().toString().length() <= 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型，任意类型
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 11);
                } else {
                    inputMid = new File(((EditText) findViewById(R.id.editText_MidiPath)).getText().toString());
                    midiFile = new MidiFile(inputMid);
                    ec = new EventsCollection(midiFile);
                    SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
                    preferences.edit().putString("path", new StringBuilder().append(((EditText) findViewById(R.id.editText_MidiPath)).getText().toString()).delete(
                            ((EditText) findViewById(R.id.editText_MidiPath)).getText().toString().lastIndexOf("/"),
                            ((EditText) findViewById(R.id.editText_MidiPath)).getText().toString().length()
                    ).toString()).apply();
                    ((TextView) findViewById(R.id.textView_MidiDetails)).setText("共" + ec.getTotalValueNum() + "个音符；可以弹奏最多" + (int) (ec.getCanPlayRatio() * 100) + "%个音符。低于80%将自动调整");
                    toast.setText("读取成功");
                    toast.show();

                }
            } catch (Exception e) {
                e.printStackTrace();
                toast.setText("失败，请检查文件路径是否正确");
                toast.show();
            }

        });
        //三个checkBox的初始化
        if (Settings.canDrawOverlays(this)) {
            ((CheckBox) findViewById(R.id.checkbox_permission_window)).setChecked(true);
        }
        if (isAccessibilitySettingsOn(getApplicationContext())) {
            ((CheckBox) findViewById(R.id.checkbox_permission_ally)).setChecked(true);
        }
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE")) {
            ((CheckBox) findViewById(R.id.checkbox_permission_io)).setChecked(true);
        }
        ((CheckBox) findViewById(R.id.checkbox_permission_window)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !Settings.canDrawOverlays(getApplicationContext())) {
                askPermission();
            }
        });
        ((CheckBox) findViewById(R.id.checkbox_permission_ally)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isAccessibilitySettingsOn(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
        ((CheckBox) findViewById(R.id.checkbox_permission_io)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }
        });
        //模拟人弹选框的设置android:layout_marginStart="10dp"
        ((CheckBox) findViewById(R.id.checkbox_is_simulatePerson)).setChecked(getSharedPreferences("simulatePerson", MODE_PRIVATE).getBoolean("simulatePerson", false));
        ((CheckBox) findViewById(R.id.checkbox_is_simulatePerson)).setOnCheckedChangeListener((buttonView, isChecked) -> getSharedPreferences("simulatePerson", MODE_PRIVATE).edit().putBoolean("simulatePerson", isChecked).apply());
        //提示模式选框的设置
        ((CheckBox) findViewById(R.id.checkbox_is_circleMode)).setChecked(getSharedPreferences("circleMode", MODE_PRIVATE).getBoolean("circleMode", false));
        ((CheckBox) findViewById(R.id.checkbox_is_circleMode)).setOnCheckedChangeListener((buttonView, isChecked) -> getSharedPreferences("circleMode", MODE_PRIVATE).edit().putBoolean("circleMode", isChecked).apply());
        //提示模式选框的设置
        ((CheckBox) findViewById(R.id.checkbox_is_lower_bpm)).setChecked(getSharedPreferences("LowerBPM", MODE_PRIVATE).getBoolean("LowerBPM", false));
        ((CheckBox) findViewById(R.id.checkbox_is_lower_bpm)).setOnCheckedChangeListener((buttonView, isChecked) -> getSharedPreferences("LowerBPM", MODE_PRIVATE).edit().putBoolean("LowerBPM", isChecked).apply());
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
        } catch (Settings.SettingNotFoundException ignored) {
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
        }

        return false;
    }


    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startFloat) {
            stopService(new Intent(MainActivity.this, FloatingView.class));
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(MainActivity.this, FloatingView.class).putExtra("midiPath", ((EditText) findViewById(R.id.editText_MidiPath)).getText().toString()));
            } else {
                askPermission();
                toast.setText("悬浮窗权限呢？");
                toast.show();
            }
        } else if (v.getId() == R.id.stopFloat) {
            stopService(new Intent(MainActivity.this, FloatingView.class));

        }

    }

    @SuppressLint({"SdCardPath", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String thePath = uri.getPath();
                    if (thePath != null) {
                        path = thePath.replace("/document/primary:", "/storage/emulated/0/");
                        System.out.println(path + "\n\n\n\n\n\n\n\n\n");
                        ((EditText) findViewById(R.id.editText_MidiPath)).setText(path);
                        try {
                            midiFile = new MidiFile(new File(path));
                            ec = new EventsCollection(midiFile);
                            SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
                            preferences.edit().putString("path", new StringBuilder().append(path).delete(
                                    path.lastIndexOf("/"),
                                    path.length()
                            ).toString()).apply();
                            ((TextView) findViewById(R.id.textView_MidiDetails)).setText("共" + ec.getTotalValueNum() + "个音符；可以弹奏最多" + (int) (ec.getCanPlayRatio() * 100) + "%个音符。低于80%将自动调整");
                            toast.setText("读取成功");
                            toast.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            toast.setText("失败，请检查文件路径是否正确");
                            toast.show();
                        }
                    }
                }
            }
        }
    }


}