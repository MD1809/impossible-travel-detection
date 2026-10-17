import React from 'react';
import { Shield, RefreshCw, Activity, Radio, Cpu } from 'lucide-react';

export default function Header({ onRefresh, loading }) {
  return (
    <header className="glass-card" style={{ marginBottom: '24px', padding: '18px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        
        {/* Left: Branding & Status */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{
            width: '46px',
            height: '46px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(2, 132, 199, 0.3))',
            border: '1px solid rgba(56, 189, 248, 0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 20px rgba(6, 182, 212, 0.3)'
          }}>
            <Shield size={26} color="#38bdf8" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h1 style={{ fontSize: '1.25rem', fontWeight: '800', letterSpacing: '-0.02em', color: '#f8fafc' }}>
                IMPOSSIBLE TRAVEL DETECTION
              </h1>
              <span className="badge badge-low" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <span className="pulse-live"></span>
                SOC ACTIVE
              </span>
            </div>
            <p style={{ fontSize: '0.8125rem', color: '#94a3b8', marginTop: '2px' }}>
              Trung tâm giám sát An ninh Không gian mạng & Phát hiện Bất thường Di chuyển Địa lý
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
            background: 'rgba(15, 23, 42, 0.6)',
            borderRadius: '8px',
            border: '1px solid rgba(148, 163, 184, 0.1)',
            fontSize: '0.8125rem',
            color: '#cbd5e1'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Cpu size={15} color="#38bdf8" />
              <span>Engine: <strong style={{ color: '#38bdf8' }}>Haversine R=6371km</strong></span>
            </div>
            <span style={{ color: 'rgba(148, 163, 184, 0.3)' }}>|</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Radio size={15} color="#f59e0b" />
              <span>Ngưỡng: <strong style={{ color: '#f59e0b' }}>900 km/h</strong></span>
            </div>
          </div>

          <button
            onClick={onRefresh}
            disabled={loading}
            className="btn-primary"
            style={{ cursor: loading ? 'not-allowed' : 'pointer' }}
          >
            <RefreshCw size={16} className={loading ? 'spin-animation' : ''} style={{ animation: loading ? 'spin 1s linear infinite' : 'none' }} />
            <span>{loading ? 'Đang cập nhật...' : 'Làm mới dữ liệu'}</span>
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
