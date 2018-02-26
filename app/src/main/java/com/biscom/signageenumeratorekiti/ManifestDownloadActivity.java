package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

public class ManifestDownloadActivity extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RS";
    private Timer timer;
    private static String responseJSON;
    private static Integer LGAfk, StructureZoneFK,  ManifestStatusFK,BillYear;
    private View parent_view;
    Spinner LGASpinnerCtrl, StructureZoneSpinnerCtrl, ManifestStatusSpinnerCtrl;
    Button BtnProceed, BtnCancel;
    //JSON Array
    private JSONArray arrayresultforLGA, arrayresultforStructureZones, arrayresultforManifestStatus,arrayresultforManifestRecord;
    //An ArrayList for Spinner Items
    private ArrayList<String> lgaarraylist, StructureZonearraylist, manifeststatusarraylist;
    ProgressDialog progressDialog;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manifest_download);
        parent_view = findViewById(android.R.id.content);
        //Initializing the ArrayList

        StructureZonearraylist = new ArrayList<String>();
        lgaarraylist = new ArrayList<String>();
        manifeststatusarraylist = new ArrayList<String>();

        LGASpinnerCtrl = (Spinner) findViewById(R.id.LGASpinner);
        StructureZoneSpinnerCtrl = (Spinner) findViewById(R.id.SzSpinner);
        ManifestStatusSpinnerCtrl = (Spinner) findViewById(R.id.manifestStatus);


        BtnProceed = (Button) findViewById(R.id.btnsignIn);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        try {
            db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS BillManifestStorage(BillArea VARCHAR,TotalCount VARCHAR,StoredJSON VARCHAR,StoredSharedPref VARCHAR,DownloadDateTime VARCHAR,IsPushed VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS BillManifestUpdate(StructureID VARCHAR,BusinessName VARCHAR,User_FK VARCHAR,ManifestStatus_FK VARCHAR,Comment VARCHAR,Fotoname VARCHAR,IsPushed VARCHAR);");
        } catch (Exception e) {
            Toast.makeText(ManifestDownloadActivity.this,"Local Database Error", Toast.LENGTH_SHORT).show();
        }

        //AysnTask class to handle WS call as separate UI Thread
        JSON_ListOfLGA task = new JSON_ListOfLGA();
        task.execute();

        BtnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy");
                String formattedDate = df.format(c.getTime());
                BillYear=Integer.valueOf(formattedDate);
                JSON_GetBillManifestRecord task = new JSON_GetBillManifestRecord();
                task.execute();
            }


        });
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(ManifestDownloadActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(ManifestDownloadActivity.this).toBundle());
            }
        });


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

        ManifestStatusSpinnerCtrl.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ManifestStatusFK = getManifestStatusFK(ManifestStatusSpinnerCtrl.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }//End on On create


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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(ManifestDownloadActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
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

                JSON_ListOfManifestStatus task2 = new JSON_ListOfManifestStatus();
                task2.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            progressDialog = MyCustomProgressDialog.ctor(ManifestDownloadActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
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
            progressDialog = MyCustomProgressDialog.ctor(ManifestDownloadActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    private class JSON_GetBillManifestRecord extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            Log.i(TAG, "doInBackground");
            invokeBillManifestRecord("JSON_GetBillManifestRecord");
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
                arrayresultforManifestRecord = j.getJSONArray("myJresult");
                //Save Record to DB
                Calendar c = Calendar.getInstance();
                try {
                    db.execSQL("DELETE FROM BillManifestStorage WHERE BillArea='"+StructureZoneSpinnerCtrl.getSelectedItem().toString()+"'");
                    db.execSQL("INSERT INTO BillManifestStorage VALUES('"+StructureZoneSpinnerCtrl.getSelectedItem().toString()+"','"+String.valueOf(arrayresultforManifestRecord.length())+"','"+responseJSON+"','"+"JSON_"+StructureZoneSpinnerCtrl.getSelectedItem().toString()+"','"+c.getTime().toString()+"','0');");
                    final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("JSON_"+StructureZoneSpinnerCtrl.getSelectedItem().toString(), responseJSON);
                    editor.commit();
                    showInfoDialog("Download Complete",String.valueOf(arrayresultforManifestRecord.length())+" Record(s) Stored Locally");
                    progressDialog.cancel();
                } catch (Exception e) {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            progressDialog = MyCustomProgressDialog.ctor(ManifestDownloadActivity.this);
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
    public void invokeBillManifestRecord(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("Lga_FK");
        paramPI.setValue(LGAfk);
        paramPI.setType(Integer.class);
        request.addProperty(paramPI);
        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("StructureZone_FK");
        paramPI2.setValue(StructureZoneFK);
        paramPI2.setType(Integer.class);
        request.addProperty(paramPI2);
        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("BilledYear");
        paramPI3.setValue(BillYear);
        paramPI3.setType(Integer.class);
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
        LGASpinnerCtrl.setAdapter(new ArrayAdapter<String>(ManifestDownloadActivity.this, android.R.layout.simple_spinner_dropdown_item, lgaarraylist));
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
        StructureZoneSpinnerCtrl.setAdapter(new ArrayAdapter<String>(ManifestDownloadActivity.this, android.R.layout.simple_spinner_dropdown_item, StructureZonearraylist));
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
        ManifestStatusSpinnerCtrl.setAdapter(new ArrayAdapter<String>(ManifestDownloadActivity.this, android.R.layout.simple_spinner_dropdown_item, manifeststatusarraylist));
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

    private void showInfoDialog(String Title,String Msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(Title);
        builder.setMessage(Msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Snackbar.make(parent_view, "Weldone!", Snackbar.LENGTH_SHORT).show();
            }
        });
        //builder.setNegativeButton(R.string.DISAGREE, null);
        builder.show();
    }
}
