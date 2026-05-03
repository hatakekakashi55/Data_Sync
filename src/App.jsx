import { useState, useEffect } from 'react';
import DashboardScreen from './components/DashboardScreen';
import SettingsScreen from './components/SettingsScreen';
import MoreScreen from './components/MoreScreen';

export default function App() {
  const [view, setView] = useState('home'); // 'home', 'settings', 'more'
  const [dataLimit, setDataLimit] = useState(() => {
    const saved = localStorage.getItem('dataLimit');
    return saved ? parseFloat(saved) : 2.0;
  });

  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'dark';
  });

  useEffect(() => {
    localStorage.setItem('dataLimit', dataLimit.toString());
  }, [dataLimit]);

  useEffect(() => {
    localStorage.setItem('theme', theme);
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  return (
    <div className="app-wrapper">
      {view === 'home' && (
        <DashboardScreen 
          dataLimit={dataLimit} 
          onOpenSettings={() => setView('settings')} 
        />
      )}
      {view === 'settings' && (
        <SettingsScreen 
          dataLimit={dataLimit} 
          onDataLimitChange={setDataLimit} 
          onBack={() => setView('home')}
          onOpenMore={() => setView('more')}
        />
      )}
      {view === 'more' && (
        <MoreScreen 
          theme={theme}
          onThemeToggle={() => setTheme(prev => prev === 'dark' ? 'light' : 'dark')}
          onBack={() => setView('settings')}
        />
      )}
    </div>
  );
}
