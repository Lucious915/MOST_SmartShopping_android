package com.example.market_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ViewFlipper;

import com.android.volley.Response;
import com.example.market_app.btlescan.containers.BluetoothLeDeviceStore;
import com.example.market_app.btlescan.util.BluetoothUtils;
import com.example.market_app.btlescan.util.BluetoothLeScanner;
import com.example.market_app.btlescan.util.CountObj;
import com.example.market_app.btlescan.util.TimeFormatter;
import com.example.market_app.btlescan.util.WriteFileData;
import com.example.market_app.libs.bluetoothlelib.device.BluetoothLeDevice;
import com.example.market_app.libs.bluetoothlelib.device.beacon.BeaconType;
import com.example.market_app.libs.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;
import com.example.market_app.libs.bluetoothlelib.device.beacon.BeaconUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.google.zxing.integration.android.IntentIntegrator;
import static com.serenegiant.utils.UIThreadHelper.runOnUiThread;
import com.example.market_app.gesture_imageview.GestureImageView;

public class SearchFragment extends Fragment {

    private final static int LAYER_0_CATEGORY = 0;
    private final static int LAYER_1_SUBCATEGORY = 1;

    private final static int API_ID_1_GET_CATEGORY = 1;
    private final static int API_ID_2_GET_SUBCATEGORY = 2;
    private final static int API_ID_3_GET_RANDOM_4ITEMS = 3;
    private final static int API_ID_8_GET_BEACON = 8;
    public static final int INFO_DIALOG_SHOW_LOCATION = 0;

    private Activity mActivity;
    private View view;
    //定義TFlite物件----------------------------------------------------------------------------------------------------------------------------
    public Interpreter tflite;
    String modelFile = "convert_model.tflite";
    //定位前/後處理----------------------------------------------------------------------------------------------------------------------------
    private boolean blescanning = false;
    final int INPUT_NUM = 26;//26;
    final int OUTPUT_NUM = 48;//105;
    final int TIME_SEQ = 3;
    float[][][] inp = new float[1][TIME_SEQ][INPUT_NUM];
    float[][] out = new float[1][OUTPUT_NUM];
    int max_index = 0;
    int most_index = 0;
    //定義NNTthread----------------------------------------------------------------------------------------------------------------------------
    private HandlerThread mThread;
    private Handler mThreadHandler;
    //定義藍芽物件-----------------------------------------------------------------------------------------------------------------------------
    private BluetoothLeDeviceStore mDeviceStore;
    private BluetoothLeScanner mScanner;
    private BluetoothUtils mBluetoothUtils;
    //定義存取Beacon資料物件--------------------------------------------------------------------------------------------------------------
    public WriteFileData mWriteFileData = new WriteFileData();
    public List<WriteFileData> wtdArray = new ArrayList<>();
    CountObj[] RSSI_CObj = new CountObj[INPUT_NUM];
    CountObj[] RSSI_bottom_CObj = new CountObj[INPUT_NUM];
    CountObj ResultQueue = new CountObj();
    //定義layout物件----------------------------------------------------------------------------------------------------------------------------
    //標題-----------------------------------------------------------------------------------------------------------------------------------------
    private Switch m_sw_blescan;
    //右側欄--------------------------------------------------------------------------------------------------------------------------------------
    //---------隱藏物件--------------------------------------------------------------------------------------------------------------------------
    private TextView m_tv_predictresult;
    private TextView m_tv_predictlocation;
    //---------上方按鈕--------------------------------------------------------------------------------------------------------------------------
    private TextView m_tv_title;
    private Button m_btn_allcategory;
    private Button m_btn_lastpage;
    private TextView m_tv_list;
    //---------商品選單--------------------------------------------------------------------------------------------------------------------------
    public ListView m_list_big_item;
    public int list_layer_index = LAYER_0_CATEGORY;
    //---------廣告推播--------------------------------------------------------------------------------------------------------------------------
    public Timer timer;
    public TextView alert_text;
    //---------廣告推播 ViewFlipper-----------------------------------------------------------------------------------------------------------
    public static class Recommand_Item
    {
        Bitmap Item_bitmap;
        String Item_name;
        String Item_location;
    }
    public class Recommand_Item_List
    {
        Recommand_Item Item[] = new Recommand_Item[4];
        int write_count = 0;
        boolean data_completed = false;
        void init(){
            write_count = 0;
            data_completed = false;
        }
        void add(Recommand_Item rim){
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
                addflipper(Item[0].Item_bitmap, Item[0].Item_name, Item[0].Item_location
                        , Item[1].Item_bitmap, Item[1].Item_name, Item[1].Item_location
                        , Item[2].Item_bitmap, Item[2].Item_name, Item[2].Item_location
                        , Item[3].Item_bitmap, Item[3].Item_name, Item[3].Item_location);
                data_completed = false;
                Log.d("Recommand_Item","data completed.");
            }
            else{
                Log.d("Recommand_Item","data not completed.");
            }
        }
    }
    public ViewFlipper m_commercial_flipper;
    public Recommand_Item_List m_recommand_itemlist = new Recommand_Item_List();
    public TextView m_tv_prebtn,m_tv_nextbtn;
    //地圖-----------------------------------------------------------------------------------------------------------------------------------------
    private GestureImageView m_gesture_imageview;
    //定位點--------------------------------------------------------------------------------------------------------------------------------------
    int predict_location=0;//預測定位點
    int pre_predict_location=0;
    boolean send_ad=true;
    final int[] ResultToPosition ={101,100,201,200,301,300,401,400,501,500,
            601,600,701,700,801,800,901,900,1001,1000,
            1101,1100,1201,1200,1301,1300,1401,1400,1501,1500};
    final int[] ResultToBeaconID ={5,4,8,7,11,10,14,13,17,16
            ,20,19,23,22,26,25,29,28,32,31
            ,35,34,38,37,41,40,44,43,47,46};
    //路徑規劃-----------------------------------------------------------------------------------------------------------------------------------
    public int BR=0;
    public int pre_nowp=0;  //先前位置
    public int nowp=0;      //現在位置
    public int nowpX=0;    //現在位置X
    public int nowpY=0;     //現在位置Y
    public int destination=0;   //目的地位置
    public int destinationX=0;  //目的地位置X
    public int destinationY=0;  //目的地位置Y
    public int temp=0;         //暫存位置
    public int tempX=0;
    public int tempY=0;
    public int routesize=0;
    public int tempk=0;
    public int tempkX=0;
    public int tempkY=0;
    public ArrayList<Integer> route_arr = new ArrayList<Integer>(); //路徑位置
    //商品資訊-----------------------------------------------------------------------------------------------------------------------------------
    public String line;
    public String [] m_catagory_list;
    public JSONArray [] m_subcatagory_jsonarray_list;
    public String [] m_subcatagory_location_list;
    public String [] m_subcatagory_list;


    //Fragment------------------------------------------------------------------------------------------------------------------------------------
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private OnFragmentInteractionListener mListener;
    private boolean view_inited = false;
    //定義藍芽Callback Function--------------------------------------------------------------------------------------------------------------
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                final IBeaconDevice beacon = new IBeaconDevice(deviceLe);
                mDeviceStore.addDevice(deviceLe);
                mWriteFileData = new WriteFileData();
                mWriteFileData.TimeStamp = TimeFormatter.getIsoDateTime(beacon.getTimestamp());
                mWriteFileData.Major = beacon.getMajor();
                mWriteFileData.Minor = beacon.getMinor();
                mWriteFileData.RSSI = beacon.getRssi();
                wtdArray.add(mWriteFileData);
                if(mWriteFileData.Major == 2){
                    if(mWriteFileData.Minor < INPUT_NUM){
                        RSSI_CObj[mWriteFileData.Minor].inputArray(mWriteFileData.RSSI);
                    }
                }
                else if(mWriteFileData.Major == 6){
                    if(mWriteFileData.Minor < 10){
                        RSSI_bottom_CObj[mWriteFileData.Minor].inputArray(mWriteFileData.RSSI);
                    }
                }
                mWriteFileData = null;
            }
        }
    };
    //===============================================================================================

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        //===========================================================================================
        //載入模型-----------------------------------------------------------------------------------------------------------------------------
        try {
            tflite = new Interpreter(loadModelFile(mActivity, modelFile));
            Log.wtf("tensorflow lite:","load " + modelFile + " success.");
        } catch (IOException e) {
            Log.wtf("tensorflow lite:","load " + modelFile + " failed.");
            e.printStackTrace();
        }
        //===========================================================================================
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
        mScanner.scanLeDevice(-1, false);
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacks(r1);
        }
        if (mThread != null) {
            mThread.quit();
        }
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_search, container, false);
        for(int i=0;i<INPUT_NUM;i++){
            RSSI_CObj[i]=new CountObj();
            RSSI_bottom_CObj[i]=new CountObj();
        }

        //繼承物件-----------------------------------------------------------------------------------------------------------------------------
        //標題-----------------------------------------------------------------------------------------------------------------------------------
        m_sw_blescan = (Switch)view.findViewById(R.id.sw_blescan);
        //右側欄--------------------------------------------------------------------------------------------------------------------------------
        //---------隱藏物件--------------------------------------------------------------------------------------------------------------------
        m_tv_predictresult = (TextView)view.findViewById(R.id.tv_show1);
        m_tv_predictlocation = (TextView)view.findViewById(R.id.tv_show2);
        //---------上方按鈕--------------------------------------------------------------------------------------------------------------------
        m_tv_title = (TextView) view.findViewById(R.id.tv_title);
        m_btn_allcategory = (Button) view.findViewById(R.id.btn_allcategory);
        m_btn_lastpage = (Button) view.findViewById(R.id.btn_lastpage);
        m_tv_list = (TextView) view.findViewById(R.id.tv_list);
        //---------商品選單--------------------------------------------------------------------------------------------------------------------
        m_list_big_item = (ListView) view.findViewById(R.id.list_big_item);
        //---------廣告推播--------------------------------------------------------------------------------------------------------------------
        alert_text = (TextView) view.findViewById(R.id.alert_text);

        m_commercial_flipper = (ViewFlipper)view.findViewById(R.id.marquee_view);
        timer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(blescanning) {
                            Log.d("SearchFragment","TimerTask");
                            new POST_position_id_TransTask().execute("" + ResultToBeaconID[predict_location]);
                            if(pre_predict_location!=predict_location){
                                send_ad = false;
                                Log.d("SearchFragment","different");
                            }
                            else if(pre_predict_location==predict_location){
                                Log.d("SearchFragment","same");
                                if(!send_ad) {
                                    send_ad = true;
                                    get_beacon("" + ResultToBeaconID[predict_location]);
                                    Log.d("SearchFragment","get_beacon");
                                }
                            }
                            pre_predict_location = predict_location;
                        }
                    }
                });
            }
        };
        timer.schedule(task, 1000, 5000);
        m_tv_prebtn = (TextView) view.findViewById(R.id.tv_prebtn);
        m_tv_prebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_commercial_flipper.stopFlipping();
                m_commercial_flipper.setInAnimation(mActivity,R.anim.marquee_enter_down);
                m_commercial_flipper.setOutAnimation(mActivity,R.anim.marquee_exit_down);
                m_commercial_flipper.showPrevious();
            }
        });
        m_tv_nextbtn = (TextView) view.findViewById(R.id.tv_nextbtn);
        m_tv_nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_commercial_flipper.stopFlipping();
                m_commercial_flipper.setInAnimation(mActivity,R.anim.marquee_enter_up);
                m_commercial_flipper.setOutAnimation(mActivity,R.anim.marquee_exit_up);
                m_commercial_flipper.showNext();
            }
        });
        //地圖-----------------------------------------------------------------------------------------------------------------------------------
        m_gesture_imageview= view.findViewById(R.id.imageViewm);
        //===========================================================================================
        mBluetoothUtils = new BluetoothUtils(mActivity);
        mDeviceStore = new BluetoothLeDeviceStore();
        mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
        wtdArray = new ArrayList<>();
        //設置監聽器--------------------------------------------------------------------------------------------------------------------------
        m_sw_blescan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    startBleScan();
                    blescanning = true;
                    route();
                }
                else {
                    mScanner.scanLeDevice(-1, false);
                    blescanning = false;
                    route();
                }
            }
        });

        m_list_big_item.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        m_list_big_item.setOnItemClickListener(onClickListViewbig);
        m_btn_allcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_layer_index=LAYER_0_CATEGORY;
                get_category();
//                jsonmode=0;
//                new TransTask().execute
//                        ("http://140.129.25.75:8080/api/navigation/categories.php");
            }
        });
        m_btn_lastpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_layer_index=LAYER_0_CATEGORY;
                m_tv_list.setText("商品清單");
                ListAdapter adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1 ,m_catagory_list);
                m_list_big_item.setAdapter(adapter);
            }
        });
        //開啟Thread--------------------------------------------------------------------------------------------------------------------------
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler=new Handler(mThread.getLooper());
        mThreadHandler.post(r1);
        //===========================================================================================
        list_layer_index=LAYER_0_CATEGORY;
        request_data_initial();

        return view;
    }
    private void request_data_initial(){
        view_inited = false;
        get_category();
    }


    //定義Runnable-----------------------------------------------------------------------------------------------------------------------------
    private Runnable r1=new Runnable () {
        public void run() {
            // TODO Auto-generated method stub
            if(blescanning){
                update_input_data();
                DNNCompute();
            }
            mThreadHandler.postDelayed(r1, 500);
        }
    };

    public void update_input_data() {
        String ss = "";
        for(int timeseq=1;timeseq<TIME_SEQ;timeseq++){
            for(int i=0;i<INPUT_NUM;i++){
                inp[0][timeseq-1][i] = inp[0][timeseq][i];
                ss = ss+inp[0][timeseq][i]+",";
            }
            Log.wtf("input array "+(timeseq-1) , ss);
            ss = "";
        }

        for(int i=0;i<INPUT_NUM;i++) {
            inp[0][TIME_SEQ-1][i] = RSSI_CObj[i].getAvg();
            ss = ss+inp[0][TIME_SEQ-1][i]+",";
            RSSI_CObj[i].initial();
        }
        Log.wtf("input array "+(TIME_SEQ-1) , ss);
    }

    public int DNNCompute() {
        Log.wtf("tflite: " , ""+tflite);
        int i=0 , counter=0;
        int max_index_bottom;
        float max = 0;
        float[] bottom_beacon = new float[INPUT_NUM];
        float[] result = new float[OUTPUT_NUM];

        long startTime = System.nanoTime();
        tflite.run(inp, out);
        long consumingTime = System.nanoTime() - startTime;
        Log.wtf("Compute time:" , ""+consumingTime);


//        for(i=0;i<INPUT_NUM;i++){bottom_beacon[i] = RSSI_bottom_CObj[i].getAvg();}
//        for(counter = 0; counter < bottom_beacon.length; counter++){
//            if(bottom_beacon[counter] > max && bottom_beacon[counter]!=70){
//                max = bottom_beacon[counter];
//                max_index_bottom = counter;
//            }
//        }


        for(i=0;i<OUTPUT_NUM;i++){result[i]=out[0][i];}
        for (counter = 0; counter < result.length; counter++) {
            if (result[counter] > max) {
                max = result[counter];
                max_index = counter;
            }
        }
        Log.wtf("max index:",""+max_index);
        ResultQueue.inputArray_10item(max_index);
        most_index = ResultQueue.getMost();
        predict_location = ((most_index/4)<INPUT_NUM)?(most_index/4):predict_location;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int predict_location = most_index/4;
                m_tv_predictresult.setText(""+most_index);
                m_tv_predictlocation.setText("Location "+ ((most_index/4)));
                nowp = ResultToPosition[predict_location];
                route();
                if(predict_location<26){
                }
            }
        });
        return max_index;
    }

    // 載入ModelFile----------------------------------------------------------------------------------------------------------------------------
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    //===============================================================================================
    //QR code掃描------------------------------------------------------------------------------------------------------------------------------
    //QR code掃描------------------------------------------------------------------------------------------------------------------------------
    public void createQRScanDialog(){
        Intent intent = new Intent(getActivity(), QRScanDialogFragment.class);
        startActivity(intent);
    }


    //路徑規劃-----------------------------------------------------------------------------------------------------------------------------------
    private void route() {

        if(blescanning) {
            destinationX = destination / 100;
            destinationY = destination % 100;
            nowpX = nowp / 100;
            nowpY = nowp % 100;
            if (pre_nowp != nowp && nowp > 300 && nowp <= 1501) {
                pre_nowp = nowp;
            }
            temp = nowp;
            tempX = nowpX;
            tempY = nowpY;
            route_arr.clear();
            // 判斷路徑規則 先判斷 Y 再判斷 X
            route_arr.add(temp);
            while (tempY < 2) {
                tempY++;
                temp = (tempX * 100) + tempY;
                route_arr.add(temp);
            }
            while (temp != destination) {
                Log.d("SearchFragment", "" + temp);
        //            if ((destinationY - tempY) > 0){
        //                tempY++;
        //            }
        //            else if((destinationY - tempY) == 0){
                do {
                    if ((destinationX - tempX) > 0) {
                        if (tempY >= 0 && tempY <= 1) {
                            tempY++;

                        } else {
                            tempX++;
                        }
                    } else if ((destinationX - tempX) == 0) {
                        if (tempY != destinationY) {
                            tempY--;
                        }
                    } else if ((destinationX - tempX) < 0) {
                        if (tempY >= 0 && tempY <= 1) {
                            tempY++;
                        } else {
                            tempX--;
                        }
                    }
                    temp = (tempX * 100) + tempY;
                    route_arr.add(temp);
                } while ((destinationY - tempY) != 0);
        //            }
        //            else if ((destinationY - nowpY) < 0){
        //                tempY--;
        //            }
                temp = (tempX * 100) + tempY;
                route_arr.add(temp);
                Log.d("route", "temp = " + temp);
            }
            m_gesture_imageview.assignArray(route_arr);//更新路徑點
        }
        else{
            route_arr.clear();
            route_arr.add(destination);
            m_gesture_imageview.assignArray(route_arr);
        }

    }

    //取得資料-----------------------------------------------------------------------------------------------------------------------------------
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
                    case API_ID_1_GET_CATEGORY:
                        parserJson_catagory(response);
                        if(!view_inited){
                            get_random4();
                        }
                        break;
                    case API_ID_2_GET_SUBCATEGORY:
                        parserJson_items(response);
                        break;
                    case API_ID_3_GET_RANDOM_4ITEMS:
                        parserJson_random4(response);
                        if(!view_inited){
                            view_inited=true;
                            get_random4();
                        }
                        break;
                    case API_ID_8_GET_BEACON:
                        parserJson_beacon(response);
                        break;
                }
            } catch (JSONException e) {
//                parserJson_beacon(response);
                e.printStackTrace();
            }
        }
    }
    private void get_category() {
        Log.d("SearchFragment","get_category()");
        String urlParkingArea = "http://140.129.25.75:8000/api/categories/";
        new TransTask().execute(urlParkingArea);
    }

    private void get_items(final String parms) {
        Log.d("SearchFragment","get_items()");
        String urlParkingArea = "http://140.129.25.75:8000/api/categories/"+parms;
        new TransTask().execute(urlParkingArea);
    }

    private void get_random4() {
        Log.d("SearchFragment","get_random4()");
        String urlParkingArea = "http://140.129.25.75:8000/api/category-activities?random=4";
        new TransTask().execute(urlParkingArea);
    }

    private void get_random4(int cat_id) {
        Log.d("SearchFragment","get_random4()");
        String urlParkingArea = "http://140.129.25.75:8000/api/category-activities?random=4&&category_id="+cat_id;
        new TransTask().execute(urlParkingArea);
    }

    private void get_beacon(final String parms) {
        Log.d("SearchFragment","get_beacon()");
        String urlParkingArea = "http://140.129.25.75:8000/api/beacons/"+parms;
        new TransTask().execute(urlParkingArea);
    }

    private void parserJson_catagory(JSONObject cat_list) {
        Log.d("SearchFragment","parserJson_catagory()");
        int cat_num;
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", cat_list.toString());
            JSONArray c = cat_list.getJSONArray("data");
            cat_num=c.length();
            m_catagory_list = new String [cat_num];
            m_subcatagory_jsonarray_list = new JSONArray[cat_num];
            for (int i = 0; i < cat_num; i++) {
                JSONObject o = c.getJSONObject(i);
                try {
//                    get_items(String.valueOf(i+1));
                    m_catagory_list[i] = o.getString("name");
                    m_subcatagory_jsonarray_list[i]=o.getJSONArray("subcategories");
                }catch(JSONException z){
                    z.printStackTrace();
                }
            }
            //Goods List ---------------------------------------------------------------------------------------------------------------------------
            ListAdapter adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1 ,m_catagory_list);
            m_list_big_item.setAdapter(adapter);
            m_list_big_item.setOnItemClickListener(onClickListViewbig);
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get catagory failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }

    private void parserJson_items(JSONObject subcat_list) {
        Log.d("SearchFragment","parserJson_items()");
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", subcat_list.toString());
            JSONArray a = subcat_list.getJSONArray("data");
            JSONArray c = a.getJSONObject(0).getJSONArray("subcategories");
            int cate_id = a.getJSONObject(0).getInt("id");
//            m_subcatagory_list[cate_id]=c;
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get items failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }

    private void parserJson_random4(JSONObject random4item_list){
        Log.d("SearchFragment","parserJson_random4()");
        try{
            JSONArray dataArray = random4item_list.getJSONArray("data");
            String home_url[] = new String[4];
            String home_text[] = new String[4];
            String home_beacon[] = new String[4];
            for(int i = 0; i < 4; i++){
                home_url[i] = dataArray.getJSONObject(i).getString("image_url");
                home_text[i] = dataArray.getJSONObject(i).getString("name");
                home_beacon[i] = dataArray.getJSONObject(i).getJSONObject("category").getJSONArray("beacons").getJSONObject(0).getString("name");
                downloadimage_item(home_url[i],home_text[i],home_beacon[i]);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void parserJson_beacon(JSONObject beacon_list) {
        Log.d("SearchFragment","parserJson_beacon()");
        try {
            //JSONArray vendor_list = jsonObject;
            Log.d("Response", beacon_list.toString());
            JSONArray a = beacon_list.getJSONArray("categories");
            int cate_id = a.getJSONObject(0).getInt("id");
            get_random4(cate_id);
        }
        catch(JSONException e)
        {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(),"Get items failed!!", duration);
            toast.show();
            e.printStackTrace();
        }
    }

    private Response.Listener m_response_listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            try {
                int api_id = response.getInt("api_id");
                Log.d("api_id",""+api_id);
                switch (api_id){
                    case API_ID_1_GET_CATEGORY:
                        parserJson_catagory(response);
                        if(!view_inited){
                            get_random4();
                        }
                        break;
                    case API_ID_2_GET_SUBCATEGORY:
                        parserJson_items(response);
                        break;
                    case API_ID_3_GET_RANDOM_4ITEMS:
                        parserJson_random4(response);
                        if(!view_inited){
                            view_inited=true;
                            get_random4();
                        }
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    //回傳資料-----------------------------------------------------------------------------------------------------------------------------------
    class POST_position_id_TransTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String...params){
            try {
                URL url = new URL("http://140.129.25.75:8000/api/position-records");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type","application/json");

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                String output_json = "{\"beacon_id\":"+params[0]+"}";
                out.write(output_json.getBytes("UTF-8"));//
                out.flush();
                out.close();
                Log.d("POST_TrasTask",output_json);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                line = in.readLine();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected  void onPostExecute(Void S){
            super.onPreExecute();

        }
    }
    class POST_item_id_TransTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String...params){
            try {
                URL url = new URL("http://140.129.25.75:8000/api/subcategory-counters");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type","application/json");

                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                String output_json = "{\"subcategory_id\":"+params[0]+"}";
                out.write(output_json.getBytes("UTF-8"));//
                out.flush();
                out.close();
                Log.d("POST_TrasTask",output_json);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                line = in.readLine();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected  void onPostExecute(Void S){
            super.onPreExecute();

        }
    }

    private AdapterView.OnItemClickListener onClickListViewbig = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (list_layer_index){
                //-----------------------------------------------------------------------------------------------------------------------------
                case LAYER_0_CATEGORY:

                    JSONArray temp_jsonarr = m_subcatagory_jsonarray_list[position];
                    m_subcatagory_list = new String[temp_jsonarr.length()];
                    m_subcatagory_location_list = new String[temp_jsonarr.length()];
                    for (int i = 0; i < temp_jsonarr.length(); i++) {
                        try{
                            JSONObject o = temp_jsonarr.getJSONObject(i);
                            m_subcatagory_list[i]=o.getString("name");
                            m_subcatagory_location_list[i]=o.getJSONObject("belong_category_first_beacon").getString("name");
                        }catch (JSONException zz) {
                            m_subcatagory_location_list[i]="0000";
                            zz.printStackTrace();
                        }
                    }
                    m_tv_list.setText(m_catagory_list[position]);
                    ListAdapter adapter = new ArrayAdapter<String>(mActivity , android.R.layout.simple_list_item_checked ,m_subcatagory_list);
                    m_list_big_item.setAdapter(adapter);
                    m_list_big_item.setOnItemClickListener(onClickListViewbig);

                    list_layer_index = LAYER_1_SUBCATEGORY;

                    //Goods List ---------------------------------------------------------------------------------------------------------------------------

                    break;
                //-----------------------------------------------------------------------------------------------------------------------------
                case LAYER_1_SUBCATEGORY:
                    m_tv_title.setText("目標商品： " + m_subcatagory_list[position]);
                    destination =Integer.parseInt(m_subcatagory_location_list[position]);
                    Log.d("destination","destination"+destination);
                    BR=0;
                    route();
//                    jsonmode= 3;//推薦商品
//                    new TransTask().execute
//                            ("http://140.129.25.75:8000/api/category-activities?random=4");
                    break;
                //-----------------------------------------------------------------------------------------------------------------------------
                default:break;
            }

        }
    };

    //下載圖片-----------------------------------------------------------------------------------------------------------------------------------
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
                Log.d("home","done");
//                addcommercialimage(result);
                super.onPostExecute(result);
            }
        }.execute(URLimageI0);
    }

    private void downloadimage_item (String URLimageI0,String ItemName,String BeaconName){
        new AsyncTask<String, Void, Recommand_Item>() {
            @Override
            protected Recommand_Item doInBackground(String... params) {
                String url = params[0];
                String item = params[1];
                String location = params[2];
                Log.d("downloadimage_item","start");
                return getBitmapFromURL_Update(url,item,location);
            }
            @Override
            protected void onPostExecute(Recommand_Item result) {
                Log.d("downloadimage_item","done");
                m_recommand_itemlist.add(result);
                super.onPostExecute(result);
            }
        }.execute(URLimageI0,ItemName,BeaconName);
    }

    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }    //讀取網路圖片，型態為Bitmap

    private static Recommand_Item getBitmapFromURL_Update(String imageUrl,String itemName,String itemLocation) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Recommand_Item rim = new Recommand_Item();
            rim.Item_name = itemName;
            rim.Item_bitmap = BitmapFactory.decodeStream(input);
            rim.Item_location = itemLocation;
            return rim;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }    //讀取網路圖片，型態為Bitmap
    //推播圖片-----------------------------------------------------------------------------------------------------------------------------------
    public void createItemInfoDialog(Bitmap bmp,String title ,String describe, int location){
        ItemInfoDialogFragment IID = ItemInfoDialogFragment.newInstance(bmp,title,describe,location);
//        IID.show(getFragmentManager(),"???");
        IID.setTargetFragment(this, INFO_DIALOG_SHOW_LOCATION);
        IID.show(getFragmentManager().beginTransaction(), "Search_InfoDialog");
    }

    private void addflipper(final Bitmap Img1, final String Text1, final String Location1,
                            final Bitmap Img2, final String Text2, final String Location2,
                            final Bitmap Img3, final String Text3, final String Location3,
                            final Bitmap Img4, final String Text4, final String Location4){
        View comm_view = (View) view.inflate(mActivity,R.layout.commview_item,null);

        //--------------------------------------------------------------------------------------------------------------------------
        LinearLayout linearlayout1 = comm_view.findViewById(R.id.cvi_linearlayout1);
        linearlayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createItemInfoDialog(Img1,Text1,"",Integer.parseInt(Location1));
            }
        });
        ImageView iv_view1 = comm_view.findViewById(R.id.cvi_image1);
        TextView tv_view1 = comm_view.findViewById(R.id.cvi_text1);
        //--------------------------------------------------------------------------------------------------------------------------
        LinearLayout linearlayout2 = comm_view.findViewById(R.id.cvi_linearlayout2);
        linearlayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createItemInfoDialog(Img2,Text2,"",Integer.parseInt(Location2));
            }
        });
        ImageView iv_view2 = comm_view.findViewById(R.id.cvi_image2);
        TextView tv_view2 = comm_view.findViewById(R.id.cvi_text2);
        //--------------------------------------------------------------------------------------------------------------------------
        LinearLayout linearlayout3 = comm_view.findViewById(R.id.cvi_linearlayout3);
        linearlayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createItemInfoDialog(Img3,Text3,"",Integer.parseInt(Location3));
            }
        });
        ImageView iv_view3 = comm_view.findViewById(R.id.cvi_image3);
        TextView tv_view3 = comm_view.findViewById(R.id.cvi_text3);
        //--------------------------------------------------------------------------------------------------------------------------
        LinearLayout linearlayout4 = comm_view.findViewById(R.id.cvi_linearlayout4);
        linearlayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createItemInfoDialog(Img4,Text4,"",Integer.parseInt(Location4));
            }
        });
        ImageView iv_view4 = comm_view.findViewById(R.id.cvi_image4);
        TextView tv_view4 = comm_view.findViewById(R.id.cvi_text4);
        iv_view1.setImageBitmap(Img1);
        tv_view1.setText(Text1);
        iv_view2.setImageBitmap(Img2);
        tv_view2.setText(Text2);
        iv_view3.setImageBitmap(Img3);
        tv_view3.setText(Text3);
        iv_view4.setImageBitmap(Img4);
        tv_view4.setText(Text4);
        m_commercial_flipper.addView(comm_view);

        m_commercial_flipper.setInAnimation(mActivity,R.anim.marquee_enter_up_slow);
        m_commercial_flipper.setOutAnimation(mActivity,R.anim.marquee_exit_up_slow);
        m_commercial_flipper.startFlipping();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INFO_DIALOG_SHOW_LOCATION:

                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    destination = bundle.getInt("location",0);

                    m_tv_title.setText("目標商品： " + bundle.getString("name"));
                    Log.d("destination","destination"+destination);
                    BR=0;
                    route();
                } else if (resultCode == Activity.RESULT_CANCELED) {

                }
                break;
        }
        Log.d("requestCode:",String.valueOf(requestCode));
    }
    //===============================================================================================
    private void startBleScan() {
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();

        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (mIsBluetoothOn && mIsBluetoothLePresent) {
            mScanner.scanLeDevice(-1, true);
        }
    }
    //===============================================================================================
    public class MainPagerAdapter extends PagerAdapter
    {
        private ArrayList<View> views = new ArrayList<View>();
        @Override
        public int getItemPosition (Object object)
        {
            int index = views.indexOf (object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }
        @Override
        public Object instantiateItem (ViewGroup container, int position)
        {
            View v = views.get (position);
            container.addView (v);
            return v;
        }
        @Override
        public void destroyItem (ViewGroup container, int position, Object object)
        {
            container.removeView (views.get (position));
        }
        @Override
        public int getCount ()
        {
            return views.size();
        }

        @Override
        public boolean isViewFromObject (View view, Object object)
        {
            return view == object;
        }

        public int addView (View v)
        {
            return addView (v, views.size());
        }

        public int addView (View v, int position)
        {
            views.add (position, v);
            return position;
        }

        public int removeView (ViewPager pager, View v)
        {
            return removeView (pager, views.indexOf (v));
        }

        public int removeView (ViewPager pager, int position)
        {
            pager.setAdapter (null);
            views.remove (position);
            pager.setAdapter (this);

            return position;
        }

        public View getView (int position)
        {
            return views.get (position);
        }



    }


}
