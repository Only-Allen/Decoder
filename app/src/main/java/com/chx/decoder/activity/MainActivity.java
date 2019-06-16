package com.chx.decoder.activity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.chx.decoder.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String[] ALLOW_MACS = {"00:0a:f5:dc:9c:f0",
            "c4:86:e9:12:41:df"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (!isMacAllowed()) {
//            Toast.makeText(this, R.string.warn_mac_not_allowed, Toast.LENGTH_SHORT).show();
//            finish();
//        }
        initView();
    }

    public void initView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_image).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.btn_image:
                startActivity(new Intent(this, ImageActivity.class));
                break;
        }
    }

    public boolean isMacAllowed() {
        String mac = getMac();
        for (String m : ALLOW_MACS) {
            if (m.equalsIgnoreCase(mac)) {
                return true;
            }
        }
        return false;
    }

    public String getMac() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getMacAddress();
    }
}
