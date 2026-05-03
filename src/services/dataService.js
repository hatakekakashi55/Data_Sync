import { registerPlugin } from '@capacitor/core';
const DataUsage = registerPlugin('DataUsage');

let dailyDownload = 0;
let dailyUpload = 0;
let dailyTotal = 0;
let dailyMobile = 0;
let dailyWifi = 0;
let monthlyDownload = 0;
let monthlyUpload = 0;
let monthlyTotal = 0;
let hasPermission = true;
let permissionError = '';

let sessionStart = Date.now();
let listeners = [];
let tickInterval = null;

export function getConnectionInfo() {
  const conn = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
  if (!conn) return { type: 'unknown', downlink: null, rtt: null, effectiveType: null, online: navigator.onLine };
  return {
    type: conn.type || 'unknown',
    downlink: conn.downlink || null,
    rtt: conn.rtt || null,
    effectiveType: conn.effectiveType || null,
    online: navigator.onLine,
  };
}

export async function checkPermissions() {
  try {
    const res = await DataUsage.checkPermissions();
    hasPermission = res.hasUsageAccess;
    if (!hasPermission) permissionError = 'Requires Usage Access Permission';
    else permissionError = '';
    return hasPermission;
  } catch (e) {
    console.warn("Permission check failed", e);
    return false;
  }
}

export async function requestPermissions() {
  try {
    await DataUsage.requestPermissions();
  } catch (e) {
    console.error("Failed to request permissions", e);
  }
}

export async function startNotification() {
  try {
    if (hasPermission) {
      await DataUsage.startNotificationService();
    }
  } catch (e) {
    console.warn("Could not start notification", e);
  }
}

export async function stopNotification() {
  try {
    await DataUsage.stopNotificationService();
  } catch (e) {
    console.warn("Could not stop notification", e);
  }
}

export async function setAutoStart(enabled) {
  try {
    await DataUsage.setAutoStart({ enabled });
  } catch (e) {
    console.warn("Could not set auto start", e);
  }
}

// Fetch real historical data usage from Android Plugin
async function fetchNativeUsage() {
  try {
    const result = await DataUsage.getRealUsage();
    if (result) {
      dailyDownload = result.dailyDownload || 0;
      dailyUpload = result.dailyUpload || 0;
      dailyTotal = result.dailyTotal || 0;
      dailyMobile = result.dailyMobile || 0;
      dailyWifi = result.dailyWifi || 0;
      
      monthlyDownload = result.monthlyDownload || 0;
      monthlyUpload = result.monthlyUpload || 0;
      monthlyTotal = result.monthlyTotal || 0;
      
      hasPermission = true;
      permissionError = '';
    }
  } catch(e) {
    console.warn("Native DataUsage failed:", e);
    hasPermission = false;
    permissionError = e.message || 'Permission Denied';
  }
}

export function startTracking() {
  sessionStart = Date.now();
  
  checkPermissions().then((granted) => {
    if (granted) fetchNativeUsage();
  });
  
  // Tick every second to fetch real usage and update UI
  tickInterval = setInterval(() => {
    if (hasPermission) {
      fetchNativeUsage().then(() => {
          notifyListeners();
      });
    } else {
      checkPermissions().then(granted => {
        if(granted) fetchNativeUsage().then(notifyListeners);
        else notifyListeners();
      });
    }
  }, 1500);
}

export function stopTracking() {
  if (tickInterval) { clearInterval(tickInterval); tickInterval = null; }
}

export function subscribe(fn) {
  listeners.push(fn);
  return () => { listeners = listeners.filter(l => l !== fn); };
}

function notifyListeners() {
  const data = getUsageSnapshot();
  listeners.forEach(fn => fn(data));
}

export function getUsageSnapshot() {
  const elapsed = (Date.now() - sessionStart) / 1000;
  return {
    dailyDownload,
    dailyUpload,
    dailyTotal,
    dailyMobile,
    dailyWifi,
    monthlyDownload,
    monthlyUpload,
    monthlyTotal,
    sessionSeconds: elapsed,
    connection: getConnectionInfo(),
    hasPermission,
    permissionError
  };
}

// Demo functions removed since we are fully native
export function startDemoTraffic() { }
export function stopDemoTraffic() { }

export function formatBytes(bytes, decimals = 1) {
  if (bytes <= 0) return { value: '0', unit: 'B', full: '0 B' };
  const k = 1024;
  const suffixes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(k)), suffixes.length - 1);
  const val = (bytes / Math.pow(k, i)).toFixed(decimals);
  return { value: val, unit: suffixes[i], full: `${val} ${suffixes[i]}` };
}

export function formatDuration(seconds) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  if (h > 0) return `${h}h ${m}m`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}
