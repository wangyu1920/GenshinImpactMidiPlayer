package com.autoclick;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyListViewAdapter {


    public static SimpleAdapter getMyListViewAdapter(Context context, File[] file, int resource, int[] to) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (file == null) {
            return null;
        }
        for (File f : file) {
            String s = f.getName();
            HashMap<String, Object> map = new HashMap<>();
            map.put("FileName", s);
            list.add(map);
        }
        return new SimpleAdapter(context, list, resource, new String[]{"FileName"}, to);
    }
}
