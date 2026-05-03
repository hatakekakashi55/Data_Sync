import { useState, useEffect } from 'react';
import DashboardScreen from './components/DashboardScreen';
import SettingsScreen from './components/SettingsScreen';

export default function App() {
  const [view, setView] = useState('home'); // 'home' or 'settings'
  const [dataLimit, setDataLimit] = useState(() => {
    const saved = localStorage.getItem('dataLimit');
    return saved ? parseFloat(saved) : 2.0;
  });

  useEffect(() => {
    localStorage.setItem('dataLimit', dataLimit.toString());
  }, [dataLimit]);

  return (
    <div className="app-wrapper">
      {view === 'home' ? (
        <DashboardScreen 
          dataLimit={dataLimit} 
          onOpenSettings={() => setView('settings')} 
        />
      ) : (
        <SettingsScreen 
          dataLimit={dataLimit} 
          onDataLimitChange={setDataLimit} 
          onBack={() => setView('home')}
        />
      )}
    </div>
  );
}
