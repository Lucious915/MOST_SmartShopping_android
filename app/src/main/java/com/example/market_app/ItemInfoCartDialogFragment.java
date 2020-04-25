package com.example.market_app;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ItemInfoCartDialogFragment extends DialogFragment {

    private final static int API_ID_3_GET_RANDOM_4ITEMS = 3;
    public Recommand_Item_List m_recommand_itemlist = new Recommand_Item_List();

    public static class Recommand_Item
    {
        Bitmap Item_bitmap;
        String Item_name;
        int Item_price;
        String Item_description;
    }
    public class Recommand_Item_List
    {
        Recommand_Item Item[] = new Recommand_Item[4];
        int write_count = 0;
        boolean data_completed = false;
        boolean data_loading = true;
        void init(){
            write_count = 0;
            data_completed = false;
            data_loading = true;
        }
        void add(Recommand_Item rim){
            data_loading = true;
            Item[write_count] = rim;
            if(write_count<3){
                write_count++;
                data_completed = false;
            }else if(write_count==3){
                write_count=0;
                data_completed = true;
                updateFlipper();
            }
        }
        void updateFlipper(){
            if(data_completed) {
                update_result(Item[0].Item_bitmap, Item[0].Item_name, Item[1].Item_bitmap, Item[1].Item_name, Item[2].Item_bitmap, Item[2].Item_name, Item[3].Item_bitmap, Item[3].Item_name);
                data_completed = false;
                data_loading = false;
            }
            else{
            }
        }
        Bitmap get_item_bitmap(int index){
            return Item[index].Item_bitmap;
        }
        String get_item_name(int index){
            return Item[index].Item_name;
        }
        int get_item_price(int index){
            return Item[index].Item_price;
        }
        String get_item_description(int index){
            return Item[index].Item_description;
        }
    }

    LinearLayout cvi_layout1, cvi_layout2, cvi_layout3, cvi_layout4;
    TextView tv_title,tv_describe;
    ImageView img_item;
    Button btn_close,btn_function,btn_add,btn_min;
    EditText buy_num;

    Bitmap bmp;
    String ImgUrl;
    String Title;
    String Describe;
    int[] cat_id;

    ImageView fiicdialog_bitmap1,fiicdialog_bitmap2,fiicdialog_bitmap3,fiicdialog_bitmap4;
    TextView fiicdialog_textview1,fiicdialog_textview2,fiicdialog_textview3,fiicdialog_textview4;
    int location;
    int buy_cnt=1;

    public static ItemInfoCartDialogFragment newInstance(String url, String Title, String Describe, int location) {
        ItemInfoCartDialogFragment f = new ItemInfoCartDialogFragment();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("ImgUrl",url);
        args.putString("Title",Title);
        args.putString("Describe",Describe);
        args.putInt("Location",location);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //byte[] byteArray = getArguments().getByteArray("Image");
        ImgUrl = getArguments().getString("ImgUrl");
        Title = getArguments().getString("Title");
        Describe = getArguments().getString("Describe");
        location = getArguments().getInt("Location");

        get_random4(String.valueOf(location));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View m_dialog = inflater.inflate(R.layout.fragment_item_info_cart_dialog, container, false);

        buy_num = (EditText) m_dialog.findViewById(R.id.editText);
        buy_num.setText("1");
        btn_add = (Button) m_dialog.findViewById(R.id.button2);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buy_cnt++;
                buy_num.setText(String.valueOf(buy_cnt));
            }
        });
        btn_min = (Button) m_dialog.findViewById(R.id.button);
        btn_min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buy_cnt>0){
                    buy_cnt--;
                    buy_num.setText(String.valueOf(buy_cnt));
                }
            }
        });
        btn_close = (Button) m_dialog.findViewById(R.id.btn_close_cart);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_function = (Button) m_dialog.findViewById(R.id.btn_function_cart);
        btn_function.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent()
                        .putExtra("num", buy_cnt)
                        .putExtra("title",Title);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                dismiss();
            }
        });
        img_item = (ImageView) m_dialog.findViewById(R.id.img_item_cart);
        //img_item.setImageBitmap(bmp);
        Picasso.get().load(ImgUrl).into(img_item);
        tv_title = (TextView) m_dialog.findViewById(R.id.tv_title_cart);
        tv_title.setText(Title);
        tv_describe = (TextView) m_dialog.findViewById(R.id.tv_describe_cart);
        tv_describe.setText(Describe);

        fiicdialog_bitmap1 = (ImageView)m_dialog.findViewById(R.id.fiicdialog_bitmap1);
        fiicdialog_bitmap2 = (ImageView)m_dialog.findViewById(R.id.fiicdialog_bitmap2);
        fiicdialog_bitmap3 = (ImageView)m_dialog.findViewById(R.id.fiicdialog_bitmap3);
        fiicdialog_bitmap4 = (ImageView)m_dialog.findViewById(R.id.fiicdialog_bitmap4);

        fiicdialog_textview1 = (TextView) m_dialog.findViewById(R.id.fiicdialog_textview1);
        fiicdialog_textview2 = (TextView) m_dialog.findViewById(R.id.fiicdialog_textview2);
        fiicdialog_textview3 = (TextView) m_dialog.findViewById(R.id.fiicdialog_textview3);
        fiicdialog_textview4 = (TextView) m_dialog.findViewById(R.id.fiicdialog_textview4);

        cvi_layout1 = (LinearLayout) m_dialog.findViewById(R.id.cvi_layout1);
        cvi_layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_recommand_itemlist.data_loading) {
                    Title = m_recommand_itemlist.get_item_name(0);
                    Describe = m_recommand_itemlist.get_item_description(0);
                    tv_title.setText(Title);
                    tv_describe.setText(Describe);
                    img_item.setImageBitmap(m_recommand_itemlist.get_item_bitmap(0));
                    get_random4(String.valueOf(cat_id[0]));
                }
            }
        });
        cvi_layout2 = (LinearLayout) m_dialog.findViewById(R.id.cvi_layout2);
        cvi_layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_recommand_itemlist.data_loading) {
                    Title = m_recommand_itemlist.get_item_name(1);
                    Describe = m_recommand_itemlist.get_item_description(1);
                    tv_title.setText(Title);
                    tv_describe.setText(Describe);
                    img_item.setImageBitmap(m_recommand_itemlist.get_item_bitmap(1));
                    get_random4(String.valueOf(cat_id[1]));
                }
            }
        });
        cvi_layout3 = (LinearLayout) m_dialog.findViewById(R.id.cvi_layout3);
        cvi_layout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_recommand_itemlist.data_loading) {
                    Title = m_recommand_itemlist.get_item_name(2);
                    Describe = m_recommand_itemlist.get_item_description(2);
                    tv_title.setText(Title);
                    tv_describe.setText(Describe);
                    img_item.setImageBitmap(m_recommand_itemlist.get_item_bitmap(2));
                    get_random4(String.valueOf(cat_id[2]));
                }
            }
        });
        cvi_layout4 = (LinearLayout) m_dialog.findViewById(R.id.cvi_layout4);
        cvi_layout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_recommand_itemlist.data_loading) {
                    Title = m_recommand_itemlist.get_item_name(3);
                    Describe = m_recommand_itemlist.get_item_description(3);
                    tv_title.setText(Title);
                    tv_describe.setText(Describe);
                    img_item.setImageBitmap(m_recommand_itemlist.get_item_bitmap(3));
                    get_random4(String.valueOf(cat_id[3]));
                }
            }
        });

        return m_dialog;
    }


    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    String line;
    class TransTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String...params){
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                line = in.readLine();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return line+"";
        }
        @Override
        protected  void onPostExecute(String S){
            super.onPreExecute();
            JSONObject response = null;
            try {
                response = new JSONObject(S);
                int api_id = response.getInt("api_id");
                Log.d("api_id",""+api_id);
                switch (api_id){
                    case API_ID_3_GET_RANDOM_4ITEMS:
                        parserJson_random4(response);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void get_random4(String id) {
        String urlParkingArea = "http://140.129.25.75:8000/api/category-activities?random=4&&category_id="+id;
        new TransTask().execute(urlParkingArea);
    }


    private void parserJson_random4(JSONObject random4item_list){
        try{
            JSONArray dataArray = random4item_list.getJSONArray("data");
            String home_url[] = new String[4];
            String home_name[] = new String[4];
            int home_price[] = new int[4];
            String home_description[] = new String[4];
            cat_id = new int[4];
            for(int i = 0; i < 4; i++){
                home_url[i] = dataArray.getJSONObject(i).getString("image_url");
                home_price[i] = dataArray.getJSONObject(i).getInt("price");
                home_name[i] = dataArray.getJSONObject(i).getString("name")+"\n單價："+home_price[i];
                home_description[i] = dataArray.getJSONObject(i).getString("description");
                cat_id[i] =dataArray.getJSONObject(i).getInt("category_id");
                downloadimage_item(home_url[i],home_name[i],home_description[i]);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadimage_item (String URLimageI0,String ItemName,String ItemDescription){
        new AsyncTask<String, Void, Recommand_Item>() {
            @Override
            protected Recommand_Item doInBackground(String... params) {
                String url = params[0];
                String item = params[1];
                String description = params[2];
                Log.d("downloadimage_item","start");
                return getBitmapFromURL_Update(url,item,description);
            }
            @Override
            protected void onPostExecute(Recommand_Item result) {
                Log.d("downloadimage_item","done");
                m_recommand_itemlist.add(result);
                super.onPostExecute(result);
            }
        }.execute(URLimageI0,ItemName,ItemDescription);
    }

    private static Recommand_Item getBitmapFromURL_Update(String imageUrl, String itemName, String itemDescription) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Recommand_Item rim = new Recommand_Item();
            rim.Item_name = itemName;
            rim.Item_bitmap = BitmapFactory.decodeStream(input);
            rim.Item_description = itemDescription;
            return rim;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void update_result(final Bitmap Img1, final String Text1, final Bitmap Img2, final String Text2, final Bitmap Img3, final String Text3, final Bitmap Img4, final String Text4){

        fiicdialog_bitmap1.setImageBitmap(Img1);
        fiicdialog_textview1.setText(Text1);
        fiicdialog_bitmap2.setImageBitmap(Img2);
        fiicdialog_textview2.setText(Text2);
        fiicdialog_bitmap3.setImageBitmap(Img3);
        fiicdialog_textview3.setText(Text3);
        fiicdialog_bitmap4.setImageBitmap(Img4);
        fiicdialog_textview4.setText(Text4);

    }

}
