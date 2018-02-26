package com.biscom.signageenumeratorekiti;

/**
 * Created by biscomtech on 5/13/17.
 */



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ListViewAdapters extends BaseAdapter implements Filterable {
    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    public ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    public ListViewAdapters(Activity activity,ArrayList<HashMap<String, String>> list){
        super();
        this.activity=activity;
        this.list=list;
        this.filteredData = list ;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return list.size();
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //return list.get(position);
        return filteredData.get(position);
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
        HashMap<String, String> map=filteredData.get(position);
        txtFirst.setText(map.get(FIRST_COLUMN));
        txtSecond.setText(map.get(SECOND_COLUMN));

        return convertView;
    }
        public Filter getFilter() {
            return mFilter;
        }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<HashMap<String, String>> list2 = list;

            int count = list2.size();
            final ArrayList<HashMap<String, String>> nlist = new ArrayList<HashMap<String, String>>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                //filterableString = list2.get(i);
                if (list2.get(i).toString().toLowerCase().contains(filterString)) {
                    nlist.add(list2.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<HashMap<String, String>>) results.values;
            notifyDataSetChanged();
        }

    }
//    // Filter Class
//    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//        list.clear();
//        if (charText.length() == 0) {
//            list.addAll(list);
//        } else {
//            for (list wp : list) {
//                if (wp.getCountry().toLowerCase(Locale.getDefault())
//                        .contains(charText)) {
//                    worldpopulationlist.add(wp);
//                }
//            }
//        }
//        notifyDataSetChanged();
//    }
}
