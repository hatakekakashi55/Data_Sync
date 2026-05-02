import { useState } from 'react';
import { LayoutDashboard, BarChart3, Settings } from 'lucide-react';
import DashboardScreen from './components/DashboardScreen';
import AnalyticsScreen from './components/AnalyticsScreen';
import SettingsScreen from './components/SettingsScreen';

export default function App() {
  const [activeTab, setActiveTab] = useState(0);
  const [dataLimit, setDataLimit] = useState(2.0);

  const tabs = [
    { icon: LayoutDashboard, label: 'HOME' },
    { icon: BarChart3, label: 'STATS' },
    { icon: Settings, label: 'CONFIG' },
  ];

  return (
    <>
      <div className="app-wrapper">
        {activeTab === 0 && <DashboardScreen dataLimit={dataLimit} />}
        {activeTab === 1 && <AnalyticsScreen />}
        {activeTab === 2 && <SettingsScreen dataLimit={dataLimit} onDataLimitChange={setDataLimit} />}

        <nav className="bottom-nav" id="main-navigation">
          {tabs.map((tab, i) => (
            <button
              key={tab.label}
              id={`nav-${tab.label.toLowerCase()}`}
              className={`nav-item ${activeTab === i ? 'active' : ''}`}
              onClick={() => setActiveTab(i)}
            >
              <tab.icon />
              <span>{tab.label}</span>
            </button>
          ))}
        </nav>
      </div>
    </>
  );
}
