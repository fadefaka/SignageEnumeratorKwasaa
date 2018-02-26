package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SummaryReportActivity extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/MobileAppWSTypeTeamA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "OS-MONTHLYINCOME";
    private static String responseJSON;
    private static String IsExec;
    ListView lstsummaryitems;
    Button BtnCancel;
    String getmyuserFK;
    String getmyuserTOKEN;
    private static String neededDesc;
    private static String neededDescription;
    Integer sItemPosition;
    private JSONArray arrayresultforsummaryitems;
    private ArrayList<String> summaryitemarraylist;
    private ArrayList<HashMap<String, String>> list;

    ProgressDialog progressDialog;

    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);
        summaryitemarraylist = new ArrayList<String>();

        lstsummaryitems =(ListView)findViewById(R.id.lstsummaryitems);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        Intent intent = getIntent();
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        IsExec = sharedPref.getString("IsExec","0");
        JSON_Get_ProjectKeyFacts task = new JSON_Get_ProjectKeyFacts();
        task.execute();
        lstsummaryitems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
                //pg.setVisibility(View.GONE);
                progressDialog.cancel();
                LayoutInflater inflater= LayoutInflater.from(SummaryReportActivity.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(neededDesc);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SummaryReportActivity.this);
                alertDialog.setTitle("Description");
                alertDialog.setIcon(R.mipmap.ic_launcher);
//alertDialog.setMessage("Here is a really long message.");
                alertDialog.setView(view);
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();


            }
        });

        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(SummaryReportActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(SummaryReportActivity.this).toBundle());
            }


        });
    }
    private class JSON_Get_ProjectKeyFacts extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS(getmyuserTOKEN,"JSON_Get_ProjectKeyFacts");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            progressDialog.cancel();
            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultforsummaryitems = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getMonthlyIncome(arrayresultforsummaryitems);
                //pg.setVisibility(View.GONE);

                if (arrayresultforsummaryitems.length()<=0){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SummaryReportActivity.this);

                    dialog.setTitle( "Montlhy Income" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("No Record to Display")
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(SummaryReportActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS(String usertoken, String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        // Set Name
        paramPI.setName("Token");
        // Set Value
        paramPI.setValue(getmyuserFK);
        // Set dataType
        paramPI.setType(String.class);
        // Add the property to request object
        request.addProperty(paramPI);
        PropertyInfo paramPI2 = new PropertyInfo();
        // Set Name
        paramPI2.setName("islandingPageItem");
        // Set Value
        paramPI2.setValue(0);
        // Set dataType
        paramPI2.setType(Integer.class);
        // Add the property to request object
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        // Set Name
        paramPI3.setName("IsExec");
        // Set Value
        paramPI3.setValue(Integer.valueOf(IsExec));
        // Set dataType
        paramPI3.setType(Integer.class);
        // Add the property to request object
        request.addProperty(paramPI3);
        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";
        }
    }
    private void getMonthlyIncome(JSONArray j){
        //Traversing through all the items in the json array
        //summaryitemarraylist.clear();
        list=new ArrayList<HashMap<String,String>>();
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                //summaryitemarraylist.add(json.getString("Name"));
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN, json.getString("Name"));
                Locale locale = new Locale("en", "NG");
                NumberFormat formatter = NumberFormat.getInstance(locale);
                
                if (android.text.TextUtils.isDigitsOnly(json.getString("Value"))){
                    temp.put(SECOND_COLUMN, (formatter.format(Integer.valueOf(json.getString("Value")))).replace(".00",""));
                    
                }else{
                    temp.put(SECOND_COLUMN, json.getString("Value"));
                }
                
                
                list.add(temp);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getBaseContext(), String.valueOf(list.size())+ " Item(s)", Toast.LENGTH_LONG).show();
        ListViewAdapters arrayAdapter=new ListViewAdapters(this, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    //view.setBackgroundColor(Color.parseColor("#EAEDED"));

                }
                else
                {
                    // Set the background color for alternate row/item
                    //view.setBackgroundColor(Color.parseColor("#D5DBDB"));
                }
                return view;
            }
        };
        //ListViewAdapters adapter=new ListViewAdapters(this, list);
        lstsummaryitems.setAdapter(arrayAdapter);

        // );
    }

    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultforsummaryitems.getJSONObject(position);
            //Fetching name from that object
            neededDesc = json.getString("Description");
            neededDescription = json.getString("Description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }
}