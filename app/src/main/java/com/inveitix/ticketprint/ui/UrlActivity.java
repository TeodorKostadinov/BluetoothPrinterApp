package com.inveitix.ticketprint.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.inveitix.ticketprint.R;
import com.inveitix.ticketprint.constants.RequestConstants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UrlActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    @Bind(R.id.txt_content) EditText edtContext;
    @Bind(R.id.check_box) CheckBox checkBox;
    @Bind(R.id.btn_open)
    Button btnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        ButterKnife.bind(this);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        checkBox.setChecked(load());
        setRememberedWeb();
    }

    @OnClick(R.id.btn_open)
    public void openUrl() {
        hideKeyboard();
        if (checkBox.isChecked()) {
            checkBox.setVisibility(View.GONE);
            edtContext.setVisibility(View.GONE);
            btnOpen.setVisibility(View.GONE);
            UrlActivity.this.finish();
        }
        loadWebContent();
    }

    private void setRememberedWeb() {
        if (checkBox.isChecked()) {
            String rememberedWeb = sharedPref.getString(RequestConstants.WEB_SITE, "");
            if (!rememberedWeb.equals("")) {
                edtContext.setText(rememberedWeb);
                edtContext.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
                loadWebContent();
                UrlActivity.this.finish();
            }
        }
    }

    private void loadWebContent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("WebAddress", edtContext.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            saveState(checkBox.isChecked());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBox.setChecked(load());
    }

    private void saveState(boolean isChecked) {
        editor = sharedPref.edit();
        editor.putBoolean(RequestConstants.IS_CHECKED, isChecked);
        if (isChecked) {
            editor.putString(RequestConstants.WEB_SITE, edtContext.getText().toString());
        }
        editor.apply();
    }

    private boolean load() {
        return sharedPref.getBoolean(RequestConstants.IS_CHECKED, false);
    }

    private void hideKeyboard() {
        edtContext = (EditText) this.getCurrentFocus();
        if (edtContext != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtContext.getWindowToken(), 0);
        }
    }
}
