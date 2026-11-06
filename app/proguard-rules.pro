# Kotlin & Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.flow.**

# Keep foreground service & broadcast receiver entrypoints
-keep class com.nexussync.datausage.DataSyncApplication { *; }
-keep class com.nexussync.datausage.MainActivity { *; }
-keep class com.nexussync.datausage.service.DataUsageForegroundService { *; }
-keep class com.nexussync.datausage.receiver.BootCompletedReceiver { *; }
