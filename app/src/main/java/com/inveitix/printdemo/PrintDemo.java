package com.inveitix.printdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

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
import android.graphics.Picture;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log;

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


public class PrintDemo extends Activity {
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int PERMISSIONS_REQUEST_BLUETOOTH = 1;
    private static final String TAG_REQUEST_PERMISSION = "Request permission";
    private static final int PERMISSIONS_REQUEST_INTERNET = 0;
    private static final int PERMISSIONS_REQUEST_BT_ADMIN = 2;
    private static final int PERMISSIONS_REQUEST_LOCATION = 3;
    private static final String WEB_SITE = "Remembered Web Site";
    private static final String IS_CHECKED = "Check box";
    @Bind(R.id.btn_search)
    Button btnSearch;
    @Bind(R.id.btn_print)
    Button btnSendDraw;
    @Bind(R.id.btn_open)
    Button btnSend;
    @Bind(R.id.btn_close)
    Button btnClose;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            btnClose.setEnabled(true);
                            btnSend.setEnabled(true);
                            btnSendDraw.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("State", "Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d("State", "Not found");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    btnClose.setEnabled(false);
                    btnSend.setEnabled(true);
                    btnSendDraw.setEnabled(false);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    @Bind(R.id.check_box)
    CheckBox checkBox;
    @Bind(R.id.txt_content)
    EditText edtContext;
    @Bind(R.id.web_view)
    WebView webView;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private SharedPreferences sharedPref;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        mService = new BluetoothService(this, mHandler);

        if (!mService.isAvailable()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");

        webView.setWebViewClient(new WebViewClient() {

            @SuppressLint("SdCardPath")
            @Override
            public void onPageFinished(final WebView view, String url) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Picture picture = view.capturePicture();
                        Bitmap b = Bitmap.createBitmap(
                                picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888); // tuk chupi koda
                        Canvas c = new Canvas(b);
                        picture.draw(c);

                        FileOutputStream fos;
                        try {
                            String path = Environment.getExternalStorageDirectory().toString();
                            File dir = new File(path, "/PrintDemo/media/img/");
                            if (!dir.isDirectory()) {
                                dir.mkdirs();
                            }
                            String arquivo = "darf_"+ System.currentTimeMillis() + ".jpg";
                            File file = new File(dir, arquivo);

                            fos = new FileOutputStream(file);
                            String imagePath =  file.getAbsolutePath();
                            //scan the image so show up in album
                            MediaScannerConnection.scanFile(PrintDemo.this, new String[]{imagePath},
                                    null, new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {

                                        }
                                    });

                            if (fos != null) {
                                b.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                                fos.close();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 10000);

            }
        });

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        checkPermissions();
    }

    private void setRememberedWeb() {
        if (checkBox.isChecked()) {
            String rememberedWeb = sharedPref.getString(WEB_SITE, "");
            if (!rememberedWeb.equals("")) {
                edtContext.setText(rememberedWeb);
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
        editor.putBoolean(IS_CHECKED, isChecked);
        if (isChecked) {
            editor.putString(WEB_SITE, edtContext.getText().toString());
        } else {
            editor.putString(WEB_SITE, getString(R.string.txt_content));
        }
        editor.apply();
    }

    private boolean load() {
        return sharedPref.getBoolean(IS_CHECKED, false);
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
                Toast.makeText(PrintDemo.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTPermission();
            }
            return false;
        } else if (permissionInternet == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_internet_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(PrintDemo.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestInternetPermission();
            }
            return false;
        } else if (permissionBTAdmin == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_bt_admin_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(PrintDemo.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTAdminPermission();
            }
            return false;
        } else if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            edtContext.setText(R.string.no_location_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(PrintDemo.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
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
                        PERMISSIONS_REQUEST_LOCATION);
    }

    private void requestBTAdminPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        PERMISSIONS_REQUEST_BT_ADMIN);
    }

    private void requestInternetPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                        PERMISSIONS_REQUEST_INTERNET);
    }

    private void requestBTPermission() {
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH},
                        PERMISSIONS_REQUEST_BLUETOOTH);
    }


    @Override
    public void onStart() {
        super.onStart();

        if (!mService.isBTopen()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        try {
            btnSendDraw = (Button) this.findViewById(R.id.btn_print);
            btnSendDraw.setOnClickListener(new ClickEvent());
            btnSearch = (Button) this.findViewById(R.id.btn_search);
            btnSearch.setOnClickListener(new ClickEvent());
            btnSend = (Button) this.findViewById(R.id.btn_open);
            btnSend.setOnClickListener(new ClickEvent());
            btnClose = (Button) this.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new ClickEvent());
            edtContext = (EditText) findViewById(R.id.txt_content);
            btnClose.setEnabled(false);
            btnSend.setEnabled(true);
            btnSendDraw.setEnabled(false);
        } catch (Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    con_dev = mService.getDevByMac(address);

                    mService.connect(con_dev);
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
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path, "/PrintDemo/media/img/");
        pg.drawImage(0, 0, dir.getPath());
        sendData = pg.printDraw();
        mService.write(sendData);
    }

    public void downloadContent() {
        if (!edtContext.getText().toString().equals("") && !edtContext.getText().toString().equals("https://")) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(edtContext.getText().toString())
                    .build();

            HttpService service = retrofit.create(HttpService.class);
            Call<ResponseBody> result = service.getContent();
            result.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response) {
                    try {
                        String summary = response.body().string();
                        webView.loadData(summary, "text/html; charset=utf-8", null);

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
            if (v == btnSearch) {
                Intent serverIntent = new Intent(PrintDemo.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else if (v == btnSend) {
                downloadContent();
            } else if (v == btnClose) {
                mService.stop();
            } else if (v == btnSendDraw) { //tova go printira
                String msg = "";
                String lang = getString(R.string.strLang);
                printImage();

                byte[] cmd = new byte[3];
                cmd[0] = 0x1b;
                cmd[1] = 0x21;
                if ((lang.compareTo("en")) == 0) {
                    cmd[2] |= 0x10;
                    mService.write(cmd);
                    mService.sendMessage("Congratulations!\n", "GBK");
                    cmd[2] &= 0xEF;
                    mService.write(cmd);
                    msg = "  You have sucessfully created communications between your device and our bluetooth printer.\n\n"
                            + "  the company is a high-tech enterprise which specializes" +
                            " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";


                    mService.sendMessage(msg, "GBK");
                } else if ((lang.compareTo("ch")) == 0) {
                    cmd[2] |= 0x10;
                    mService.write(cmd);
                    mService.sendMessage("Message\n", "GBK");
                    cmd[2] &= 0xEF;
                    mService.write(cmd);
                    msg = "  Message\n\n"
                            + "  another message.\n\n";

                    mService.sendMessage(msg, "GBK");
                }
            }
        }
    }
}
