import React from 'react';
import { Shield, RefreshCw, Cpu, Radio } from 'lucide-react';

export default function Header({ onRefresh, loading }) {
  return (
    <header className="glass-card" style={{ marginBottom: '24px', padding: '18px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        
        {/* Left: Branding & Status */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{
            width: '44px',
            height: '44px',
            borderRadius: '10px',
            background: 'rgba(37, 99, 235, 0.15)',
            border: '1px solid rgba(37, 99, 235, 0.35)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Shield size={24} color="#60a5fa" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h1 style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.02em', color: '#f1f5f9' }}>
                IMPOSSIBLE TRAVEL DETECTION
              </h1>
              <span className="badge badge-low">
                <span className="pulse-live"></span>
                SOC ACTIVE
              </span>
            </div>
            <p style={{ fontSize: '0.8125rem', color: '#94a3b8', marginTop: '2px' }}>
              Hệ thống giám sát an ninh và phát hiện bất thường di chuyển địa lý
            </p>
          </div>
        </div>

        {/* Right: Engine Specs & Refresh Action */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '6px 14px',
            background: '#151923',
            borderRadius: '8px',
            border: '1px solid #2d3748',
            fontSize: '0.8125rem',
            color: '#94a3b8'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Cpu size={15} color="#60a5fa" />
              <span>Thuật toán: <strong style={{ color: '#93c5fd' }}>Haversine R=6371km</strong></span>
            </div>
            <span style={{ color: '#334155' }}>|</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Radio size={15} color="#fbbf24" />
              <span>Ngưỡng: <strong style={{ color: '#fcd34d' }}>900 km/h</strong></span>
            </div>
          </div>

          <button
            onClick={onRefresh}
            disabled={loading}
            className="btn-primary"
            style={{ cursor: loading ? 'not-allowed' : 'pointer', whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            <RefreshCw size={15} style={{ animation: loading ? 'spin 1s linear infinite' : 'none', flexShrink: 0 }} />
            <span style={{ whiteSpace: 'nowrap' }}>{loading ? 'Đang cập nhật' : 'Làm mới'}</span>
          </button>
        </div>

      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </header>
  );
}
