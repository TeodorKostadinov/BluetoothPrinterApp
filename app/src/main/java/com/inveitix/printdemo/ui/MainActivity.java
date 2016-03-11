package com.inveitix.printdemo.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.inveitix.printdemo.R;
import com.inveitix.printdemo.constants.RequestConstants;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

import io.fabric.sdk.android.Fabric;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_print)
    Button btnSendDraw;
    @Bind(R.id.btn_open)
    Button btnOpen;
    @Bind(R.id.btn_close)
    Button btnDisconnect;
    String path;
    File dir;
    File file;
    @Bind(R.id.check_box)
    CheckBox checkBox;
    @Bind(R.id.txt_content)
    EditText edtContext;
    @Bind(R.id.web_view)
    WebView webView;

    BluetoothService mService;
    BluetoothDevice con_dev;
    SharedPreferences sharedPref;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            btnDisconnect.setEnabled(true);
                            btnOpen.setEnabled(true);
                            btnSendDraw.setEnabled(true);
                            btnDisconnect.setVisibility(View.VISIBLE);
                            btnSendDraw.setVisibility(View.VISIBLE);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "Connecting...",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:

                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    btnDisconnect.setEnabled(false);
                    btnOpen.setEnabled(false);
                    btnSendDraw.setEnabled(false);
                    btnDisconnect.setVisibility(View.GONE);
                    btnSendDraw.setVisibility(View.GONE);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        mService = new BluetoothService(this, mHandler);

        if (!mService.isAvailable()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        initWebView();
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        checkPermissions();
    }

    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient() {

            @SuppressLint("SdCardPath")
            @Override
            public void onPageFinished(final WebView view, String url) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        siteToImage();
                    }
                }, 2000);
            }
        });
    }

    private void siteToImage() {
        webView.measure(View.MeasureSpec.makeMeasureSpec(
                        View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();
        Bitmap b = Bitmap.createBitmap(webView.getMeasuredWidth(),
                webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint();
        int iHeight = b.getHeight();
        c.drawBitmap(b, 0, iHeight, paint);
        webView.draw(c);

        FileOutputStream fos;
        try {
            path = Environment.getExternalStorageDirectory().toString();
            dir = new File(path, "/MainActivity/media/img/");
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            String arquivo = "darf_" + System.currentTimeMillis() + ".jpg";
            file = new File(dir, arquivo);
            fos = new FileOutputStream(file);
            String imagePath = file.getAbsolutePath();
            //scan the image so show up in album
            MediaScannerConnection.scanFile(MainActivity.this, new String[]{imagePath},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
            b.compress(Bitmap.CompressFormat.PNG, 50, fos);
            fos.flush();
            fos.close();
            b.recycle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRememberedWeb() {
        if (checkBox.isChecked()) {
            String rememberedWeb = sharedPref.getString(RequestConstants.WEB_SITE, "");
            if (!rememberedWeb.equals("")) {
                edtContext.setText(rememberedWeb);
                edtContext.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
                btnOpen.setVisibility(View.GONE);
                downloadContent();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState(checkBox.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBox.setChecked(load());
        setRememberedWeb();
    }

    private void saveState(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(RequestConstants.IS_CHECKED, isChecked);
        if (isChecked) {
            editor.putString(RequestConstants.WEB_SITE, edtContext.getText().toString());
        } else {
            editor.putString(RequestConstants.WEB_SITE, getString(R.string.txt_content));
        }
        editor.apply();
    }

    private boolean load() {
        return sharedPref.getBoolean(RequestConstants.IS_CHECKED, false);
    }

    private boolean checkPermissions() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int permissionInternet =
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permissionBTAdmin =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        int permissionLocation =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_bluetooth_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTPermission();
            }
            return false;
        } else if (permissionInternet == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_internet_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestInternetPermission();
            }
            return false;
        } else if (permissionBTAdmin == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_bt_admin_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTAdminPermission();
            }
            return false;
        } else if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_location_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestLocationPermission();
            }
            return false;
        } else {
            return true;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        RequestConstants.PERMISSIONS_REQUEST_LOCATION);
    }

    private void requestBTAdminPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        RequestConstants.PERMISSIONS_REQUEST_BT_ADMIN);
    }

    private void requestInternetPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                        RequestConstants.PERMISSIONS_REQUEST_INTERNET);
    }

    private void requestBTPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH},
                        RequestConstants.PERMISSIONS_REQUEST_BLUETOOTH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bt_connect:
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, RequestConstants.REQUEST_CONNECT_DEVICE);
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mService.isBTopen()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, RequestConstants.REQUEST_ENABLE_BT);
        }
        try {
            btnSendDraw.setOnClickListener(new ClickEvent());
            btnOpen.setOnClickListener(new ClickEvent());
            btnDisconnect.setOnClickListener(new ClickEvent());

            btnDisconnect.setVisibility(View.GONE);
            btnOpen.setEnabled(true);
            btnSendDraw.setVisibility(View.GONE);
        } catch (Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        if (mService != null)
            mService.stop();
        mService = null;
        deleteFile();
        super.onDestroy();
    }

    private void deleteFile() {
        file.delete();
        if(file.exists()){
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(file.exists()){
                getApplicationContext().deleteFile(file.getName());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestConstants.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
                break;
            case RequestConstants.REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    con_dev = mService.getDevByMac(address);
                    mService.connect(con_dev);
                    btnSendDraw.setVisibility(View.VISIBLE);
                    btnDisconnect.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @SuppressLint("SdCardPath")
    private void printImage() {
        byte[] sendData;
        PrintPic pg = new PrintPic();
        pg.initCanvas(384);
        pg.initPaint();
        pg.drawImage(0, 0, file.getPath());
        sendData = pg.printDraw();
        mService.write(sendData);
    }

    public void downloadContent() {
        if (!edtContext.getText().toString().equals("") && !edtContext.getText().toString().equals(getString(R.string.txt_content))) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(edtContext.getText().toString()).build();

            HttpService service = retrofit.create(HttpService.class);
            Call<ResponseBody> result = service.getContent();
            result.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            String summary = response.body().string();
                            webView.loadData(summary, "text/html; charset=utf-8", null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
        }
    }

    public interface HttpService {
        @GET("/")
        Call<ResponseBody> getContent();
    }

    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == btnOpen) {
                downloadContent();
            } else if (v == btnDisconnect) {
                mService.stop();
            } else if (v == btnSendDraw) {
                if (file != null) {
                    printImage();
                }
            }
        }
    }
}
