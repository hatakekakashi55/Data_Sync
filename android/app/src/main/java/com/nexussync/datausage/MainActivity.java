package com.nexussync.datausage;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DataUsagePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
