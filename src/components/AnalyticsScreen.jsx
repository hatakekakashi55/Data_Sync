import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, Cell, PieChart, Pie } from 'recharts';
import { subscribe, formatBytes, getUsageSnapshot } from '../services/dataService';

const COLORS = ['#888888', '#444444', '#666666', '#222222'];

export default function AnalyticsScreen() {
  const [data, setData] = useState(getUsageSnapshot());

  useEffect(() => {
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  const pieData = [
    { name: 'Mobile Data', value: data.dailyMobile, color: COLORS[0] },
    { name: 'Wi-Fi Data', value: data.dailyWifi, color: COLORS[1] }
  ].filter(d => d.value > 0);

  if (pieData.length === 0) {
    pieData.push({ name: 'No data', value: 1, color: '#1A1A1A' });
  }
  
  const barData = [
    { label: 'Today', download: data.dailyDownload, upload: data.dailyUpload },
    { label: 'This Month', download: data.monthlyDownload, upload: data.monthlyUpload }
  ];

  return (
    <div className="page-container">
      <h1 className="page-header">ANALYTICS</h1>

      <div className="section-title">USAGE COMPARISON</div>
      <div className="glass analytics-chart-card">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={barData} barCategoryGap="20%">
            <XAxis dataKey="label" tick={{ fill: '#888', fontSize: 11 }} axisLine={false} tickLine={false} />
            <YAxis hide />
            <Tooltip contentStyle={{ background: '#0A0A0A', border: '1px solid #222', borderRadius: 8, fontSize: 12, color: '#CCC' }}
              formatter={(v) => [formatBytes(v).full, '']} cursor={{ fill: '#111' }} />
            <Bar dataKey="download" name="Download" stackId="a" radius={[0, 0, 0, 0]} fill="#555" fillOpacity={0.85} />
            <Bar dataKey="upload" name="Upload" stackId="a" radius={[4, 4, 0, 0]} fill="#333" fillOpacity={0.85} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="section-title">DAILY DISTRIBUTION</div>
      <div className="glass pie-chart-card">
        <ResponsiveContainer width="100%" height={260}>
          <PieChart>
            <Pie data={pieData} cx="50%" cy="50%" innerRadius={45} outerRadius={80}
              paddingAngle={2} dataKey="value"
              label={({ name, percent }) => percent > 0.05 ? name : ''}
              labelLine={false}
              style={{ fontSize: 10, fill: '#888' }}
            >
              {pieData.map((entry, i) => (
                <Cell key={i} fill={entry.color} stroke="none" />
              ))}
            </Pie>
            <Tooltip contentStyle={{ background: '#0A0A0A', border: '1px solid #222', borderRadius: 8, fontSize: 12, color: '#CCC' }}
              formatter={(v) => [formatBytes(v).full, 'Usage']} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
