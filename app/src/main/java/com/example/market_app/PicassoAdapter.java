package com.example.market_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PicassoAdapter extends BaseAdapter{
    private Context context;
    private String[] imageURL;
    private String[] title;
    private String[] description;

    public PicassoAdapter(Context context, String[] imageURL,String[] title,String[] description ){

        this.context = context;
        this.title = title;
        this.imageURL = imageURL;
        this.description = description;
    }

    @Override
    public int getCount() {
        return title.length;
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
        View holder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //ListView tries to reuse invisible Row-Views to save memory, thats why this method can be called with null OR actually a ready View
        if(convertView == null){
            //in this case we need to create a new View
            //create a holder Object that we will attach to the view
            holder = new View(context);
            //in this line we actually create a new row from the xml-file
            holder = layoutInflater.inflate(R.layout.layout_listview,null);
            //we attach the Image- and Text-View to our holder, so we can access them in the next step

            //convertView.setTag(holder)
        } else {
            //if the view is already created, simply get the holder-object
            holder = (View) convertView;
        }
        ImageView placeIcon = (ImageView)holder.findViewById(R.id.listview_image);
        TextView placeTitle = (TextView)holder.findViewById(R.id.listview_title);
        TextView placeDescrip = (TextView) holder.findViewById(R.id.listview_discription);
        //I assume the URL is a String, if you need another Type, simply change it here and in class declaration
        placeIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //String url = getItem(position);
        Picasso.get()
                .load(imageURL[position])
                //now we have an ImageView, that we can use as target
                .into(placeIcon);
        //you can set the info here
        placeTitle.setText(title[position]);
        placeDescrip.setText(description[position]);

        return holder;
    }
}