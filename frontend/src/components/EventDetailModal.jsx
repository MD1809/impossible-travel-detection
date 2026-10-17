import React, { useState } from 'react';
import { X, ShieldAlert, MapPin, Clock, Gauge, Compass, Copy, Check } from 'lucide-react';

export default function EventDetailModal({ eventId, eventDetail, onClose }) {
  const [copied, setCopied] = useState(false);

  if (!eventDetail) return null;

  const current = eventDetail.currentAttempt || {};
  const ref = eventDetail.referenceAttempt || {};
  const metrics = eventDetail.metrics || {};

  const handleCopyReport = () => {
    navigator.clipboard.writeText(JSON.stringify(eventDetail, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const speedRatio = (metrics.calculatedSpeedKmh && metrics.thresholdSpeedKmh)
    ? (metrics.calculatedSpeedKmh / metrics.thresholdSpeedKmh).toFixed(1)
    : null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'rgba(10, 14, 23, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 9999,
      padding: '20px'
    }}>
      <div
        className="glass-card"
        style={{
          width: '100%',
          maxWidth: '780px',
          maxHeight: '90vh',
          overflowY: 'auto',
          background: '#181e2b',
          border: '1px solid #354157',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
          borderRadius: '16px',
          padding: '28px'
        }}
      >
        {/* Header */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #2d3748',
          paddingBottom: '18px',
          marginBottom: '22px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid rgba(239, 68, 68, 0.35)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <ShieldAlert size={22} color="#f87171" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '1.1875rem', fontWeight: '800', color: '#f1f5f9' }}>
                  Hồ sơ điều tra sự kiện #{eventDetail.id}
                </h3>
                <span className="badge badge-critical">
                  {eventDetail.severity || 'CRITICAL'}
                </span>
              </div>
              <p style={{ fontSize: '0.8125rem', color: '#94a3b8', marginTop: '2px' }}>
                Tài khoản: <strong style={{ color: '#60a5fa' }}>{eventDetail.username}</strong>
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="btn-secondary"
            style={{ padding: '8px', borderRadius: '8px' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Block Type Banner */}
        <div style={{
          background: 'rgba(37, 99, 235, 0.1)',
          border: '1px solid rgba(37, 99, 235, 0.3)',
          borderRadius: '8px',
          padding: '12px 16px',
          marginBottom: '20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '8px'
        }}>
          <div>
            <span style={{ fontSize: '0.75rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Loại chặn
            </span>
            <div style={{ fontSize: '0.9375rem', fontWeight: '700', color: '#60a5fa', marginTop: '2px' }}>
              {eventDetail.eventType === 'CONCURRENT_IMPOSSIBLE_TRAVEL'
                ? 'Phát hiện phiên đăng nhập đồng thời bất thường của tài khoản'
                : 'Phát hiện phiên đăng nhập bất khả thi về khoảng cách trong thời gian ngắn'}
            </div>
          </div>
          <span className="badge badge-blocked">
            {eventDetail.eventType}
          </span>
        </div>

        {/* 2 Locations Comparison Cards */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '16px',
          marginBottom: '22px'
        }}>
          {/* Reference Location */}
          <div style={{
            background: '#1c2230',
            border: '1px solid #2d3748',
            borderRadius: '12px',
            padding: '18px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', color: '#60a5fa' }}>
              <Compass size={16} />
              <span style={{ fontSize: '0.75rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Mốc đối chiếu
              </span>
            </div>
            <div style={{ fontSize: '1.125rem', fontWeight: '700', color: '#f1f5f9', marginBottom: '6px' }}>
              {ref.city || 'N/A'}, {ref.country || ''}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '0.8125rem', color: '#94a3b8' }}>
              <div>Địa chỉ IP: <span className="code-pill">{ref.ipAddress || 'N/A'}</span></div>
              <div>Tọa độ: <strong style={{ color: '#cbd5e1' }}>[{ref.latitude?.toFixed(4)}, {ref.longitude?.toFixed(4)}]</strong></div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Clock size={13} />
                <span>Thời điểm: {formatDateTime(ref.time)}</span>
              </div>
            </div>
          </div>

          {/* Current Attempt Location */}
          <div style={{
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.35)',
            borderRadius: '12px',
            padding: '18px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', color: '#f87171' }}>
              <MapPin size={16} />
              <span style={{ fontSize: '0.75rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Lần đăng nhập mới
              </span>
            </div>
            <div style={{ fontSize: '1.125rem', fontWeight: '700', color: '#fca5a5', marginBottom: '6px' }}>
              {current.city || 'N/A'}, {current.country || ''}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '0.8125rem', color: '#94a3b8' }}>
              <div>Địa chỉ IP: <span className="code-pill" style={{ color: '#f87171' }}>{current.ipAddress || 'N/A'}</span></div>
              <div>Tọa độ: <strong style={{ color: '#cbd5e1' }}>[{current.latitude?.toFixed(4)}, {current.longitude?.toFixed(4)}]</strong></div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Clock size={13} />
                <span>Thời điểm: {formatDateTime(current.time)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Algorithm Forensics Metric Box */}
        <div style={{
          background: '#1c2230',
          border: '1px solid #2d3748',
          borderRadius: '12px',
          padding: '20px',
          marginBottom: '22px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px', color: '#fbbf24' }}>
            <Gauge size={18} />
            <h4 style={{ fontSize: '0.875rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Kết quả tính toán Haversine
            </h4>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
            gap: '14px',
            marginBottom: '16px'
          }}>
            <div style={{ background: '#151923', border: '1px solid #273142', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Khoảng cách</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#f1f5f9', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.distanceKm?.toLocaleString()} km
              </div>
            </div>

            <div style={{ background: '#151923', border: '1px solid #273142', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Thời gian chênh lệch</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#f1f5f9', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.timeDiffSeconds} giây
              </div>
            </div>

            <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.75rem', color: '#f87171' }}>Vận tốc tính toán</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#ef4444', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.calculatedSpeedKmh > 1000000 ? '> 1.000.000 km/h' : `${metrics.calculatedSpeedKmh?.toLocaleString()} km/h`}
              </div>
            </div>

            <div style={{ background: '#151923', border: '1px solid #273142', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Ngưỡng tốc độ</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#60a5fa', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.thresholdSpeedKmh} km/h
              </div>
            </div>
          </div>

          <div style={{
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.35)',
            borderLeft: '4px solid #ef4444',
            padding: '12px 14px',
            borderRadius: '6px',
            fontSize: '0.8125rem',
            color: '#fca5a5'
          }}>
            <strong>Kết luận:</strong> Vận tốc tính toán vượt{' '}
            <strong style={{ color: '#ffffff' }}>{speedRatio ? `${speedRatio} lần` : 'quá'}</strong> giới hạn vật lý của máy bay thương mại 900 km/h. Hệ thống đã kích hoạt luật an ninh{' '}
            <code className="code-pill" style={{ color: '#f87171' }}>{eventDetail.eventType}</code> và thực thi lệnh <strong>Chặn truy cập</strong>.
          </div>
        </div>

        {/* Footer Actions */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <button
            onClick={handleCopyReport}
            className="btn-secondary"
            style={{ fontSize: '0.8125rem', whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            {copied ? <Check size={15} color="#34d399" style={{ flexShrink: 0 }} /> : <Copy size={15} style={{ flexShrink: 0 }} />}
            <span style={{ whiteSpace: 'nowrap' }}>{copied ? 'Đã sao chép' : 'Sao chép JSON'}</span>
          </button>

          <button onClick={onClose} className="btn-primary" style={{ fontSize: '0.8125rem', whiteSpace: 'nowrap', flexShrink: 0 }}>
            <span style={{ whiteSpace: 'nowrap' }}>Đóng</span>
          </button>
        </div>

      </div>
    </div>
  );
}
