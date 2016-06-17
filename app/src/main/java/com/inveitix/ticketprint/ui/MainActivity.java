package com.inveitix.ticketprint.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.inveitix.ticketprint.R;
import com.inveitix.ticketprint.constants.RequestConstants;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import android.util.Log;

import io.fabric.sdk.android.Fabric;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Bind(R.id.btn_print)
    Button btnSendDraw;
    @Bind(R.id.btn_close)
    Button btnDisconnect;
    @Bind(R.id.btn_scan)
    Button btnScan;
    String path;
    File dir;
    File file;
    @Bind(R.id.web_view)
    WebView webView;
    @Bind(R.id.bottom_sheet)
    View bottomSheet;
    BluetoothService mService;
    BluetoothDevice con_dev;
    private BottomSheetBehavior mBottomSheetBehavior;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), R.string.connect_success,
                                    Toast.LENGTH_SHORT).show();
                            btnDisconnect.setVisibility(View.VISIBLE);
                            btnSendDraw.setVisibility(View.VISIBLE);
                            btnScan.setVisibility(View.GONE);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), R.string.connecting,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:

                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), R.string.conn_lost,
                            Toast.LENGTH_SHORT).show();
                    btnDisconnect.setVisibility(View.GONE);
                    btnSendDraw.setVisibility(View.GONE);
                    btnScan.setVisibility(View.VISIBLE);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), R.string.unable_conn,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    private ProgressDialog dialog;

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
        initBottomSheet();
        if (!mService.isAvailable()) {
            Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
            finish();
        }
        initWebView();
        checkPermissions();
        downloadContent();
    }

    private void initBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(200);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(200);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName(getString(R.string.encoding));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @SuppressLint("SdCardPath")
            @Override
            public void onPageFinished(final WebView view, String url) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        siteToImage();
                        dialog.cancel();
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
            dir = new File(path, getString(R.string.file_path));
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            String webCapture = "darf_temp_pic.jpg";
            file = new File(dir, webCapture);
            fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 50, fos);
            fos.flush();
            fos.close();
            b.recycle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadingListProgress() {
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
        int permissionExternalStorage =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTPermission();
            }
            return false;
        } else if (permissionInternet == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestInternetPermission();
            }
            return false;
        } else if (permissionExternalStorage == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestExternalStoragePermission();
            }
            return false;
        } else if (permissionBTAdmin == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this,
                        RequestConstants.TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTAdminPermission();
            }
            return false;
        } else if (permissionLocation == PackageManager.PERMISSION_DENIED) {
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

    private void requestExternalStoragePermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RequestConstants.WRITE_EXTERNAL_STORAGE);
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
    public void onStart() {
        super.onStart();
        if (!mService.isBTopen()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, RequestConstants.REQUEST_ENABLE_BT);
        }
        try {
            btnSendDraw.setOnClickListener(new ClickEvent());
            btnScan.setOnClickListener(new ClickEvent());
            btnDisconnect.setOnClickListener(new ClickEvent());

            if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                btnSendDraw.setVisibility(View.VISIBLE);
                btnDisconnect.setVisibility(View.VISIBLE);
            } else {
                btnDisconnect.setVisibility(View.GONE);
                btnSendDraw.setVisibility(View.GONE);
            }
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
        if (file != null) {
            file.delete();

            if (file.exists()) {
                try {
                    file.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (file.exists()) {
                    getApplicationContext().deleteFile(file.getName());
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestConstants.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_open, Toast.LENGTH_LONG).show();
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
                    if (mService.getState() == BluetoothService.STATE_CONNECTED) {
                        btnSendDraw.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        btnScan.setVisibility(View.GONE);
                    }
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

    public String validateUrl(String webAddress) {
        String validWebAddress = webAddress;
        StringBuilder builder = new StringBuilder(webAddress);
        char lastLetter = validWebAddress.charAt(webAddress.length() - 1);

        if (!validWebAddress.startsWith("http")) {
            builder.insert(0, "http://");
        }
        if (lastLetter != '/') {
            builder.append("/");
            validWebAddress = builder.toString();
        }
        validWebAddress = validWebAddress.replace(" ", "");
        return validWebAddress.trim();
    }

    private void downloadContent() {
        if (!getIntent().getStringExtra("WebAddress").equals("") && !getIntent().getStringExtra("WebAddress")
                .equals(getString(R.string.txt_content))) {
            String webAddress = validateUrl(getIntent().getStringExtra("WebAddress"));
            webView.loadUrl(webAddress);
            loadingListProgress();
        }
    }

    private void doExit() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setPositiveButton(getString(R.string.txt_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.txt_cancel), null);
        alertDialog.setMessage(getString(R.string.txt_exit));
        alertDialog.setTitle(getString(R.string.app_name));
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        doExit();
    }

    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == btnScan) {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, RequestConstants.REQUEST_CONNECT_DEVICE);
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
