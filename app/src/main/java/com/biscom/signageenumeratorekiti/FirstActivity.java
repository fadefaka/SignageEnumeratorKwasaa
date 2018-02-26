package com.biscom.signageenumeratorekiti;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class FirstActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "FirstActivity";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 0; // 10 meters

    private AnimationDrawable animation;
    Button btnxplore;
    Button btnlogin;
    String shareduser;
    String sharedpass;
    double latitude;
    double longitude;

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    String User_Fk="0";
    String version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        //RelativeLayout rl =(RelativeLayout) findViewById(R.id.rltanimation);
        //rl.setBackgroundResource(R.drawable.mainlayoutanimation);
        //animation = (AnimationDrawable) rl.getBackground();
        //animation.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        TextView versionview = (TextView) findViewById(R.id.version);
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

         versionview.setText("Version "+version);

        try{
            final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
            shareduser = sharedPref.getString("PREUSERNAME","");
            sharedpass = sharedPref.getString("PREPASSWORD","");
            User_Fk = sharedPref.getString("User_Fk","0");
        }catch(Exception e){
            shareduser = "";
            sharedpass = "";
        }
        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        btnxplore = (Button) findViewById(R.id.btnxplore);
        btnlogin = (Button) findViewById(R.id.btnlogin);

        btnxplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //What happens when button is clicked goes here
                Intent myintent = new Intent(FirstActivity.this, MainActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(FirstActivity.this).toBundle());
            }
        });//End btnxplore Click Event

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //What happens when button is clicked goes here
                //Intent myintent = new Intent(FirstActivity.this, AfterLoginActivity.class);
                //startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(FirstActivity.this).toBundle());


                if (shareduser.length()>3){
                    //Redirect to AfterLoginActivity
                    Intent myintent = new Intent(FirstActivity.this, MenuActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(FirstActivity.this).toBundle());
                    //finish();
                }else{
                    Intent myintent = new Intent(FirstActivity.this, LoginActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(FirstActivity.this).toBundle());
                }
                //end check for Previous Login




            }
        });//End btnxplore Click Event

        startService(new Intent ( this , EndlessService.class)) ;
    }



    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        try{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }catch(Exception e){

        }

    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        try{
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This device is not supported.", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                return false;
            }
            return true;
        }catch(Exception e){
            return true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }catch(Exception e){

        }

    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        try{
            mGoogleApiClient.connect();
        }catch(Exception e){

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch(Exception e){

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            //stopLocationUpdates();
        }catch(Exception e){

        }

    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        try{
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                // Starting the location updates
                startLocationUpdates();
                Log.d(TAG, "Periodic location updates started!");
            } else {
                // Changing the button text
                //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

                //mRequestingLocationUpdates = false;

                // Stopping the location updates
                //stopLocationUpdates();

                //Log.d(TAG, "Periodic location updates stopped!");
            }
        }catch(Exception e){

        }

    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        try{
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        }catch(Exception e){

        }

    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        try{
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch(Exception e){

        }

    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch(Exception e){

        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        //displayLocation();
        try {
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch (Exception e){

        }

    }
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try{
            mLastLocation = location;

            if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
            }else{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    JSON_PushNewLocationFromMobile task = new JSON_PushNewLocationFromMobile();
                    task.execute();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    Toast.makeText(FirstActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                    longitude=0.00;
                    latitude=0.00;
                }


            }

            //Toast.makeText(getApplicationContext(), "Location changed!",
            //Toast.LENGTH_SHORT).show();
        }catch(Exception e){

        }

        // Displaying the new location on UI
        //displayLocation();
    }



    private class JSON_PushNewLocationFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS2("JSON_PushNewLocationFromMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.i(TAG, "onPostExecute");
            //progressDialog.cancel();
            JSONObject j = null;
            try {

                if (responseJSON.contains("-FAILED-")){
                    //progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    //progressDialog.cancel();
                    //db.execSQL("UPDATE reftable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                }else{
                    //progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            //progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            //progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            //progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS2(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("latitude");
        paramPI.setValue(String.valueOf(latitude));
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("longitude");
        paramPI2.setValue(String.valueOf(longitude));
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("User_FK");
        paramPI3.setValue(String.valueOf(User_Fk));
        paramPI3.setType(String.class);
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
