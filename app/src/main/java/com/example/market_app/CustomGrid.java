package com.example.market_app;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomGrid extends BaseAdapter {
    private Context context;
    private String[] text;
    private String[] imageId;
    private int layer;

    public CustomGrid(Context context, String[] text, String[] imageId, int layer) {
        this.context = context;
        this.text = text;
        this.imageId = imageId;
        this.layer = layer;
    }

    public CustomGrid(Context context, String[] text, int layer) {
        this.context = context;
        this.text = text;
        this.layer = layer;
    }

    public void refresh(String[] text, String[] imageId)
    {
        this.text = text;
        this.imageId = imageId;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        //View grid=(View) convertView;

        // Context 動態放入mainActivity
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(context);
            // 將grid_single 動態載入(image+text)
            grid = layoutInflater.inflate(R.layout.gridview, null);


        } else {
            grid = (View) convertView;
        }

        TextView textView = (TextView) grid.findViewById(R.id.grid_text);
        ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
        textView.setText(text[position]);
        //imageView.setImageResource(imageId[position]);


        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //imageView.setPadding(8, 8, 8, 8);
        if(layer!=2){
            imageView.setImageResource(R.drawable.jpoint);
        }
        else {
            if (position >= imageId.length) {
                Picasso.get().load(imageId[imageId.length - 1]).into(imageView);
            } else {
                Picasso.get().load(imageId[position]).placeholder(R.drawable.jpoint).resize(100, 100).centerInside().into(imageView);
            }
        }

        return grid;
    }
}
