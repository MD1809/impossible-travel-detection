import React, { useState } from 'react';
import { ShieldAlert, AlertTriangle, ArrowRight, Eye, Filter, Search, Globe, ChevronLeft, ChevronRight } from 'lucide-react';

export default function SecurityEventsTable({ events, totalPages, currentPage, onPageChange, onSelectEvent, onFilterSeverity, selectedSeverity }) {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredEvents = (events || []).filter((item) => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      item.username?.toLowerCase().includes(term) ||
      item.ipAddress?.toLowerCase().includes(term) ||
      item.city?.toLowerCase().includes(term) ||
      item.refCity?.toLowerCase().includes(term) ||
      item.country?.toLowerCase().includes(term)
    );
  });

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <span className="badge badge-critical">
            <span className="pulse-critical"></span>
            CRITICAL
          </span>
        );
      case 'HIGH':
        return (
          <span className="badge badge-high">
            <AlertTriangle size={12} />
            HIGH
          </span>
        );
      default:
        return (
          <span className="badge badge-low">
            INFO
          </span>
        );
    }
  };

  const formatEventType = (type) => {
    if (type === 'CONCURRENT_IMPOSSIBLE_TRAVEL') {
      return (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ color: '#fda4af', fontWeight: '600' }}>Concurrent Login</span>
          <span style={{ fontSize: '0.725rem', color: '#94a3b8' }}>Xung đột phiên đồng thời</span>
        </div>
      );
    }
    return (
      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <span style={{ color: '#fcd34d', fontWeight: '600' }}>Sequential Travel</span>
        <span style={{ fontSize: '0.725rem', color: '#94a3b8' }}>Di chuyển nối tiếp siêu thanh</span>
      </div>
    );
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  return (
    <div className="glass-card" style={{ padding: '24px', marginBottom: '32px' }}>
      
      {/* Top Controls: Title, Filter & Search */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '16px',
        marginBottom: '20px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <ShieldAlert size={22} color="#f43f5e" />
          <h2 style={{ fontSize: '1.125rem', fontWeight: '700', color: '#f8fafc' }}>
            DANH SÁCH SỰ KIỆN AN NINH (SECURITY INCIDENT LOGS)
          </h2>
          <span className="badge badge-blocked">
            {filteredEvents.length} bản ghi
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          {/* Search Box */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            background: 'rgba(15, 23, 42, 0.8)',
            border: '1px solid rgba(148, 163, 184, 0.2)',
            borderRadius: '8px',
            padding: '6px 12px'
          }}>
            <Search size={15} color="#94a3b8" />
            <input
              type="text"
              placeholder="Tìm theo user, IP, thành phố..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                background: 'transparent',
                border: 'none',
                outline: 'none',
                color: '#f8fafc',
                fontSize: '0.8125rem',
                width: '200px'
              }}
            />
          </div>

          {/* Severity Filters */}
          <div style={{ display: 'flex', gap: '6px' }}>
            {['ALL', 'CRITICAL', 'HIGH'].map((sev) => (
              <button
                key={sev}
                onClick={() => onFilterSeverity(sev === 'ALL' ? null : sev)}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '0.75rem',
                  fontWeight: '600',
                  cursor: 'pointer',
                  border: '1px solid',
                  background: (selectedSeverity === sev || (sev === 'ALL' && !selectedSeverity))
                    ? 'rgba(56, 189, 248, 0.2)'
                    : 'rgba(30, 41, 59, 0.5)',
                  borderColor: (selectedSeverity === sev || (sev === 'ALL' && !selectedSeverity))
                    ? 'rgba(56, 189, 248, 0.5)'
                    : 'rgba(148, 163, 184, 0.15)',
                  color: (selectedSeverity === sev || (sev === 'ALL' && !selectedSeverity))
                    ? '#38bdf8'
                    : '#94a3b8'
                }}
              >
                {sev}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Table Container */}
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
          <thead>
            <tr style={{
              borderBottom: '1px solid rgba(148, 163, 184, 0.15)',
              color: '#94a3b8',
              textTransform: 'uppercase',
              fontSize: '0.725rem',
              letterSpacing: '0.05em'
            }}>
              <th style={{ padding: '12px 14px' }}>Thời gian</th>
              <th style={{ padding: '12px 14px' }}>Tài khoản</th>
              <th style={{ padding: '12px 14px' }}>Loại vi phạm</th>
              <th style={{ padding: '12px 14px' }}>IP truy cập</th>
              <th style={{ padding: '12px 14px' }}>Lộ trình di chuyển</th>
              <th style={{ padding: '12px 14px' }}>Khoảng cách / Vận tốc</th>
              <th style={{ padding: '12px 14px' }}>Mức độ</th>
              <th style={{ padding: '12px 14px' }}>Quyết định</th>
              <th style={{ padding: '12px 14px', textAlign: 'center' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filteredEvents.length === 0 ? (
              <tr>
                <td colSpan="9" style={{ padding: '40px 14px', textAlign: 'center', color: '#64748b' }}>
                  <Globe size={36} color="rgba(148, 163, 184, 0.3)" style={{ margin: '0 auto 10px', display: 'block' }} />
                  Chưa ghi nhận sự kiện an ninh vi phạm Impossible Travel nào.
                </td>
              </tr>
            ) : (
              filteredEvents.map((event) => (
                <tr
                  key={event.id}
                  style={{
                    borderBottom: '1px solid rgba(148, 163, 184, 0.08)',
                    transition: 'background-color 0.15s ease'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(30, 41, 59, 0.4)'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  {/* Timestamp */}
                  <td style={{ padding: '14px', whiteFont: 'nowrap', color: '#cbd5e1' }}>
                    {formatDateTime(event.createdAt)}
                  </td>

                  {/* Username */}
                  <td style={{ padding: '14px' }}>
                    <span style={{
                      fontWeight: '700',
                      color: '#38bdf8',
                      background: 'rgba(56, 189, 248, 0.1)',
                      padding: '3px 8px',
                      borderRadius: '6px',
                      border: '1px solid rgba(56, 189, 248, 0.2)'
                    }}>
                      {event.username || 'unknown'}
                    </span>
                  </td>

                  {/* Event Type */}
                  <td style={{ padding: '14px' }}>
                    {formatEventType(event.eventType)}
                  </td>

                  {/* IP Address */}
                  <td style={{ padding: '14px' }}>
                    <span className="code-pill">{event.ipAddress}</span>
                  </td>

                  {/* Route */}
                  <td style={{ padding: '14px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ color: '#94a3b8' }}>
                        {event.refCity || 'Gốc'}, {event.refCountry || ''}
                      </span>
                      <ArrowRight size={13} color="#f43f5e" />
                      <span style={{ color: '#f8fafc', fontWeight: '600' }}>
                        {event.city || 'Mới'}, {event.country || ''}
                      </span>
                    </div>
                  </td>

                  {/* Distance & Speed */}
                  <td style={{ padding: '14px' }}>
                    <div>
                      <span style={{ color: '#94a3b8' }}>
                        {event.distanceKm ? `${event.distanceKm.toLocaleString()} km` : 'N/A'}
                      </span>
                      <div style={{ color: '#f43f5e', fontWeight: '700', fontFamily: 'var(--font-mono)' }}>
                        {event.calculatedSpeedKmh
                          ? (event.calculatedSpeedKmh > 1000000
                              ? '> 1.000.000 km/h'
                              : `${event.calculatedSpeedKmh.toLocaleString()} km/h`)
                          : 'N/A'}
                      </div>
                    </div>
                  </td>

                  {/* Severity */}
                  <td style={{ padding: '14px' }}>
                    {getSeverityBadge(event.severity)}
                  </td>

                  {/* Action Taken */}
                  <td style={{ padding: '14px' }}>
                    <span className="badge badge-blocked">
                      {event.actionTaken || 'BLOCKED'}
                    </span>
                  </td>

                  {/* Action Button */}
                  <td style={{ padding: '14px', textAlign: 'center' }}>
                    <button
                      onClick={() => onSelectEvent(event.id)}
                      className="btn-secondary"
                      style={{ padding: '5px 10px', fontSize: '0.75rem' }}
                      title="Xem chi tiết tọa độ và công thức tính toán"
                    >
                      <Eye size={13} />
                      Chi tiết
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'flex-end',
          gap: '12px',
          marginTop: '20px',
          paddingTop: '16px',
          borderTop: '1px solid rgba(148, 163, 184, 0.1)'
        }}>
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
            className="btn-secondary"
            style={{ padding: '6px 12px', cursor: currentPage === 0 ? 'not-allowed' : 'pointer', opacity: currentPage === 0 ? 0.4 : 1 }}
          >
            <ChevronLeft size={14} /> Trước
          </button>
          <span style={{ fontSize: '0.8125rem', color: '#94a3b8' }}>
            Trang <strong style={{ color: '#f8fafc' }}>{currentPage + 1}</strong> / {totalPages}
          </span>
          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            className="btn-secondary"
            style={{ padding: '6px 12px', cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: currentPage >= totalPages - 1 ? 0.4 : 1 }}
          >
            Sau <ChevronRight size={14} />
          </button>
        </div>
      )}

    </div>
  );
}
