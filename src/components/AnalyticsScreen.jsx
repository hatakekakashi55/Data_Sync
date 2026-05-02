import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, Cell, PieChart, Pie } from 'recharts';
import { subscribe, formatBytes, getUsageSnapshot } from '../services/dataService';

const COLORS = ['#DC143C', '#8B0000', '#FF4D6A', '#FFB800'];

export default function AnalyticsScreen() {
  const [data, setData] = useState(getUsageSnapshot());

  useEffect(() => {
    const unsub = subscribe((snap) => {
      setData(snap);
    });
    return () => { unsub(); };
  }, []);

  const pieData = [
    { name: 'Daily Download', value: data.dailyDownload, color: COLORS[0] },
    { name: 'Daily Upload', value: data.dailyUpload, color: COLORS[1] }
  ].filter(d => d.value > 0);

  if (pieData.length === 0) {
    pieData.push({ name: 'No data', value: 1, color: 'rgba(255,255,255,0.1)' });
  }
  
  const barData = [
    { label: 'Today', download: data.dailyDownload, upload: data.dailyUpload },
    { label: 'This Month', download: data.monthlyDownload, upload: data.monthlyUpload }
  ];

  return (
    <div className="page-container">
      <h1 className="page-header">N E T W O R K &nbsp; I N S I G H T S</h1>

      <div className="section-title">USAGE COMPARISON</div>
      <div className="glass analytics-chart-card">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={barData} barCategoryGap="20%">
            <XAxis dataKey="label" tick={{ fill: 'rgba(255,255,255,0.35)', fontSize: 11 }} axisLine={false} tickLine={false} />
            <YAxis hide />
            <Tooltip contentStyle={{ background: '#1a1a2e', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, fontSize: 12 }}
              formatter={(v) => [formatBytes(v).full, '']} labelStyle={{ color: 'rgba(255,255,255,0.5)' }} cursor={{ fill: 'rgba(255,255,255,0.05)' }} />
            <Bar dataKey="download" name="Download" stackId="a" radius={[0, 0, 0, 0]} fill="#DC143C" fillOpacity={0.85} />
            <Bar dataKey="upload" name="Upload" stackId="a" radius={[6, 6, 0, 0]} fill="#8B0000" fillOpacity={0.85} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="section-title">DAILY DISTRIBUTION</div>
      <div className="glass pie-chart-card">
        <ResponsiveContainer width="100%" height={260}>
          <PieChart>
            <Pie data={pieData} cx="50%" cy="50%" innerRadius={45} outerRadius={80}
              paddingAngle={4} dataKey="value"
              label={({ name, percent }) => percent > 0.05 ? name : ''}
              labelLine={false}
              style={{ fontSize: 10, fill: 'rgba(255,255,255,0.7)' }}
            >
              {pieData.map((entry, i) => (
                <Cell key={i} fill={entry.color} stroke="none" />
              ))}
            </Pie>
            <Tooltip contentStyle={{ background: '#1a1a2e', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, fontSize: 12 }}
              formatter={(v) => [formatBytes(v).full, 'Usage']} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div style={{ height: 20 }} />
    </div>
  );
}
