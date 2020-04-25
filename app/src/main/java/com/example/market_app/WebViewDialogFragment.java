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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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


public class WebViewDialogFragment extends DialogFragment {


    String WebUrl;
    Button btn_close;
    WebView m_webview;
    public static WebViewDialogFragment newInstance(String url) {
        WebViewDialogFragment f = new WebViewDialogFragment();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("Url",url);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        WebUrl = getArguments().getString("Url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View m_dialog = inflater.inflate(R.layout.fragment_web_view_dialog, container, false);

        btn_close = (Button) m_dialog.findViewById(R.id.btn_close_web);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        m_webview = (WebView) m_dialog.findViewById(R.id.web_view);
        WebSettings webSettings = m_webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        setContentView(m_webview);
        m_webview.setWebViewClient(new WebViewClient());
        m_webview.loadUrl(WebUrl);
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



}
