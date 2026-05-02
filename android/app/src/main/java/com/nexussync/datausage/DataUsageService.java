package com.nexussync.datausage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class DataUsageService extends Service {
    private static final String CHANNEL_ID = "DataUsageChannel";
    private static final int NOTIFICATION_ID = 1001;
    private Handler handler;
    private Runnable updateRunnable;
    private NetworkStatsManager networkStatsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Tracking Data..."));
        
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        handler.post(updateRunnable);
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Data Sync - Today's Usage")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Using default round icon
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();
    }

    private void updateNotification() {
        try {
            Calendar calendar = Calendar.getInstance();
            long now = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            long total = 0;
            // Catch security exception if permission is removed
            try {
                NetworkStats.Bucket mobile = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startOfDay, now);
                total += mobile.getRxBytes() + mobile.getTxBytes();
            } catch (Exception e) {}
            
            try {
                NetworkStats.Bucket wifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startOfDay, now);
                total += wifi.getRxBytes() + wifi.getTxBytes();
            } catch (Exception e) {}

            String text = "Total Used: " + formatBytes(total);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(NOTIFICATION_ID, createNotification(text));
        } catch (Exception e) {
            Log.e("DataUsageService", "Error updating stats", e);
        }
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Data Sync Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Persistent notification for real-time data usage");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
