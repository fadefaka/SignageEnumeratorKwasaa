package com.biscom.signageenumeratorekiti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class SuggestionActivity extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/MobileAppWSTypeTeamA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "OS-STRCTURESBYLGA";
    private static String responseJSON;


    EditText txttitleedit;
    EditText txtmsgedit;
    Button btnback;
    Button btnsubmit;
    String getmyuserFK;
    String getmyuserTOKEN;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        btnback = (Button) findViewById(R.id.btnback);
        btnsubmit = (Button) findViewById(R.id.btnsubmit);
        txtmsgedit = (EditText) findViewById(R.id.txtmsgedit);
        txttitleedit = (EditText) findViewById(R.id.txttitleedit);
        txtmsgedit.setHorizontallyScrolling(false);
        txtmsgedit.setLines(Integer.MAX_VALUE);

        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myintent = new Intent(SuggestionActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(SuggestionActivity.this).toBundle());
            }});

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSON_MakeSuggestion task = new JSON_MakeSuggestion();
                task.execute();
            }});


    }

    private class JSON_MakeSuggestion extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS(getmyuserTOKEN,"JSON_MakeSuggestion");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            progressDialog.cancel();
            //JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                //j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                //arrayresultforstructuresbylga = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                //Toast.makeText(getBaseContext(), String.valueOf(arrayresultforstructuresbylga.length()), Toast.LENGTH_LONG).show();
                //getMonthlyIncome(arrayresultforstructuresbylga);
                //pg.setVisibility(View.GONE);

                if (responseJSON.contains("Suggestion Submitted")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SuggestionActivity.this);

                    dialog.setTitle( "Record Submitted" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("Thank you for your suggestion")
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    txttitleedit.setText("");
                                    txtmsgedit.setText("");
                                }
                            }).show();
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
            progressDialog = MyCustomProgressDialog.ctor(SuggestionActivity.this);
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
        paramPI2.setName("Topic");
        // Set Value
        paramPI2.setValue(txttitleedit.getText().toString());
        // Set dataType
        paramPI2.setType(String.class);
        // Add the property to request object
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        // Set Name
        paramPI3.setName("SuggestionMessage");
        // Set Value
        paramPI3.setValue(txtmsgedit.getText().toString());
        // Set dataType
        paramPI3.setType(String.class);
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
}
