package com.leff.midi.examples;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventsCollection {
    MidiFile midiFile;
    public Values values;
    public EventsCollection(MidiFile file) {
        midiFile=file;
        values = new Values();
        putValues();
    }

    public void setBPM(float f) {
        MidiTrack tempoTrack = midiFile.getTracks().get(0);
        for (MidiEvent event : tempoTrack.getEvents()) {
            if (event instanceof Tempo) {
                Tempo tempoEvent = (Tempo) event;
                tempoEvent.setBpm(tempoEvent.getBpm() * f);
            }
        }
    }

    //返回BPM,出错返回-1
    public long getBPM() {
        MidiTrack tempoTrack = midiFile.getTracks().get(0);
        for (MidiEvent event : tempoTrack.getEvents()) {
            if (event instanceof Tempo) {
                Tempo tempoEvent = (Tempo) event;
                return (long) tempoEvent.getBpm();
            }
        }
        return -1;
    }

    //返回音调在c4-b6的音符中可以弹奏（白键）的比例
    public float getCanPlayRatio() {
        return  ((float) values.getValuesMoreThanC4LessThanB6AndCanPlay()) / ((float) values.getValuesMoreThanC4LessThanB6());
    }
    public void putValues() {
        List<MidiTrack> midiTrack = midiFile.getTracks();
        for (MidiTrack track : midiTrack) {
            for (MidiEvent event : track.getEvents()) {
                if (event instanceof NoteOn) {
                    values.put(((NoteOn) event).getNoteValue());
                }
            }
        }
    }

    //把所有的音移动what个单位
    public void moveAllValues(int what) {
        if (what == 0) {
            return;
        }
        List<MidiTrack> midiTrack = midiFile.getTracks();
        for (MidiTrack track : midiTrack) {
            for (MidiEvent event : track.getEvents()) {
                if (event instanceof NoteOn) {
                    ((NoteOn) event).setNoteValue(((NoteOn) event).getNoteValue()+what);
                }
                if (event instanceof NoteOff) {
                    ((NoteOff) event).setNoteValue(((NoteOff) event).getNoteValue()+what);
                }
            }
        }
        values=new Values();
        putValues();
    }

    //自动升降调，返回升/降的音阶数
    public int autoMoveValues() {
        //调整的幅度
        int changeWhat = 0;
        //是否降调
        boolean ifDrop = false;
        //先将曲子尽可能升调
        while (values.getValuesMoreThanB6() < 1) {
            moveAllValues(1);
            changeWhat++;
        }
        //将曲子降到合适的调
        while (values.getValuesMoreThanB6() > 5) {
            moveAllValues(-1);
            ifDrop = true;
            changeWhat--;
        }
        //经过上面的调整，曲子的最大音调应该刚好在B6附近；接下来尝试依次降调并记录降调之后可以弹出的音符的比例
        float max = ((float) values.getValuesMoreThanC4LessThanB6AndCanPlay()) / ((float) values.getValuesMoreThanC4LessThanB6());
        float[] answers = new float[7];
        for (int i = 0; i < 7; i++) {
            moveAllValues(-1);
            answers[i] = ((float) values.getValuesMoreThanC4LessThanB6AndCanPlay()) / ((float) values.getValuesMoreThanC4LessThanB6());
        }
        moveAllValues(7);
        int what = 0;
        for (int index = 0; index < 7; index++) {
            if (answers[index] > max) {
                max = answers[index];
                what = index + 1;
            }
        }
        changeWhat+=what;
        moveAllValues(what * (-1));
        return changeWhat;
    }

    public int getTotalValueNum() {
        return values.getValuesTotalNum();
    }

    static class Values {
        private int v_0x00=0;
        private int v_0x01=0;
        private int v_0x02=0;
        private int v_0x03=0;
        private int v_0x04=0;
        private int v_0x05=0;
        private int v_0x06=0;
        private int v_0x07=0;
        private int v_0x08=0;
        private int v_0x09=0;
        private int v_0x0a=0;
        private int v_0x0b=0;
        private int v_0x0c=0;
        private int v_0x0d=0;
        private int v_0x0e=0;
        private int v_0x0f=0;
        private int v_0x10=0;
        private int v_0x11=0;
        private int v_0x12=0;
        private int v_0x13=0;
        private int v_0x14=0;
        private int v_0x15=0;
        private int v_0x16=0;
        private int v_0x17=0;
        private int v_0x18=0;
        private int v_0x19=0;
        private int v_0x1a=0;
        private int v_0x1b=0;
        private int v_0x1c=0;
        private int v_0x1d=0;
        private int v_0x1e=0;
        private int v_0x1f=0;
        private int v_0x20=0;
        private int v_0x21=0;
        private int v_0x22=0;
        private int v_0x23=0;
        private int v_0x24=0;
        private int v_0x25=0;
        private int v_0x26=0;
        private int v_0x27=0;
        private int v_0x28=0;
        private int v_0x29=0;
        private int v_0x2a=0;
        private int v_0x2b=0;
        private int v_0x2c=0;
        private int v_0x2d=0;
        private int v_0x2e=0;
        private int v_0x2f=0;
        private int v_0x30=0;
        private int v_0x31=0;
        private int v_0x32=0;
        private int v_0x33=0;
        private int v_0x34=0;
        private int v_0x35=0;
        private int v_0x36=0;
        private int v_0x37=0;
        private int v_0x38=0;
        private int v_0x39=0;
        private int v_0x3a=0;
        private int v_0x3b=0;
        private int v_0x3c=0;
        private int v_0x3d=0;
        private int v_0x3e=0;
        private int v_0x3f=0;
        private int v_0x40=0;
        private int v_0x41=0;
        private int v_0x42=0;
        private int v_0x43=0;
        private int v_0x44=0;
        private int v_0x45=0;
        private int v_0x46=0;
        private int v_0x47=0;
        private int v_0x48=0;
        private int v_0x49=0;
        private int v_0x4a=0;
        private int v_0x4b=0;
        private int v_0x4c=0;
        private int v_0x4d=0;
        private int v_0x4e=0;
        private int v_0x4f=0;
        private int v_0x50=0;
        private int v_0x51=0;
        private int v_0x52=0;
        private int v_0x53=0;
        private int v_0x54=0;
        private int v_0x55=0;
        private int v_0x56=0;
        private int v_0x57=0;
        private int v_0x58=0;
        private int v_0x59=0;
        private int v_0x5a=0;
        private int v_0x5b=0;
        private int v_0x5c=0;
        private int v_0x5d=0;
        private int v_0x5e=0;
        private int v_0x5f=0;
        private int v_0x60=0;
        private int v_0x61=0;
        private int v_0x62=0;
        private int v_0x63=0;
        private int v_0x64=0;
        private int v_0x65=0;
        private int v_0x66=0;
        private int v_0x67=0;
        private int v_0x68=0;
        private int v_0x69=0;
        private int v_0x6a=0;
        private int v_0x6b=0;
        private int v_0x6c=0;
        private int v_0x6d=0;
        private int v_0x6e=0;
        private int v_0x6f=0;
        private int v_0x70=0;
        private int v_0x71=0;
        private int v_0x72=0;
        private int v_0x73=0;
        private int v_0x74=0;
        private int v_0x75=0;
        private int v_0x76=0;
        private int v_0x77=0;
        private int v_0x78=0;
        private int v_0x79=0;
        private int v_0x7a=0;
        private int v_0x7b=0;
        private int v_0x7c=0;
        private int v_0x7d=0;
        private int v_0x7e=0;
        private int v_0x7f=0;

        public int getValuesMoreThanB6() {
            int num=0;
            for (int i = 0x60; i < 128; i++) {
                num+=getValueByNum(i);
            }
            return num;
        }

        public int getValuesLessThanC4() {
            int num=0;
            for (int i = 0; i < 0x3c; i++) {
                num+=getValueByNum(i);
            }
            return num;
        }

        public int getValuesMoreThanC4LessThanB6() {
            int num=0;
            for (int i = 0x3c; i < 0x60; i++) {
                num+=getValueByNum(i);
            }
            return num;
        }

        public int getValuesMoreThanC4LessThanB6AndCanPlay() {
            int num=0;
            for (int i = 0x3c; i < 0x60; i++) {
                if ((i - 0x3c) % 12 == 1) {
                    continue;
                }
                if ((i - 0x3c) % 12 == 3) {
                    continue;
                }
                if ((i - 0x3c) % 12 == 6) {
                    continue;
                }
                if ((i - 0x3c) % 12 == 8) {
                    continue;
                }
                if ((i - 0x3c) % 12 == 10) {
                    continue;
                }
                num+=getValueByNum(i);
            }
            return num;
        }

        public int getValuesMoreThanC4LessThanB6ButCanNotPlay() {
            return getValuesMoreThanC4LessThanB6()-getValuesMoreThanC4LessThanB6AndCanPlay();
        }

        public int getValuesTotalNum() {
            int num=0;
            for (int i = 0; i < 128; i++) {
                num+=getValueByNum(i);
            }
            return num;
        }

        public ArrayList<Integer> getAllValues() {
            ArrayList<Integer> values = new ArrayList<>();
            for (int i = 0; i < 128; i++) {
                values.add(getValueByNum(i));
            }
            return values;
        }

        public int getValueByNum(int value) {
            switch (value) {
                case 0 : return v_0x00;
                case 1 : return v_0x01;
                case 2 : return v_0x02;
                case 3 : return v_0x03;
                case 4 : return v_0x04;
                case 5 : return v_0x05;
                case 6 : return v_0x06;
                case 7 : return v_0x07;
                case 8 : return v_0x08;
                case 9 : return v_0x09;
                case 10 : return v_0x0a;
                case 11 : return v_0x0b;
                case 12 : return v_0x0c;
                case 13 : return v_0x0d;
                case 14 : return v_0x0e;
                case 15 : return v_0x0f;
                case 16 : return v_0x10;
                case 17 : return v_0x11;
                case 18 : return v_0x12;
                case 19 : return v_0x13;
                case 20 : return v_0x14;
                case 21 : return v_0x15;
                case 22 : return v_0x16;
                case 23 : return v_0x17;
                case 24 : return v_0x18;
                case 25 : return v_0x19;
                case 26 : return v_0x1a;
                case 27 : return v_0x1b;
                case 28 : return v_0x1c;
                case 29 : return v_0x1d;
                case 30 : return v_0x1e;
                case 31 : return v_0x1f;
                case 32 : return v_0x20;
                case 33 : return v_0x21;
                case 34 : return v_0x22;
                case 35 : return v_0x23;
                case 36 : return v_0x24;
                case 37 : return v_0x25;
                case 38 : return v_0x26;
                case 39 : return v_0x27;
                case 40 : return v_0x28;
                case 41 : return v_0x29;
                case 42 : return v_0x2a;
                case 43 : return v_0x2b;
                case 44 : return v_0x2c;
                case 45 : return v_0x2d;
                case 46 : return v_0x2e;
                case 47 : return v_0x2f;
                case 48 : return v_0x30;
                case 49 : return v_0x31;
                case 50 : return v_0x32;
                case 51 : return v_0x33;
                case 52 : return v_0x34;
                case 53 : return v_0x35;
                case 54 : return v_0x36;
                case 55 : return v_0x37;
                case 56 : return v_0x38;
                case 57 : return v_0x39;
                case 58 : return v_0x3a;
                case 59 : return v_0x3b;
                case 60 : return v_0x3c;
                case 61 : return v_0x3d;
                case 62 : return v_0x3e;
                case 63 : return v_0x3f;
                case 64 : return v_0x40;
                case 65 : return v_0x41;
                case 66 : return v_0x42;
                case 67 : return v_0x43;
                case 68 : return v_0x44;
                case 69 : return v_0x45;
                case 70 : return v_0x46;
                case 71 : return v_0x47;
                case 72 : return v_0x48;
                case 73 : return v_0x49;
                case 74 : return v_0x4a;
                case 75 : return v_0x4b;
                case 76 : return v_0x4c;
                case 77 : return v_0x4d;
                case 78 : return v_0x4e;
                case 79 : return v_0x4f;
                case 80 : return v_0x50;
                case 81 : return v_0x51;
                case 82 : return v_0x52;
                case 83 : return v_0x53;
                case 84 : return v_0x54;
                case 85 : return v_0x55;
                case 86 : return v_0x56;
                case 87 : return v_0x57;
                case 88 : return v_0x58;
                case 89 : return v_0x59;
                case 90 : return v_0x5a;
                case 91 : return v_0x5b;
                case 92 : return v_0x5c;
                case 93 : return v_0x5d;
                case 94 : return v_0x5e;
                case 95 : return v_0x5f;
                case 96 : return v_0x60;
                case 97 : return v_0x61;
                case 98 : return v_0x62;
                case 99 : return v_0x63;
                case 100 : return v_0x64;
                case 101 : return v_0x65;
                case 102 : return v_0x66;
                case 103 : return v_0x67;
                case 104 : return v_0x68;
                case 105 : return v_0x69;
                case 106 : return v_0x6a;
                case 107 : return v_0x6b;
                case 108 : return v_0x6c;
                case 109 : return v_0x6d;
                case 110 : return v_0x6e;
                case 111 : return v_0x6f;
                case 112 : return v_0x70;
                case 113 : return v_0x71;
                case 114 : return v_0x72;
                case 115 : return v_0x73;
                case 116 : return v_0x74;
                case 117 : return v_0x75;
                case 118 : return v_0x76;
                case 119 : return v_0x77;
                case 120 : return v_0x78;
                case 121 : return v_0x79;
                case 122 : return v_0x7a;
                case 123 : return v_0x7b;
                case 124 : return v_0x7c;
                case 125 : return v_0x7d;
                case 126 : return v_0x7e;
                case 127 : return v_0x7f;
            }
            return 0;
        }
        
        public void put(int value) {
            switch (value) {
                case 0:v_0x00++;break;
                case 1:v_0x01++;break;
                case 2:v_0x02++;break;
                case 3:v_0x03++;break;
                case 4:v_0x04++;break;
                case 5:v_0x05++;break;
                case 6:v_0x06++;break;
                case 7:v_0x07++;break;
                case 8:v_0x08++;break;
                case 9:v_0x09++;break;
                case 10:v_0x0a++;break;
                case 11:v_0x0b++;break;
                case 12:v_0x0c++;break;
                case 13:v_0x0d++;break;
                case 14:v_0x0e++;break;
                case 15:v_0x0f++;break;
                case 16:v_0x10++;break;
                case 17:v_0x11++;break;
                case 18:v_0x12++;break;
                case 19:v_0x13++;break;
                case 20:v_0x14++;break;
                case 21:v_0x15++;break;
                case 22:v_0x16++;break;
                case 23:v_0x17++;break;
                case 24:v_0x18++;break;
                case 25:v_0x19++;break;
                case 26:v_0x1a++;break;
                case 27:v_0x1b++;break;
                case 28:v_0x1c++;break;
                case 29:v_0x1d++;break;
                case 30:v_0x1e++;break;
                case 31 :v_0x1f++;break;
                case 32 :v_0x20++;break;
                case 33:v_0x21++;break;
                case 34:v_0x22++;break;
                case 35:v_0x23++;break;
                case 36:v_0x24++;break;
                case 37:v_0x25++;break;
                case 38:v_0x26++;break;
                case 39:v_0x27++;break;
                case 40:v_0x28++;break;
                case 41:v_0x29++;break;
                case 42:v_0x2a++;break;
                case 43:v_0x2b++;break;
                case 44:v_0x2c++;break;
                case 45:v_0x2d++;break;
                case 46:v_0x2e++;break;
                case 47:v_0x2f++;break;
                case 48:v_0x30++;break;
                case 49:v_0x31++;break;
                case 50:v_0x32++;break;
                case 51:v_0x33++;break;
                case 52:v_0x34++;break;
                case 53:v_0x35++;break;
                case 54:v_0x36++;break;
                case 55:v_0x37++;break;
                case 56:v_0x38++;break;
                case 57:v_0x39++;break;
                case 58:v_0x3a++;break;
                case 59:v_0x3b++;break;
                case 60:v_0x3c++;break;
                case 61:v_0x3d++;break;
                case 62:v_0x3e++;break;
                case 63:v_0x3f++;break;
                case 64:v_0x40++;break;
                case 65:v_0x41++;break;
                case 66:v_0x42++;break;
                case 67:v_0x43++;break;
                case 68:v_0x44++;break;
                case 69:v_0x45++;break;
                case 70:v_0x46++;break;
                case 71:v_0x47++;break;
                case 72:v_0x48++;break;
                case 73:v_0x49++;break;
                case 74:v_0x4a++;break;
                case 75:v_0x4b++;break;
                case 76:v_0x4c++;break;
                case 77:v_0x4d++;break;
                case 78:v_0x4e++;break;
                case 79:v_0x4f++;break;
                case 80:v_0x50++;break;
                case 81:v_0x51++;break;
                case 82:v_0x52++;break;
                case 83:v_0x53++;break;
                case 84:v_0x54++;break;
                case 85:v_0x55++;break;
                case 86:v_0x56++;break;
                case 87:v_0x57++;break;
                case 88:v_0x58++;break;
                case 89:v_0x59++;break;
                case 90:v_0x5a++;break;
                case 91:v_0x5b++;break;
                case 92:v_0x5c++;break;
                case 93:v_0x5d++;break;
                case 94:v_0x5e++;break;
                case 95:v_0x5f++;break;
                case 96:v_0x60++;break;
                case 97:v_0x61++;break;
                case 98:v_0x62++;break;
                case 99:v_0x63++;break;
                case 100:v_0x64++;break;
                case 101:v_0x65++;break;
                case 102:v_0x66++;break;
                case 103:v_0x67++;break;
                case 104:v_0x68++;break;
                case 105:v_0x69++;break;
                case 106:v_0x6a++;break;
                case 107:v_0x6b++;break;
                case 108:v_0x6c++;break;
                case 109:v_0x6d++;break;
                case 110:v_0x6e++;break;
                case 111:v_0x6f++;break;
                case 112:v_0x70++;break;
                case 113:v_0x71++;break;
                case 114:v_0x72++;break;
                case 115:v_0x73++;break;
                case 116:v_0x74++;break;
                case 117:v_0x75++;break;
                case 118:v_0x76++;break;
                case 119:v_0x77++;break;
                case 120:v_0x78++;break;
                case 121:v_0x79++;break;
                case 122:v_0x7a++;break;
                case 123:v_0x7b++;break;
                case 124:v_0x7c++;break;
                case 125:v_0x7d++;break;
                case 126:v_0x7e++;break;
                case 127:v_0x7f++;break;
            }
        }
    }
    

}
