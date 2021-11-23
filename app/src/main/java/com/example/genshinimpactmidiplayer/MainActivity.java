package com.example.genshinimpactmidiplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //FrameLayout mLayout;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    MidiFile midiFile;
    EventsCollection ec;
    String path="";
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
            verifyStoragePermissions();
            try {
                File inputMid;
                if (((EditText) findViewById(R.id.editText_MidiPath)).getText().toString().length() <= 1) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                    //intent.setType(“audio/*”); //选择音频
                    //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
                    //intent.setType(“video/*;image/*”);//同时选择视频和图片
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
                    Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
                Toast.makeText(this, "失败，请检查文件路径是否正确", Toast.LENGTH_LONG).show();
            }

        });
        //三个checkBox的初始化
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
            ((CheckBox)findViewById(R.id.checkbox_permission_window)).setChecked(true);
        }
        if(isAccessibilitySettingsOn(getApplicationContext())) {
            ((CheckBox)findViewById(R.id.checkbox_permission_ally)).setChecked(true);
        }
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE")) {
            ((CheckBox)findViewById(R.id.checkbox_permission_io)).setChecked(true);
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
        ((CheckBox)findViewById(R.id.checkbox_permission_io)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                verifyStoragePermissions();
            }
        });
        //模拟人弹选框的设置
        ((CheckBox)findViewById(R.id.checkbox_is_simulatePerson)).setChecked(getSharedPreferences("simulatePerson", MODE_PRIVATE).getBoolean("simulatePerson", false));
        ((CheckBox)findViewById(R.id.checkbox_is_simulatePerson)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("simulatePerson", MODE_PRIVATE).edit().putBoolean("simulatePerson", isChecked).apply();
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

    public void verifyStoragePermissions() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.MOUNT_UNMOUNT_FILESYSTEMS"},REQUEST_EXTERNAL_STORAGE);
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

    @SuppressLint("SdCardPath")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String thePath = getPath(this, uri);
                    if (thePath != null) {
                        path=thePath.replace("/storage/emulated/0/","/sdcard/");
                        System.out.println(path+"\n\n\n\n\n\n\n\n\n");
                        ((EditText)findViewById(R.id.editText_MidiPath)).setText(path);
                        try {
                            midiFile = new MidiFile(new File(path));
                            ec = new EventsCollection(midiFile);
                            SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
                            preferences.edit().putString("path", new StringBuilder().append(path).delete(
                                    path.lastIndexOf("/"),
                                    path.length()
                            ).toString()).apply();
                            ((TextView) findViewById(R.id.textView_MidiDetails)).setText("共" + ec.getTotalValueNum() + "个音符；可以弹奏最多" + (int) (ec.getCanPlayRatio() * 100) + "%个音符。低于80%将自动调整");
                            Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "失败，请检查文件路径是否正确", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            Log.e("导入失败","");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
