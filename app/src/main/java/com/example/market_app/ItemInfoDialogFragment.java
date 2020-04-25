package com.example.market_app;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;


public class ItemInfoDialogFragment extends DialogFragment {

    TextView tv_title,tv_describe;
    ImageView img_item;
    Button btn_close,btn_function;

    Bitmap bmp;
    String Title;
    String Price;
    String Describe;
    int location;

    public static ItemInfoDialogFragment newInstance(Bitmap bm, String Title, String Price, String Describe, int location) {
        ItemInfoDialogFragment f = new ItemInfoDialogFragment();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putByteArray("Image",byteArray);
        args.putString("Title",Title);
        args.putString("Price",Price);
        args.putString("Describe",Describe);
        args.putInt("Location",location);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        byte[] byteArray = getArguments().getByteArray("Image");
        bmp = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        Title = getArguments().getString("Title");
        Price = getArguments().getString("Price");
        Describe = getArguments().getString("Describe");
        location = getArguments().getInt("Location");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View m_dialog = inflater.inflate(R.layout.fragment_item_info_dialog, container, false);

        btn_close = (Button) m_dialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_function = (Button) m_dialog.findViewById(R.id.btn_function);
        btn_function.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent()
                        .putExtra("location", location)
                        .putExtra("name",Title);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                dismiss();
            }
        });
        img_item = (ImageView) m_dialog.findViewById(R.id.img_item);
        img_item.setImageBitmap(bmp);
        tv_title = (TextView) m_dialog.findViewById(R.id.tv_title);
        tv_title.setText(Title + "\n特惠價: $"+Price);
        tv_describe = (TextView) m_dialog.findViewById(R.id.tv_describe);
        tv_describe.setText(Describe);

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
