import { useState } from 'react';
import { Trash2, Info, Wifi, ChevronRight } from 'lucide-react';
import { formatBytes, getUsageSnapshot } from '../services/dataService';

function Toggle({ value, onChange }) {
  return (
    <button className={`toggle-track ${value ? 'on' : ''}`} onClick={() => onChange(!value)} aria-label="Toggle">
      <span className="toggle-knob" />
    </button>
  );
}

export default function SettingsScreen({ dataLimit, onDataLimitChange }) {
  const [notifications, setNotifications] = useState(true);
  const [bgTracking, setBgTracking] = useState(true);
  const snap = getUsageSnapshot();

  return (
    <div className="page-container">
      <h1 className="page-header">C O N F I G U R A T I O N</h1>

      {/* Limits */}
      <div className="settings-section-title">LIMITS</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-row">
          <span className="setting-label">Daily Data Limit</span>
          <span className="setting-value">{dataLimit.toFixed(1)} GB</span>
        </div>
        <div className="slider-container">
          <input
            type="range"
            className="slider-input"
            min="0.5"
            max="10"
            step="0.5"
            value={dataLimit}
            onChange={(e) => onDataLimitChange(parseFloat(e.target.value))}
          />
        </div>
      </div>

      {/* Preferences */}
      <div className="settings-section-title">PREFERENCES</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-row">
          <span className="setting-label">Push Notifications</span>
          <Toggle value={notifications} onChange={setNotifications} />
        </div>
        <div className="setting-row">
          <span className="setting-label">Background Tracking</span>
          <Toggle value={bgTracking} onChange={setBgTracking} />
        </div>
        <div className="setting-item-row">
          <div className="setting-item-icon"><Wifi /></div>
          <div className="setting-item-info">
            <div className="setting-label">Network Mode</div>
            <div className="setting-subtitle">Auto-Optimize</div>
          </div>
          <div className="setting-item-chevron"><ChevronRight /></div>
        </div>
      </div>

      {/* Usage Info */}
      <div className="settings-section-title">USAGE ACCESS</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-row">
          <span className="setting-label">Historical Tracking</span>
          <span className="setting-value" style={{ color: snap.hasPermission ? 'var(--text-primary)' : '#555' }}>{snap.hasPermission ? 'Granted' : 'Denied'}</span>
        </div>
        <div className="setting-row">
          <span className="setting-label">Connection Type</span>
          <span className="setting-value">{snap.connection.effectiveType?.toUpperCase() || 'N/A'}</span>
        </div>
        <div className="setting-row">
          <span className="setting-label">Downlink Speed</span>
          <span className="setting-value">{snap.connection.downlink ? `${snap.connection.downlink} Mbps` : 'N/A'}</span>
        </div>
      </div>

      {/* System */}
      <div className="settings-section-title">SYSTEM</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row">
          <div className="setting-item-icon"><Info /></div>
          <div className="setting-item-info">
            <div className="setting-label">About Data Sync</div>
            <div className="setting-subtitle">v3.0.0 — Daily/Monthly Tracker</div>
          </div>
          <div className="setting-item-chevron"><ChevronRight /></div>
        </div>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
