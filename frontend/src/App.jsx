import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import StatCards from './components/StatCards';
import SecurityEventsTable from './components/SecurityEventsTable';
import EventDetailModal from './components/EventDetailModal';
import LiveSimulationPanel from './components/LiveSimulationPanel';
import { DashboardApi } from './services/api';
import { ShieldCheck, History, Radio, Users, AlertCircle } from 'lucide-react';

export default function App() {
  const [summary, setSummary] = useState(null);
  const [events, setEvents] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedSeverity, setSelectedSeverity] = useState(null);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [selectedEventDetail, setSelectedEventDetail] = useState(null);
  const [activeTab, setActiveTab] = useState('events'); // 'events' | 'sessions' | 'history'
  const [activeSessions, setActiveSessions] = useState([]);
  const [loginHistory, setLoginHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch Summary Metrics
  const fetchSummary = useCallback(async () => {
    try {
      const data = await DashboardApi.getSummary();
      setSummary(data);
    } catch (err) {
      console.error('Lỗi tải KPI Summary:', err);
    }
  }, []);

  // Fetch Security Events
  const fetchEvents = useCallback(async (page = 0, severity = selectedSeverity) => {
    try {
      const data = await DashboardApi.getSecurityEvents(page, 10, severity);
      setEvents(data.content || []);
      setTotalPages(data.totalPages || 1);
      setCurrentPage(data.currentPage || 0);
    } catch (err) {
      console.error('Lỗi tải Security Events:', err);
    }
  }, [selectedSeverity]);

  // Fetch Active Sessions
  const fetchSessions = useCallback(async () => {
    try {
      const data = await DashboardApi.getActiveSessions();
      setActiveSessions(data || []);
    } catch (err) {
      console.error('Lỗi tải Active Sessions:', err);
    }
  }, []);

  // Fetch Login History
  const fetchHistory = useCallback(async () => {
    try {
      const data = await DashboardApi.getLoginHistory(50);
      setLoginHistory(data || []);
    } catch (err) {
      console.error('Lỗi tải Login History:', err);
    }
  }, []);

  // Refresh All Data
  const refreshAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      await Promise.all([
        fetchSummary(),
        fetchEvents(currentPage, selectedSeverity),
        fetchSessions(),
        fetchHistory()
      ]);
    } catch (err) {
      setError('Không thể kết nối đến máy chủ Backend tại localhost:8080. Vui lòng đảm bảo Spring Boot đang chạy.');
    } finally {
      setLoading(false);
    }
  }, [fetchSummary, fetchEvents, fetchSessions, fetchHistory, currentPage, selectedSeverity]);

  // Load data on mount
  useEffect(() => {
    refreshAll();
    // Auto-refresh polling every 10 seconds
    const interval = setInterval(() => {
      fetchSummary();
      fetchEvents(currentPage, selectedSeverity);
    }, 10000);
    return () => clearInterval(interval);
  }, [fetchSummary, fetchEvents, currentPage, selectedSeverity]);

  // Handle Event Detail Inspection
  const handleSelectEvent = async (id) => {
    setSelectedEventId(id);
    try {
      const detail = await DashboardApi.getSecurityEventDetail(id);
      setSelectedEventDetail(detail);
    } catch (err) {
      console.error('Lỗi tải chi tiết sự kiện:', err);
    }
  };

  const handleFilterSeverity = (sev) => {
    setSelectedSeverity(sev);
    setCurrentPage(0);
    fetchEvents(0, sev);
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    fetchEvents(newPage, selectedSeverity);
  };

  return (
    <div style={{ minHeight: '100vh', padding: '24px 28px', maxWidth: '1440px', margin: '0 auto' }}>
      
      {/* Header */}
      <Header onRefresh={refreshAll} loading={loading} />

      {/* Backend connection alert if error */}
      {error && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          padding: '12px 18px',
          background: 'rgba(225, 29, 72, 0.15)',
          border: '1px solid rgba(225, 29, 72, 0.4)',
          borderRadius: '10px',
          marginBottom: '20px',
          color: '#fda4af',
          fontSize: '0.875rem'
        }}>
          <AlertCircle size={20} color="#f43f5e" />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Stat Cards */}
      <StatCards summary={summary} />

      {/* Live Simulation & Test Console */}
      <LiveSimulationPanel onSimulationComplete={refreshAll} />

      {/* Navigation Tabs */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        marginBottom: '18px',
        borderBottom: '1px solid rgba(148, 163, 184, 0.12)',
        paddingBottom: '10px'
      }}>
        <button
          onClick={() => setActiveTab('events')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: '8px',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: 'none',
            background: activeTab === 'events' ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
            color: activeTab === 'events' ? '#38bdf8' : '#94a3b8'
          }}
        >
          <Radio size={16} />
          Sự kiện an ninh (Security Alerts)
        </button>

        <button
          onClick={() => setActiveTab('sessions')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: '8px',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: 'none',
            background: activeTab === 'sessions' ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
            color: activeTab === 'sessions' ? '#38bdf8' : '#94a3b8'
          }}
        >
          <Users size={16} />
          Phiên đang hoạt động ({activeSessions.length})
        </button>

        <button
          onClick={() => setActiveTab('history')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: '8px',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: 'none',
            background: activeTab === 'history' ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
            color: activeTab === 'history' ? '#38bdf8' : '#94a3b8'
          }}
        >
          <History size={16} />
          Nhật ký đăng nhập ({loginHistory.length})
        </button>
      </div>

      {/* Tab 1: Security Events Table */}
      {activeTab === 'events' && (
        <SecurityEventsTable
          events={events}
          totalPages={totalPages}
          currentPage={currentPage}
          onPageChange={handlePageChange}
          onSelectEvent={handleSelectEvent}
          onFilterSeverity={handleFilterSeverity}
          selectedSeverity={selectedSeverity}
        />
      )}

      {/* Tab 2: Active Sessions Table */}
      {activeTab === 'sessions' && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '32px' }}>
          <h3 style={{ fontSize: '1.0625rem', fontWeight: '700', marginBottom: '16px', color: '#f8fafc' }}>
            DANH SÁCH CÁC PHIÊN LÀM VIỆC ĐANG MỞ (ACTIVE USER SESSIONS)
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.15)', color: '#94a3b8' }}>
                  <th style={{ padding: '10px 14px' }}>Tài khoản</th>
                  <th style={{ padding: '10px 14px' }}>Session Token</th>
                  <th style={{ padding: '10px 14px' }}>Địa chỉ IP</th>
                  <th style={{ padding: '10px 14px' }}>Vị trí địa lý</th>
                  <th style={{ padding: '10px 14px' }}>Tọa độ [Lat, Lon]</th>
                  <th style={{ padding: '10px 14px' }}>Thời điểm tạo</th>
                  <th style={{ padding: '10px 14px' }}>Hoạt động cuối</th>
                </tr>
              </thead>
              <tbody>
                {activeSessions.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ padding: '30px', textAlign: 'center', color: '#64748b' }}>
                      Hiện tại chưa có phiên làm việc nào đang active.
                    </td>
                  </tr>
                ) : (
                  activeSessions.map((s) => (
                    <tr key={s.id} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.08)' }}>
                      <td style={{ padding: '12px 14px', fontWeight: '700', color: '#38bdf8' }}>{s.username}</td>
                      <td style={{ padding: '12px 14px' }}><span className="code-pill">{s.sessionToken}</span></td>
                      <td style={{ padding: '12px 14px' }}>{s.ipAddress}</td>
                      <td style={{ padding: '12px 14px', color: '#f8fafc' }}>{s.city}, {s.country}</td>
                      <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: '#cbd5e1' }}>
                        [{s.latitude?.toFixed(4)}, {s.longitude?.toFixed(4)}]
                      </td>
                      <td style={{ padding: '12px 14px', color: '#94a3b8' }}>{new Date(s.loginTime).toLocaleString('vi-VN')}</td>
                      <td style={{ padding: '12px 14px', color: '#94a3b8' }}>{new Date(s.lastActivity).toLocaleString('vi-VN')}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Login History Table */}
      {activeTab === 'history' && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '32px' }}>
          <h3 style={{ fontSize: '1.0625rem', fontWeight: '700', marginBottom: '16px', color: '#f8fafc' }}>
            LỊCH SỬ TẤT CẢ LẦN ĐĂNG NHẬP (FULL LOGIN AUDIT TRAIL)
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.15)', color: '#94a3b8' }}>
                  <th style={{ padding: '10px 14px' }}>Thời gian</th>
                  <th style={{ padding: '10px 14px' }}>Tài khoản</th>
                  <th style={{ padding: '10px 14px' }}>IP</th>
                  <th style={{ padding: '10px 14px' }}>Vị trí</th>
                  <th style={{ padding: '10px 14px' }}>Trạng thái</th>
                  <th style={{ padding: '10px 14px' }}>Lý do (nếu lỗi/bị chặn)</th>
                </tr>
              </thead>
              <tbody>
                {loginHistory.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: '#64748b' }}>
                      Chưa có dữ liệu lịch sử đăng nhập.
                    </td>
                  </tr>
                ) : (
                  loginHistory.map((h) => (
                    <tr key={h.id} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.08)' }}>
                      <td style={{ padding: '12px 14px', color: '#cbd5e1' }}>{new Date(h.loginTime).toLocaleString('vi-VN')}</td>
                      <td style={{ padding: '12px 14px', fontWeight: '700', color: '#38bdf8' }}>{h.username}</td>
                      <td style={{ padding: '12px 14px' }}><span className="code-pill">{h.ipAddress}</span></td>
                      <td style={{ padding: '12px 14px', color: '#f8fafc' }}>{h.city}, {h.country}</td>
                      <td style={{ padding: '12px 14px' }}>
                        {h.status === 'SUCCESS' ? (
                          <span className="badge badge-allowed">SUCCESS</span>
                        ) : h.status === 'BLOCKED' ? (
                          <span className="badge badge-blocked">BLOCKED</span>
                        ) : (
                          <span className="badge badge-high">FAILED</span>
                        )}
                      </td>
                      <td style={{ padding: '12px 14px', color: '#fca5a5' }}>{h.failureReason || '—'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Forensic Investigation Modal */}
      {selectedEventId && (
        <EventDetailModal
          eventId={selectedEventId}
          eventDetail={selectedEventDetail}
          onClose={() => {
            setSelectedEventId(null);
            setSelectedEventDetail(null);
          }}
        />
      )}

      {/* Footer */}
      <footer style={{
        marginTop: '36px',
        paddingTop: '20px',
        borderTop: '1px solid rgba(148, 163, 184, 0.1)',
        textAlign: 'center',
        fontSize: '0.8125rem',
        color: '#64748b'
      }}>
        Đề tài: <strong>Hệ thống phát hiện di chuyển bất khả thi (Impossible Travel Detection)</strong> • Đại học Phenikaa (2026)
      </footer>

    </div>
  );
}
