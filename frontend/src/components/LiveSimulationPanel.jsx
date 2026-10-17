import React, { useState } from 'react';
import { Play, Zap, CheckCircle, ShieldAlert, UserPlus, LogOut } from 'lucide-react';
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
      desc: 'Kịch bản 1: Đồng thời khi phiên Hà Nội đang mở'
    },
    {
      name: 'Tokyo',
      ip: '133.242.0.1',
      city: 'Tokyo, Nhật Bản',
      type: 'critical',
      desc: 'Kịch bản 2: Tuần tự sau khi bấm Đăng xuất phiên'
    },
    {
      name: 'TP. Hồ Chí Minh',
      ip: '171.244.10.1',
      city: 'TP.HCM, Việt Nam',
      type: 'normal',
      desc: 'Khoảng cách 1.138 km'
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

  const handleLogoutAll = async () => {
    setLoading(true);
    setResult(null);
    try {
      const res = await DashboardApi.logoutAll(username);
      setResult({ status: 200, data: res });
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
    <div className="glass-card" style={{ padding: '22px', marginBottom: '28px' }}>
      {/* Top Banner */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', marginBottom: isExpanded ? '18px' : '0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: 'rgba(37, 99, 235, 0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '1px solid rgba(37, 99, 235, 0.35)'
          }}>
            <Zap size={20} color="#60a5fa" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.0625rem', fontWeight: '800', color: '#f1f5f9' }}>
              Mô phỏng đăng nhập
            </h3>
            <p style={{ fontSize: '0.8125rem', color: '#94a3b8' }}>
              Kiểm tra các kịch bản Impossible Travel trực tiếp trên hệ thống
            </p>
          </div>
        </div>

        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="btn-secondary"
          style={{ padding: '5px 12px', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
        >
          <span style={{ whiteSpace: 'nowrap' }}>{isExpanded ? 'Thu gọn' : 'Mở rộng'}</span>
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
                  background: preset.type === 'critical' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(16, 185, 129, 0.1)',
                  border: `1px solid ${preset.type === 'critical' ? 'rgba(239, 68, 68, 0.35)' : 'rgba(16, 185, 129, 0.35)'}`,
                  textAlign: 'left',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px', whiteSpace: 'nowrap', gap: '8px' }}>
                  <strong style={{
                    fontSize: '0.8125rem',
                    color: preset.type === 'critical' ? '#f87171' : '#34d399',
                    whiteSpace: 'nowrap'
                  }}>
                    {preset.name}
                  </strong>
                  <span style={{ fontSize: '0.725rem', color: '#94a3b8', whiteSpace: 'nowrap' }}>{preset.city}</span>
                </div>
                <div style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: '#60a5fa', marginBottom: '4px', whiteSpace: 'nowrap' }}>
                  IP: {preset.ip}
                </div>
                <div style={{ fontSize: '0.725rem', color: '#94a3b8', lineHeight: '1.4' }}>
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
            background: '#151923',
            borderRadius: '10px',
            border: '1px solid #2d3748'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#94a3b8' }}>Tài khoản:</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{
                  background: '#1c2230',
                  border: '1px solid #2d3748',
                  borderRadius: '6px',
                  color: '#f1f5f9',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '120px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#94a3b8' }}>Mật khẩu:</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{
                  background: '#1c2230',
                  border: '1px solid #2d3748',
                  borderRadius: '6px',
                  color: '#f1f5f9',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '130px'
                }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '0.8125rem', color: '#94a3b8' }}>Địa chỉ IP:</span>
              <input
                type="text"
                value={ipAddress}
                onChange={(e) => setIpAddress(e.target.value)}
                placeholder="Ví dụ: 161.185.160.93"
                style={{
                  background: '#1c2230',
                  border: '1px solid #2d3748',
                  borderRadius: '6px',
                  color: '#60a5fa',
                  fontFamily: 'var(--font-mono)',
                  padding: '6px 10px',
                  fontSize: '0.8125rem',
                  width: '140px'
                }}
              />
            </div>

            <div style={{ display: 'flex', gap: '8px', marginLeft: 'auto', flexWrap: 'wrap' }}>
              <button
                onClick={handleRegisterUser}
                disabled={loading}
                className="btn-secondary"
                style={{ fontSize: '0.78125rem', padding: '6px 12px', whiteSpace: 'nowrap', flexShrink: 0 }}
                title="Đăng ký tài khoản nếu chưa có"
              >
                <UserPlus size={14} style={{ flexShrink: 0 }} />
                <span style={{ whiteSpace: 'nowrap' }}>Tạo tài khoản</span>
              </button>

              <button
                onClick={handleLogoutAll}
                disabled={loading}
                className="btn-secondary"
                style={{ fontSize: '0.78125rem', padding: '6px 12px', whiteSpace: 'nowrap', flexShrink: 0 }}
                title="Đóng các phiên đang mở để thử Kịch bản 2 di chuyển tuần tự"
              >
                <LogOut size={14} style={{ flexShrink: 0 }} />
                <span style={{ whiteSpace: 'nowrap' }}>Đăng xuất phiên cũ</span>
              </button>

              <button
                onClick={() => handleRunLogin()}
                disabled={loading}
                className="btn-primary"
                style={{ fontSize: '0.78125rem', padding: '6px 14px', whiteSpace: 'nowrap', flexShrink: 0 }}
              >
                <Play size={14} style={{ flexShrink: 0 }} />
                <span style={{ whiteSpace: 'nowrap' }}>{loading ? 'Đang gửi...' : 'Đăng nhập'}</span>
              </button>
            </div>
          </div>

          {/* Live Response Result Box */}
          {result && (() => {
            const isSuccess = (result.status >= 200 && result.status < 300) && result.data?.success !== false;
            let statusText = 'Lỗi';
            if (result.status === 201) {
              statusText = 'CREATED - Đăng ký tài khoản thành công';
            } else if (result.status === 200) {
              statusText = 'OK - Cho phép truy cập';
            } else if (result.status === 403) {
              statusText = 'FORBIDDEN - Chặn do phát hiện di chuyển bất khả thi';
            } else if (result.status === 401) {
              statusText = 'UNAUTHORIZED - Sai thông tin xác thực';
            }

            return (
              <div style={{
                marginTop: '16px',
                padding: '16px',
                borderRadius: '10px',
                background: isSuccess ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)',
                border: `1px solid ${isSuccess ? 'rgba(16, 185, 129, 0.35)' : 'rgba(239, 68, 68, 0.35)'}`
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {isSuccess ? (
                      <CheckCircle size={18} color="#34d399" />
                    ) : (
                      <ShieldAlert size={18} color="#f87171" />
                    )}
                    <strong style={{
                      fontSize: '0.875rem',
                      color: isSuccess ? '#34d399' : '#f87171'
                    }}>
                      HTTP {result.status} {statusText}
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
                background: '#12161f',
                border: '1px solid #242c3c',
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
          );
        })()}
        </>
      )}
    </div>
  );
}
