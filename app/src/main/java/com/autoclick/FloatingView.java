package com.autoclick;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.genshinimpactmidiplayer.MainActivity;
import com.example.genshinimpactmidiplayer.R;
import com.hb.dialog.myDialog.MyAlertInputDialog;
import com.leff.midi.MidiFile;
import com.leff.midi.event.NoteOn;
import com.leff.midi.examples.EventPrinter;
import com.leff.midi.examples.EventsCollection;
import com.leff.midi.util.MidiProcessor;

import java.io.File;
import java.io.IOException;


public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    MidiFile midiToPlay;
    EventsCollection ec;
    MidiProcessor processor;
    boolean isFirstAdjust=true;

    @Override
    public IBinder onBind(Intent intent) {
//        intent.get
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String path = intent.getCharSequenceExtra("midiPath").toString();
        try {
            File inputMid=new File(path);
            midiToPlay = new MidiFile(inputMid);
            ec = new EventsCollection(midiToPlay);
            if (ec.getCanPlayRatio() < 0.80f) {
                int moveWhat = ec.autoMoveValues();
                Toast.makeText(getApplicationContext(),"调整了"+moveWhat+"个音阶\n现在可以弹奏至多"+(int)(ec.getCanPlayRatio()*100)+"%个音符",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);


        int layout_parms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        }

        else {

            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;

        }

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);



        //adding an touchlistener to make drag movement of the floating widget
        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TOUCH","THIS IS TOUCHED");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        Button startButton = (Button) myFloatingView.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        Button stopButton = (Button) myFloatingView.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
        Button adjustButton = (Button) myFloatingView.findViewById(R.id.adjust);
        adjustButton.setOnClickListener(this);
        Button chooseButton = (Button) myFloatingView.findViewById(R.id.choose);
        chooseButton.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onClick_Stop();
        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start:
                //Log.d("START","THIS IS STARTED");
                if (processor != null) {
                    processor.reset();
                }
                //Get x0,y0,x1,y1 to construct the EventPrinter
                //获取简单存储器
                SharedPreferences preferences = getSharedPreferences("p", MODE_PRIVATE);
                int x0=preferences.getInt("x0", 600);
                int y0=preferences.getInt("y0", 960);
                int x1=preferences.getInt("x1", 1800);
                int y1=preferences.getInt("y1", 620);
                // Create a new MidiProcessor:
                processor = new MidiProcessor(midiToPlay);
                // Register for the events you're interested in:
                EventPrinter ep = new EventPrinter("Individual Listener",getApplicationContext(),x0,y0,x1,y1);
                processor.registerEventListener(ep, NoteOn.class);
                // Start the processor:
                processor.start();
//                onClick_Start();
                break;
            case R.id.stop:
                if (processor != null) {
                    if (processor.isRunning()) {
                        processor.stop();
                    } else {
                        processor.start();
                    }

                }
                break;
//                onClick_Stop();
                //mWindowManager.removeView(myFloatingView);
                //Intent appMain = new Intent(getApplicationContext(), MainActivity.class);

                //getApplication().startActivity(appMain);
                //requires the FLAG_ACTIVITY_NEW_TASK flag
            case R.id.adjust:
                if (processor != null) {
                    processor.stop();
                    processor=null;
                }
                if (isFirstAdjust) {
                    Toast.makeText(getApplicationContext(),"第一次校准",Toast.LENGTH_SHORT).show();
                    int[] location = new int[2];
                    myFloatingView.getLocationOnScreen(location);
                    SharedPreferences pr = getSharedPreferences("p", MODE_PRIVATE);
                    pr.edit().putInt("x0", location[0]).apply();
                    pr.edit().putInt("y0", location[1]).apply();
                    isFirstAdjust = false;
                } else {
                    Toast.makeText(getApplicationContext(),"第二次校准",Toast.LENGTH_SHORT).show();
                    int[] location = new int[2];
                    myFloatingView.getLocationOnScreen(location);
                    SharedPreferences pr = getSharedPreferences("p", MODE_PRIVATE);
                    pr.edit().putInt("x1", location[0]).apply();
                    pr.edit().putInt("y1", location[1]).apply();
                    isFirstAdjust = true;
                }
                break;

            case R.id.choose:
                Toast.makeText(getApplicationContext(),"请输入文件名",Toast.LENGTH_SHORT).show();
//                String path = getSharedPreferences("path", MODE_PRIVATE).getString("path", null);
//                if (null == path) {break;}
//                final MyAlertInputDialog myAlertInputDialog = new MyAlertInputDialog(this).builder()
//                        .setTitle("请输入")
//                        .setEditText("");
//                myAlertInputDialog.setPositiveButton("确认", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String newPath=path+"/"+myAlertInputDialog.getResult();
//                        if (newPath.charAt(newPath.length() - 1) != 'd') {
//                            newPath += ".mid";
//                        }
//                        try {
//                            File inputMid=new File(newPath);
//                            midiToPlay = new MidiFile(inputMid);
//                            ec = new EventsCollection(midiToPlay);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Toast.makeText(getApplicationContext(),"错误",Toast.LENGTH_SHORT).show();
//                            }
//                        myAlertInputDialog.dismiss();
//                    }
//                }).setNegativeButton("取消", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        myAlertInputDialog.dismiss();
//                    }
//                });
//                myAlertInputDialog.show();
                try {
                    String path = getSharedPreferences("path", MODE_PRIVATE).getString("path", null);
                    if (null != path) {
                        int layout_parms;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        }
                        else {
                            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                        //setting the layout parameters
                        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.WRAP_CONTENT,
                                layout_parms,
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ,
                                PixelFormat.TRANSLUCENT);
                        //getting windows services and adding the floating view to it
                        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        View chooseMidiView = LayoutInflater.from(this).inflate(R.layout.floating_choose_midi, null);
                        mWindowManager.addView(chooseMidiView, params);
                        mWindowManager.removeView(myFloatingView);
                        chooseMidiView.findViewById(R.id.thisIsAnotherID).setOnTouchListener(new View.OnTouchListener() {
                            private int initialX;
                            private int initialY;
                            private float initialTouchX;
                            private float initialTouchY;
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                Log.d("TOUCH","THIS IS TOUCHED");
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        initialX = params.x;
                                        initialY = params.y;
                                        initialTouchX = event.getRawX();
                                        initialTouchY = event.getRawY();
                                        return true;

                                    case MotionEvent.ACTION_UP:

                                        return true;

                                    case MotionEvent.ACTION_MOVE:
                                        //this code is helping the widget to move around the screen with fingers
                                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                        mWindowManager.updateViewLayout(chooseMidiView, params);
                                        return true;
                                }
                                return false;
                            }
                        });
                        TextView textView = (TextView) chooseMidiView.findViewById(R.id.text_file_name);
                        File[] list = new File(path).getCanonicalFile().listFiles();
                        StringBuilder builder = new StringBuilder();
                        assert list != null;
                        for (File f : list) {
                            String s=f.getName();
                            System.out.println(s);
                            if (s.endsWith(".mid"))
                            builder.append(s).append("\n");
                        }
                        textView.setText(builder.toString());
                        Button commitButton = (Button) chooseMidiView.findViewById(R.id.commit);
                        commitButton.setOnClickListener(v1 -> {
                            String newPath = path + "/" + ((EditText) chooseMidiView.findViewById(R.id.editText_MidiName)).getText().toString();
                            if (newPath.charAt(newPath.length() - 1) != 'd') {
                                newPath += ".mid";
                            }
                            File inputMid=new File(newPath);
                            try {
                                midiToPlay = new MidiFile(inputMid);
                                ec = new EventsCollection(midiToPlay);
                                Toast.makeText(getApplicationContext(),"成功找到文件:"+newPath,Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(),"没找到文件:"+newPath,Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }finally {
                                mWindowManager.removeView(chooseMidiView);
                                mWindowManager.addView(myFloatingView,params);
                            }
                        });
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"错误",Toast.LENGTH_SHORT).show();
                }


        }

    }

    public void onClick_Start() {
        Intent intent = new Intent(getApplicationContext(), AutoService.class);
        int[] location = new int[2];
        myFloatingView.getLocationOnScreen(location);
        intent.putExtra("action", "play");
        intent.putExtra("x", location[0] - 1);
        intent.putExtra("y", location[1] - 1);
        getApplication().startService(intent);
    }

    public void onClick_Stop() {
        if (processor != null) {
            processor.stop();
        }
        Intent intent = new Intent(getApplicationContext(), AutoService.class);
        intent.putExtra("action", "stop");
        getApplication().startService(intent);
    }

}