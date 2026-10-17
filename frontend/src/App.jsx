import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import StatCards from './components/StatCards';
import EventDetailModal from './components/EventDetailModal';
import LiveSimulationPanel from './components/LiveSimulationPanel';
import { DashboardApi } from './services/api';
import { Users, History, AlertCircle, Eye } from 'lucide-react';

export default function App() {
  const [summary, setSummary] = useState(null);
  const [events, setEvents] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedSeverity, setSelectedSeverity] = useState(null);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [selectedEventDetail, setSelectedEventDetail] = useState(null);
  const [activeTab, setActiveTab] = useState('history'); // 'history' | 'events' | 'sessions'
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

  // Mở modal chi tiết sự kiện bị chặn từ bảng Nhật ký hoạt động
  const handleOpenDetail = async (item) => {
    if (item.securityEventId) {
      try {
        const detail = await DashboardApi.getSecurityEventDetail(item.securityEventId);
        if (detail) {
          setSelectedEventDetail(detail);
          setSelectedEventId(item.securityEventId);
          return;
        }
      } catch (err) {
        console.warn('Lỗi tải chi tiết sự kiện an ninh từ API:', err);
      }
    }

    // Dự phòng hiển thị nếu sự kiện chưa kịp liên kết ID
    const isConcurrent = item.failureReason?.includes('đồng thời');
    setSelectedEventDetail({
      id: item.id,
      userId: item.userId,
      username: item.username || 'unknown',
      eventType: isConcurrent ? 'CONCURRENT_IMPOSSIBLE_TRAVEL' : 'SEQUENTIAL_IMPOSSIBLE_TRAVEL',
      severity: 'HIGH',
      actionTaken: 'BLOCKED',
      createdAt: item.loginTime,
      currentAttempt: {
        ipAddress: item.ipAddress,
        city: item.city || 'Không xác định',
        country: item.country || '',
        latitude: item.latitude,
        longitude: item.longitude,
        time: item.loginTime
      },
      referenceAttempt: {
        ipAddress: 'Hệ thống đối chiếu',
        city: 'Vị trí đăng nhập trước',
        country: '',
        latitude: null,
        longitude: null,
        time: null
      },
      metrics: {
        distanceKm: null,
        timeDiffSeconds: null,
        timeDiffMinutes: null,
        calculatedSpeedKmh: null,
        thresholdSpeedKmh: 900
      }
    });
    setSelectedEventId(item.id);
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
          background: 'rgba(239, 68, 68, 0.12)',
          border: '1px solid rgba(239, 68, 68, 0.35)',
          borderRadius: '8px',
          marginBottom: '20px',
          color: '#f87171',
          fontSize: '0.875rem'
        }}>
          <AlertCircle size={20} color="#ef4444" />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Stat Cards */}
      <StatCards summary={summary} />

      {/* Live Simulation Console */}
      <LiveSimulationPanel onSimulationComplete={refreshAll} />

      {/* Navigation Tabs */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        marginBottom: '18px',
        borderBottom: '1px solid #242c3c',
        paddingBottom: '10px'
      }}>
        <button
          onClick={() => setActiveTab('history')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: '6px',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: activeTab === 'history' ? '1px solid rgba(37, 99, 235, 0.4)' : '1px solid transparent',
            background: activeTab === 'history' ? 'rgba(37, 99, 235, 0.18)' : 'transparent',
            color: activeTab === 'history' ? '#60a5fa' : '#94a3b8',
            transition: 'all 0.15s ease',
            whiteSpace: 'nowrap'
          }}
        >
          <History size={16} />
          <span>Nhật ký hoạt động</span>
          <span className="badge badge-low" style={{ padding: '2px 7px', fontSize: '0.7rem' }}>
            {loginHistory.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('sessions')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: '6px',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            border: activeTab === 'sessions' ? '1px solid rgba(37, 99, 235, 0.4)' : '1px solid transparent',
            background: activeTab === 'sessions' ? 'rgba(37, 99, 235, 0.18)' : 'transparent',
            color: activeTab === 'sessions' ? '#60a5fa' : '#94a3b8',
            transition: 'all 0.15s ease',
            whiteSpace: 'nowrap'
          }}
        >
          <Users size={16} />
          <span>Phiên hoạt động</span>
          <span className="badge badge-low" style={{ padding: '2px 7px', fontSize: '0.7rem' }}>
            {activeSessions.length}
          </span>
        </button>
      </div>

      {/* Tab 2: Active Sessions Table */}
      {activeTab === 'sessions' && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '32px' }}>
          <h3 style={{ fontSize: '1.0625rem', fontWeight: '700', marginBottom: '16px', color: '#f1f5f9' }}>
            Danh sách phiên làm việc
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{ background: '#161b26', borderBottom: '1px solid #2d3748', color: '#94a3b8', fontSize: '0.75rem', letterSpacing: '0.03em' }}>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Tài khoản</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Session Token</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Địa chỉ IP</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Vị trí địa lý</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Tọa độ</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Thời điểm tạo</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Hoạt động cuối</th>
                </tr>
              </thead>
              <tbody>
                {activeSessions.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ padding: '30px', textAlign: 'center', color: '#64748b' }}>
                      Hiện tại chưa có phiên làm việc nào đang kết nối
                    </td>
                  </tr>
                ) : (
                  activeSessions.map((s) => (
                    <tr key={s.id} style={{ borderBottom: '1px solid #222a3a' }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#222938'} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                      <td style={{ padding: '12px 14px', fontWeight: '700', color: '#60a5fa' }}>{s.username}</td>
                      <td style={{ padding: '12px 14px' }}><span className="code-pill">{s.sessionToken}</span></td>
                      <td style={{ padding: '12px 14px', color: '#cbd5e1' }}>{s.ipAddress}</td>
                      <td style={{ padding: '12px 14px', color: '#f1f5f9' }}>{s.city}, {s.country}</td>
                      <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: '#94a3b8' }}>
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

      {/* Tab 3: Activity Log Table */}
      {activeTab === 'history' && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <div>
              <h3 style={{ fontSize: '1.0625rem', fontWeight: '700', color: '#f1f5f9', margin: 0 }}>
                Nhật ký hoạt động
              </h3>
              <p style={{ margin: '4px 0 0 0', fontSize: '0.8125rem', color: '#64748b' }}>
                Theo dõi toàn bộ hoạt động đăng nhập, đăng xuất và các nỗ lực truy cập bị chặn hoặc sai thông tin
              </p>
            </div>
            <span className="badge badge-low" style={{ padding: '4px 10px' }}>
              {loginHistory.length} bản ghi
            </span>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{ background: '#161b26', borderBottom: '1px solid #2d3748', color: '#94a3b8', fontSize: '0.75rem', letterSpacing: '0.03em' }}>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Thời gian</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Tài khoản</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Địa chỉ IP</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Vị trí</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Hoạt động</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600' }}>Chi tiết</th>
                  <th style={{ padding: '12px 14px', fontWeight: '600', textAlign: 'center', whiteSpace: 'nowrap' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {loginHistory.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ padding: '30px', textAlign: 'center', color: '#64748b' }}>
                      Chưa có dữ liệu nhật ký hoạt động
                    </td>
                  </tr>
                ) : (
                  loginHistory.map((h) => (
                    <tr key={h.id} style={{ borderBottom: '1px solid #222a3a' }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#222938'} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                      <td style={{ padding: '12px 14px', color: '#cbd5e1' }}>{new Date(h.loginTime).toLocaleString('vi-VN')}</td>
                      <td style={{ padding: '12px 14px', fontWeight: '700', color: '#60a5fa' }}>{h.username || 'unknown'}</td>
                      <td style={{ padding: '12px 14px' }}><span className="code-pill">{h.ipAddress}</span></td>
                      <td style={{ padding: '12px 14px', color: '#f1f5f9' }}>{h.city ? `${h.city}, ${h.country}` : 'Không xác định'}</td>
                      <td style={{ padding: '12px 14px' }}>
                        {h.status === 'SUCCESS' && (
                          <span className="badge badge-allowed">ĐĂNG NHẬP</span>
                        )}
                        {h.status === 'LOGOUT' && (
                          <span className="badge badge-logout">ĐĂNG XUẤT</span>
                        )}
                        {h.status === 'BLOCKED' && (
                          <span className="badge badge-blocked">BỊ CHẶN</span>
                        )}
                        {h.status === 'FAILED' && (
                          <span className="badge badge-failed">LỖI ĐĂNG NHẬP</span>
                        )}
                        {!['SUCCESS', 'LOGOUT', 'BLOCKED', 'FAILED'].includes(h.status) && (
                          <span className="badge">{h.status}</span>
                        )}
                      </td>
                      <td style={{ padding: '12px 14px' }}>
                        {h.status === 'SUCCESS' && (
                          <span style={{ color: '#34d399', fontWeight: '500' }}>Thành công</span>
                        )}
                        {h.status === 'LOGOUT' && (
                          <span style={{ color: '#94a3b8' }}>{h.failureReason || 'Đã kết thúc phiên'}</span>
                        )}
                        {h.status === 'BLOCKED' && (
                          <span style={{ color: '#f87171', fontWeight: '500' }}>
                            {(() => {
                              const r = h.failureReason || '';
                              if (r.includes('đồng thời') || r.includes('CONCURRENT')) {
                                return 'Phát hiện phiên đăng nhập đồng thời bất thường của tài khoản';
                              }
                              if (r.includes('khoảng cách') || r.includes('nối tiếp') || r.includes('bất khả thi') || r.includes('SEQUENTIAL') || r.includes('thời gian')) {
                                return 'Phát hiện phiên đăng nhập bất khả thi về khoảng cách trong thời gian ngắn';
                              }
                              return r || 'Phát hiện phiên đăng nhập bất khả thi về khoảng cách trong thời gian ngắn';
                            })()}
                          </span>
                        )}
                        {h.status === 'FAILED' && (
                          <span style={{ color: '#fbbf24', fontWeight: '500' }}>{h.failureReason || 'Sai thông tin xác thực'}</span>
                        )}
                        {!['SUCCESS', 'LOGOUT', 'BLOCKED', 'FAILED'].includes(h.status) && (
                          <span style={{ color: '#94a3b8' }}>{h.failureReason || '—'}</span>
                        )}
                      </td>
                      <td style={{ padding: '12px 14px', textAlign: 'center', whiteSpace: 'nowrap' }}>
                        {h.status === 'BLOCKED' ? (
                          <button
                            onClick={() => handleOpenDetail(h)}
                            style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '6px',
                              padding: '5px 12px',
                              borderRadius: '6px',
                              fontSize: '0.75rem',
                              fontWeight: '600',
                              color: '#60a5fa',
                              background: 'rgba(37, 99, 235, 0.15)',
                              border: '1px solid rgba(37, 99, 235, 0.35)',
                              cursor: 'pointer',
                              transition: 'all 0.15s ease',
                              whiteSpace: 'nowrap',
                              flexShrink: 0
                            }}
                            onMouseEnter={(e) => {
                              e.currentTarget.style.background = '#2563eb';
                              e.currentTarget.style.color = '#ffffff';
                            }}
                            onMouseLeave={(e) => {
                              e.currentTarget.style.background = 'rgba(37, 99, 235, 0.15)';
                              e.currentTarget.style.color = '#60a5fa';
                            }}
                          >
                            <Eye size={13} style={{ flexShrink: 0 }} />
                            <span style={{ whiteSpace: 'nowrap' }}>Chi tiết</span>
                          </button>
                        ) : (
                          <span style={{ color: '#475569' }}>—</span>
                        )}
                      </td>
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
        borderTop: '1px solid #242c3c',
        textAlign: 'center',
        fontSize: '0.8125rem',
        color: '#64748b'
      }}>
        Hệ thống phát hiện di chuyển bất khả thi • Đại học Phenikaa 2026
      </footer>

    </div>
  );
}
