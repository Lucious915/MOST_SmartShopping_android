package com.example.market_app.btlescan.util;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class Calculation {

    private double X = 0, Y = 0, sum = 0;

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    private class Point {//點的class，X座標Y座標和權重
        double X, Y, Weight;

        private Point(double x, double y, double weight) {
            X = x;
            Y = y;
            Weight = weight;
        }
    }

    public void CustomSubspceFingerPrint(double[] calbeacon) {
        // TODO Auto-generated method stub
        ArrayList<Calculation> tmp = new ArrayList<>();
        Calculation c;

        DataBase db = new DataBase();
        double[][] testlist = db.getArray();//取得資料庫訊號

        for (int i = 0; i < calbeacon.length; i++) {//確認收集到的訊號是否為NaN值，是的話代表沒收到訊號並指定為0
            if (Double.isNaN(calbeacon[i])) {
                calbeacon[i] = 0;
            }
        }

        for (int i = 0; i < testlist.length; i++) {//和資料的點計算間距
            c = new Calculation();
            for (int j = 0; j < calbeacon.length; j++) {
                c.sum = c.sum + Math.pow(testlist[i][j] - calbeacon[j], 2);//訊號相減後平方
                c.X = i / 5;//x座標
                c.Y = i % 5;//y座標
            }
            c.sum = Math.sqrt(c.sum);//歐幾里德距離
            tmp.add(c);
        }

        ArrayList<Point> nearP = new ArrayList<>();//離定位點的最近四點
        for (int i = 0; i < 4; i++) {//4NN，找四個最近距離
            double min = 1000;//初始化
            int temp = 0;
            for (int j = 0; j < tmp.size(); j++) {//找距離最小
                if (min > tmp.get(j).sum) {
                    min = tmp.get(j).sum;
                    temp = j;
                }
            }

            Point P = new Point(tmp.get(temp).X, tmp.get(temp).Y, tmp.get(temp).sum);
            Log.d("asd", tmp.get(temp).X + " " + tmp.get(temp).Y + " " + tmp.get(temp).sum);
            nearP.add(P);
            tmp.remove(temp);//找到最小距離的點並從arraylist中移除，以免再找到重複
        }

        for (int i = 0; i < nearP.size(); i++) {//總權重
            sum = sum + 1 / (nearP.get(i).Weight * nearP.get(i).Weight);
        }
        Log.d("asd", sum + "");

        for (int i = 0; i < nearP.size(); i++) {
            Log.d("PP", nearP.get(i).X + " " + nearP.get(i).Y);//距離定位點最近的四點
            Log.d("PP", (1 / (nearP.get(i).Weight * nearP.get(i).Weight)) / sum + "");
            X = X + nearP.get(i).X * ((1 / (nearP.get(i).Weight * nearP.get(i).Weight)) / sum);//每點權重分量加權
            Y = Y + nearP.get(i).Y * ((1 / (nearP.get(i).Weight * nearP.get(i).Weight)) / sum);
        }

        DecimalFormat df = new DecimalFormat("#.##");//小數點後兩位
        X = Double.parseDouble(df.format(X));
        Y = Double.parseDouble(df.format(Y));
    }
}

