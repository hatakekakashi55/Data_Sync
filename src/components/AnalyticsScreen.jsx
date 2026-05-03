import { useState, useEffect } from 'react';
import { Smartphone, Wifi, Download, Upload, Activity } from 'lucide-react';
import { subscribe, formatBytes, getUsageSnapshot } from '../services/dataService';

export default function AnalyticsScreen() {
  const [data, setData] = useState(getUsageSnapshot());

  useEffect(() => {
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  // Safe percentage calculator
  const getPct = (val, total) => {
    if (!total || total === 0) return 0;
    return Math.min(Math.round((val / total) * 100), 100);
  };

  const mobilePct = getPct(data.dailyMobile, data.dailyTotal);
  const wifiPct = getPct(data.dailyWifi, data.dailyTotal);
  
  const dlPct = getPct(data.dailyDownload, data.dailyTotal);
  const ulPct = getPct(data.dailyUpload, data.dailyTotal);

  const mDlPct = getPct(data.monthlyDownload, data.monthlyTotal);
  const mUlPct = getPct(data.monthlyUpload, data.monthlyTotal);

  return (
    <div className="page-container">
      <h1 className="page-header">STATISTICS</h1>

      {/* TODAY'S DISTRIBUTION */}
      <div className="section-title">TODAY'S DISTRIBUTION</div>
      <div className="glass" style={{ padding: 24, marginBottom: 24 }}>
        
        {/* Mobile vs WiFi */}
        <div style={{ marginBottom: 24 }}>
          <div className="flex-between" style={{ marginBottom: 12 }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)' }}>
                <Smartphone size={16} /> <span style={{ fontSize: '0.85rem' }}>Mobile Data</span>
             </div>
             <div style={{ fontSize: '0.9rem', color: 'var(--text-primary)' }}>{formatBytes(data.dailyMobile).full} ({mobilePct}%)</div>
          </div>
          <div className="progress-bg" style={{ marginTop: 0, height: 8, background: '#111' }}>
             <div className="progress-fill" style={{ width: `${mobilePct}%`, background: '#555' }} />
          </div>
        </div>

        <div>
          <div className="flex-between" style={{ marginBottom: 12 }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)' }}>
                <Wifi size={16} /> <span style={{ fontSize: '0.85rem' }}>Wi-Fi Data</span>
             </div>
             <div style={{ fontSize: '0.9rem', color: 'var(--text-primary)' }}>{formatBytes(data.dailyWifi).full} ({wifiPct}%)</div>
          </div>
          <div className="progress-bg" style={{ marginTop: 0, height: 8, background: '#111' }}>
             <div className="progress-fill" style={{ width: `${wifiPct}%`, background: '#888' }} />
          </div>
        </div>
      </div>

      {/* TODAY'S TRAFFIC */}
      <div className="section-title">TODAY'S TRAFFIC</div>
      <div className="glass" style={{ padding: 24, marginBottom: 24 }}>
        
        <div style={{ marginBottom: 24 }}>
          <div className="flex-between" style={{ marginBottom: 12 }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)' }}>
                <Download size={16} /> <span style={{ fontSize: '0.85rem' }}>Downloaded</span>
             </div>
             <div style={{ fontSize: '0.9rem', color: 'var(--text-primary)' }}>{formatBytes(data.dailyDownload).full} ({dlPct}%)</div>
          </div>
          <div className="progress-bg" style={{ marginTop: 0, height: 8, background: '#111' }}>
             <div className="progress-fill" style={{ width: `${dlPct}%`, background: '#555' }} />
          </div>
        </div>

        <div>
          <div className="flex-between" style={{ marginBottom: 12 }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)' }}>
                <Upload size={16} /> <span style={{ fontSize: '0.85rem' }}>Uploaded</span>
             </div>
             <div style={{ fontSize: '0.9rem', color: 'var(--text-primary)' }}>{formatBytes(data.dailyUpload).full} ({ulPct}%)</div>
          </div>
          <div className="progress-bg" style={{ marginTop: 0, height: 8, background: '#111' }}>
             <div className="progress-fill" style={{ width: `${ulPct}%`, background: '#333' }} />
          </div>
        </div>
      </div>

      {/* THIS MONTH */}
      <div className="section-title">THIS MONTH (TOTAL)</div>
      <div className="glass" style={{ padding: 24 }}>
         <div className="flex-between" style={{ marginBottom: 20 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, color: 'var(--text-secondary)' }}>
               <Activity size={20} />
               <span style={{ fontWeight: '400', fontSize: '0.9rem' }}>Combined Usage</span>
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: '400', color: 'var(--text-primary)' }}>
               {formatBytes(data.monthlyTotal).full}
            </div>
         </div>
         
         {/* Simple Split Bar */}
         <div style={{ display: 'flex', height: 12, borderRadius: 6, overflow: 'hidden', background: '#111' }}>
            <div style={{ width: `${mDlPct}%`, background: '#555' }} title="Download" />
            <div style={{ width: `${mUlPct}%`, background: '#333' }} title="Upload" />
         </div>
         <div className="flex-between" style={{ marginTop: 12, fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            <div>DL: {formatBytes(data.monthlyDownload).value} {formatBytes(data.monthlyDownload).unit}</div>
            <div>UL: {formatBytes(data.monthlyUpload).value} {formatBytes(data.monthlyUpload).unit}</div>
         </div>
      </div>

      <div style={{ height: 40 }} />
    </div>
  );
}
