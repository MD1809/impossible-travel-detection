import React, { useState } from 'react';
import { ShieldAlert, AlertTriangle, ArrowRight, Eye, Search, Globe, ChevronLeft, ChevronRight } from 'lucide-react';

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
          <span style={{ color: '#f87171', fontWeight: '600' }}>Concurrent Login</span>
          <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Xung đột phiên đồng thời</span>
        </div>
      );
    }
    return (
      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <span style={{ color: '#fbbf24', fontWeight: '600' }}>Sequential Travel</span>
        <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Di chuyển tuần tự quá tốc độ</span>
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
          <ShieldAlert size={22} color="#ef4444" />
          <h2 style={{ fontSize: '1.125rem', fontWeight: '700', color: '#f1f5f9' }}>
            Sự kiện an ninh
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
            background: '#151923',
            border: '1px solid #2d3748',
            borderRadius: '8px',
            padding: '6px 12px'
          }}>
            <Search size={15} color="#94a3b8" />
            <input
              type="text"
              placeholder="Tìm theo user, IP, địa điểm..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                background: 'transparent',
                border: 'none',
                outline: 'none',
                color: '#f1f5f9',
                fontSize: '0.8125rem',
                width: '200px'
              }}
            />
          </div>

          {/* Severity Filters */}
          <div style={{ display: 'flex', gap: '6px' }}>
            {['ALL', 'CRITICAL', 'HIGH'].map((sev) => {
              const isSelected = selectedSeverity === sev || (sev === 'ALL' && !selectedSeverity);
              return (
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
                    background: isSelected ? '#2563eb' : '#232a3b',
                    borderColor: isSelected ? '#2563eb' : '#354157',
                    color: isSelected ? '#ffffff' : '#94a3b8',
                    transition: 'all 0.15s ease',
                    whiteSpace: 'nowrap',
                    flexShrink: 0
                  }}
                >
                  {sev}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Table Container */}
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
          <thead>
            <tr style={{
              background: '#161b26',
              borderBottom: '1px solid #2d3748',
              color: '#94a3b8',
              fontSize: '0.75rem',
              letterSpacing: '0.03em'
            }}>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Thời gian</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Tài khoản</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Loại vi phạm</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>IP truy cập</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Lộ trình di chuyển</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Khoảng cách và vận tốc</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Mức độ</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', whiteSpace: 'nowrap' }}>Trạng thái</th>
              <th style={{ padding: '12px 14px', fontWeight: '600', textAlign: 'center', whiteSpace: 'nowrap' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filteredEvents.length === 0 ? (
              <tr>
                <td colSpan="9" style={{ padding: '40px 14px', textAlign: 'center', color: '#64748b' }}>
                  <Globe size={32} color="#354157" style={{ margin: '0 auto 10px', display: 'block' }} />
                  Chưa ghi nhận sự kiện vi phạm an ninh
                </td>
              </tr>
            ) : (
              filteredEvents.map((event) => (
                <tr
                  key={event.id}
                  style={{
                    borderBottom: '1px solid #222a3a',
                    transition: 'background-color 0.15s ease'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#222938'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  {/* Timestamp */}
                  <td style={{ padding: '14px', whiteSpace: 'nowrap', color: '#cbd5e1' }}>
                    {formatDateTime(event.createdAt)}
                  </td>

                  {/* Username */}
                  <td style={{ padding: '14px' }}>
                    <span style={{
                      fontWeight: '700',
                      color: '#60a5fa',
                      background: 'rgba(37, 99, 235, 0.12)',
                      padding: '3px 8px',
                      borderRadius: '6px',
                      border: '1px solid rgba(37, 99, 235, 0.3)'
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
                      <ArrowRight size={13} color="#ef4444" />
                      <span style={{ color: '#f1f5f9', fontWeight: '600' }}>
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
                      <div style={{ color: '#f87171', fontWeight: '700', fontFamily: 'var(--font-mono)' }}>
                        {event.calculatedSpeedKmh
                          ? (event.calculatedSpeedKmh > 1000000
                              ? '> 1.000.000 km/h'
                              : `${event.calculatedSpeedKmh.toLocaleString()} km/h`)
                          : 'N/A'}
                      </div>
                    </div>
                  </td>

                  {/* Severity */}
                  <td style={{ padding: '14px', whiteSpace: 'nowrap' }}>
                    {getSeverityBadge(event.severity)}
                  </td>

                  {/* Action Taken */}
                  <td style={{ padding: '14px', whiteSpace: 'nowrap' }}>
                    <span className="badge badge-blocked">
                      {event.actionTaken || 'BLOCKED'}
                    </span>
                  </td>

                  {/* Action Button */}
                  <td style={{ padding: '14px', textAlign: 'center', whiteSpace: 'nowrap' }}>
                    <button
                      onClick={() => onSelectEvent(event.id)}
                      className="btn-secondary"
                      style={{ padding: '5px 12px', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
                      title="Xem chi tiết tọa độ và tính toán"
                    >
                      <Eye size={13} style={{ flexShrink: 0 }} />
                      <span style={{ whiteSpace: 'nowrap' }}>Chi tiết</span>
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
          borderTop: '1px solid #242c3c'
        }}>
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
            className="btn-secondary"
            style={{ padding: '6px 12px', cursor: currentPage === 0 ? 'not-allowed' : 'pointer', opacity: currentPage === 0 ? 0.4 : 1, whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            <ChevronLeft size={14} style={{ flexShrink: 0 }} />
            <span style={{ whiteSpace: 'nowrap' }}>Trước</span>
          </button>
          <span style={{ fontSize: '0.8125rem', color: '#94a3b8', whiteSpace: 'nowrap' }}>
            Trang <strong style={{ color: '#f1f5f9' }}>{currentPage + 1}</strong> / {totalPages}
          </span>
          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            className="btn-secondary"
            style={{ padding: '6px 12px', cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: currentPage >= totalPages - 1 ? 0.4 : 1, whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            <span style={{ whiteSpace: 'nowrap' }}>Sau</span>
            <ChevronRight size={14} style={{ flexShrink: 0 }} />
          </button>
        </div>
      )}

    </div>
  );
}
