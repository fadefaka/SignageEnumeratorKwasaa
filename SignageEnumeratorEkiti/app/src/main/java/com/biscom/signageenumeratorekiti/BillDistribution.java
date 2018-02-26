package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.HashMap;

public class BillDistribution extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/MobileAppWSTypeTeamA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "OS-MONTHLYINCOME";
    private static String responseJSON;
    ListView lstreport;
    TextView txtTotal, txtTotal2, txtTotal3;
    Button BtnCancel;
    String getmyuserFK;
    String getmyuserTOKEN;
    Integer TotalValue, TotalValue2, TotalValue3;
    private static String neededDesc;
    private static String neededDescription;
    Integer sItemPosition;
    private JSONArray arrayresultformanifestreport;
    private ArrayList<String> manifestreportarraylist;
    private ArrayList<HashMap<String, String>> list;

    ProgressBar pg;
    ProgressDialog progressDialog;

    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";
    public static final String THIRD_COLUMN="Third";
    public static final String FOURTH_COLUMN="Fourth";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_distribution);
        manifestreportarraylist = new ArrayList<String>();
        txtTotal =(TextView)findViewById(R.id.totalvalue);
        txtTotal2 =(TextView)findViewById(R.id.totalvalue2);
        txtTotal3 =(TextView)findViewById(R.id.totalvalue3);
        lstreport =(ListView)findViewById(R.id.lstreport);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        Intent intent = getIntent();
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        pg = (ProgressBar) findViewById(R.id.progressBar1);
        JSON_Get_WeeklyBillDistributionInLastxWeeks task = new JSON_Get_WeeklyBillDistributionInLastxWeeks();
        task.execute();
        lstreport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
                //pg.setVisibility(View.GONE);
                progressDialog.hide();
                LayoutInflater inflater= LayoutInflater.from(BillDistribution.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(neededDesc);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BillDistribution.this);
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
                Intent myintent = new Intent(BillDistribution.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(BillDistribution.this).toBundle());
            }


        });
    }
    private class JSON_Get_WeeklyBillDistributionInLastxWeeks extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS(getmyuserTOKEN,"JSON_Get_WeeklyBillDistributionInLastxWeeks");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("responseJSON", responseJSON);
            progressDialog.hide();
            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultformanifestreport = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getMonthlyIncome(arrayresultformanifestreport);
                //pg.setVisibility(View.GONE);

                if (arrayresultformanifestreport.length()<=0){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BillDistribution.this);

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
            progressDialog = MyCustomProgressDialog.ctor(BillDistribution.this);
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
        paramPI2.setName("Xweeks");
        // Set Value
        paramPI2.setValue(52);
        // Set dataType
        paramPI2.setType(Integer.class);
        // Add the property to request object
        request.addProperty(paramPI2);

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
        //weeklyreportarraylist.clear();
        list=new ArrayList<HashMap<String,String>>();
        TotalValue=0;TotalValue2=0;TotalValue3=0;
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                //weeklyreportarraylist.add(json.getString("Name"));
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN, json.getString("Name"));
                temp.put(SECOND_COLUMN, json.getString("Value1"));
                temp.put(THIRD_COLUMN, json.getString("Value2"));
                temp.put(FOURTH_COLUMN, json.getString("Value3"));
                TotalValue=TotalValue+Integer.valueOf(json.getString("Value1"));
                TotalValue2=TotalValue2+Integer.valueOf(json.getString("Value2"));
                TotalValue3=TotalValue3+Integer.valueOf(json.getString("Value3"));

                list.add(temp);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        txtTotal.setText(""+TotalValue);
        txtTotal2.setText(""+TotalValue2);
        txtTotal3.setText(""+TotalValue3);
        Toast.makeText(getBaseContext(), String.valueOf(list.size())+ " Item(s) in the last 52 Weeks", Toast.LENGTH_LONG).show();
        ListViewManifestAdapters arrayAdapter=new ListViewManifestAdapters(this, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#EAEDED"));

                }
                else
                {
                    // Set the background color for alternate row/item
                    view.setBackgroundColor(Color.parseColor("#D5DBDB"));
                }
                return view;
            }
        };
        //ListViewAdapters adapter=new ListViewAdapters(this, list);
        lstreport.setAdapter(arrayAdapter);

        // );
    }

    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultformanifestreport.getJSONObject(position);
            //Fetching name from that object
            neededDesc = json.getString("Description").replace("\\n","\n");
            neededDescription = json.getString("Description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }
}