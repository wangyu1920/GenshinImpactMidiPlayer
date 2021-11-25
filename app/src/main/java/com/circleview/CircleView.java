package com.circleview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CircleView extends View {
    //存储Circle对象
    List<Circle> list = new LinkedList<>();

    public void addCircle(Intent intent,long stayTime) {
        list.add(new Circle(
                intent.getIntExtra("x", 0),
                intent.getIntExtra("y", 0),
                stayTime));
    }

    public void addCircle() {
        list.add(new Circle(
                500,500,
                10000));
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawCircle(100,100,50,new Paint());
        for (int i = 0; i < list.size(); i++) {
            Circle circle= list.get(i);
            if (circle.isExist) {
                circle.draw(canvas);
            }
        }
        if (list.size() > 20) {
            removeNotNeedCircle();
        }
        postInvalidateDelayed(20);
    }

    public void init() {
        list.clear();
    }

    private void removeNotNeedCircle() {
        for (Circle circle : list) {
            if (!circle.isExist) {
                list.remove(circle);
                removeNotNeedCircle();
                return;
            }
        }

    }
}