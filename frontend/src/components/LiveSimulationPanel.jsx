import React, { useState } from 'react';
import { Play, Zap, CheckCircle, ShieldAlert, UserPlus } from 'lucide-react';
import { DashboardApi } from '../services/api';

export default function LiveSimulationPanel({ onSimulationComplete }) {
  const [username, setUsername] = useState('student01');
  const [password, setPassword] = useState('Password@123');
  const [ipAddress, setIpAddress] = useState('113.160.224.1');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [isExpanded, setIsExpanded] = useState(true);

  const presets = [
    {
      name: 'Hà Nội',
      ip: '113.160.224.1',
      city: 'Hà Nội, Việt Nam',
      type: 'normal',
      desc: 'Đăng nhập điểm gốc'
    },
    {
      name: 'New York',
      ip: '161.185.160.93',
      city: 'New York, Hoa Kỳ',
      type: 'critical',
      desc: 'Khoảng cách 13.125 km ngay sau phiên Hà Nội'
    },
    {
      name: 'TP. Hồ Chí Minh',
      ip: '171.244.10.1',
      city: 'TP.HCM, Việt Nam',
      type: 'normal',
      desc: 'Khoảng cách 1.138 km'
    },
    {
      name: 'Tokyo',
      ip: '133.242.0.1',
      city: 'Tokyo, Nhật Bản',
      type: 'critical',
      desc: 'Khoảng cách 3.650 km sau vài giây'
    }
  ];

  const handleRegisterUser = async () => {
    setLoading(true);
    setResult(null);
    try {
      const res = await DashboardApi.register(username, password, 'Sinh Viên Phenikaa');
      setResult({ status: 201, data: res });
      if (onSimulationComplete) onSimulationComplete();
    } catch (err) {
      setResult({ status: err.status || 400, data: err });
    } finally {
      setLoading(false);
    }
  };

  const handleRunLogin = async (targetIp) => {
    const ipToUse = targetIp || ipAddress;
    setLoading(true);
    setResult(null);
    try {
      const res = await DashboardApi.login(username, password, ipToUse);
      setResult({ status: 200, data: res });
      if (onSimulationComplete) onSimulationComplete();
    } catch (err) {
      setResult({ status: err.status || 403, data: err });
      if (onSimulationComplete) onSimulationComplete();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="glass-card" style={{ padding: '22px', marginBottom: '28px', border: '1px solid #e2e8f0' }}>
      {/* Top Banner */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', marginBottom: isExpanded ? '18px' : '0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: '#e0f2fe',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '1px solid #bae6fd'
          }}>
            <Zap size={20} color="#0284c7" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.0625rem', fontWeight: '800', color: '#0f172a' }}>
              Mô phỏng đăng nhập
            </h3>
            <p style={{ fontSize: '0.8125rem', color: '#64748b' }}>
              Kiểm tra các kịch bản Impossible Travel trực tiếp trên hệ thống
            </p>
          </div>
        </div>

        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="btn-secondary"
          style={{ padding: '5px 12px', fontSize: '0.75rem' }}
        >
          {isExpanded ? 'Thu gọn' : 'Mở rộng'}
        </button>
      </div>

      {isExpanded && (
        <>
          {/* Quick 1-Click Presets */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '12px',
            marginBottom: '18px'
          }}>
            {presets.map((preset, idx) => (
              <button
                key={idx}
                onClick={() => {
                  setIpAddress(preset.ip);
                  handleRunLogin(preset.ip);
                }}
                disabled={loading}
                style={{
                  padding: '12px 14px',
                  borderRadius: '10px',
                  background: preset.type === 'critical' ? '#fef2f2' : '#f0fdf4',
                  border: `1px solid ${preset.type === 'critical' ? '#fecaca' : '#bbf7d0'}`,
                  textAlign: 'left',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <strong style={{
                    fontSize: '0.8125rem',
                    color: preset.type === 'critical' ? '#dc2626' : '#16a34a'
                  }}>
                    {preset.name}
                  </strong>
                  <span style={{ fontSize: '0.725rem', color: '#64748b' }}>{preset.city}</span>
                </div>
                <div style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: '#0284c7', marginBottom: '4px' }}>
                  IP: {preset.ip}
                </div>
                <div style={{ fontSize: '0.725rem', color: '#64748b' }}>
                  {preset.desc}
                </div>
              </button>
            ))}
          </div>

          {/* Form & Controls */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            flexWrap: 'wrap',
            padding: '14px',
            background: '#f8fafc',
            borderRadius: '10px',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#475569' }}>Tài khoản:</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{
                  background: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: '6px',
                  color: '#0f172a',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '120px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#475569' }}>Mật khẩu:</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{
                  background: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: '6px',
                  color: '#0f172a',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '130px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#475569' }}>Địa chỉ IP:</span>
              <input
                type="text"
                value={ipAddress}
                onChange={(e) => setIpAddress(e.target.value)}
                placeholder="Ví dụ: 161.185.160.93"
                style={{
                  background: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: '6px',
                  color: '#0284c7',
                  fontFamily: 'var(--font-mono)',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '140px'
                }}
              />
            </div>

            <div style={{ display: 'flex', gap: '8px', marginLeft: 'auto' }}>
              <button
                onClick={handleRegisterUser}
                disabled={loading}
                className="btn-secondary"
                style={{ fontSize: '0.78125rem', padding: '6px 12px' }}
                title="Đăng ký tài khoản nếu chưa có"
              >
                <UserPlus size={14} />
                Tạo tài khoản
              </button>

              <button
                onClick={() => handleRunLogin()}
                disabled={loading}
                className="btn-primary"
                style={{ fontSize: '0.78125rem', padding: '6px 14px' }}
              >
                <Play size={14} />
                {loading ? 'Đang gửi...' : 'Đăng nhập'}
              </button>
            </div>
          </div>

          {/* Live Response Result Box */}
          {result && (
            <div style={{
              marginTop: '16px',
              padding: '16px',
              borderRadius: '10px',
              background: result.status === 200 ? '#f0fdf4' : '#fef2f2',
              border: `1px solid ${result.status === 200 ? '#bbf7d0' : '#fecaca'}`
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {result.status === 200 ? (
                    <CheckCircle size={18} color="#16a34a" />
                  ) : (
                    <ShieldAlert size={18} color="#dc2626" />
                  )}
                  <strong style={{
                    fontSize: '0.875rem',
                    color: result.status === 200 ? '#15803d' : '#b91c1c'
                  }}>
                    HTTP {result.status} {result.status === 200 ? 'OK - Cho phép truy cập' : result.status === 403 ? 'FORBIDDEN - Chặn do phát hiện di chuyển bất khả thi' : 'Lỗi'}
                  </strong>
                </div>

                <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                  {result.data?.message}
                </span>
              </div>

              {/* Formatted JSON debug block */}
              <pre style={{
                margin: 0,
                padding: '12px',
                background: '#ffffff',
                border: '1px solid #e2e8f0',
                borderRadius: '6px',
                fontSize: '0.75rem',
                fontFamily: 'var(--font-mono)',
                color: '#334155',
                maxHeight: '180px',
                overflowY: 'auto'
              }}>
                {JSON.stringify(result.data, null, 2)}
              </pre>
            </div>
          )}
        </>
      )}
    </div>
  );
}
