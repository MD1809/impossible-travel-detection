import { useState } from 'react';
import { CheckCircle2, LogOut } from 'lucide-react';
import { DashboardApi } from '../services/api';
import './WelcomePage.css';

export default function WelcomePage({ session, onLogout }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleLogout() {
    if (loading) return;
    setLoading(true);
    setError('');
    try {
      await DashboardApi.logout(session.sessionToken);
      onLogout();
    } catch (err) {
      setError(err.message || 'Không thể đăng xuất. Vui lòng thử lại.');
      setLoading(false);
    }
  }

  return (
    <main className="welcome-page">
      <section className="welcome-card" aria-labelledby="welcome-title">
        <CheckCircle2 className="welcome-icon" size={48} aria-hidden="true" />
        <h1 id="welcome-title">Đăng nhập thành công</h1>
        <p>Xin chào, <strong>{session.userInfo?.username || 'bạn'}</strong>!</p>
        <button type="button" onClick={handleLogout} disabled={loading}>
          <LogOut size={17} aria-hidden="true" />
          {loading ? 'Đang đăng xuất…' : 'Đăng xuất'}
        </button>
        {error && <p className="welcome-error" role="alert">{error}</p>}
      </section>
    </main>
  );
}
