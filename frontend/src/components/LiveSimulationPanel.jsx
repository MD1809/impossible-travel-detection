import React, { useState } from 'react';
import { Play, Zap, Shield, AlertTriangle, CheckCircle, Terminal, UserPlus, Globe } from 'lucide-react';
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
      name: '1. Hà Nội (Hợp lệ)',
      ip: '113.160.224.1',
      city: 'Hà Nội, VN',
      expected: '200 OK (Allowed)',
      type: 'normal',
      desc: 'Đăng nhập điểm gốc'
    },
    {
      name: '2. New York (Đồng thời)',
      ip: '161.185.160.93',
      city: 'New York, US',
      expected: '403 Blocked (Concurrent)',
      type: 'critical',
      desc: 'Khoảng cách 13.125 km ngay sau phiên Hà Nội'
    },
    {
      name: '3. TP. Hồ Chí Minh',
      ip: '171.244.10.1',
      city: 'TP.HCM, VN',
      expected: 'Đánh giá vận tốc',
      type: 'warning',
      desc: 'Khoảng cách 1.138 km'
    },
    {
      name: '4. Tokyo (Bất khả thi)',
      ip: '133.242.0.1',
      city: 'Tokyo, JP',
      expected: '403 Blocked (Sequential)',
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
    <div className="glass-card" style={{ padding: '22px', marginBottom: '28px', border: '1px solid rgba(56, 189, 248, 0.25)' }}>
      {/* Top Banner */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', marginBottom: isExpanded ? '18px' : '0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, rgba(56, 189, 248, 0.2), rgba(6, 182, 212, 0.3))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '1px solid rgba(56, 189, 248, 0.4)'
          }}>
            <Zap size={20} color="#38bdf8" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.0625rem', fontWeight: '800', color: '#f8fafc' }}>
              MÔ PHỎNG ĐĂNG NHẬP & KIỂM THỬ TẤN CÔNG (LIVE TEST CONSOLE)
            </h3>
            <p style={{ fontSize: '0.78125rem', color: '#94a3b8' }}>
              Kiểm tra tức thời 2 Kịch bản Impossible Travel trực tiếp từ giao diện điều khiển
            </p>
          </div>
        </div>

        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="btn-secondary"
          style={{ padding: '5px 12px', fontSize: '0.75rem' }}
        >
          {isExpanded ? 'Thu gọn console' : 'Mở rộng console'}
        </button>
      </div>

      {isExpanded && (
        <>
          {/* Quick 1-Click Attack / Normal Presets */}
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
                  background: preset.type === 'critical'
                    ? 'rgba(225, 29, 72, 0.12)'
                    : preset.type === 'warning'
                    ? 'rgba(245, 158, 11, 0.1)'
                    : 'rgba(16, 185, 129, 0.1)',
                  border: `1px solid ${
                    preset.type === 'critical'
                      ? 'rgba(225, 29, 72, 0.4)'
                      : preset.type === 'warning'
                      ? 'rgba(245, 158, 11, 0.35)'
                      : 'rgba(16, 185, 129, 0.35)'
                  }`,
                  textAlign: 'left',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s',
                  position: 'relative',
                  overflow: 'hidden'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                  <strong style={{
                    fontSize: '0.8125rem',
                    color: preset.type === 'critical' ? '#fda4af' : preset.type === 'warning' ? '#fcd34d' : '#86efac'
                  }}>
                    {preset.name}
                  </strong>
                  <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>{preset.city}</span>
                </div>
                <div style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: '#38bdf8', marginBottom: '4px' }}>
                  IP: {preset.ip}
                </div>
                <div style={{ fontSize: '0.7rem', color: '#94a3b8' }}>
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
            background: 'rgba(15, 23, 42, 0.7)',
            borderRadius: '10px',
            border: '1px solid rgba(148, 163, 184, 0.12)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Username:</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{
                  background: 'rgba(30, 41, 59, 0.8)',
                  border: '1px solid rgba(148, 163, 184, 0.2)',
                  borderRadius: '6px',
                  color: '#fff',
                  padding: '5px 10px',
                  fontSize: '0.8125rem',
                  width: '120px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Password:</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{
                  background: 'rgba(30, 41, 59, 0.8)',
                  border: '1px solid rgba(148, 163, 184, 0.2)',
                  borderRadius: '6px',
                  color: '#fff',
                  padding: '5px 10px',
                  fontSize: '0.8125rem',
                  width: '130px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>IP Address:</span>
              <input
                type="text"
                value={ipAddress}
                onChange={(e) => setIpAddress(e.target.value)}
                placeholder="Ví dụ: 161.185.160.93"
                style={{
                  background: 'rgba(30, 41, 59, 0.8)',
                  border: '1px solid rgba(148, 163, 184, 0.2)',
                  borderRadius: '6px',
                  color: '#38bdf8',
                  fontFamily: 'var(--font-mono)',
                  padding: '5px 10px',
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
                title="Đăng ký tài khoản nếu chưa có trong DB"
              >
                <UserPlus size={14} />
                Tạo user test
              </button>

              <button
                onClick={() => handleRunLogin()}
                disabled={loading}
                className="btn-primary"
                style={{ fontSize: '0.78125rem', padding: '6px 14px' }}
              >
                <Play size={14} />
                {loading ? 'Đang gửi...' : 'Gửi yêu cầu đăng nhập'}
              </button>
            </div>
          </div>

          {/* Live Response Result Box */}
          {result && (
            <div style={{
              marginTop: '16px',
              padding: '16px',
              borderRadius: '10px',
              background: result.status === 200
                ? 'rgba(16, 185, 129, 0.08)'
                : result.status === 403
                ? 'rgba(225, 29, 72, 0.12)'
                : 'rgba(245, 158, 11, 0.1)',
              border: `1px solid ${
                result.status === 200
                  ? 'rgba(16, 185, 129, 0.4)'
                  : result.status === 403
                  ? 'rgba(225, 29, 72, 0.5)'
                  : 'rgba(245, 158, 11, 0.4)'
              }`
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {result.status === 200 ? (
                    <CheckCircle size={18} color="#10b981" />
                  ) : (
                    <ShieldAlert size={18} color="#f43f5e" />
                  )}
                  <strong style={{
                    fontSize: '0.875rem',
                    color: result.status === 200 ? '#86efac' : '#fda4af'
                  }}>
                    HTTP {result.status} {result.status === 200 ? 'OK - Cho phép truy cập' : result.status === 403 ? 'FORBIDDEN - CHẶN DO IMPOSSIBLE TRAVEL' : 'Lỗi'}
                  </strong>
                </div>

                <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                  {result.data?.message}
                </span>
              </div>

              {/* Formatted JSON debug block */}
              <pre style={{
                margin: 0,
                padding: '12px',
                background: 'rgba(5, 8, 16, 0.85)',
                borderRadius: '6px',
                fontSize: '0.75rem',
                fontFamily: 'var(--font-mono)',
                color: '#cbd5e1',
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
