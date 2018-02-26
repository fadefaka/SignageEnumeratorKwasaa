package com.biscom.signageenumeratorekiti;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EndlessService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;

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

    double latitude;
    double longitude;
    String expStructureID;
    String expManifestStatus_FK;
    String expManifestComment;
    String expUser_FK;
    String expBusinessName;
    String expPhotoname;
    String[] reflistc1;
    String[] reflistc2;
    String[] reflistc3;
    String[] reflistc4;
    String[] reflistc5;
    String[] reflistc6;
    int reflistcounter=0;
    SQLiteDatabase db;


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            new HitToTheInternet().execute("");
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the
        // job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        if ( intent == null )
        {
            Log.e("intentStatus", "intent is null");
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    class HitToTheInternet extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            Log.e("Endless-doInBackground", "Running") ;
            pushRecordOnlineBackground();
            try{
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }
            }catch(Exception e){

            }
            // First we need to check availability of play services
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }


            if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
            }else{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    //FirstActivity.JSON_PushNewLocationFromMobile task = new FirstActivity.JSON_PushNewLocationFromMobile();
                    //task.execute();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    //Toast.makeText(FirstActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                    longitude=0.00;
                    latitude=0.00;
                }


            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Message msg = mServiceHandler.obtainMessage();
                    mServiceHandler.sendMessage(msg);
                }
            }, 10000);
        }
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
//                    GooglePlayServicesUtil.getErrorDialog(resultCode, Endles,
//                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This device is not supported.", Toast.LENGTH_LONG)
                            .show();
                }
                return false;
            }
            return true;
        }catch(Exception e){
            return true;
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
                    //FirstActivity.JSON_PushNewLocationFromMobile task = new FirstActivity.JSON_PushNewLocationFromMobile();
                    //task.execute();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    //Toast.makeText(FirstActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
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

    private void pushRecordOnlineBackground() {
        Log.e(TAG, "Attempting to Push record in background");
        try {
            db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
            Cursor c=db.rawQuery("SELECT * FROM BillManifestUpdate WHERE IsPushed='0'", null);
            Log.e("BillManifestUpdateCount", String.valueOf(c.getCount()));
            if(c.getCount()==0)
            {
                return;
            }
            reflistc1 = new String[c.getCount()];
            reflistc2 = new String[c.getCount()];
            reflistc3 = new String[c.getCount()];
            reflistc4 = new String[c.getCount()];
            reflistc5 = new String[c.getCount()];
            reflistc6 = new String[c.getCount()];
            reflistcounter=0;
            while(c.moveToNext())
            {
                expStructureID=c.getString(0);
                expBusinessName=c.getString(1);
                expUser_FK=c.getString(2);
                expManifestStatus_FK=c.getString(3);
                expManifestComment=c.getString(4);
                expPhotoname=c.getString(5);
                reflistc1[reflistcounter]=expStructureID;
                reflistc2[reflistcounter]=expBusinessName;
                reflistc3[reflistcounter]=expUser_FK;
                reflistc4[reflistcounter]=expManifestStatus_FK;
                reflistc5[reflistcounter]=expManifestComment;
                reflistc6[reflistcounter]=expPhotoname;
                reflistcounter = reflistcounter+1;
            }
            JSON_ReceiveManifestUpdateFromMobile task = new JSON_ReceiveManifestUpdateFromMobile();
            task.execute();
            Log.e(TAG, "Running Push record in background");
        } catch (Exception e) {
//            Toast.makeText(AfterLoginActivity.this, "You Have to Capture First",
//                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error while Attempting to Push record in background");
        }
    }
    private class JSON_ReceiveManifestUpdateFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try{
                Toast.makeText(getBaseContext(), "Pushing "+String.valueOf(reflistcounter) +" More", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }

            expStructureID=reflistc1[reflistcounter-1];
            expBusinessName=reflistc2[reflistcounter-1];
            expUser_FK=reflistc3[reflistcounter-1];
            expManifestStatus_FK=reflistc4[reflistcounter-1];
            expManifestComment=reflistc5[reflistcounter-1];
            expPhotoname=reflistc6[reflistcounter-1];
            invokeJSONWS4("JSON_ReceiveManifestUpdateFromMobile");
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
                    db.execSQL("UPDATE BillManifestUpdate SET IsPushed='1' WHERE StructureID='"+expStructureID+"' AND Fotoname='"+expPhotoname+"'");
                }else{
                    //progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            reflistcounter=reflistcounter-1;
            if (reflistcounter>0){
                JSON_ReceiveManifestUpdateFromMobile task = new JSON_ReceiveManifestUpdateFromMobile();
                task.execute();
            }
            if (reflistcounter<=0){

//            AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
//            dialog.setTitle( "Export Finished" )
//                    .setIcon(R.mipmap.ic_launcher)
//                    .setMessage("Done with the Export")
//                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialoginterface, int i) {
//                        }
//                    }).show();
//            progressDialog.cancel();
            }


        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
//        progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
//        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS4(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("StructureID");
        paramPI.setValue(expStructureID.toString());
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("ManifestStatus_FK");
        paramPI2.setValue(Integer.valueOf(expManifestStatus_FK.toString()));
        paramPI2.setType(Integer.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("Comment");
        paramPI3.setValue(expManifestComment.toString());
        paramPI3.setType(String.class);
        request.addProperty(paramPI3);

        PropertyInfo paramPI4 = new PropertyInfo();
        paramPI4.setName("BusinessName");
        paramPI4.setValue(expBusinessName.toString());
        paramPI4.setType(String.class);
        request.addProperty(paramPI4);

        PropertyInfo paramPI5 = new PropertyInfo();
        paramPI5.setName("User_FK");
        paramPI5.setValue(Integer.valueOf(expUser_FK));
        paramPI5.setType(Integer.class);
        request.addProperty(paramPI5);

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File newDir = new File(root + "/ENFORCEMENT_Images");
        File file = new File (newDir, expPhotoname.toString());
        String encodedImage="";
        encodedImage=convertToBase64(file.toString());
        try {
        encodedImage=compressBase64StringToGzip(encodedImage);
        }catch (IOException e){
        }
        PropertyInfo paramPI6 = new PropertyInfo();
        paramPI6.setName("ImageBase64");
        paramPI6.setValue(encodedImage.toString());
        paramPI6.setType(String.class);
        request.addProperty(paramPI6);

        PropertyInfo paramPI7 = new PropertyInfo();
        paramPI7.setName("PictureRef");
        paramPI7.setValue(expPhotoname);
        paramPI7.setType(String.class);
        request.addProperty(paramPI7);

        PropertyInfo paramPI8 = new PropertyInfo();
        paramPI8.setName("Latitude");
        paramPI8.setValue(String.valueOf(latitude));
        paramPI8.setType(String.class);
        request.addProperty(paramPI8);

        PropertyInfo paramPI9 = new PropertyInfo();
        paramPI9.setName("Longitude");
        paramPI9.setValue(String.valueOf(longitude));
        paramPI9.setType(String.class);
        request.addProperty(paramPI9);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
        String formattedDate = df.format(c.getTime());

        PropertyInfo paramPI10 = new PropertyInfo();
        paramPI10.setName("ValueTime");
        paramPI10.setValue(formattedDate);
        paramPI10.setType(String.class);
        request.addProperty(paramPI10);

        df = new SimpleDateFormat("yyyy/MM/dd");
        formattedDate = df.format(c.getTime());

        PropertyInfo paramPI11 = new PropertyInfo();
        paramPI11.setName("ValueDate");
        paramPI11.setValue(formattedDate);
        paramPI11.setType(String.class);
        request.addProperty(paramPI11);

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

    private String convertToBase64(String imagePath){
        Log.e("Image",imagePath);
        //Bitmap bm = BitmapFactory.decodeFile(imagePath);
        Bitmap bm = makeBitmap(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }

    public static String compressBase64StringToGzip(String Base64string) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream(Base64string.length());
            GZIPOutputStream gos = new GZIPOutputStream(os);
//            gos.write(string.getBytes());
            gos.write(Base64.decode(Base64string,Base64.DEFAULT));
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            String encodedImage = Base64.encodeToString(compressed, Base64.DEFAULT);
            return encodedImage;
    }
    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    private Bitmap makeBitmap(String path) {

        try {
            final int IMAGE_MAX_SIZE = 360000; // 0.36MP
            //resource = getResources();

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("TAG", "scale = " + scale + ", orig-width: " + options.outWidth + ", orig-height: " + options.outHeight);

            Bitmap pic = null;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                pic = BitmapFactory.decodeFile(path, options);

                // resize to desired dimensions

                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.y;
                int height = size.x;

                //int height = imageView.getHeight();
                //int width = imageView.getWidth();
                Log.d("TAG", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(pic, (int) x, (int) y, true);
                pic.recycle();
                pic = scaledBitmap;

                System.gc();
            } else {
                pic = BitmapFactory.decodeFile(path);
            }

            Log.d("TAG", "bitmap size - width: " +pic.getWidth() + ", height: " + pic.getHeight());
            return pic;

        } catch (Exception e) {
            Log.e("TAG", e.getMessage(),e);
            return null;
        }

    }
}