import { useState, useEffect } from 'react';
import { Bell, HardDrive, Smartphone, RefreshCw, Power, ShieldAlert, ChevronRight, ArrowLeft } from 'lucide-react';
import { requestPermissions, checkPermissions, startNotification, stopNotification, setAutoStart as nativeSetAutoStart } from '../services/dataService';

export default function SettingsScreen({ dataLimit, onDataLimitChange, onBack, onOpenMore }) {
  const [hasPerm, setHasPerm] = useState(false);
  const [notifEnabled, setNotifEnabled] = useState(localStorage.getItem('notifEnabled') !== 'false');
  const [autoStart, setAutoStart] = useState(localStorage.getItem('autoStart') !== 'false');

  useEffect(() => {
    checkPermissions().then(setHasPerm);
  }, []);

  const handleLimitChange = (e) => {
    onDataLimitChange(parseFloat(e.target.value));
  };

  const toggleNotif = async () => {
    const val = !notifEnabled;
    setNotifEnabled(val);
    localStorage.setItem('notifEnabled', val);
    if (val) startNotification();
    else stopNotification();
  };

  const toggleAutoStart = () => {
    const val = !autoStart;
    setAutoStart(val);
    localStorage.setItem('autoStart', val);
    nativeSetAutoStart(val);
  };

  return (
    <div className="page-container">
      <div className="flex-between" style={{ paddingBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <ArrowLeft onClick={onBack} style={{ cursor: 'pointer' }} />
          <h1 className="page-header" style={{ padding: 0 }}>SETTINGS</h1>
        </div>
      </div>
      <div className="section-title">DATA LIMITS</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px', borderBottom: 'none' }}>
          <div className="setting-item-icon"><HardDrive size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Daily Data Warning</div>
            <div className="setting-subtitle">Alert at {dataLimit.toFixed(1)} GB</div>
          </div>
          <div className="setting-value">{dataLimit.toFixed(1)} GB</div>
        </div>
        <div className="slider-container" style={{ padding: '0 20px 20px' }}>
          <input type="range" min="0.5" max="10.0" step="0.5" value={dataLimit} onChange={handleLimitChange} className="slider-input" />
        </div>
      </div>

      <div className="section-title">NOTIFICATION & DISPLAY</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px' }} onClick={toggleNotif}>
          <div className="setting-item-icon"><Bell size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Status Bar Notification</div>
            <div className="setting-subtitle">Show live data usage in drawer</div>
          </div>
          <div className={`toggle-track ${notifEnabled ? 'on' : ''}`}>
            <div className="toggle-knob" />
          </div>
        </div>
        
        <div className="setting-item-row" style={{ padding: '16px 20px' }} onClick={toggleAutoStart}>
          <div className="setting-item-icon"><Power size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Auto Start on Boot</div>
            <div className="setting-subtitle">Start tracking when phone turns on</div>
          </div>
          <div className={`toggle-track ${autoStart ? 'on' : ''}`}>
            <div className="toggle-knob" />
          </div>
        </div>
      </div>

      <div className="section-title">SYSTEM PREFERENCES</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px' }} onClick={!hasPerm ? requestPermissions : undefined}>
          <div className="setting-item-icon"><ShieldAlert size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Usage Access Permission</div>
            <div className="setting-subtitle">{hasPerm ? 'Granted for historical tracking' : 'Required for accurate tracking'}</div>
          </div>
          <div className="setting-value" style={{ color: hasPerm ? 'var(--text-primary)' : '#555' }}>
            {hasPerm ? 'Active' : 'Missing'}
          </div>
        </div>
        
        <div className="setting-item-row" style={{ padding: '16px 20px', cursor: 'pointer' }} onClick={() => alert("Billing Cycle is currently fixed to the 1st of every month. Custom billing dates will be added in a future update.")}>
          <div className="setting-item-icon"><RefreshCw size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Billing Cycle Reset</div>
            <div className="setting-subtitle">1st of every month</div>
          </div>
          <div className="setting-item-chevron"><ChevronRight size={18} /></div>
        </div>

        <div className="setting-item-row" style={{ padding: '16px 20px' }} onClick={onOpenMore}>
          <div className="setting-item-icon"><Smartphone size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">More</div>
            <div className="setting-subtitle">App info, feedback and theme</div>
          </div>
          <div className="setting-item-chevron"><ChevronRight size={18} /></div>
        </div>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
