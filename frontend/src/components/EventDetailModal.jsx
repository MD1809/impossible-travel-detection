import React, { useState } from 'react';
import { X, ShieldAlert, MapPin, Clock, Gauge, Compass, Copy, Check, ArrowRight } from 'lucide-react';

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
      background: 'rgba(5, 8, 16, 0.85)',
      backdropFilter: 'blur(12px)',
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
          background: '#0b1120',
          border: '1px solid rgba(225, 29, 72, 0.4)',
          boxShadow: '0 25px 60px rgba(0, 0, 0, 0.8), 0 0 35px rgba(225, 29, 72, 0.25)',
          borderRadius: '16px',
          padding: '28px'
        }}
      >
        {/* Header */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid rgba(148, 163, 184, 0.15)',
          paddingBottom: '18px',
          marginBottom: '22px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(225, 29, 72, 0.15)',
              border: '1px solid rgba(225, 29, 72, 0.4)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <ShieldAlert size={22} color="#f43f5e" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '1.1875rem', fontWeight: '800', color: '#f8fafc' }}>
                  HỒ SƠ ĐIỀU TRA SỰ KIỆN AN NINH #{eventDetail.id}
                </h3>
                <span className="badge badge-critical">
                  {eventDetail.severity || 'CRITICAL'}
                </span>
              </div>
              <p style={{ fontSize: '0.8125rem', color: '#94a3b8', marginTop: '2px' }}>
                Tài khoản mục tiêu: <strong style={{ color: '#38bdf8' }}>{eventDetail.username}</strong> (User ID: {eventDetail.userId})
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

        {/* 2 Locations Comparison Cards */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '16px',
          marginBottom: '22px'
        }}>
          {/* Reference Location */}
          <div style={{
            background: 'rgba(15, 23, 42, 0.85)',
            border: '1px solid rgba(56, 189, 248, 0.25)',
            borderRadius: '12px',
            padding: '18px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', color: '#38bdf8' }}>
              <Compass size={16} />
              <span style={{ fontSize: '0.75rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Mốc đối chiếu (Reference Base)
              </span>
            </div>
            <div style={{ fontSize: '1.125rem', fontWeight: '700', color: '#f8fafc', marginBottom: '6px' }}>
              {ref.city || 'N/A'}, {ref.country || ''}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '0.8125rem', color: '#94a3b8' }}>
              <div>IP: <span className="code-pill">{ref.ipAddress || '113.160.224.1'}</span></div>
              <div>Tọa độ: <strong style={{ color: '#cbd5e1' }}>[{ref.latitude?.toFixed(4)}, {ref.longitude?.toFixed(4)}]</strong></div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Clock size={13} />
                <span>Thời điểm: {formatDateTime(ref.time)}</span>
              </div>
            </div>
          </div>

          {/* Current Attempt Location */}
          <div style={{
            background: 'rgba(15, 23, 42, 0.85)',
            border: '1px solid rgba(225, 29, 72, 0.35)',
            borderRadius: '12px',
            padding: '18px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', color: '#f43f5e' }}>
              <MapPin size={16} />
              <span style={{ fontSize: '0.75rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Lần đăng nhập mới (Blocked Attempt)
              </span>
            </div>
            <div style={{ fontSize: '1.125rem', fontWeight: '700', color: '#fda4af', marginBottom: '6px' }}>
              {current.city || 'N/A'}, {current.country || ''}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '0.8125rem', color: '#94a3b8' }}>
              <div>IP: <span className="code-pill" style={{ color: '#fda4af' }}>{current.ipAddress || 'N/A'}</span></div>
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
          background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.6), rgba(15, 23, 42, 0.9))',
          border: '1px solid rgba(148, 163, 184, 0.2)',
          borderRadius: '12px',
          padding: '20px',
          marginBottom: '22px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px', color: '#f59e0b' }}>
            <Gauge size={18} />
            <h4 style={{ fontSize: '0.875rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              KẾT QUẢ ĐÁNH GIÁ THUẬT TOÁN HAVERSINE & VẬN TỐC (v = d / Δt)
            </h4>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
            gap: '14px',
            marginBottom: '16px'
          }}>
            <div style={{ background: 'rgba(15, 23, 42, 0.7)', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.725rem', color: '#94a3b8', textTransform: 'uppercase' }}>Khoảng cách địa lý (d)</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#f8fafc', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.distanceKm?.toLocaleString()} km
              </div>
            </div>

            <div style={{ background: 'rgba(15, 23, 42, 0.7)', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.725rem', color: '#94a3b8', textTransform: 'uppercase' }}>Độ chênh thời gian (Δt)</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#f8fafc', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.timeDiffSeconds}s ({metrics.timeDiffMinutes} phút)
              </div>
            </div>

            <div style={{ background: 'rgba(225, 29, 72, 0.1)', border: '1px solid rgba(225, 29, 72, 0.3)', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.725rem', color: '#fda4af', textTransform: 'uppercase' }}>Vận tốc suy ra (v)</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#f43f5e', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.calculatedSpeedKmh > 1000000 ? '> 1.000.000 km/h' : `${metrics.calculatedSpeedKmh?.toLocaleString()} km/h`}
              </div>
            </div>

            <div style={{ background: 'rgba(15, 23, 42, 0.7)', padding: '12px', borderRadius: '8px' }}>
              <div style={{ fontSize: '0.725rem', color: '#94a3b8', textTransform: 'uppercase' }}>Ngưỡng máy bay (V_max)</div>
              <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#38bdf8', fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {metrics.thresholdSpeedKmh} km/h
              </div>
            </div>
          </div>

          <div style={{
            background: 'rgba(225, 29, 72, 0.15)',
            borderLeft: '4px solid #f43f5e',
            padding: '10px 14px',
            borderRadius: '4px',
            fontSize: '0.8125rem',
            color: '#fecdd3'
          }}>
            <strong>Kết luận điều tra:</strong> Vận tốc di chuyển tính toán vượt{' '}
            <strong style={{ color: '#fff' }}>{speedRatio ? `${speedRatio} lần` : 'quá xa'}</strong> giới hạn vật lý của máy bay thương mại thông dụng (900 km/h). Hệ thống tự động kích hoạt luật{' '}
            <code className="code-pill" style={{ color: '#fda4af' }}>{eventDetail.eventType}</code> và đã thực thi lệnh <strong>CHẶN TRUY CẬP (HTTP 403 Forbidden)</strong>.
          </div>
        </div>

        {/* Footer Actions */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <button
            onClick={handleCopyReport}
            className="btn-secondary"
            style={{ fontSize: '0.8125rem' }}
          >
            {copied ? <Check size={15} color="#10b981" /> : <Copy size={15} />}
            <span>{copied ? 'Đã sao chép JSON!' : 'Sao chép JSON báo cáo'}</span>
          </button>

          <button onClick={onClose} className="btn-primary" style={{ fontSize: '0.8125rem' }}>
            Đóng cửa sổ
          </button>
        </div>

      </div>
    </div>
  );
}
