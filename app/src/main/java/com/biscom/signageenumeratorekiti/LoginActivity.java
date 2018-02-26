package com.biscom.signageenumeratorekiti;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class LoginActivity extends Activity {
    // LogCat tag
    private static final String TAG = "LoginActivity";
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    private static String neededUserFk;
    private static String neededIsExec;
    private JSONArray arrayresultformyStructures;

    ProgressBar pg;
    String username;
    String userpass;
    String shareduser;
    String sharedpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pg = (ProgressBar) findViewById(R.id.progressBar1);
        final EditText txtusername = (EditText) findViewById(R.id.txtusername);
        final EditText txtuserpass = (EditText) findViewById(R.id.txtpassword);

        //Check for Previous Login
        //try{
        //final SharedPreferences sharedPref = this.getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        // shareduser = sharedPref.getString("PREUSERNAME","");
        // sharedpass = sharedPref.getString("PREPASSWORD","");
        //}catch(Exception e){
        //   shareduser = "";
//
        //}

        final Button webserviceCallButton = (Button) findViewById(R.id.btnsignIn);
        webserviceCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //What happens when button is clicked goes here
                username = txtusername.getText().toString();
                userpass = txtuserpass.getText().toString();
                pg.setVisibility(View.VISIBLE);
                //Check Login Online
                JSON_SignageCheckMobileLogin task = new JSON_SignageCheckMobileLogin();
                task.execute();
            }
            }); //End of Button Click Event
/*
        if (shareduser.length()>3){
            //Redirect to AfterLoginActivity
            Intent myintent = new Intent(LoginActivity.this, AfterLoginActivity.class);
            startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle());
            //finish();
        }*///end check for Previous Login
    }
    private class JSON_SignageCheckMobileLogin extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            //Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            //Toast.makeText(getBaseContext(), "Am here! ", Toast.LENGTH_LONG).show();
            invokeJSONWS("JSON_SignageCheckMobileLogin");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            JSONObject j = null;
            try {

                if (responseJSON.contains("-NOT VALID-")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                    dialog.setTitle( "Invalid Details" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("Check and Try Again")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                    pg.setVisibility(View.GONE);
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    //Parsing the fetched Json String to JSON Object
                    j = new JSONObject(responseJSON);
                    //Storing the Array of JSON String to our JSON Array
                    arrayresultformyStructures = j.getJSONArray("myJresult");
                    pg.setVisibility(View.GONE);
                    final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("PREUSERNAME", username);
                    editor.putString("PREPASSWORD", userpass);
                    getneededvalues(0);
                    editor.putString("User_Fk", neededUserFk);
                    editor.putString("IsExec", neededIsExec);
                    editor.commit();
                    Intent myintent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(myintent);
                }else{
                    pg.setVisibility(View.GONE);
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("UserName");
        paramPI.setValue(username.toString());
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("UserPass");
        paramPI2.setValue(userpass.toString());
        paramPI2.setType(String.class);
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
    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultformyStructures.getJSONObject(position);
            //Fetching name from that object
            neededUserFk = json.getString("User_FK");
            neededIsExec = json.getString("IsExec");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }
}
