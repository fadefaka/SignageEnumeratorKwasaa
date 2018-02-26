package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Timer;

import static java.lang.Integer.parseInt;

public class MapRecordSelectActivity extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RS";
    private Timer timer;
    private static String responseJSON;
    private static Integer LGAfk;
    private static Integer StructureZoneFK;
    Spinner LGASpinnerCtrl;
    Spinner StructureZoneSpinnerCtrl;
    Button BtnProceed;
    Button BtnCancel;
    //JSON Array
    private JSONArray arrayresultforLGA;
    private JSONArray arrayresultforStructureZones;
    //An ArrayList for Spinner Items
    private ArrayList<String> lgaarraylist;
    private ArrayList<String> StructureZonearraylist;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_record_select);
        //Initializing the ArrayList
        StructureZonearraylist = new ArrayList<String>();
        lgaarraylist = new ArrayList<String>();
        LGASpinnerCtrl = (Spinner) findViewById(R.id.LGASpinner);
        StructureZoneSpinnerCtrl = (Spinner) findViewById(R.id.SzSpinner);
        BtnProceed = (Button) findViewById(R.id.btnsignIn);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        //AysnTask class to handle WS call as separate UI Thread
        JSON_ListOfLGA task = new JSON_ListOfLGA();
        task.execute();
        BtnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(MapRecordSelectActivity.this, MapRecordListActivity.class);

                final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("MAP_StructureZoneFK", StructureZoneFK);
                editor.putString("MAP_StructureZone", StructureZoneSpinnerCtrl.getSelectedItem().toString());
                editor.commit();

                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MapRecordSelectActivity.this).toBundle());
            }


        });
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(MapRecordSelectActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MapRecordSelectActivity.this).toBundle());
            }
        });
//Project Types Means ProjectCategory
        LGASpinnerCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            //When an item is selected from Country Spinner Control
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                //Get the selected  item value
                LGAfk = getLGAFK(LGASpinnerCtrl.getSelectedItemPosition());
                JSON_ListOfStructureZones_ByLGA_FK task = new JSON_ListOfStructureZones_ByLGA_FK();
                //Execute the task
                task.execute(LGAfk);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
//Projects Means Work Type
        StructureZoneSpinnerCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            //When an item is selected from Country Spinner Control
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3){
                // TODO Auto-generated method stub
                //Get the selected  item value
                StructureZoneFK = getStructureZoneFK(StructureZoneSpinnerCtrl.getSelectedItemPosition());

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


    }//End on On create


    //AysnTask class to handle Country WS call as separate UI Thread
    private class JSON_ListOfLGA extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            invokeJSONWS("0","JSON_ListOfLGA");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultforLGA = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getLGAs(arrayresultforLGA);
                //pg.setVisibility(View.INVISIBLE);
                progressDialog.cancel();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(MapRecordSelectActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    //AysnTask class to handle Country WS call as separate UI Thread
    private class JSON_ListOfStructureZones_ByLGA_FK extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            invokeSpin2WS(params[0],"JSON_ListOfStructureZones_ByLGA_FK");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");

            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                Log.i(TAG, responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultforStructureZones = j.getJSONArray("myJresult");
                getStructureZones(arrayresultforStructureZones);
                progressDialog.cancel();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(MapRecordSelectActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }

    //Method which invoke web methods
    public void invokeJSONWS(String usertoken, String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);

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
        }
    }
    public void invokeSpin2WS(Integer LGA_Fk, String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        // Set Name
        paramPI.setName("LGA_FK");
        // Set Value
        paramPI.setValue(LGA_Fk);
        // Set dataType
        paramPI.setType(Integer.class);
        // Add the property to request object
        request.addProperty(paramPI);
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
        }
    }
    
    private void getLGAs(JSONArray j){
        //Traversing through all the items in the json array
        lgaarraylist.clear();
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);

                //Adding the name of the student to array list
                lgaarraylist.add(json.getString("Name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        LGASpinnerCtrl.setAdapter(new ArrayAdapter<String>(MapRecordSelectActivity.this, android.R.layout.simple_spinner_dropdown_item, lgaarraylist));
    }

    private void getStructureZones(JSONArray j){
        //Traversing through all the items in the json array
        StructureZonearraylist.clear();
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                StructureZonearraylist.add(json.getString("StructureZone"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        StructureZoneSpinnerCtrl.setAdapter(new ArrayAdapter<String>(MapRecordSelectActivity.this, android.R.layout.simple_spinner_dropdown_item, StructureZonearraylist));
    }

    private int  getLGAFK(int position){
        int FK=0;
        try {
            //Getting object of given index
            JSONObject json =arrayresultforLGA.getJSONObject(position);
            //Fetching name from that object
            FK = json.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return FK;
    }

    private int getStructureZoneFK(int position){
        int FK=0;
        try {
            //Getting object of given index
            JSONObject json =arrayresultforStructureZones.getJSONObject(position);
            //Fetching name from that object
            FK = json.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return FK;
    }



}
