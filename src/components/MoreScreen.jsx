import { ArrowLeft, MessageSquare, Moon, Sun, ShieldCheck } from 'lucide-react';

export default function MoreScreen({ theme, onThemeToggle, onBack }) {
  return (
    <div className="page-container">
      <div className="flex-between" style={{ paddingBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <ArrowLeft onClick={onBack} style={{ cursor: 'pointer' }} />
          <h1 className="page-header" style={{ padding: 0 }}>MORE</h1>
        </div>
      </div>

      <div style={{ textAlign: 'center', padding: '40px 0' }}>
        <div style={{ 
          width: 80, 
          height: 80, 
          background: 'var(--accent-dim)', 
          borderRadius: 20, 
          margin: '0 auto 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          border: '1px solid var(--border-glass)'
        }}>
           <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <div style={{ width: 4, height: 20, background: '#555', borderRadius: 2 }} />
              <div style={{ width: 4, height: 10, background: '#888', borderRadius: 2, marginTop: 4 }} />
           </div>
        </div>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: 4 }}>Data Sync</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Made with <span style={{ color: 'var(--text-primary)', fontWeight: '600' }}>Codedth</span></p>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: 8 }}>Version 3.5.0</p>
      </div>

      <div className="section-title">PREFERENCES</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px' }} onClick={onThemeToggle}>
          <div className="setting-item-icon">
            {theme === 'dark' ? <Moon size={22} /> : <Sun size={22} />}
          </div>
          <div className="setting-item-info">
            <div className="setting-label">Appearance</div>
            <div className="setting-subtitle">{theme === 'dark' ? 'Dark Mode Active' : 'Light Mode Active'}</div>
          </div>
          <div className={`toggle-track ${theme === 'dark' ? 'on' : ''}`}>
            <div className="toggle-knob" />
          </div>
        </div>
      </div>

      <div className="section-title">COMMUNITY</div>
      <div className="glass" style={{ padding: 0 }}>
        <div className="setting-item-row" style={{ padding: '16px 20px', cursor: 'pointer' }} onClick={() => window.location.href = 'mailto:support@codedth.com?subject=Data Sync Feedback'}>
          <div className="setting-item-icon"><MessageSquare size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Send Feedback</div>
            <div className="setting-subtitle">Report bugs or suggest features</div>
          </div>
        </div>
        
        <div className="setting-item-row" style={{ padding: '16px 20px', borderBottom: 'none', cursor: 'pointer' }} onClick={() => alert("Privacy Policy: Data Sync does not collect, store, or share your network usage data. All tracking is performed locally on your device for your own awareness.")}>
          <div className="setting-item-icon"><ShieldCheck size={22} /></div>
          <div className="setting-item-info">
            <div className="setting-label">Privacy Policy</div>
            <div className="setting-subtitle">How we protect your data</div>
          </div>
        </div>
      </div>

      <div style={{ height: 40 }} />
    </div>
  );
}
