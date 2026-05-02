package com.nexussync.datausage;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.os.Process;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Calendar;

@CapacitorPlugin(name = "DataUsage")
public class DataUsagePlugin extends Plugin {

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("hasUsageAccess", hasUsageAccess());
        call.resolve(ret);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (!hasUsageAccess()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
        call.resolve();
    }

    @PluginMethod
    public void getRealUsage(PluginCall call) {
        if (!hasUsageAccess()) {
            call.reject("Permission Denied: PACKAGE_USAGE_STATS required");
            return;
        }

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        
        // Time calculations
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        
        // Start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = calendar.getTimeInMillis();

        long dailyRx = 0, dailyTx = 0;
        long monthlyRx = 0, monthlyTx = 0;

        try {
            // Daily Mobile
            NetworkStats.Bucket dailyMobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfDay, now);
            dailyRx += dailyMobile.getRxBytes();
            dailyTx += dailyMobile.getTxBytes();
            
            // Daily WiFi
            NetworkStats.Bucket dailyWifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfDay, now);
            dailyRx += dailyWifi.getRxBytes();
            dailyTx += dailyWifi.getTxBytes();
            
            // Monthly Mobile
            NetworkStats.Bucket monthlyMobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfMonth, now);
            monthlyRx += monthlyMobile.getRxBytes();
            monthlyTx += monthlyMobile.getTxBytes();
            
            // Monthly WiFi
            NetworkStats.Bucket monthlyWifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfMonth, now);
            monthlyRx += monthlyWifi.getRxBytes();
            monthlyTx += monthlyWifi.getTxBytes();

        } catch (Exception e) {
            call.reject("Failed to query NetworkStatsManager: " + e.getMessage());
            return;
        }

        JSObject ret = new JSObject();
        
        ret.put("dailyDownload", dailyRx);
        ret.put("dailyUpload", dailyTx);
        ret.put("dailyTotal", dailyRx + dailyTx);
        
        ret.put("monthlyDownload", monthlyRx);
        ret.put("monthlyUpload", monthlyTx);
        ret.put("monthlyTotal", monthlyRx + monthlyTx);
        
        call.resolve(ret);
    }
}
