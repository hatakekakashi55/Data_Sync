import { useState, useEffect, useRef } from 'react';
import { Wifi, Smartphone, AlertTriangle, Activity, Grid, CalendarDays, ArrowLeft, Settings } from 'lucide-react';
import { subscribe, startTracking, formatBytes, getUsageSnapshot, requestPermissions, startNotification, fetchAppUsage, fetchLastMonthUsage, fetchDailyHistory } from '../services/dataService';

export default function DashboardScreen({ dataLimit, onOpenSettings }) {
  const [data, setData] = useState(getUsageSnapshot());
  const [viewState, setViewState] = useState('dashboard'); // 'dashboard', 'apps', 'lastMonth'
  const [appsData, setAppsData] = useState([]);
  const [lastMonthData, setLastMonthData] = useState(null);
  const [dailyHistory, setDailyHistory] = useState([]);
  
  const startedRef = useRef(false);

  useEffect(() => {
    if (!startedRef.current) {
      startTracking();
      if (localStorage.getItem('notifEnabled') !== 'false' && localStorage.getItem('serviceRunning') !== 'false') {
        setTimeout(startNotification, 2000);
      }
      startedRef.current = true;
    }
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  const openAppsUsage = async () => {
    setViewState('apps');
    const apps = await fetchAppUsage();
    setAppsData(apps);
  };

  const openLastMonth = async () => {
    setViewState('lastMonth');
    const [lm, history] = await Promise.all([
      fetchLastMonthUsage(),
      fetchDailyHistory()
    ]);
    setLastMonthData(lm);
    setDailyHistory(history.reverse()); // Show chronological
  };

  if (viewState === 'apps') {
    return (
      <div className="page-container">
        <div className="flex-between" style={{ paddingBottom: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, width: '100%' }}>
            <ArrowLeft onClick={() => setViewState('dashboard')} style={{ cursor: 'pointer' }} />
            <h1 className="page-header" style={{ padding: 0 }}>Apps Usage</h1>
          </div>
        </div>
        <div className="glass" style={{ padding: 0 }}>
          {appsData.length === 0 ? (
            <div className="empty-state">Loading apps data...</div>
          ) : (
            appsData.map((app, i) => (
              <div className="app-item" key={i} style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16 }}>
                {app.iconBase64 ? (
                  <img 
                    src={app.iconBase64} 
                    alt="" 
                    style={{ width: 40, height: 40, borderRadius: 8, objectFit: 'contain' }} 
                  />
                ) : (
                  <div className="app-icon" style={{ minWidth: 40, height: 40, borderRadius: 8, background: 'var(--accent-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', color: 'var(--text-muted)' }}>
                    {app.appName.charAt(0).toUpperCase()}
                  </div>
                )}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="app-name" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontWeight: '500' }}>
                    {app.appName}
                  </div>
                  <div className="app-usage" style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {app.packageName}
                  </div>
                </div>
                <div className="card-value" style={{ fontSize: '0.9rem', whiteSpace: 'nowrap' }}>
                  {formatBytes(app.totalBytes).full}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    );
  }

  if (viewState === 'lastMonth') {
    const maxVal = Math.max(...dailyHistory.map(d => d.totalBytes), 1);
    
    return (
      <div className="page-container">
        <div className="flex-between" style={{ paddingBottom: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <ArrowLeft onClick={() => setViewState('dashboard')} style={{ cursor: 'pointer' }} />
            <h1 className="page-header" style={{ padding: 0 }}>Last Month Usage</h1>
          </div>
        </div>
        {!lastMonthData ? (
           <div className="empty-state">Loading data...</div>
        ) : (
           <>
            <div className="glass" style={{ padding: 24, marginBottom: 24 }}>
                <div style={{ textAlign: 'center', marginBottom: 32 }}>
                  <div className="card-label">TOTAL LAST MONTH</div>
                  <div className="data-large">{formatBytes(lastMonthData.totalBytes).value} <span className="data-unit">{formatBytes(lastMonthData.totalBytes).unit}</span></div>
                </div>
                <div className="stats-grid">
                  <div className="glass" style={{ padding: 16, background: 'var(--bg-secondary)', borderColor: 'var(--border-glass)' }}>
                    <div className="flex-between" style={{ marginBottom: 12 }}>
                      <div className="card-label" style={{ margin: 0 }}>MOBILE</div>
                      <Smartphone size={16} color="var(--text-muted)" />
                    </div>
                    <div className="card-value" style={{ fontSize: '1.2rem' }}>{formatBytes(lastMonthData.mobileBytes).full}</div>
                  </div>
                  <div className="glass" style={{ padding: 16, background: 'var(--bg-secondary)', borderColor: 'var(--border-glass)' }}>
                    <div className="flex-between" style={{ marginBottom: 12 }}>
                      <div className="card-label" style={{ margin: 0 }}>WI-FI</div>
                      <Wifi size={16} color="var(--text-muted)" />
                    </div>
                    <div className="card-value" style={{ fontSize: '1.2rem' }}>{formatBytes(lastMonthData.wifiBytes).full}</div>
                  </div>
                </div>
            </div>

            <div className="section-title">DAILY BREAKDOWN (LAST 30 DAYS)</div>
            <div className="glass" style={{ padding: '32px 24px', marginBottom: 20 }}>
               <div style={{ height: 200, display: 'flex', alignItems: 'flex-end', gap: 4, position: 'relative', borderBottom: '1px solid var(--border-glass)' }}>
                  {/* Vertical Grid Lines */}
                  {[0.25, 0.5, 0.75, 1].map((p, i) => (
                    <div 
                      key={i} 
                      style={{ 
                        position: 'absolute', 
                        bottom: `${p * 100}%`, 
                        left: 0, 
                        right: 0, 
                        height: '1px', 
                        background: 'var(--border-glass)', 
                        opacity: 0.3,
                        zIndex: 0 
                      }} 
                    />
                  ))}

                  {dailyHistory.map((day, i) => {
                    const date = new Date(day.timestamp);
                    const isToday = i === dailyHistory.length - 1;
                    const hPct = (day.totalBytes / maxVal) * 100;
                    
                    return (
                      <div 
                        key={i} 
                        className="bar-container"
                        style={{ 
                          flex: 1, 
                          height: '100%', 
                          display: 'flex', 
                          flexDirection: 'column', 
                          justifyContent: 'flex-end',
                          zIndex: 1,
                          position: 'relative'
                        }}
                      >
                        <div 
                          className="bar-fill"
                          style={{ 
                            height: `${Math.max(hPct, 2)}%`, 
                            background: isToday ? 'var(--accent)' : 'var(--accent-dark)', 
                            borderRadius: '4px 4px 0 0',
                            width: '100%',
                            transition: 'all 0.3s ease',
                            cursor: 'pointer'
                          }} 
                          onClick={() => alert(`${date.toLocaleDateString()}: ${formatBytes(day.totalBytes).full}`)}
                        />
                      </div>
                    );
                  })}
               </div>
               
               {/* Clean X-Axis Labels */}
               <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12, padding: '0 4px' }}>
                  <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: '500' }}>
                    {new Date(dailyHistory[0]?.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                  </div>
                  <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: '500' }}>
                    {new Date(dailyHistory[Math.floor(dailyHistory.length / 2)]?.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                  </div>
                  <div style={{ fontSize: '0.65rem', color: 'var(--accent)', fontWeight: '600' }}>
                    TODAY
                  </div>
               </div>
            </div>

            <div className="glass" style={{ padding: 16, display: 'flex', alignItems: 'center', gap: 12 }}>
               <div style={{ width: 12, height: 12, borderRadius: 3, background: 'var(--accent)' }} />
               <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Today's Usage</div>
               <div style={{ width: 12, height: 12, borderRadius: 3, background: 'var(--accent-dark)', marginLeft: 16 }} />
               <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Past Days</div>
            </div>
            <div style={{ height: 40 }} />
           </>
        )}
      </div>
    );
  }

  const limitBytes = dataLimit * 1024 * 1024 * 1024;
  const tot = formatBytes(data.dailyTotal);
  const conn = data.connection;

  const totalPct = limitBytes > 0 ? Math.min((data.dailyTotal / limitBytes) * 100, 100) : 0;
  const mobilePct = limitBytes > 0 ? Math.min((data.dailyMobile / limitBytes) * 100, 100) : 0;
  const wifiPct = limitBytes > 0 ? Math.min((data.dailyWifi / limitBytes) * 100, 100) : 0;

  return (
    <div className="page-container" style={{ paddingBottom: 100 }}>
      <div className="flex-between" style={{ paddingBottom: 24 }}>
        <h1 className="page-header" style={{ padding: 0 }}>DATA SYNC</h1>
        <div className="status-pill">
          <div className={`status-dot ${conn.online ? 'active' : ''}`} />
          {conn.online ? 'CONNECTED' : 'OFFLINE'}
        </div>
      </div>

      {!data.hasPermission && (
        <div className="glass" style={{ marginBottom: 20, borderColor: 'var(--border-glass)' }}>
           <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <AlertTriangle color="#888888" />
              <div style={{ flex: 1, fontSize: '0.85rem', color: '#AAAAAA' }}>
                 <strong>Permission Required</strong><br />
                 Usage Access is needed for history.
              </div>
              <button 
                 onClick={requestPermissions}
                 style={{ background: 'var(--accent)', color: 'var(--bg-primary)', border: 'none', padding: '8px 16px', borderRadius: 8, cursor: 'pointer', fontWeight: '600', fontSize: '0.75rem' }}
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

      <div className="stats-grid" style={{ marginBottom: 32 }}>
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

      <div className="section-title">INSIGHTS</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px', cursor: 'pointer' }} onClick={openAppsUsage}>
          <div className="setting-item-icon"><Grid size={22} /></div>
          <div className="setting-item-info" style={{ minWidth: 0 }}>
            <div className="setting-label">Apps Usage</div>
            <div className="setting-subtitle">See data used by each app today</div>
          </div>
          <div className="setting-item-chevron"><ArrowLeft size={18} style={{ transform: 'rotate(180deg)' }} /></div>
        </div>
        
        <div className="setting-item-row" style={{ padding: '16px 20px', borderBottom: 'none', cursor: 'pointer' }} onClick={openLastMonth}>
          <div className="setting-item-icon"><CalendarDays size={22} /></div>
          <div className="setting-item-info" style={{ minWidth: 0 }}>
            <div className="setting-label">Last Month Usage</div>
            <div className="setting-subtitle">Check your previous billing cycle stats</div>
          </div>
          <div className="setting-item-chevron"><ArrowLeft size={18} style={{ transform: 'rotate(180deg)' }} /></div>
        </div>
      </div>

      {/* Settings Button at the bottom */}
      <div style={{ position: 'fixed', bottom: 32, left: 0, right: 0, padding: '0 32px', display: 'flex', justifyContent: 'center' }}>
        <button 
          onClick={onOpenSettings}
          className="glass"
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: 12, 
            padding: '12px 24px', 
            borderRadius: 30, 
            color: 'var(--text-primary)',
            fontSize: '0.9rem',
            fontWeight: '600',
            cursor: 'pointer',
            boxShadow: '0 10px 30px rgba(0,0,0,0.5)'
          }}
        >
          <Settings size={20} />
          SETTINGS
        </button>
      </div>

      <div style={{ height: 40 }} />
    </div>
  );
}
