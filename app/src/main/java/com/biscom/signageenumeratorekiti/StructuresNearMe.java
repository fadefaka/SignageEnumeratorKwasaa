package com.biscom.signageenumeratorekiti;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
import java.util.Timer;

public class StructuresNearMe extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RS";
    private Timer timer;
    private static String responseJSON;
    private static Integer BillValueFK, DistanceValueFK, ManifestStatusFK;
    Spinner  ManifestStatusSpinnerCtrl, billValueCtrl, distanceCtrl;
    Button BtnProceed, BtnCancel;
    //JSON Array
    private JSONArray  arrayresultforManifestStatus;
    //An ArrayList for Spinner Items
    private ArrayList<String> lgaarraylist, StructureZonearraylist, manifeststatusarraylist;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structures_near_me);
        //Initializing the ArrayList

        StructureZonearraylist = new ArrayList<String>();
        lgaarraylist = new ArrayList<String>();
        manifeststatusarraylist = new ArrayList<String>();

        ManifestStatusSpinnerCtrl = (Spinner) findViewById(R.id.manifestStatus);
        billValueCtrl = (Spinner) findViewById(R.id.billValue);
        distanceCtrl = (Spinner) findViewById(R.id.distance);

        BtnProceed = (Button) findViewById(R.id.btnsignIn);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        JSON_ListOfManifestStatus task = new JSON_ListOfManifestStatus();
        task.execute();

        String [] billValue = {"All","Above 25K","Above 50K","Above 100K"};
        String [] distanceValue = {"50","100","200","300","400","500","600","700","800","900","1000"};

        ArrayAdapter<String> billValueArray = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, billValue);
        billValueArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        billValueCtrl.setAdapter(billValueArray);

        ArrayAdapter<String> distanceArray = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, distanceValue);
        distanceArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceCtrl.setAdapter(distanceArray);


        //AysnTask class to handle WS call as separate UI Thread


        BtnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(StructuresNearMe.this, StructuresNearMeListActivity.class);
                String selected = ManifestStatusSpinnerCtrl.getSelectedItem().toString()+" Structures at "+distanceCtrl.getSelectedItem().toString()+" : "+billValueCtrl.getSelectedItem().toString();
                final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorosams.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("MAP_ManifestStatusFK", ManifestStatusFK);
                editor.putInt("MAP_BillValueFK", BillValueFK);
                editor.putInt("MAP_DistanceValueFK", DistanceValueFK);
                editor.putString("MAP_Selected", selected);
                editor.commit();
               startActivity(myintent);
            }


        });
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(StructuresNearMe.this, MenuActivity.class);
                startActivity(myintent);
            }
        });

//Manifest Status

        ManifestStatusSpinnerCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ManifestStatusFK = getManifestStatusFK(ManifestStatusSpinnerCtrl.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        billValueCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BillValueFK = position;
                //      Toast.makeText(getBaseContext(), position+" clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        distanceCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DistanceValueFK = Integer.parseInt(distanceCtrl.getSelectedItem().toString());
                //    Toast.makeText(getBaseContext(), DistanceValueFK+" clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }//End on On create


    //AysnTask class to handle Country WS call as separate UI Thread

    private class JSON_ListOfManifestStatus extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            invokeSpin3WS("0","JSON_ListOfManifestStatus");
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
                arrayresultforManifestStatus = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getManifestStatus(arrayresultforManifestStatus);
                //pg.setVisibility(View.INVISIBLE);
                progressDialog.hide();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(StructuresNearMe.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }


   public void invokeSpin3WS(String usertoken, String methName) {
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
    

    private void getManifestStatus(JSONArray j){
        //Traversing through all the items in the json array
        manifeststatusarraylist.clear();
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);

                //Adding the name of the manifeststatusarraylist to array list
                manifeststatusarraylist.add(json.getString("Name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Setting adapter to show the items in the spinner
        ManifestStatusSpinnerCtrl.setAdapter(new ArrayAdapter<String>(StructuresNearMe.this, android.R.layout.simple_spinner_dropdown_item, manifeststatusarraylist));
    }

    private int getManifestStatusFK(int position){
        int FK=0;
        try {
            //Getting object of given index
            JSONObject json =arrayresultforManifestStatus.getJSONObject(position);
            //Fetching name from that object
            FK = json.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name
        return FK;
    }



}
