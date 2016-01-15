package com.inveitix.bluetoothprinterapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.io.IOException;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;


public class MainActivity extends Activity {

    private static final int PERMISSIONS_REQUEST_BLUETOOTH = 1;
    private static final String TAG_REQUEST_PERMISSION = "Request permission";
    private static final int PERMISSIONS_REQUEST_INTERNET = 0;
    private static final int PERMISSIONS_REQUEST_BT_ADMIN = 2;
    private static final int PERMISSIONS_REQUEST_LOCATION = 3;
    private static final String WEB_SITE = "Remembered Web Site";
    private static final String IS_CHECKED = "Check box";
    private static BluetoothSocket btsocket;
    private static OutputStream btoutputstream;

    byte FONT_TYPE;
    @Bind(R.id.et_web_address)
    EditText etWebAddress;
    @Bind(R.id.printButton)
    Button btnPrint;
    @Bind(R.id.textView)
    TextView textView;
    @Bind(R.id.checkBox)
    CheckBox checkBox;
    SharedPreferences sharedPref;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        checkPermissions();
    }

    private void setRememberedWeb() {
        if (checkBox.isChecked()) {
            String rememberedWeb = sharedPref.getString(WEB_SITE, "");
            if (!rememberedWeb.equals("")) {
                etWebAddress.setText(rememberedWeb);
            }
        }
    }

    @OnClick(R.id.button)
    public void downloadContent() {
        if (!etWebAddress.getText().toString().equals("") && !etWebAddress.getText().toString().equals("https://")) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(etWebAddress.getText().toString())
                    .build();

            HttpService service = retrofit.create(HttpService.class);
            Call<ResponseBody> result = service.getContent();
            result.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response) {
                    try {
                        closeKeyboard();
                        textView.setText(response.body().string());
                        btnPrint.setVisibility(View.VISIBLE);
                        dialog.cancel();

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
            editor.putString(WEB_SITE, etWebAddress.getText().toString());
        } else {
            editor.putString(WEB_SITE, getString(R.string.txt_http));
        }
        editor.apply();
    }

    private boolean load() {
        return sharedPref.getBoolean(IS_CHECKED, false);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnClick(R.id.button)
    public void loadingContent() {
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Downloading. Please wait...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(dialog.isShowing()) {
                    Toast.makeText(MainActivity.this, "Connection timeout", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            }
        }, 30000);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @OnClick(R.id.printButton)
    protected void connect() {
        if (btsocket == null) {
            Intent BTIntent = new Intent(getApplicationContext(), BTDeviceList.class);
            this.startActivityForResult(BTIntent, BTDeviceList.REQUEST_CONNECT_BT);
        } else {
            OutputStream opstream = null;
            try {
                opstream = btsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btoutputstream = opstream;
            print_bt();
        }
    }

    private void print_bt() {
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            btoutputstream = btsocket.getOutputStream();
            byte[] printformat = {0x1B, 0x21, FONT_TYPE};
            btoutputstream.write(printformat);
            String msg = textView.getText().toString();
            btoutputstream.write(msg.getBytes());
            btoutputstream.write(0x0D);
            btoutputstream.write(0x0D);
            btoutputstream.write(0x0D);
            btoutputstream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (btsocket != null) {
                btoutputstream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            etWebAddress.setText(R.string.no_bluetooth_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH)) {
                Toast.makeText(MainActivity.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTPermission();
            }
            return false;
        } else if (permissionInternet == PackageManager.PERMISSION_DENIED) {
            etWebAddress.setText(R.string.no_internet_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestInternetPermission();
            }
            return false;
        } else if (permissionBTAdmin == PackageManager.PERMISSION_DENIED) {
            etWebAddress.setText(R.string.no_bt_admin_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                Toast.makeText(MainActivity.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
            } else {
                requestBTAdminPermission();
            }
            return false;
        } else if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            etWebAddress.setText(R.string.no_location_permissions);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(MainActivity.this, TAG_REQUEST_PERMISSION, Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = BTDeviceList.getSocket();
            if (btsocket != null) {
                print_bt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface HttpService {
        @GET("/")
        Call<ResponseBody> getContent();
    }
}
