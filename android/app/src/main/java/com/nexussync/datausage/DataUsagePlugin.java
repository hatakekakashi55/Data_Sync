package com.nexussync.datausage;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.os.Build;
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
        
        long dailyMobileTotal = 0;
        long dailyWifiTotal = 0;

        try {
            // Daily Mobile
            NetworkStats.Bucket dailyMobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfDay, now);
            dailyRx += dailyMobile.getRxBytes();
            dailyTx += dailyMobile.getTxBytes();
            dailyMobileTotal = dailyMobile.getRxBytes() + dailyMobile.getTxBytes();
            
            // Daily WiFi
            NetworkStats.Bucket dailyWifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfDay, now);
            dailyRx += dailyWifi.getRxBytes();
            dailyTx += dailyWifi.getTxBytes();
            dailyWifiTotal = dailyWifi.getRxBytes() + dailyWifi.getTxBytes();
            
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
        
        ret.put("dailyMobile", dailyMobileTotal);
        ret.put("dailyWifi", dailyWifiTotal);
        
        ret.put("monthlyDownload", monthlyRx);
        ret.put("monthlyUpload", monthlyTx);
        ret.put("monthlyTotal", monthlyRx + monthlyTx);
        
        call.resolve(ret);
    }

    @PluginMethod
    public void startNotificationService(PluginCall call) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(new Intent(getContext(), DataUsageService.class));
            } else {
                getContext().startService(new Intent(getContext(), DataUsageService.class));
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Could not start notification service: " + e.getMessage());
        }
    }

    @PluginMethod
    public void stopNotificationService(PluginCall call) {
        try {
            getContext().stopService(new Intent(getContext(), DataUsageService.class));
            call.resolve();
        } catch (Exception e) {
            call.reject("Could not stop notification service: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getAppUsage(PluginCall call) {
        if (!hasUsageAccess()) {
            call.reject("Permission Denied");
            return;
        }

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        android.content.pm.PackageManager pm = getContext().getPackageManager();
        
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        com.getcapacitor.JSArray appsArray = new com.getcapacitor.JSArray();
        java.util.HashMap<Integer, Long> uidUsageMap = new java.util.HashMap<>();

        try {
            NetworkStats mobileStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startOfDay, now);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (mobileStats.hasNextBucket()) {
                mobileStats.getNextBucket(bucket);
                int uid = bucket.getUid();
                long bytes = bucket.getRxBytes() + bucket.getTxBytes();
                uidUsageMap.put(uid, uidUsageMap.getOrDefault(uid, 0L) + bytes);
            }
            
            NetworkStats wifiStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startOfDay, now);
            while (wifiStats.hasNextBucket()) {
                wifiStats.getNextBucket(bucket);
                int uid = bucket.getUid();
                long bytes = bucket.getRxBytes() + bucket.getTxBytes();
                uidUsageMap.put(uid, uidUsageMap.getOrDefault(uid, 0L) + bytes);
            }

            // List to sort
            java.util.List<java.util.Map.Entry<Integer, Long>> list = new java.util.ArrayList<>(uidUsageMap.entrySet());
            list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            int count = 0;
            for (java.util.Map.Entry<Integer, Long> entry : list) {
                if (count > 50) break; // Limit to top 50 apps for performance
                int uid = entry.getKey();
                long totalBytes = entry.getValue();
                
                if (totalBytes > 0) {
                    String[] packages = pm.getPackagesForUid(uid);
                    if (packages != null && packages.length > 0) {
                        try {
                            android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packages[0], 0);
                            String appName = pm.getApplicationLabel(appInfo).toString();
                            
                            JSObject appObj = new JSObject();
                            appObj.put("uid", uid);
                            appObj.put("packageName", packages[0]);
                            appObj.put("appName", appName);
                            appObj.put("totalBytes", totalBytes);

                            // Get Icon as Base64 (only for top 30 to avoid excessive JS size)
                            if (count < 30) {
                                try {
                                    android.graphics.drawable.Drawable icon = pm.getApplicationIcon(appInfo);
                                    android.graphics.Bitmap bitmap;
                                    if (icon instanceof android.graphics.drawable.BitmapDrawable) {
                                        bitmap = ((android.graphics.drawable.BitmapDrawable)icon).getBitmap();
                                    } else {
                                        bitmap = android.graphics.Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
                                        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                                        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                        icon.draw(canvas);
                                    }
                                    java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream);
                                    String base64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT);
                                    appObj.put("iconBase64", "data:image/png;base64," + base64);
                                } catch (Exception iconEx) {}
                            }
                            
                            appsArray.put(appObj);
                            count++;
                        } catch (Exception e) {}
                    }
                }
            }

        } catch (Exception e) {
            call.reject("Error querying app stats: " + e.getMessage());
            return;
        }

        JSObject ret = new JSObject();
        ret.put("apps", appsArray);
        call.resolve(ret);
    }

    @PluginMethod
    public void getDailyHistory(PluginCall call) {
        if (!hasUsageAccess()) {
            call.reject("Permission Denied");
            return;
        }

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        com.getcapacitor.JSArray historyArray = new com.getcapacitor.JSArray();
        
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            for (int i = 0; i < 30; i++) {
                long endOfDay = cal.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1;
                long startOfDay = cal.getTimeInMillis();
                
                long mobileBytes = 0;
                long wifiBytes = 0;
                
                try {
                    NetworkStats.Bucket mobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfDay, endOfDay);
                    mobileBytes = mobile.getRxBytes() + mobile.getTxBytes();
                    
                    NetworkStats.Bucket wifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfDay, endOfDay);
                    wifiBytes = wifi.getRxBytes() + wifi.getTxBytes();
                } catch (Exception e) {}
                
                JSObject dayObj = new JSObject();
                dayObj.put("timestamp", startOfDay);
                dayObj.put("mobileBytes", mobileBytes);
                dayObj.put("wifiBytes", wifiBytes);
                dayObj.put("totalBytes", mobileBytes + wifiBytes);
                historyArray.put(dayObj);
                
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
        } catch (Exception e) {
            call.reject("Failed: " + e.getMessage());
            return;
        }

        JSObject ret = new JSObject();
        ret.put("history", historyArray);
        call.resolve(ret);
    }

    @PluginMethod
    public void getLastMonthUsage(PluginCall call) {
        if (!hasUsageAccess()) {
            call.reject("Permission Denied");
            return;
        }

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long endOfLastMonth = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
        long startOfLastMonth = calendar.getTimeInMillis();

        long mobileBytes = 0;
        long wifiBytes = 0;

        try {
            NetworkStats.Bucket mobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfLastMonth, endOfLastMonth);
            mobileBytes = mobile.getRxBytes() + mobile.getTxBytes();
            
            NetworkStats.Bucket wifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfLastMonth, endOfLastMonth);
            wifiBytes = wifi.getRxBytes() + wifi.getTxBytes();
        } catch (Exception e) {
            call.reject("Failed: " + e.getMessage());
            return;
        }

        JSObject ret = new JSObject();
        ret.put("mobileBytes", mobileBytes);
        ret.put("wifiBytes", wifiBytes);
        ret.put("totalBytes", mobileBytes + wifiBytes);
        call.resolve(ret);
    }

    @PluginMethod
    public void setAutoStart(PluginCall call) {
        try {
            boolean enabled = call.getBoolean("enabled", true);
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("DataSyncPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("autoStart", enabled).apply();
            call.resolve();
        } catch (Exception e) {
            call.reject("Error setting auto start: " + e.getMessage());
        }
    }
}
