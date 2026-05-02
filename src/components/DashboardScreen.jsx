import { useState, useEffect, useRef } from 'react';
import { Download, Upload, AlertTriangle, CalendarDays } from 'lucide-react';
import { subscribe, startTracking, formatBytes, formatDuration, getUsageSnapshot, requestPermissions } from '../services/dataService';

export default function DashboardScreen({ dataLimit }) {
  const [data, setData] = useState(getUsageSnapshot());
  const startedRef = useRef(false);

  useEffect(() => {
    if (!startedRef.current) {
      startTracking();
      startedRef.current = true;
    }
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  const limitBytes = dataLimit * 1024 * 1024 * 1024;
  const pct = limitBytes > 0 ? Math.min(data.dailyTotal / limitBytes, 1) : 0;
  const circumference = 2 * Math.PI * 82;
  const dashOffset = circumference - pct * circumference;

  const dl = formatBytes(data.dailyDownload);
  const ul = formatBytes(data.dailyUpload);
  const tot = formatBytes(data.dailyTotal);
  
  const mDl = formatBytes(data.monthlyDownload);
  const mUl = formatBytes(data.monthlyUpload);
  const mTot = formatBytes(data.monthlyTotal);

  const conn = data.connection;

  return (
    <div className="page-container">
      <h1 className="page-header">D A T A &nbsp; S Y N C</h1>

      {/* Connection bar */}
      <div className="connection-bar">
        <span className={`conn-dot ${conn.online ? 'online' : 'offline'}`} />
        <span>{conn.online ? 'Online' : 'Offline'}</span>
        {conn.effectiveType && <span>• {conn.effectiveType.toUpperCase()}</span>}
        {conn.downlink && <span>• {conn.downlink} Mbps</span>}
        <span>• Uptime: {formatDuration(data.sessionSeconds)}</span>
      </div>

      {!data.hasPermission && (
        <div className="glass" style={{ marginBottom: 20, borderColor: 'rgba(255,100,100,0.3)', background: 'rgba(255,100,100,0.05)' }}>
           <div style={{ display: 'flex', alignItems: 'center', gap: 12, color: '#FF4D6A' }}>
              <AlertTriangle />
              <div style={{ flex: 1, fontSize: '0.85rem' }}>
                 <strong>Permission Required</strong><br />
                 Please grant "Usage Access" to track your device's historical data usage.
              </div>
              <button 
                 onClick={requestPermissions}
                 style={{ background: '#FF4D6A', color: '#fff', border: 'none', padding: '8px 12px', borderRadius: 8, cursor: 'pointer', fontWeight: 'bold', fontSize: '0.75rem' }}
              >
                GRANT
              </button>
           </div>
        </div>
      )}

      {/* Main usage ring - Daily */}
      <div className="glass data-ring-card">
        <div className="data-ring-label">TODAY'S USAGE</div>
        <div className="live-badge"><span className="live-dot" /> LIVE DEVICE SYNC</div>
        <div className="ring-wrapper">
          <svg className="ring-svg" viewBox="0 0 200 200">
            <defs>
              <linearGradient id="ringGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#DC143C" />
                <stop offset="100%" stopColor="#8B0000" />
              </linearGradient>
            </defs>
            <circle className="ring-track" cx="100" cy="100" r="82" />
            <circle className="ring-progress" cx="100" cy="100" r="82"
              strokeDasharray={circumference}
              strokeDashoffset={dashOffset}
            />
          </svg>
          <div className="ring-center">
            <span className="ring-value">{tot.value}</span>
            <span className="ring-unit">{tot.unit}</span>
          </div>
        </div>
        <div className="ring-limit">{dataLimit.toFixed(1)} GB Daily Limit</div>
      </div>

      {/* Stats row - Daily */}
      <div style={{ marginTop: 16 }} className="stats-row">
        <div className="glass stat-card">
          <div className="stat-icon download"><Download /></div>
          <div>
            <div className="stat-label">DAILY DOWN</div>
            <div className="stat-value">{dl.full}</div>
          </div>
        </div>
        <div className="glass stat-card">
          <div className="stat-icon upload"><Upload /></div>
          <div>
            <div className="stat-label">DAILY UP</div>
            <div className="stat-value">{ul.full}</div>
          </div>
        </div>
      </div>

      <div className="section-title">MONTHLY OVERVIEW</div>
      <div className="glass" style={{ padding: '20px' }}>
         <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, color: 'rgba(255,255,255,0.7)' }}>
               <CalendarDays size={20} />
               <span style={{ fontWeight: '600', fontSize: '0.9rem' }}>This Month</span>
            </div>
            <div style={{ fontSize: '1.4rem', fontWeight: '700', color: '#DC143C' }}>
               {mTot.full}
            </div>
         </div>
         
         <div style={{ display: 'flex', gap: 10 }}>
            <div style={{ flex: 1, background: 'rgba(220,20,60,0.05)', padding: 10, borderRadius: 12, border: '1px solid rgba(220,20,60,0.1)' }}>
               <div style={{ fontSize: '0.65rem', color: 'rgba(255,255,255,0.5)', letterSpacing: 1, marginBottom: 4 }}>DOWNLOAD</div>
               <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{mDl.full}</div>
            </div>
            <div style={{ flex: 1, background: 'rgba(139,0,0,0.05)', padding: 10, borderRadius: 12, border: '1px solid rgba(139,0,0,0.1)' }}>
               <div style={{ fontSize: '0.65rem', color: 'rgba(255,255,255,0.5)', letterSpacing: 1, marginBottom: 4 }}>UPLOAD</div>
               <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{mUl.full}</div>
            </div>
         </div>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
