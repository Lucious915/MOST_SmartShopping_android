package com.example.market_app;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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

    View view;
    private ListView record_view;
    private TextView textView;
    private Button btn_reload,btn_last;
    int cnt=2;
    String[] orders_list = new String[]{"暫無訂單資料"};
    String[] id_list = new String[]{"暫無id資料"};
    String[] img_list = new String[]{"https://img.icons8.com/wired/64/000000/list.png"};
    //String[] orders_list = new

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account, container, false);

        btn_reload= (Button) view.findViewById(R.id.button_reload_);
        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clk_cnt=0;
                get_orders("1");
            }
        });
        btn_last =(Button) view.findViewById(R.id.button_last_);
        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clk_cnt==1){
                    record_view.setAdapter(new PicassoAdapter(view.getContext(),img_list,orders_list,id_list));
                    clk_cnt--;
                }
            }
        });

        textView = (TextView) view.findViewById(R.id.textView2);
        textView.setText("帳號：aaaaa777@gmail.com");

        record_view=(ListView) view.findViewById(R.id.record_view);
        record_view.setAdapter(new PicassoAdapter(view.getContext(),orders_list,orders_list,id_list));
        record_view.setOnItemClickListener(choose_record);

        get_orders("1");

        return view;
    }

    int clk_cnt=0;
    JSONArray[] temp_orders_products;
    String[] temp_;
    private AdapterView.OnItemClickListener choose_record = new  AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position , long id){
            //String sel=parent.getItemAtPosition(position).toString();
            //ConfirmOrder(position);
            if(clk_cnt==0){
                try {
                    JSONArray temp_jsonarr = orders_products[position];
                    int temp_num = temp_jsonarr.length();
                    temp_= new String[temp_num];
                    String[] temp_img  = new String[temp_num];
                    String[] temp_disc = new String[temp_num];
                    int price=0;
                    for (int i = 0; i < temp_num; i++) {
                        try{
                            JSONObject o = temp_jsonarr.getJSONObject(i);
                            try {
                                int a = Integer.valueOf(o.getString("amount"));
                                int b = Integer.valueOf(o.getJSONObject("product").getString("price"));
                                price+= a*b;
                                temp_[i]=o.getJSONObject("product").getString("name");
                                temp_img[i]=o.getJSONObject("product").getString("image_url");
                                temp_disc[i]="數量："+o.getString("amount")+"\n"+"總價："+(a*b);
                            }catch(JSONException z){
                                z.printStackTrace();
                            }
                        }catch (JSONException zz) {
                            zz.printStackTrace();
                        }
                    }
                    record_view.setAdapter(new PicassoAdapter(view.getContext(),temp_img,temp_,temp_disc));
                    clk_cnt++;
                }catch(Exception e){
                    Toast toast = Toast.makeText(getActivity(),"Nothing found!!",1);
                    toast.show();
                    e.printStackTrace();
                }
            }


            /*
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity()," "+position,duration);
            toast.show();

             */
            //mTextMessage.setText(sel);
        }
    };


    private void get_orders(String param) {
        String urlParkingArea = "http://140.129.25.75:8000/api/orders/"+param;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, //!!!!!!!!!!!!!!POST HERE
                urlParkingArea,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        parserJson_orders(response);
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
        Volley.newRequestQueue(getActivity()).add(jsonArrayRequest);
    }

    int orders_num;
    //String[] orders_list;
    JSONArray[] orders_products;
    private void parserJson_orders(JSONArray ord_list) {
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", ord_list.toString());
            orders_num=ord_list.length();
            orders_list = new String[orders_num];
            id_list = new String[orders_num];
            img_list = new String[orders_num];
            orders_products = new JSONArray[orders_num];
            //subcatagory_list = new JSONArray[cat_num];
            //mThumbIds = new String[cat_num];
            for (int i = 0; i < orders_num; i++) {
                JSONObject o = ord_list.getJSONObject(i);
                try {
                    orders_list[i]="購買日期："+o.getString("created_at");
                    orders_products[i]=o.getJSONArray("order_products");
                    id_list[i]="點擊查看詳細訂單";
                    img_list[i]= "https://img.icons8.com/wired/64/000000/list.png";
                }catch(JSONException z){
                    z.printStackTrace();
                }
            }
            //orders_list = new String[cnt];
            //id_list = new String[cnt];
            //loadcatlist();

            record_view.setAdapter(new PicassoAdapter(view.getContext(),img_list,orders_list,id_list));

        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
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
