package com.biscom.signageenumeratorekiti;
import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class LoggedInTakeAShotActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // LogCat tag
    private static final String TAG = "LoggedIn";
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


    Button btntakephoto, btnsave;
    Button btnback;
    ImageView ivdisplayphoto;
    SeekBar sbSeekBar;
    TextView photoname;

    private ColorMatrix colorMatrix;
    private ColorMatrixColorFilter filter;
    private Paint paint;
    private Canvas cv;

    int n = 10000;
    String nn="";
    String User_Fk="0";

    private File photofile;
    private int TAKENPHOTO = 0;
    Bitmap photo, canvasBitmap;
    double latitude;
    double longitude;
    SQLiteDatabase db;

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_take_ashot);
        btntakephoto = (Button)findViewById(R.id.btn_takephoto);
        btnsave = (Button)findViewById(R.id.btn_save);
        btnback = (Button) findViewById(R.id.btnback);
        btnsave.setEnabled(false);
        btntakephoto.setText("Open Camera");
        ivdisplayphoto = (ImageView)findViewById(R.id.iv_displayphoto2);
        photoname = (TextView) findViewById(R.id.tv_titleapp);
        sbSeekBar = (SeekBar) findViewById(R.id.skbarChangeColor);
        sbSeekBar.setMax(100);
        sbSeekBar.setKeyProgressIncrement(1);
        sbSeekBar.setProgress(50);
        sbSeekBar.setVisibility(View.GONE);
        colorMatrix = new ColorMatrix();
        filter = new ColorMatrixColorFilter(this.colorMatrix);
        paint = new Paint();
        paint.setColorFilter(filter);
        try {
        db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS reftable(myrefno VARCHAR,istreated VARCHAR,valuedate VARCHAR);");
        } catch (Exception e) {
            Toast.makeText(LoggedInTakeAShotActivity.this,e.toString(), Toast.LENGTH_LONG).show();
        }
        //Check for Previous Login
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String shareduser = sharedPref.getString("PREUSERNAME","");
        String sharedpass = sharedPref.getString("PREPASSWORD","");
        User_Fk = sharedPref.getString("User_Fk","0");
        //if (shareduser.length()<2){
          //  Intent myintent = new Intent(LoggedInTakeAShotActivity.this, LoginActivity.class);
        //    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(LoggedInTakeAShotActivity.this).toBundle());
       // }//end check for Previous Login

        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        sbSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {

                applyColorFilter(progress);

            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

        });
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myintent = new Intent(LoggedInTakeAShotActivity.this, AfterLoginActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(LoggedInTakeAShotActivity.this).toBundle());

            }
        });
        btntakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //What todo if there is no permission
                    Toast.makeText(LoggedInTakeAShotActivity.this, "Cannot Capture: No Complete permission on the device)", Toast.LENGTH_LONG).show();
                }else{
                    ivdisplayphoto.destroyDrawingCache();
                    togglePeriodicLocationUpdates();
                    Random gen = new Random();
                    n = 9999;
                    n = gen.nextInt(n);
                    String pattern="0000";
                    DecimalFormat myFormatter = new DecimalFormat(pattern);
                    nn=myFormatter.format(n);
                    btnsave.setEnabled(true);
                    btntakephoto.setText("Discard/Re-take");
                    File photostorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    photofile = new File(photostorage, (System.currentTimeMillis()) + ".jpg");
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //intent to start camera
                    i.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".provider", photofile));
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(i, TAKENPHOTO);
                }



            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivdisplayphoto.setDrawingCacheEnabled(true);
               Bitmap bitmap = ivdisplayphoto.getDrawingCache();
                 try
                {
                    ivdisplayphoto.setImageBitmap(bitmap);
                }catch (Exception e) {

                }
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => " + c.getTime());
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c.getTime());
                String root = Environment.getExternalStorageDirectory().toString();
                File newDir = new File(root + "/EKITIsignage_Images/"+formattedDate);
                newDir.mkdirs();

                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //What todo if there is no permission
                    Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
                }else{
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        latitude = mLastLocation.getLatitude();
                        longitude = mLastLocation.getLongitude();
                        //lblLocation.setText(latitude + ", " + longitude);
                        //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                        //must include userFk in front to make it unique
                        String fotoname =User_Fk +""+ nn+"_"+String.valueOf(latitude)+"_"+String.valueOf(longitude)+"_EK.jpg";
                        photoname.setText("Ref No: "+User_Fk+""+nn+"\nPlease Note the ref no");
                        File file = new File (newDir, fotoname);
                        if (file.exists ()) file.delete ();
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                            //Save RefNo to SQLITE Here

                            try {
                                db.execSQL("INSERT INTO reftable VALUES('"+fotoname.replace(".jpg","")+"','NO','"+formattedDate+"');");
                            } catch (Exception e) {

                            }
                            //END Save RefNo to SQLITE Here
                            Toast.makeText(getApplicationContext(), "Image Record Saved!", Toast.LENGTH_SHORT ).show();
                            btnsave.setEnabled(false);
                            btntakephoto.setText("Open Camera");
                        } catch (Exception e) {

                        }

                    } else {
                        //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                        Toast.makeText(LoggedInTakeAShotActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                        longitude=0.00;
                        latitude=0.00;
                    }


                }


            }
        });
    }//End Of On Create
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
        if (requestCode == TAKENPHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                //Log.d(TAG + ".PICK_CAMERA_IMAGE", "Selected image uri path :" + Uri.fromFile(photofile));
                ivdisplayphoto.setImageURI(Uri.fromFile(photofile));
                photoname.setText("Click 'Save Image' button");
            }else{
                //No Picture Taken
                Toast.makeText(this, "No Picture Taken", Toast.LENGTH_LONG).show();
            }
        }else {
            //No Photo was taken
            Toast.makeText(this, "No Picture Taken", Toast.LENGTH_LONG).show();
        }
        }catch(Exception e){
           Toast.makeText(this, "Oops,can't get the photo from your gallery, Try Again", Toast.LENGTH_LONG).show();
       }
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // TODO Auto-generated method stub
//        try{
//            super.onActivityResult(requestCode, resultCode, data);
//
//            photoname.setText("Click 'Save Image' button");
//            if (requestCode == TAKENPHOTO){
//                try{
//                    photo = (Bitmap) data.getExtras().get("data");
//                    ivdisplayphoto.setImageBitmap(photo);
//                }
//                catch(NullPointerException ex){
//                    //;
//                    try{
//                        photo = BitmapFactory.decodeFile(photofile.getAbsolutePath());
//                        //Toast.makeText(this, "THIS IMAGE TOO LARGE", Toast.LENGTH_LONG).show();
//                    }catch(Exception e){
//
//                    }
//                }
//
//                if(photo != null){
//                    try{
//                    ivdisplayphoto.setImageBitmap(scaleDown(photo, 1500, true));
//                        //ivdisplayphoto.setImageBitmap(photo);
//                    //sbSeekBar.setVisibility(View.VISIBLE);
//                    //Toast.makeText(this, "I GOT HERE", Toast.LENGTH_LONG).show();
//                    }catch(Exception e){
//
//                    }
//                }
//                else{
//
//                    Toast.makeText(this, "Oops,can't get the photo from your gallery", Toast.LENGTH_LONG).show();
//                }
//            }
//        }catch(Exception e){
//            Toast.makeText(this, "Oops,can't get the photo from your gallery, Try Again", Toast.LENGTH_LONG).show();
//        }
//
//
//    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {

        try{

            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            //int height = Math.round((float) ratio * realImage.getHeight());

            int height = Math.round((float) realImage.getHeight());

            Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
            return newBitmap;
        }catch(Exception e){
            Bitmap newBitmap =null;
            return newBitmap;
        }

    }

    public void applyColorFilter(int progress){

        colorMatrix.setSaturation(progress/(float)40);
        filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvasBitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888);
        cv = new Canvas(canvasBitmap);
        cv.drawBitmap(photo, 0, 0, paint);
        ivdisplayphoto.setImageBitmap(canvasBitmap);

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
                    Toast.makeText(LoggedInTakeAShotActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
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
