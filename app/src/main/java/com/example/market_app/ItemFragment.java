package com.example.market_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemFragment extends Fragment {
    private final static int LAYER_0_CATEGORY = 0;
    private final static int LAYER_1_SUBCATEGORY = 1;
    private final static int LAYER_2_ITEM = 2;
    public static final int RESULT_QR_CODE = 3;
    public static final int RESULT_ZXING_QR_CODE = 0x0000c0de;
    public static final int DATEPICKER_FRAGMENT = 1;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    private OnFragmentInteractionListener mListener;
    private String[] text = {""};
    private int layer_index;
    CustomGrid adapter;
    ListView cart_view;
    private GridView grid;
    private Button btn_reload,btn_last,btn_clear;
    private ImageButton m_imgbtn_qrcode;
    private TextView text_total,text_cat_title;
    View view;
    JSONArray[] temp_subcat_product;
    String[] temp_text,temp_img,temp_id,temp_discrip;
    int cnt,cart_cnt=0,price_total=0;
    public int amount = 0;
    Bitmap m_bmp;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    int cat_num;
    String[] catagory_list;
    String[] temp_url_list;
    JSONArray[] subcatagory_list;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    private IntentIntegrator scanIntegrator;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    public ItemFragment() {
        // Required empty public constructor
    }
    private String dataGotFromServer;


    public static ItemFragment newInstance(String dataGotFromServer, String param2) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString("test",dataGotFromServer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d("get store", getArguments().getString("test"));
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Fragment item", "onCreateView");
        // Inflate the layout for this fragment
        layer_index = LAYER_0_CATEGORY;
        view = inflater.inflate(R.layout.fragment_item, container, false);
        //-----------------------------------------------------------------------------------------------------------------------------------------
        // Find view
        btn_reload      =(Button) view.findViewById(R.id.button_reload);
        btn_last        =(Button) view.findViewById(R.id.button_last);
        btn_clear       =(Button) view.findViewById(R.id.button_clear);
        m_imgbtn_qrcode =(ImageButton) view.findViewById(R.id.imgbtn_qrcode_);
        cart_view       =(ListView) view.findViewById(R.id.cart_view);
        grid            =(GridView) view.findViewById(R.id.grid);
        text_total      =(TextView) view.findViewById(R.id.text_total);
        text_cat_title  =(TextView) view.findViewById(R.id.textView2);
        //-----------------------------------------------------------------------------------------------------------------------------------------
        // Set Listener & Adapter
        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layer_index = LAYER_0_CATEGORY;
                get_category();
            }
        });
        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(layer_index == LAYER_1_SUBCATEGORY){
                    adapter = new CustomGrid(view.getContext(), catagory_list, LAYER_0_CATEGORY);
                    grid.setAdapter(adapter);
                    layer_index = LAYER_0_CATEGORY;

                    text_cat_title.setText("商品搜尋");
                }else if(layer_index == LAYER_2_ITEM){
                    adapter = new CustomGrid(view.getContext(), temp_text, LAYER_1_SUBCATEGORY);
                    grid.setAdapter(adapter);
                    layer_index = LAYER_1_SUBCATEGORY;

                    String[] temp = text_cat_title.getText().toString().split(">");
                    text_cat_title.setText(temp[0]);
                }
            }
        });
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                price_total=0;
                cart_cnt=0;
                text =new String[1];
                text[0]="";
                ArrayAdapter adp_clr = new ArrayAdapter(view.getContext(),android.R.layout.simple_list_item_1,text);
                cart_view.setAdapter(adp_clr);
                text_total.setText("總金額：0");
            }
        });
        m_imgbtn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScan();
            }
        });
        // Caculator List
        ArrayAdapter adapter_ = new ArrayAdapter(view.getContext(),android.R.layout.simple_list_item_1,text);
        cart_view.setAdapter(adapter_);
        cart_view.setOnItemClickListener(onClickListView);
        // Grid View
        adapter = new CustomGrid(view.getContext(), text, 0);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(GridView_OnItemClickListener);
        //-----------------------------------------------------------------------------------------------------------------------------------------
        m_bmp = BitmapFactory.decodeResource(view.getContext().getResources(),
                R.drawable.btn_faq);
        //Initial Function
        get_category();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Fragment item", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Fragment item", "onDestroy");
    }



    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("dataGotFromServer", dataGotFromServer);
        Log.d("onSaveInstanceState", "save");
    }

     */

    /*
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            dataGotFromServer = savedInstanceState.getString("dataGotFromServer");
            Log.d("onActivityCreated", "load");
        }
        Log.d("onActivityCreated", "null");
    }

     */

    private AdapterView.OnItemClickListener GridView_OnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            dataGotFromServer = "clicked";
            switch(layer_index){
                //-----------------------------------------------------------------------------------------------------------------------------
                case LAYER_0_CATEGORY:
                    try {
                        JSONArray temp_jsonarr = subcatagory_list[position];
                        int temp_num = temp_jsonarr.length();
                        temp_text= new String[temp_num];
                        temp_subcat_product = new JSONArray[temp_num];
                        text_cat_title.setText(catagory_list[position]);
                        for (int i = 0; i < temp_num; i++) {
                            try{
                                JSONObject o = temp_jsonarr.getJSONObject(i);
                                try {
                                    temp_text[i]=o.getString("name");
                                    temp_subcat_product[i]=o.getJSONArray("products");
                                }catch(JSONException z){
                                    z.printStackTrace();
                                }
                            }catch (JSONException zz) {
                                zz.printStackTrace();
                            }
                        }
                        adapter = new CustomGrid(view.getContext(), temp_text, LAYER_1_SUBCATEGORY);
                        grid.setAdapter(adapter);
                        layer_index = LAYER_1_SUBCATEGORY;
                    }catch(Exception e){
                        Toast toast = Toast.makeText(getActivity(),"Nothing found!!",Toast.LENGTH_LONG);
                        toast.show();
                        e.printStackTrace();
                    }
                    break;
                //-----------------------------------------------------------------------------------------------------------------------------
                case LAYER_1_SUBCATEGORY:

                    text_cat_title.setText(text_cat_title.getText()+">"+temp_text[position]);
                    JSONArray temp_jsonarr = temp_subcat_product[position];
                    int temp_num = temp_jsonarr.length();
                    String[] temp_text= new String[temp_num];
                    temp_img = new String[temp_num];
                    temp_id = new String[temp_num];
                    temp_discrip = new String[temp_num];
                    //temp_subcat_product = new JSONArray[temp_num];
                    for (int i = 0; i < temp_num; i++) {
                        try{
                            JSONObject o = temp_jsonarr.getJSONObject(i);
                            try {
                                temp_text[i]=o.getString("name")+"\n單價："+o.getInt("price")+"元";
                                temp_img[i]=o.getString("image_url");
                                temp_id[i]= String.valueOf(o.getInt("id"));
                                temp_discrip[i]=o.getString("description");
                                //temp_subcat_product[i]=o.getJSONArray("products");
                                Log.d("name of subcat", temp_text[i]);
                            }catch(JSONException z){
                                z.printStackTrace();
                            }
                        }catch (JSONException zz) {
                            zz.printStackTrace();
                        }
                    }
                    adapter = new CustomGrid(view.getContext(), temp_text, temp_img, LAYER_2_ITEM);
                    grid.setAdapter(adapter);
                    layer_index = LAYER_2_ITEM;
                    break;
                //-----------------------------------------------------------------------------------------------------------------------------
                case LAYER_2_ITEM:
                    final TextView text_ = view.findViewById(R.id.grid_text);
                    final String str_item_info = text_.getText().toString();

                    createItemInfoDialog(temp_img[position],str_item_info,temp_discrip[position],0);
                    post_id(temp_id[position]);

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode:",String.valueOf(requestCode));
        switch (requestCode) {
            case DATEPICKER_FRAGMENT:

                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    int temp_num = bundle.getInt("num",0);

                    String str_item_info = bundle.getString("title");
                    String[] t_t;
                    ArrayAdapter adapter_;
                    int t_l=text.length;
                    if(cart_cnt==0){
                        //text=new String[t_l];
                        text[0] = str_item_info+"\n數量："+temp_num;
                        adapter_ = new ArrayAdapter(view.getContext(),android.R.layout.simple_list_item_1,text);

                    }else{
                        //text=new String[t_l+1];
                        t_t =new String[t_l+1];
                        System.arraycopy(text,0,t_t,0,text.length);
                        t_t[t_l]=str_item_info+"\n數量："+temp_num;
                        adapter_ = new ArrayAdapter(view.getContext(),android.R.layout.simple_list_item_1,t_t);
                        text=new String[t_l+1];
                        System.arraycopy(t_t,0,text,0,t_t.length);
                    }
                    int price_ = str_item_info.indexOf("單價：");
                    Log.d("indexof",String.valueOf(price_));
                    String price = str_item_info.substring(price_+3).replace("元","");
                    Log.d("price",price);
                    price_=Integer.valueOf(price)*temp_num;
                    price_total+=price_;
                    String text = "總金額："+price_total;
                    text_total.setText(text);
                    cart_view.setAdapter(adapter_);
                    cart_cnt++;
                    cnt=0;

                    //Toast.makeText(getActivity(),"num="+temp_num, Toast.LENGTH_SHORT).show();
                } else if (resultCode == Activity.RESULT_CANCELED) {

                }
                break;
            case RESULT_QR_CODE:
                try{
                    String result = data.getExtras().getString("result");//得到新Activity 关闭后返回的数据
                    Log.d("result from scan:",result);
                    if(result!=null){
                        get_product(result);
                        Toast.makeText(getActivity(),"掃描成功，讀取中...", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case RESULT_ZXING_QR_CODE:
                IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanningResult != null) {
                    if(scanningResult.getContents() != null) {
                        String scanContent = scanningResult.getContents();
                        if (!scanContent.equals("")) {
                            Toast.makeText(getActivity().getApplicationContext(),"掃描內容: "+scanContent.toString(), Toast.LENGTH_SHORT).show();
                            String result = scanContent.toString();
                            if(result!=null){
                                get_product(result);
                                Toast.makeText(getActivity(),"掃描成功，讀取中...", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
        }

    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(getActivity(),text[position], Toast.LENGTH_SHORT).show();
        }
    };


    public void onScan() {
        //Intent intent = new Intent(getActivity(), QRScanDialogFragment.class);
        //startActivity(intent);
        Configuration conf = getResources().getConfiguration();
        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        if(isLandscape) {
            startActivityForResult(new Intent(getActivity(), QRScanDialogFragment.class), RESULT_QR_CODE);
        }
        else {
            scanIntegrator = new IntentIntegrator(getActivity());
            //scanIntegrator = new IntentIntegrator(getActivity());
            scanIntegrator.setPrompt("請掃描");
            scanIntegrator.setTimeout(300000);
            scanIntegrator.setOrientationLocked(false);
            scanIntegrator.forSupportFragment(ItemFragment.this).initiateScan();
            Log.d("onscan","onScan else");
        }
    }

    private void get_product(final String parms) {
        String urlParkingArea = parms;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, //!!!!!!!!!!!!!!POST HERE
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ItemFragment","get_product onResponse()");
                        Log.d("Response", response.toString());
                        parserJson_product(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, "error : " + error.toString());
                        Log.d("ERROR","error => "+error.toString());
                        // update_done=false;
                    }
                }
        );
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    private void parserJson_product(JSONObject cat_list) {
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", cat_list.toString());
            JSONArray c = cat_list.getJSONArray("data");
            String temp_title,temp_disc,temp_img;
            JSONObject o = c.getJSONObject(0);
            try {
                temp_title=o.getString("name")+"\n單價："+o.getInt("price")+"元";
                temp_disc=o.getString("description");
                temp_img=o.getString("image_url");
                createItemInfoDialog(temp_img,temp_title,temp_disc,0);
            }catch(JSONException z){
                z.printStackTrace();
            }

            //orders_list = new String[cnt];
            //id_list = new String[cnt];
            //loadcatlist();
            adapter = new CustomGrid(view.getContext(), catagory_list,0);
            grid.setAdapter(adapter);

            //adapter.refresh(catagory_list,mThumbIds);
            for (int j = 0; j < cat_num; j++) {
                get_items(String.valueOf(j+1));
            }
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get catagory failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }

    private void get_items(final String parms) {
        String urlParkingArea = "http://140.129.25.75:8000/api/categories/"+parms;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, //!!!!!!!!!!!!!!POST HERE
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ItemFragment","get_items onResponse()");
                        Log.d("Response", response.toString());
                        parserJson_items(response,parms);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, "error : " + error.toString());
                        Log.d("ERROR","error => "+error.toString());
                        // update_done=false;
                    }
                }
        );
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    private void get_category() {
        Log.d("ItemFragment","get_category()");
        String urlParkingArea = "http://140.129.25.75:8000/api/categories/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, //!!!!!!!!!!!!!!POST HERE
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ItemFragment","get_category onResponse()");
                        Log.d("Response", response.toString());
                        parserJson_catagory(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, "error : " + error.toString());
                        Log.d("ERROR","error => "+error.toString());
                        // update_done=false;
                    }
                }
        );
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }


    private void post_id(String id) {
        JSONObject postparams = new JSONObject();
        try{

            postparams.put("subcategory_id", id);
        }catch(JSONException e){
            // Recovery
        }

        String urlParkingArea = "http://140.129.25.75:8000/api/subcategory-counters";
        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest(Request.Method.POST, //!!!!!!!!!!!!!!POST HERE
                urlParkingArea,postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, "response = " + response.toString());
                        Log.d("ItemFragment","post_id onResponse()");
                        Log.d("Response", response.toString());
                        //update_done=true;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, "error : " + error.toString());
                        Log.d("ERROR","error => "+error.toString());
                        // update_done=false;
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("content-type","application/json");
                return params;
            }
        };
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }


    private void parserJson_catagory(JSONObject cat_list) {
        Log.d("ItemFragment","parserJson_catagory()");
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", cat_list.toString());
            JSONArray c = cat_list.getJSONArray("data");
            cat_num=c.length();
            catagory_list = new String[cat_num];
            subcatagory_list = new JSONArray[cat_num];
            for (int i = 0; i < cat_num; i++) {
                JSONObject o = c.getJSONObject(i);
                try {
//                    get_items(String.valueOf(i+1));
                    catagory_list[i]=o.getString("name");
                    subcatagory_list[i] = o.getJSONArray("subcategories");
                    Log.d("catagory_list", catagory_list[i]);
                }catch(JSONException z){
                    z.printStackTrace();
                }
            }
            //orders_list = new String[cnt];
            //id_list = new String[cnt];
            //loadcatlist();
            adapter = new CustomGrid(view.getContext(), catagory_list,0);
            grid.setAdapter(adapter);

            //adapter.refresh(catagory_list,mThumbIds);
            for (int j = 0; j < cat_num; j++) {
                get_items(String.valueOf(j+1));
            }
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get catagory failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }



    int subcat_num;
    //String[][] subcatagory_list;

    private void parserJson_items(JSONObject subcat_list,String parms) {
        Log.d("ItemFragment","parserJson_items()");
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", subcat_list.toString());
            JSONArray a = subcat_list.getJSONArray("data");
            JSONArray c = a.getJSONObject(0).getJSONArray("subcategories");
            subcatagory_list[Integer.valueOf(parms)-1]=c;
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get items failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }

    public void createItemInfoDialog(String url, String title , String describe, int location){
        //ItemInfoCartDialogFragment IID = ItemInfoCartDialogFragment.newInstance(bmp,title,describe,location);
        //IID.show(getFragmentManager(),"???");


        ItemInfoCartDialogFragment dialog = ItemInfoCartDialogFragment.newInstance(url,title,describe,location);
        // optionally pass arguments to the dialog fragment
        // setup link back to use and display
        dialog.setTargetFragment(this, DATEPICKER_FRAGMENT);
        dialog.show(getFragmentManager().beginTransaction(), "MyProgressDialog");
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
}

