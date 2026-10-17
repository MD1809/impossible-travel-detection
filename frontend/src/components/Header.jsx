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
            background: '#e0f2fe',
            border: '1px solid #bae6fd',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Shield size={24} color="#0284c7" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h1 style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.02em', color: '#0f172a' }}>
                IMPOSSIBLE TRAVEL DETECTION
              </h1>
              <span className="badge badge-low">
                <span className="pulse-live"></span>
                SOC ACTIVE
              </span>
            </div>
            <p style={{ fontSize: '0.8125rem', color: '#64748b', marginTop: '2px' }}>
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
            background: '#f8fafc',
            borderRadius: '8px',
            border: '1px solid #e2e8f0',
            fontSize: '0.8125rem',
            color: '#475569'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Cpu size={15} color="#0284c7" />
              <span>Thuật toán: <strong style={{ color: '#0284c7' }}>Haversine R=6371km</strong></span>
            </div>
            <span style={{ color: '#cbd5e1' }}>|</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Radio size={15} color="#d97706" />
              <span>Ngưỡng: <strong style={{ color: '#d97706' }}>900 km/h</strong></span>
            </div>
          </div>

          <button
            onClick={onRefresh}
            disabled={loading}
            className="btn-primary"
            style={{ cursor: loading ? 'not-allowed' : 'pointer' }}
          >
            <RefreshCw size={15} style={{ animation: loading ? 'spin 1s linear infinite' : 'none' }} />
            <span>{loading ? 'Đang cập nhật' : 'Làm mới'}</span>
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
