package com.autoclick;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.circleview.CircleView;
import com.example.genshinimpactmidiplayer.R;
import com.leff.midi.MidiFile;
import com.leff.midi.event.NoteOn;
import com.leff.midi.examples.EventDrawer;
import com.leff.midi.examples.EventPrinter;
import com.leff.midi.examples.EventsCollection;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiProcessor;

import java.io.File;
import java.io.IOException;


public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;
    String path;
    MidiFile midiToPlay;
    EventsCollection ec;
    MidiProcessor processor;
    boolean isFirstAdjust=true;
    private View floatingCircleView;
    private CircleView circleView;
    Toast toast;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        toast=Toast.makeText(this, null, Toast.LENGTH_SHORT);
        path = intent.getCharSequenceExtra("midiPath").toString();
        try {
            File inputMid=new File(path);
            midiToPlay = new MidiFile(inputMid);
            ec = new EventsCollection(midiToPlay);
            if (ec.getCanPlayRatio() < 0.80f) {
                int moveWhat = ec.autoMoveValues();
                //降速判断
                if (getSharedPreferences("LowerBPM", MODE_PRIVATE).getBoolean("LowerBPM", false)) {
                    ec.setBPM(0.75f);
                }
                //调音调
                toast.setText("调整了"+moveWhat+"个音阶\n现在可以弹奏至多"+(int)(ec.getCanPlayRatio()*100)+"%个音符");
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);


        int layout_parms;
        layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getRealMetrics(new DisplayMetrics());
        mWindowManager.addView(myFloatingView, params);


        //设置并获取显示圆环的View--------------------------------------------------
        if (getSharedPreferences("circleMode", MODE_PRIVATE).getBoolean("circleMode", false)) {
            final WindowManager.LayoutParams params1 = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layout_parms,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            floatingCircleView = LayoutInflater.from(this).inflate(R.layout.floating_circle_view, null);
            mWindowManager.addView(floatingCircleView, params1);
            circleView = (CircleView) floatingCircleView.findViewById(R.id.circleView);
        }
        //-------------------------------------------------------------------------


        //adding an touchListener to make drag movement of the floating widget
        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @SuppressLint("ClickableViewAccessibility")
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

        Button startButton = myFloatingView.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        Button stopButton = myFloatingView.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
        Button adjustButton = myFloatingView.findViewById(R.id.adjust);
        adjustButton.setOnClickListener(this);
        Button chooseButton = myFloatingView.findViewById(R.id.choose);
        chooseButton.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onClick_Stop();
        if (myFloatingView != null){
            mWindowManager.removeView(floatingCircleView);
            mWindowManager.removeView(myFloatingView);
        }

    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                toast.setText("重新播放");
                toast.show();
                //Log.d("START","THIS IS STARTED");
                if (processor != null) {
                    processor.simulatePerson = getSharedPreferences("simulatePerson", MODE_PRIVATE).getBoolean("simulatePerson", false);
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
                processor = new MidiProcessor(midiToPlay,getSharedPreferences("simulatePerson", MODE_PRIVATE).getBoolean("simulatePerson", false));
                // Register for the events you're interested in:
                MidiEventListener listener;
                if (!getSharedPreferences("circleMode", MODE_PRIVATE).getBoolean("circleMode", false)) {
                    listener = new EventPrinter("Individual Listener", getApplicationContext(), x0, y0, x1, y1);
                } else {
                    circleView.init();
                    long stayTime=60000/ ec.getBPM();
                    if (stayTime < 700) {
                        stayTime = 700;
                    }
                    listener = new EventDrawer("Individual Listener", getApplicationContext(), x0, y0, x1, y1,stayTime,circleView);
                }
                processor.registerEventListener(listener, NoteOn.class);
                // Start the processor:
                processor.start();
                break;
            case R.id.stop:
                if (processor != null) {
                    if (processor.isRunning()) {
                        toast.setText("暂停播放");
                        toast.show();
                        processor.stop();
                    } else {
                        toast.setText("继续播放");
                        toast.show();
                        processor.start();
                    }

                }
                break;
            case R.id.adjust:
                if (processor != null) {
                    processor.stop();
                    processor=null;
                }
                if (isFirstAdjust) {
                    int[] location = new int[2];
                    myFloatingView.getLocationOnScreen(location);
                    SharedPreferences pr = getSharedPreferences("p", MODE_PRIVATE);
                    pr.edit().putInt("x0", location[0]).apply();
                    pr.edit().putInt("y0", location[1]).apply();
                    toast.setText("第一次校准:"+location[0]+","+location[1]);
                    toast.show();
                    isFirstAdjust = false;
                } else {
                    int[] location = new int[2];
                    myFloatingView.getLocationOnScreen(location);
                    SharedPreferences pr = getSharedPreferences("p", MODE_PRIVATE);
                    pr.edit().putInt("x1", location[0]).apply();
                    pr.edit().putInt("y1", location[1]).apply();
                    toast.setText("第二次校准:"+location[0]+","+location[1]);
                    toast.show();
                    isFirstAdjust = true;
                }
                break;

            case R.id.choose://换曲
                //关播放器
                if (processor != null) {
                    if (processor.isRunning()) {
                        processor.stop();
                    }
                }
                try {
                    String path = getSharedPreferences("path", MODE_PRIVATE).getString("path", null);
                    //设置悬浮窗
                    if (null != path) {
                        int layout_parms;
                        layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        //setting the layout parameters
                        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.WRAP_CONTENT,
                                layout_parms,
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN ,
                                PixelFormat.TRANSLUCENT);
                        //getting windows services and adding the floating view to it
                        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        @SuppressLint("InflateParams") View chooseMidiView = LayoutInflater.from(this).inflate(R.layout.floating_choose_midi_view, null);
                        mWindowManager.addView(chooseMidiView, params);
                        //listview的设置
                        ListView listView = (ListView) chooseMidiView.findViewById(R.id.listview);
                        //获取目录下的mid文件集合
                        File[] files = new File(path).getCanonicalFile().listFiles((dir, name) -> name.endsWith(".mid"));
                        //设置adapter和listener
                        listView.setAdapter(MyListViewAdapter.getMyListViewAdapter(this, files, R.layout.listview_item_layout, new int[]{R.id.midiFileName}));
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            try {
                                assert files != null;
                                //改MidiFile对象
                                midiToPlay = new MidiFile(files[position]);
                                ec = new EventsCollection(midiToPlay);
                                //降速
                                if (getSharedPreferences("LowerBPM", MODE_PRIVATE).getBoolean("LowerBPM", false)) {
                                    ec.setBPM(0.75f);
                                }
                                //自动调音
                                if (ec.getCanPlayRatio() < 0.80f) {
                                    int moveWhat = ec.autoMoveValues();
                                    toast.setText("调整了" + moveWhat + "个音阶\n现在可以弹奏至多" + (int) (ec.getCanPlayRatio() * 100) + "%个音符");
                                } else {
                                    toast.setText("可以弹奏至多" + (int) (ec.getCanPlayRatio() * 100) + "%个音符");
                                }
                                toast.show();
                            } catch (IOException e) {
                                toast.setText("没找到文件");
                                toast.show();
                                e.printStackTrace();
                            }finally {
                                mWindowManager.removeView(chooseMidiView);
                            }
                        });
                    }
                } catch (Exception e) {
                    toast.setText("未知错误");
                    toast.show();
                }

        }

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