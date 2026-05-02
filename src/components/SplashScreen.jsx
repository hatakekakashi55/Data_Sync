import { useState, useEffect } from 'react';
import { RefreshCw } from 'lucide-react';

export default function SplashScreen({ onFinish }) {
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setHidden(true);
      setTimeout(onFinish, 600);
    }, 2800);
    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div className={`splash-screen ${hidden ? 'hidden' : ''}`}>
      <div className="splash-icon-ring">
        <RefreshCw />
      </div>
      <div className="splash-title">NEXUS SYNC</div>
      <div className="splash-subtitle">REAL-TIME DATA TRACKING</div>
      <div className="splash-loader" />
    </div>
  );
}
