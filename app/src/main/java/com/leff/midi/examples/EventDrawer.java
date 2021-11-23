package com.leff.midi.examples;

import android.content.Context;
import android.content.Intent;

import com.autoclick.AutoService;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiEventListener;

import java.util.Random;

public class EventDrawer implements MidiEventListener
{
    private final String mLabel;
    private final Context context;
    public int x0=600;
    public int y0=960;
    public int x1=1800;
    public int y1=620;
    //模拟人弹
    public boolean simulatePerson;

    public EventDrawer(String label,Context context)
    {
        mLabel = label;
        this.context=context;
    }

    public EventDrawer(String mLabel, Context context, int x0, int y0, int x1, int y1) {
        this.mLabel = mLabel;
        this.context = context;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    // 0. Implement the listener functions that will be called by the
    // MidiProcessor
    @Override
    public void onStart(boolean fromBeginning)
    {
        if(fromBeginning)
        {
            System.out.println(mLabel + " Started!");
        }
        else
        {
            System.out.println(mLabel + " resumed");
        }
    }

    @Override
    public void onEvent(MidiEvent event, long ms)
    {
        System.out.println(mLabel + " received event: " + event);
        Intent intent = getIntentByEvent(event);
        if (intent != null) {
            context.startService(intent);
        }
    }

    private Intent getIntentByEvent(MidiEvent event){
        if(event instanceof NoteOn){
            int addX=(x1-x0)/6;
            int addY=(y1-y0)/2;
            Intent intent = new Intent(context, AutoService.class);
            intent.putExtra("action", "play");
            com.leff.midi.event.NoteOn note=(NoteOn)event;
            switch(note.getNoteValue()){
                case 0x3c   :
                case 0x3d   :intent=setXY(intent,x0,y0);break;
                case 0x3e   :
                case 0x3f   :intent=setXY(intent,x0+addX,y0);break;
                case 0x40   :intent=setXY(intent,x0+addX*2,y0);break;
                case 0x41   :
                case 0x42   :intent=setXY(intent,x0+addX*3,y0);break;
                case 0x43   :
                case 0x44   :intent=setXY(intent,x0+addX*4,y0);break;
                case 0x45   :
                case 0x46   :intent=setXY(intent,x0+addX*5,y0);break;
                case 0x47   :intent=setXY(intent,x0+addX*6,y0);break;
                case 0x48   :
                case 0x49   :intent=setXY(intent,x0+addX*0,y0+addY*1);break;
                case 0x4a   :intent=setXY(intent,x0+addX*1,y0+addY*1);break;
                case 0x4b   :intent=setXY(intent,x0+addX*1,y0+addY*1);break;
                case 0x4c   :intent=setXY(intent,x0+addX*2,y0+addY*1);break;
                case 0x4d   :intent=setXY(intent,x0+addX*3,y0+addY*1);break;
                case 0x4e   :intent=setXY(intent,x0+addX*3,y0+addY*1);break;
                case 0x4f   :intent=setXY(intent,x0+addX*4,y0+addY*1);break;
                case 0x50   :intent=setXY(intent,x0+addX*4,y0+addY*1);break;
                case 0x51   :intent=setXY(intent,x0+addX*5,y0+addY*1);break;
                case 0x52   :intent=setXY(intent,x0+addX*5,y0+addY*1);break;
                case 0x53   :intent=setXY(intent,x0+addX*6,y0+addY*1);break;
                case 0x54   :intent=setXY(intent,x0+addX*0,y0+addY*2);break;
                case 0x55   :intent=setXY(intent,x0+addX*0,y0+addY*2);break;
                case 0x56   :intent=setXY(intent,x0+addX*1,y0+addY*2);break;
                case 0x57   :intent=setXY(intent,x0+addX*1,y0+addY*2);break;
                case 0x58   :intent=setXY(intent,x0+addX*2,y0+addY*2);break;
                case 0x59   :intent=setXY(intent,x0+addX*3,y0+addY*2);break;
                case 0x5a   :intent=setXY(intent,x0+addX*3,y0+addY*2);break;
                case 0x5b   :intent=setXY(intent,x0+addX*4,y0+addY*2);break;
                case 0x5c   :intent=setXY(intent,x0+addX*4,y0+addY*2);break;
                case 0x5d   :intent=setXY(intent,x0+addX*5,y0+addY*2);break;
                case 0x5e   :intent=setXY(intent,x0+addX*5,y0+addY*2);break;
                case 0x5f   :intent=setXY(intent,x0+addX*6,y0+addY*2);break;
                case 0x60   :intent=setXY(intent,x0+addX*6,y0+addY*2);break;
                default:return null;
            }
            return intent;
        }
        return null;
    }

    private Intent setXY(Intent intent,int x,int y){
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        return intent;
    }

    @Override
    public void onStop(boolean finished)
    {
        if(finished)
        {
            System.out.println(mLabel + " Finished!");
        }
        else
        {
            System.out.println(mLabel + " paused");
        }
    }

}
