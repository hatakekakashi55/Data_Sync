import { useState, useEffect, useRef } from 'react';
import { Wifi, Smartphone, AlertTriangle, Activity } from 'lucide-react';
import { subscribe, startTracking, formatBytes, getUsageSnapshot, requestPermissions, startNotification } from '../services/dataService';

export default function DashboardScreen({ dataLimit }) {
  const [data, setData] = useState(getUsageSnapshot());
  const startedRef = useRef(false);

  useEffect(() => {
    if (!startedRef.current) {
      startTracking();
      setTimeout(startNotification, 2000);
      startedRef.current = true;
    }
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  const limitBytes = dataLimit * 1024 * 1024 * 1024;
  const tot = formatBytes(data.dailyTotal);
  
  const mDl = formatBytes(data.monthlyTotal);

  const conn = data.connection;

  // Simple percentage calculations for progress bars
  const mobilePct = limitBytes > 0 ? Math.min((data.dailyMobile / limitBytes) * 100, 100) : 0;
  const wifiPct = limitBytes > 0 ? Math.min((data.dailyWifi / limitBytes) * 100, 100) : 0;
  const totalPct = limitBytes > 0 ? Math.min((data.dailyTotal / limitBytes) * 100, 100) : 0;

  return (
    <div className="page-container">
      <div className="flex-between" style={{ paddingBottom: 24 }}>
        <h1 className="page-header" style={{ padding: 0 }}>DATA SYNC</h1>
        <div className="status-pill">
          <div className={`status-dot ${conn.online ? 'active' : ''}`} />
          {conn.online ? 'CONNECTED' : 'OFFLINE'}
        </div>
      </div>

      {!data.hasPermission && (
        <div className="glass" style={{ marginBottom: 20, borderColor: '#333333' }}>
           <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <AlertTriangle color="#888888" />
              <div style={{ flex: 1, fontSize: '0.85rem', color: '#AAAAAA' }}>
                 <strong>Permission Required</strong><br />
                 Usage Access is needed for history.
              </div>
              <button 
                 onClick={requestPermissions}
                 style={{ background: '#333333', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: 8, cursor: 'pointer', fontWeight: '500', fontSize: '0.75rem' }}
              >
                GRANT
              </button>
           </div>
        </div>
      )}

      {/* Main Today's Usage Header */}
      <div className="section-title" style={{ marginTop: 0 }}>TODAY'S USAGE</div>
      
      <div style={{ margin: '10px 0 32px 0' }}>
        <div className="data-large">{tot.value} <span className="data-unit">{tot.unit}</span></div>
        <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: 8 }}>
          Out of {dataLimit.toFixed(1)} GB Daily Limit
        </div>
        <div className="progress-bg" style={{ height: 4 }}>
          <div className="progress-fill" style={{ width: `${totalPct}%` }} />
        </div>
      </div>

      <div className="section-title">USAGE BREAKDOWN</div>

      <div className="stats-grid">
        {/* Mobile Data Card */}
        <div className="glass" style={{ padding: 20 }}>
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <div className="card-label" style={{ margin: 0 }}>MOBILE</div>
            <Smartphone size={18} color="var(--text-muted)" />
          </div>
          <div className="card-value">{formatBytes(data.dailyMobile).full}</div>
          <div className="progress-bg">
            <div className="progress-fill" style={{ width: `${mobilePct}%` }} />
          </div>
        </div>

        {/* WiFi Data Card */}
        <div className="glass" style={{ padding: 20 }}>
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <div className="card-label" style={{ margin: 0 }}>WI-FI</div>
            <Wifi size={18} color="var(--text-muted)" />
          </div>
          <div className="card-value">{formatBytes(data.dailyWifi).full}</div>
          <div className="progress-bg">
            <div className="progress-fill" style={{ width: `${wifiPct}%` }} />
          </div>
        </div>
      </div>

      <div className="section-title">THIS MONTH</div>
      
      <div className="glass" style={{ padding: 20 }}>
         <div className="flex-between">
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, color: 'var(--text-secondary)' }}>
               <Activity size={20} />
               <span style={{ fontWeight: '400', fontSize: '0.9rem' }}>Total Combined Usage</span>
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: '400', color: 'var(--text-primary)' }}>
               {mDl.full}
            </div>
         </div>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
