package com.biscom.signageenumeratorekiti;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.AsyncTask;

import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
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

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    ListView lstStructures;
    private static String SearchS;
    private Timer timer;
    private static String neededName;
    private static String neededLocation;
    private static String neededStructureType;
    private static String neededCurrentBilledYear;
    private static String neededNegotiatedAmount;
    private static String neededAmountPaid;
    Integer sItemPosition;
    private JSONArray arrayresultformyStructures;
    private ArrayList<String> myStructurearraylist;
    //ProgressBar pg;
    private AnimationDrawable animation;
    Button btnfind;
    EditText txtfind;

    static final Integer LOCATION = 0x1;
    //static final Integer CALL = 0x2;
    static final Integer WRITE_EXST = 0x3;
    //static final Integer READ_EXST = 0x4;
    static final Integer CAMERA = 0x5;
    //static final Integer ACCOUNTS = 0x6;
    //static final Integer GPS_SETTINGS = 0x7;

    ProgressBar pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myStructurearraylist = new ArrayList<String>();
        btnfind = (Button) findViewById(R.id.btnsearch);
        txtfind = (EditText) findViewById(R.id.txtsearchtext);
        lstStructures = (ListView) findViewById(R.id.lstResult);
        pg = (ProgressBar) findViewById(R.id.progressBar1);

        //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
        //rl.setBackgroundResource(R.drawable.mainlayoutanimation);
        //animation = (AnimationDrawable) rl.getBackground();
        //animation.start();

        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }
        btnfind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Utils.hideKeyboard(MainActivity.this);
                SearchS = txtfind.getText().toString();

                try{
                togglePeriodicLocationUpdates(); //Starting the location updates
                }catch(Exception e){

                }

                if (SearchS.length()>2){
                    JSON_SearchStructureFrmMobile task = new JSON_SearchStructureFrmMobile();
                    task.execute();
                }


                // Run a timer after you started the AsyncTask
                //try{
                  //  new CountDownTimer(60000, 1000) {

                     //   public void onTick(long millisUntilFinished) {
                            // Do nothing
                      // }
                       // public void onFinish() {
                            //task.cancel(true);
                       // }
                   // }.start();
                //}catch(Exception e){

                //}
            }
        });

        lstStructures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
                //pg.setVisibility(View.GONE);
                LayoutInflater inflater= LayoutInflater.from(MainActivity.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(Html.fromHtml("<b>Business Name:</b> ").toString()+"\n"+neededName+"\n"+"\n"+"Structure Type: "+"\n"+neededStructureType+"\n"+"\n"+"Location: "+"\n"+neededLocation+"\n"+"\n"+"Current Billing Year: "+"\n"+neededCurrentBilledYear+"\n"+"\n"+"Amount Billed: "+"\n"+neededNegotiatedAmount+"\n"+"\n"+"Amount Paid: "+"\n"+neededAmountPaid);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Details");
                alertDialog.setIcon(R.mipmap.ic_launcher);
                //alertDialog.setMessage("Here is a really long message.");
                alertDialog.setView(view);
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
                //Toast.makeText(MyPendingWorkActivity.this, neededProjectName.toString(), Toast.LENGTH_LONG).show();
            }
        });

        //Begin of Button Click Event for enumerator
        final Button btnhome = (Button) findViewById(R.id.btnhome);
        btnhome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //What happens when button is clicked goes here
                Intent myintent = new Intent(MainActivity.this, FirstActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());

            }
        }); //End of Button Click Event for enumerator

        //Begin of Button Click Event for AnonButton (Capture Buttion)
        final Button AnonButton = (Button) findViewById(R.id.btnstartcapture);
        AnonButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //What happens when button is clicked goes here
                try{
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                    askForPermission(Manifest.permission.CAMERA,CAMERA);
                    //askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
                }catch (Exception e){
                    e.printStackTrace();
                    //Intent myintent = new Intent(MainActivity.this, TakeAShortActivity.class);
                    //startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                }

            }
        }); //End of Button Click Event for AnonButton

    } //End of OnCreate


    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //What todo if there is no permission
            //Toast.makeText(MainActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
        }else{
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    double latitude = mLastLocation.getLatitude();
                    double longitude = mLastLocation.getLongitude();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(MainActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();

                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    //Toast.makeText(MainActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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
        mGoogleApiClient.connect();
    }

    private class JSON_SearchStructureFrmMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            //Invoke web method 'PopulateCountries' with dummy value
            //Toast.makeText(getBaseContext(), "Am here! ", Toast.LENGTH_LONG).show();
            invokeJSONWS("JSON_SearchStructureFrmMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");

            JSONObject j = null;
            try {
                if (responseJSON.contains("-NOT FOUND-")){
                    //pg.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle( "NOT FOUND" )
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("No Such Structure Exist")
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                }
                            }).show();
                   pg.setVisibility(View.INVISIBLE);
                    //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
                    //rl.setBackgroundResource(R.drawable.landbg);
                    j = new JSONObject(responseJSON);
                    //Storing the Array of JSON String to our JSON Array
                    arrayresultformyStructures = j.getJSONArray("myJresult");
                    getMyStructures(arrayresultformyStructures);
                    return;
                }
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultformyStructures = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                getMyStructures(arrayresultformyStructures);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle( "Search Complete" )
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Confirm if the Actual Name Exist/ Otherwise Take Picture")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        }).show();
                pg.setVisibility(View.INVISIBLE);
                //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
                //rl.setBackgroundResource(R.drawable.landbg);


                //Toast.makeText(RptLastDaysActivity.this, getmyuserTOKEN, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            //RelativeLayout rl =(RelativeLayout) findViewById(R.id.animation);
            //rl.setBackgroundResource(R.color.white);
            pg.setVisibility(View.VISIBLE);
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
        // Set Name
        paramPI.setName("SearchString");
        // Toast.makeText(FinalAssignActivity.this, "Oh no! " + getmyuserTOKEN, Toast.LENGTH_LONG).show();
        // Set Value
        paramPI.setValue(SearchS.toString());
        //Toast.makeText(MainActivity.this, SearchS.toString(), Toast.LENGTH_LONG).show();
        // Set dataType
        paramPI.setType(String.class);
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
            responseJSON="Nothing Returned";

        }
    }
    private void getMyStructures(JSONArray j){
        //Traversing through all the items in the json array
        myStructurearraylist.clear();

        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                //Adding the name of the student to array list
                myStructurearraylist.add(json.getString("BusinessName").toString() +" ("+json.getString("TheStructureType").toString()+")");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, myStructurearraylist){
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
        //Setting adapter to show the items in the spinner
        //lstpendingstaff.setAdapter(new ArrayAdapter<>(MyPendingWorkActivity.this, android.R.layout.simple_list_item_1, mypendingtaskarraylist)
        lstStructures.setAdapter(arrayAdapter);
        // );
    }
    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultformyStructures.getJSONObject(position);
            //Fetching name from that object
            neededName = json.getString("BusinessName");
            neededLocation = json.getString("StructureLocation_Town");
            neededStructureType = json.getString("TheStructureType");
            neededCurrentBilledYear = json.getString("CurrentBilledYear");
            neededNegotiatedAmount = "N "+String.format("%,.2f",Double.valueOf(json.getString("NegotiatedAmount")));
            neededAmountPaid = "N "+String.format("%,.2f",Double.valueOf(json.getString("AmountPaid")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
            Intent myintent = new Intent(MainActivity.this, TakeAShortActivity.class);
            startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
                switch (requestCode) {
                    //Location
                    case 1:
                        //askForGPS();
                        break;
                    //Call
                    //case 2:
                        //Intent callIntent = new Intent(Intent.ACTION_CALL);
                        //callIntent.setData(Uri.parse("tel:" + "{This is a telephone number}"));
                        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        //     startActivity(callIntent);
                        // }
                       // break;
                    //Write external Storage
                    case 3:
                        break;
                    //Read External Storage
                    //case 4:
                       // Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                       // startActivityForResult(imageIntent, 11);
                       // break;
                    //Camera
                    case 5:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, 12);
                        }
                        break;

                }

                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                Intent myintent = new Intent(MainActivity.this, TakeAShortActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());

            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();


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
            stopLocationUpdates();
        }catch(Exception e){

        }

    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));
try{
    mRequestingLocationUpdates = true;

    // Starting the location updates
    startLocationUpdates();

    Log.d(TAG, "Periodic location updates started!");
}catch(Exception e){

}


        } else {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

           // mRequestingLocationUpdates = false;

            // Stopping the location updates
            //stopLocationUpdates();

            //Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //What todo if there is no permission
            Toast.makeText(MainActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        //Toast.makeText(getApplicationContext(), "Updating Location...",
                //Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }
}
