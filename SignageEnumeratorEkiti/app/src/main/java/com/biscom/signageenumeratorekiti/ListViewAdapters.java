package com.biscom.signageenumeratorekiti;

/**
 * Created by biscomtech on 5/13/17.
 */



import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdapters extends BaseAdapter{
    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    public ListViewAdapters(Activity activity,ArrayList<HashMap<String, String>> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub


        LayoutInflater inflater=activity.getLayoutInflater();


        if(convertView == null){

            convertView=inflater.inflate(R.layout.colmn_row, null);


        }
        txtFirst=(TextView) convertView.findViewById(R.id.name);
        txtSecond=(TextView) convertView.findViewById(R.id.value);
        HashMap<String, String> map=list.get(position);
        txtFirst.setText(map.get(FIRST_COLUMN));
        txtSecond.setText(map.get(SECOND_COLUMN));

        return convertView;
    }

}
