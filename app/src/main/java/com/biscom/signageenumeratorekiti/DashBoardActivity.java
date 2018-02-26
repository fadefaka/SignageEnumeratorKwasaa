package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class DashBoardActivity extends AppCompatActivity {
    private WebView webview;
    String getmyusername;
    String getmyuserFK;
    String getmyuserTOKEN;
    private static final String TAG = "DashBoardActivity:";
    ProgressDialog progressDialog;
    Button btnclose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        this.webview  = (WebView)findViewById(R.id.mywebview);
        btnclose = (Button) findViewById(R.id.btnclose);
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyusername = sharedPref.getString("PREUSERNAME","na");
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myintent = new Intent(DashBoardActivity.this, MenuActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(DashBoardActivity.this).toBundle());
                //finish();
            }
        });

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        progressDialog = MyCustomProgressDialog.ctor(this);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();
        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Finished loading URL: " +url);
                //if (progressBar.isShowing()) {
                //  progressBar.dismiss();
                //}
                progressDialog.cancel();
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(LandPage.this, "Oh no! " + description, Toast.LENGTH_LONG).show();
                //webview.loadUrl("file:///android_asset/index.html");
            }
        });
        webview.loadUrl("http://eksaa.biscomtdigits.com/DesktopModules/Dashboard/MobileDashboard/MobileDashboard.aspx?userfk=" + getmyuserFK);



    }
}
