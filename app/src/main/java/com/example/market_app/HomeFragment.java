package com.example.market_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public String line;
    private TextView data;
    public String [] goods_number;
    public String [] goods_name;
    public String [] goods_price;
    public String [] goods_description;
    public String [] goods_specification;
    public String [] home_url;
    public String [] home_name;

    public String bigcategorynum="";
    public ListView list_big_item;
    public Integer jsonmode=5;

    /** 推播 **/
    public ViewFlipper m_vf_commercial;
    public ImageView m_imv_left;
    public ImageView m_imv_right;
    public TextView alert_text;

    public LinearLayout m_ll_about_btn,m_ll_new_btn,m_ll_instruc_btn,m_ll_custom_btn;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    private MainPagerAdapter mPagerAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        m_vf_commercial = (ViewFlipper) view.findViewById(R.id.vf_commercial);
        m_imv_left = (ImageView) view.findViewById(R.id.imv_left);
        m_imv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_vf_commercial.stopFlipping();
                m_vf_commercial.setInAnimation(getActivity(),R.anim.marquee_enter_right);
                m_vf_commercial.setOutAnimation(getActivity(),R.anim.marquee_exit_right);
                m_vf_commercial.showPrevious();
            }
        });
        m_imv_right = (ImageView) view.findViewById(R.id.imv_right);
        m_imv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_vf_commercial.stopFlipping();
                m_vf_commercial.setInAnimation(getActivity(),R.anim.marquee_enter_left);
                m_vf_commercial.setOutAnimation(getActivity(),R.anim.marquee_exit_left);
                m_vf_commercial.showNext();
            }
        });
        m_ll_about_btn = (LinearLayout) view.findViewById(R.id.ll_about_btn);
        m_ll_about_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewDialogFragment wvdf = new WebViewDialogFragment().newInstance("https://www.j-mart.com.tw/cm/page.aspx?wmt=6");
                wvdf.show(getFragmentManager().beginTransaction(), "WebDialog");
            }
        });
        m_ll_new_btn = (LinearLayout) view.findViewById(R.id.ll_new_btn);
        m_ll_new_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewDialogFragment wvdf = new WebViewDialogFragment().newInstance("https://www.j-mart.com.tw/cm/news.aspx");
                wvdf.show(getFragmentManager().beginTransaction(), "WebDialog");
            }
        });
        m_ll_instruc_btn = (LinearLayout) view.findViewById(R.id.ll_instruc_btn);
        m_ll_instruc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewDialogFragment wvdf = new WebViewDialogFragment().newInstance("https://www.j-mart.com.tw/cm/dms.aspx");
                wvdf.show(getFragmentManager().beginTransaction(), "WebDialog");
            }
        });
        m_ll_custom_btn = (LinearLayout) view.findViewById(R.id.ll_custom_btn);
        m_ll_custom_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebViewDialogFragment wvdf = new WebViewDialogFragment().newInstance("https://www.j-mart.com.tw/cm/contactus.aspx");
                wvdf.show(getFragmentManager().beginTransaction(), "WebDialog");
            }
        });

        jsonmode = 5;
        if(jsonmode == 5) {
            new HomeFragment.TransTask().execute
            ("http://140.129.25.75:8000/api/activities");

        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /********************** json  *********************************/
    class TransTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String...params){
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                line = in.readLine();

                Log.d("HTTP", line);

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
            //data.append("s= "+S);
            //大分類
            if(jsonmode== 0){
                parseJSON0(S);
            }
            //小分類
            else if(jsonmode== 1){
                parseJSON1(S);
            }
            else if(jsonmode== 2){
                parseJSON2(S);
            }
            else if(jsonmode==5){
                parseJSON5(S);
                jsonmode=0;
            }
            //super.onPostExecute(s);
        }
    }
    //解析 JSON 格式 //大分類
    private  void parseJSON0(String s){
        //data.append("aaa");
        try{
            //data.append("bbb");
            JSONArray dataArray  = new JSONArray(s);
            goods_number = new String [dataArray .length()];
            goods_name = new String [dataArray .length()];
            //data.append("eee");
            for (int i = 0; i < dataArray.length(); i++) {
                goods_number[i] = dataArray.getJSONObject(i).getString("number");
                goods_name[i] = dataArray.getJSONObject(i).getString("name");
                //data.append(goods_number[i]);
                //data.append(goods_name[i]);
            }

            //Goods List ---------------------------------------------------------------------------------------------------------------------------
            ListAdapter adapter = new ArrayAdapter<String>(getActivity() , android.R.layout.simple_list_item_1 ,goods_name);//丟入你要顯示的文字
            //list_big_item.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);  //設定選擇的模式
            list_big_item.setAdapter(adapter);                                  //使用ListAdapter來顯示你輸入的文字//將ListAdapter設定至ListView裡面
            list_big_item.setOnItemClickListener(onClickListViewbig);          //指定事件 Method
        }
        catch(JSONException e) {
            e.printStackTrace();
            //data.append("ccc");
        }
    }

    //解析 JSON 格式 //小分類
    private  void parseJSON1(String s){
        //data.append("aaa");
        try{
            //data.append("bbb");
            JSONArray dataArray  = new JSONArray(s);
            goods_number = new String [dataArray .length()];
            goods_name = new String [dataArray .length()];

            //data.append("eee");
            for (int i = 0; i < dataArray.length(); i++) {
                goods_number[i] = dataArray.getJSONObject(i).getString("number");
                goods_name[i] = dataArray.getJSONObject(i).getString("name");
                //data.append(goods_number[i]);
                //data.append(goods_name[i]);
            }
            //Goods List ---------------------------------------------------------------------------------------------------------------------------
            ListAdapter adapter = new ArrayAdapter<String>(getActivity() , android.R.layout.simple_list_item_checked ,goods_name);//丟入你要顯示的文字
            list_big_item.setAdapter(adapter);                                  //使用ListAdapter來顯示你輸入的文字//將ListAdapter設定至ListView裡面
            list_big_item.setOnItemClickListener(onClickListViewbig);          //指定事件 Method

        }
        catch(JSONException e) {
            e.printStackTrace();
            //data.append("ccc");
        }
    }

    //解析 JSON 格式 // QR Code
    private  void parseJSON2(String s){
        //data.append("aaa");
        try{
            //data.append("bbb");
            JSONArray dataArray  = new JSONArray(s);
            goods_name = new String [dataArray .length()];
            goods_price = new String [dataArray .length()];
            goods_number = new String [dataArray .length()];
            goods_description = new String [dataArray .length()];
            goods_specification = new String [dataArray .length()];

            //data.append("eee");
            for (int i = 0; i < dataArray.length(); i++) {
                goods_name[i] = "";
                goods_number[i]= "";
                goods_price[i] = "";
                goods_description[i] = "";
                goods_specification[i]= "";

                goods_name[i] = dataArray.getJSONObject(i).getString("name");
                goods_price[i] = dataArray.getJSONObject(i).getString("price");
                goods_description[i] = dataArray.getJSONObject(i).getString("description");
                goods_specification[i] = dataArray.getJSONObject(i).getString("specification");
                String textcommercial = "商品名稱: "+goods_name[i]+"\n"+"售價: "+goods_price[i]+"\n"+
                        goods_description[i]+"\n"+goods_specification[i];
                Log.d("textcommercial","textcommercial = "+textcommercial);

            }

        }
        catch(JSONException e) {
            e.printStackTrace();
            //data.append("ccc");
        }
        jsonmode= 0;
    }
    //解析 JSON 格式 //首頁照片及文字
    private  void parseJSON5(String s){

        try{
            //data.append("bbb");
            JSONObject o = new JSONObject(s);
            JSONArray dataArray  = o.getJSONArray("data");
            home_url = new String [dataArray.length()];
            home_name = new String [dataArray.length()];
            Log.d("j5","parseJSON5-1");
            //data.append("eee");
            for (int i = 0; i < dataArray.length(); i++) {
                home_url[i] = dataArray.getJSONObject(i).getString("image_url");
                home_name[i] = dataArray.getJSONObject(i).getString("name");
                downloadimage(home_url[i]);

            }
        }
        catch(JSONException e) {
            e.printStackTrace();
            //data.append("ccc");
        }
    }

    /**************** List View **************************/
    private AdapterView.OnItemClickListener onClickListViewbig
            = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            //Toast.makeText(MainActivity.this,"點選第 "+(position) +" 個 \n內容："+bigcategorynum+"", Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity.this,"location= "+location, Toast.LENGTH_SHORT).show();
            if(jsonmode==0){
                bigcategorynum = goods_number[position];
                jsonmode= 1;
                new HomeFragment.TransTask().execute
                        ("http://140.129.25.75:8080/api/navigation/categoryToSubcategory.php?number="+bigcategorynum);
            }

        }
    };

    /********************** 下載 URL 圖片*******************/
    // First image
    private void downloadimage (String URLimageI0){
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                String url = params[0];
                Log.d("image","image 0");
                return getBitmapFromURL(url);
            }
            @Override
            protected void onPostExecute(Bitmap result) {
                addcommercialimage(result);
                //imgp1. setImageBitmap (result);
                Log.d("image","image 1");
                super.onPostExecute(result);
            }
        }.execute(URLimageI0);
    }


    /**************** 推播欄位 動胎新增 ImageLayout **********/
    private void addcommercialimage(Bitmap addimage){
        //commercial.removeAllViews();  //clear linearlayout
        //for (int i = 0; i < size; i++) {
        /** 動態新增 Image View **/
        ImageView imageView = new ImageView(this.getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap(addimage);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(addimage.getWidth(), addimage.getHeight());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        m_vf_commercial.addView(imageView);
    }

    //讀取網路圖片，型態為Bitmap
    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Log.d("image","image 2");
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }
        catch (IOException e) {
            Log.d("image","image 3");
            e.printStackTrace();
            return null;
        }
    }

    /******************************************************/

    public class MainPagerAdapter extends PagerAdapter
    {
        // This holds all the currently displayable views, in order from left to right.
        private ArrayList<View> views = new ArrayList<View>();

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
        // page should be displayed, from left-to-right.  If the page no longer exists,
        // return POSITION_NONE.
        @Override
        public int getItemPosition (Object object)
        {
            int index = views.indexOf (object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
        // to add the page to the container, which is normally the ViewPager itself.  Since
        // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
        @Override
        public Object instantiateItem (ViewGroup container, int position)
        {
            View v = views.get (position);
            container.addView (v);
            return v;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
        // is our job to remove the page from the container, which is normally the
        // ViewPager itself.  Since all our pages are persistent, we do nothing to the
        // contents of our "views" ArrayList.
        @Override
        public void destroyItem (ViewGroup container, int position, Object object)
        {
            container.removeView (views.get (position));
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager; can be used by app as well.
        // Returns the total number of pages that the ViewPage can display.  This must
        // never be 0.
        @Override
        public int getCount ()
        {
            return views.size();
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.
        @Override
        public boolean isViewFromObject (View view, Object object)
        {
            return view == object;
        }

        //-----------------------------------------------------------------------------
        // Add "view" to right end of "views".
        // Returns the position of the new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v)
        {
            return addView (v, views.size());
        }

        //-----------------------------------------------------------------------------
        // Add "view" at "position" to "views".
        // Returns position of new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v, int position)
        {
            views.add (position, v);
            return position;
        }

        //-----------------------------------------------------------------------------
        // Removes "view" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView (ViewPager pager, View v)
        {
            return removeView (pager, views.indexOf (v));
        }

        //-----------------------------------------------------------------------------
        // Removes the "view" at "position" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView (ViewPager pager, int position)
        {
            // ViewPager doesn't have a delete method; the closest is to set the adapter
            // again.  When doing so, it deletes all its views.  Then we can delete the view
            // from from the adapter and finally set the adapter to the pager again.  Note
            // that we set the adapter to null before removing the view from "views" - that's
            // because while ViewPager deletes all its views, it will call destroyItem which
            // will in turn cause a null pointer ref.
            pager.setAdapter (null);
            views.remove (position);
            pager.setAdapter (this);

            return position;
        }

        //-----------------------------------------------------------------------------
        // Returns the "view" at "position".
        // The app should call this to retrieve a view; not used by ViewPager.
        public View getView (int position)
        {
            return views.get (position);
        }

        // Other relevant methods:

        // finishUpdate - called by the ViewPager - we don't care about what pages the
        // pager is displaying so we don't use this method.
    }
}
