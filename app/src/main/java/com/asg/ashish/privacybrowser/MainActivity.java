package com.asg.ashish.privacybrowser;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView web;
    private TextView glow;
    private ProgressBar progress;
    AutoCompleteTextView Address;
    private ShareActionProvider mShareActionProvider;
    static String searchengine;
    SharedPreferences mPreferences, tPreferences,sp, desk;
    SwipeRefreshLayout mswipe;
    String TAG="THEME";
    RelativeLayout layout1;
    String storedpath, imgpath;
    private static int RESULT_LOAD_IMG = 1, check = 0, fs = 0;
    Button button;
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.81 Safari/537.36";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        web = (WebView)findViewById(R.id.web);
        mswipe = (SwipeRefreshLayout)findViewById(R.id.mswipe);
        Address = findViewById(R.id.Address);
        glow = findViewById(R.id.textViewstart);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                // This activity implements OnMenuItemClickListener;
        /*desk = getSharedPreferences("desktop",MODE_PRIVATE);
        i.setTitle(desk.getString("deskmode","Desktop Mode OFF"));*/


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.reload:
                                web.reload();
                                return true;

                            case R.id.forward:
                                if (web.canGoForward()) {
                                    web.goForward();
                                }
                                return true;

                            case R.id.action_theme:
                                loadImagefromGallery();
                                return true;

                            case R.id.share:
                                mShareActionProvider = (ShareActionProvider) item.getActionProvider();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                String sharebody = web.getUrl();
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharebody);
                                startActivity(Intent.createChooser(shareIntent, "Share via"));
                                return true;


                            case R.id.engine:
                                CharSequence engines[] = new CharSequence[] {"Google", "Bing", "Duck Duck GO", "Yahoo"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Select a Search Engine");
                                builder.setItems(engines, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, Integer.toString(which),Toast.LENGTH_SHORT).show();
                                        if(which == 0)
                                            searchengine = "https://www.google.com/search?q=";
                                        else if (which == 1)
                                            searchengine = "https://www.bing.com/search?q=";
                                        else if (which == 2)
                                            searchengine = "https://duckduckgo.com/?q=";
                                        else searchengine = "https://search.yahoo.com/search?q=";

                                    }
                                });
                                builder.show();

                                return true;

                            case R.id.desktop:
                                desk = getSharedPreferences("desktop", MODE_PRIVATE);
                                SharedPreferences.Editor editor = desk.edit();

                                if(check == 0){
                                    check = 1;
                                    item.setTitle("Desktop Mode ON");
                                    editor.putString("deskmode","Desktop Mode ON" );


                                }
                                else {
                                    check = 0;
                                    item.setTitle("Desktop Mode OFF");
                                    editor.putString("deskmode", "Desktop Mode OFF");
                                }
                                editor.apply();

                                if(check == 1){
                                    Toast.makeText(MainActivity.this, "Desktop Mode ON", Toast.LENGTH_SHORT).show();
                                    setDesktopMode(web, true);
                                    item.setTitle("Desktop Mode ON");


                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Desktop Mode OFF", Toast.LENGTH_SHORT).show();
                                    setDesktopMode(web,false);
                                    item.setTitle("Desktop Mode OFF");


                                }

                                return true;



                            default:
                                return MainActivity.super.onOptionsItemSelected(item);
                        }
                    }
                });
                popup.inflate(R.menu.menu_main);
                popup.show();


            }


        });

        Intent intent = getIntent();
        final Uri uri = intent.getData();
        try {

            String url = uri.toString();
            if (Patterns.WEB_URL.matcher(url).matches()) {
                web.setVisibility(View.VISIBLE);
                glow.setVisibility(View.INVISIBLE);
                Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
                if (!url.startsWith("http")) url = "http://" + url;
                web.loadUrl(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sp=getSharedPreferences("setback", MODE_PRIVATE);
        if(sp.contains("imagepath")) {
            storedpath=sp.getString("imagepath", "");
            Bitmap myBitmap = BitmapFactory.decodeFile(storedpath);
            BitmapDrawable b = new BitmapDrawable(myBitmap);
            final RelativeLayout layout1 = findViewById(R.id.layout1);
            layout1.setBackgroundDrawable(b);
        }
        else{
            final RelativeLayout layout1 = findViewById(R.id.layout1);
            layout1.setBackground(getResources().getDrawable(R.drawable.dark));
        }


        mswipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String url = web.getUrl();
                Log.v(TAG,"URL Is "+url);
                if(url == null) {
                    url = "http://" +
                            "www.google.com";
                    web.setVisibility(View.VISIBLE);
                    glow.setVisibility(View.INVISIBLE);
                }
                web.loadUrl(url);
                mswipe.scheduleLayoutAnimation();

            }
        });




        glow.setShadowLayer(20, 0, 0, Color.WHITE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup.LayoutParams params = Address.getLayoutParams();
            final int width = params.width;
            tPreferences = getSharedPreferences("Width", MODE_PRIVATE);
            SharedPreferences.Editor tpreferencesEditor = tPreferences.edit();
            tpreferencesEditor.putInt("width", width);
            tpreferencesEditor.apply();
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, WEB);

        web.getSettings().setJavaScriptEnabled(true);

        web.getSettings().setSupportZoom(true);       //Zoom Control on web
        web.getSettings().setBuiltInZoomControls(true); //Enable Multitouch if supported by ROM
        web.getSettings().setAllowFileAccess(true);
        web.addJavascriptInterface(new MyJavaScriptInterface(), "android");
        web.getSettings().setGeolocationEnabled(true);
        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        web.getSettings().setDisplayZoomControls(false);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClientDemo());
        web.setWebChromeClient(new WebChromeClientDemo());

        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        web.getSettings().setAllowContentAccess(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        CookieManager.getInstance().acceptThirdPartyCookies(web);
        CookieManager.getInstance().acceptCookie();
        haveStoragePermission();


        progress = findViewById(R.id.progress);

        mPreferences = getSharedPreferences("maindata", MODE_PRIVATE);
        searchengine = mPreferences.getString("pos2", "https://www.google.com/search?q=");



        Address.setOnEditorActionListener(new EditText.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                String query;

                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (isConnect()) {
                        try {

                            query = Address.getText().toString();
                            if (!query.equals("")) {
                                web.setVisibility(View.VISIBLE);
                                glow.setVisibility(View.INVISIBLE);

                                if (!Patterns.WEB_URL.matcher(query).matches()) {

                                    query = searchengine + query;

                                    web.loadUrl(query);
                                } else if (query.startsWith("https") || query.startsWith("http")) {
                                    web.loadUrl(query);

                                } else {
                                    query = "http://" + query;
                                    web.loadUrl(query);
                                }

                                Address.setCursorVisible(false);
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                assert imm != null;
                                imm.hideSoftInputFromWindow(Address.getWindowToken(), 0);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Check Ur Connection", Toast.LENGTH_LONG).show();
                    }
                    handled = true;
                }
                return handled;
            }
        });




        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                if (haveStoragePermission()) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    request.setMimeType(mimeType);
                    //------------------------COOKIE!!------------------------
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    //------------------------COOKIE!!------------------------
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    assert dm != null;
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                }
            }
        });


        Address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Address.setCursorVisible(true);
                Address.setAdapter(adapter);

                if (Address.getText().toString().equals(web.getTitle()))
                    Address.setText(web.getUrl());

            }
        });


        Address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Address.setText(web.getTitle());
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem i = menu.findItem(R.id.desktop);
        return true;
    }



    public void loadImagefromGallery() {


        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sp=getSharedPreferences("setback", MODE_PRIVATE);
        layout1 = findViewById(R.id.layout1);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.MediaColumns.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgpath = cursor.getString(columnIndex);
                Log.d("path", imgpath);
                cursor.close();

                SharedPreferences.Editor edit=sp.edit();
                edit.putString("imagepath",imgpath);
                edit.apply();


            }
            else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }

    public void setDesktopMode(WebView webView,boolean enabled) {
        /*String newUserAgent = webView.getSettings().getUserAgentString();
        if (enabled) {
            try {
                String ua = webView.getSettings().getUserAgentString();
                String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
                newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        webView.getSettings().setUserAgentString(newUserAgent);
        webView.getSettings().setUseWideViewPort(enabled);
        webView.getSettings().setLoadWithOverviewMode(enabled);*/
        if(enabled)
            webView.getSettings().setUserAgentString(DESKTOP_USER_AGENT);
        else
            webView.getSettings().setUserAgentString(MOBILE_USER_AGENT);
        webView.reload();
    }


    private static final String[] WEB = new String[]{
            "www.amazon.com", "www.android.com", "www.bing.com", "www.craiglist.com", "www.diply.com", "www.ebay.com", "www.facebook.com", "www.flipkart.com", "www.google.com", "www.huffingtonpost.com", "www.hotmail.com", "www.imgur.com",
            "www.jcpenney.com", "www.kohls.com", "www.live.com", "www.msn.com", "www.netflix.com", "www.outbrain.com", "www.outlook.com", "www.pinterest.com", "www.qvc.com",
            "www.reddit.com", "www.slickdeals.com", "www.snapdeal.com", "www.twitter.com", "www.theguardian.com", "www.theverge.com", "www.techsavvydotcom.wordpress.com", "www.usps.com", "www.verizonwireless.com", "www.wikipedia.org", "www.xfinity.com", "www.youtube.com", "www.zillow.com",
            "amazon.com", "android.com", "bing.com", "craiglist.com", "diply.com", "ebay.com", "facebook.com", "flipkart.com", "google.com", "huffingtonpost.com", "hotmail.com", "imgur.com",
            "jcpenney.com", "kohls.com", "live.com", "msn.com", "netflix.com", "outbrain.com", "outlook.com", "pinterest.com", "qvc.com",
            "reddit.com", "slickdeals.com", "snapdeal.com", "twitter.com", "theguardian.com", "theverge.com", "techsavvydotcom.wordpress.com", "usps.com", "verizonwireless.com", "wikipedia.org", "xfinity.com", "youtube.com", "zillow.com"
    };



    public void clear(View view) {
        Address.setText(null);
        Address.setCursorVisible(true);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Display display = getWindowManager().getDefaultDisplay();
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            int screenWidth = display.getWidth();
            ViewGroup.LayoutParams params = Address.getLayoutParams();
            params.width = screenWidth - 200;
            Address.setLayoutParams(params);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            ViewGroup.LayoutParams params = Address.getLayoutParams();
            tPreferences = getSharedPreferences("Width", MODE_PRIVATE);
            int width = tPreferences.getInt("width", 220);
            params.width = width;
            Address.setLayoutParams(params);


        }
    }




    @Override

    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        web = findViewById(R.id.web);

        if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
            web.goBack();

            return true;
        }


        else
            return super.onKeyDown(keyCode, event);
    }

    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if( checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED&& (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)){

                Log.e("Permission error", "You have permission");
                return true;
            } else {

                Log.e("Permission error", "You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},1);
                return false;
            }
        } else {
            Log.e("Permission error", "You already have the permission");
            return true;
        }
    }



    private boolean isConnect() {
        ConnectivityManager c = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert c != null;
        NetworkInfo activeNetwork = c.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();

    }


    public class WebViewClientDemo extends WebViewClient {

        ProgressBar progress = findViewById(R.id.progress);




        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setProgress(0);
            view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
            Address.setText(web.getTitle());
            Address.setCursorVisible(false);
            Address.setAdapter(null);
            mswipe.setRefreshing(false);
            progress.setVisibility(View.INVISIBLE);


        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progress.setVisibility(View.VISIBLE);
            progress.setProgress(0);
            Address.setText(web.getTitle());

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("play.google.com")) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }
            else return false;


        }

    }


    private class WebChromeClientDemo extends WebChromeClient {
        public void onProgressChanged(WebView view, int prog) {

                ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress",prog);
                animation.setDuration(500); // 0.5 second
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();


        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {

            callback.invoke(origin, true, false);
        }

    }





    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            Log.d("hydrated", "onUrlChange" + url);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        web.clearHistory();
        web.clearCache(true);
        web.clearFormData();
        web.clearMatches();
        web.clearSslPreferences();
        web.clearFocus();
        clearCookiesAndCache(this);

    }

    @Override
    protected void onStop() {
        super.onStop();



        mPreferences = getSharedPreferences("maindata", MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("pos2", searchengine);
        preferencesEditor.apply();



    }

    public void clearCookiesAndCache(Context context){
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        }
        else {
            cookieManager.removeAllCookie();
        }
    }



    @Override
    protected void onStart(){
        super.onStart();



    }
    @Override
    protected void onResume(){
        super.onResume();
        if(sp.contains("imagepath")) {
            storedpath=sp.getString("imagepath", "");
            Bitmap myBitmap = BitmapFactory.decodeFile(storedpath);
            BitmapDrawable b = new BitmapDrawable(myBitmap);
            final RelativeLayout layout1 = findViewById(R.id.layout1);
            layout1.setBackgroundDrawable(b);
        }
        else{
            final RelativeLayout layout1 = findViewById(R.id.layout1);
            layout1.setBackground(getResources().getDrawable(R.drawable.dark));
        }

    }


}

