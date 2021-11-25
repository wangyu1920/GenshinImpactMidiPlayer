package com.circleview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class Circle {
    //初始半径
    int R0 = 130 / 2 + (165 - 130);
    //最后半径
    int R1 = 130 / 2;
    //圆心坐标
    int x, y;
    //计时相关
    long bornTime;//创建时间
    long stayTime;//持续时间
    //持续结束时返回false
    boolean isExist;

    public Circle(int x,int y,long stayTime) {
        this.x = x;
        this.y = y;
        this.stayTime = stayTime;
        isExist = true;
        bornTime = System.currentTimeMillis();
    }

    void draw(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= (bornTime + stayTime)) {
            isExist = false;
            return;
        }
        //计算半径
        int r = R0 - (int)((R0 - R1) *  ((float)(currentTime - bornTime) / (float)stayTime));
        //设置画笔属性
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(13- (int)(4*  ((float)(currentTime - bornTime) / (float)stayTime)));//粗细
        paint.setColor(Color.argb(255, 148, 0, 148));//颜色
        //设置Path
//        Path path = new Path();
//        RectF rectF = new RectF(x - r, y - r, x + r, y + r);
//        path.addArc(rectF, 0, 360);
        //画圆
//        canvas.drawPath(path, paint);
        canvas.drawCircle(x,y,r,paint);
        System.out.println("draw__________________________________________"+r);
    }
}
