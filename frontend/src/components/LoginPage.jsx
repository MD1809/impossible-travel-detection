import React, { useState } from 'react';
import {
  Shield,
  Lock,
  User,
  Globe,
  MapPin,
  AlertTriangle,
  CheckCircle2,
  LayoutDashboard,
  LogOut,
  RefreshCw,
  Clock,
  Gauge
} from 'lucide-react';
import { DashboardApi } from '../services/api';

const IP_PRESETS = [
  { label: 'Hà Nội, Việt Nam', ip: '118.70.128.1', flag: '🇻🇳', desc: 'Vị trí máy chủ / Trụ sở chính' },
  { label: 'TP. Hồ Chí Minh, VN', ip: '1.53.255.1', flag: '🇻🇳', desc: 'Cách Hà Nội ~1.138 km' },
  { label: 'Tokyo, Nhật Bản', ip: '220.100.0.1', flag: '🇯🇵', desc: 'Cách Hà Nội ~3.650 km' },
  { label: 'New York, Hoa Kỳ', ip: '128.122.1.1', flag: '🇺🇸', desc: 'Cách Hà Nội ~13.150 km' }
];

export default function LoginPage({ onGoToDashboard }) {
  const [username, setUsername] = useState('student01');
  const [password, setPassword] = useState('Password123!');
  const [ipAddress, setIpAddress] = useState('118.70.128.1');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null); // { type: 'success' | 'blocked' | 'error', data: ... }
  const [logoutMessage, setLogoutMessage] = useState(null);

  const handleSelectPreset = (presetIp) => {
    setIpAddress(presetIp);
    setResult(null);
    setLogoutMessage(null);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    setLogoutMessage(null);

    try {
      const response = await DashboardApi.login(username.trim(), password, ipAddress.trim());
      setResult({
        type: 'success',
        title: 'Đăng nhập thành công',
        data: response
      });
    } catch (err) {
      if (err.status === 403 || err.blocked) {
        setResult({
          type: 'blocked',
          title: 'CẢNH BÁO: TRUY CẬP BỊ TỪ CHỐI!',
          subtitle: err.message || 'Phát hiện hành vi di chuyển bất khả thi (Impossible Travel)',
          data: err
        });
      } else {
        setResult({
          type: 'error',
          title: 'Đăng nhập thất bại',
          subtitle: err.message || 'Thông tin tài khoản hoặc mật khẩu không chính xác',
          data: err
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogoutAll = async () => {
    if (!username.trim()) return;
    setLoading(true);
    try {
      await DashboardApi.logoutAll(username.trim());
      setLogoutMessage(`Đã dọn sạch tất cả phiên đang mở của tài khoản "${username}". Bạn có thể bắt đầu phiên thử nghiệm mới!`);
      setResult(null);
    } catch (err) {
      setLogoutMessage('Lỗi khi đăng xuất các phiên: ' + (err.message || ''));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px 16px',
      background: 'radial-gradient(circle at 50% 15%, #1e293b 0%, #0a0e17 100%)',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>

      {/* Top Bar Switch to Dashboard */}
      <div style={{
        width: '100%',
        maxWidth: '480px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '16px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#60a5fa' }}>
          <Shield size={20} />
          <span style={{ fontSize: '0.875rem', fontWeight: '700', letterSpacing: '0.05em' }}>
            DEMO LOGIN PORTAL
          </span>
        </div>

        {onGoToDashboard && (
          <button
            onClick={onGoToDashboard}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '6px 14px',
              fontSize: '0.8125rem',
              fontWeight: '600',
              color: '#38bdf8',
              background: 'rgba(56, 189, 248, 0.1)',
              border: '1px solid rgba(56, 189, 248, 0.3)',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            <LayoutDashboard size={14} />
            <span>Mở SOC Dashboard</span>
          </button>
        )}
      </div>

      {/* Main Login Card */}
      <div
        className="glass-card"
        style={{
          width: '100%',
          maxWidth: '480px',
          background: 'rgba(22, 27, 38, 0.9)',
          backdropFilter: 'blur(16px)',
          border: '1px solid #2d3748',
          borderRadius: '16px',
          padding: '32px 28px',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)'
        }}
      >
        {/* Card Header */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div style={{
            width: '56px',
            height: '56px',
            margin: '0 auto 12px auto',
            borderRadius: '14px',
            background: 'linear-gradient(135deg, rgba(37, 99, 235, 0.25) 0%, rgba(59, 130, 246, 0.1) 100%)',
            border: '1px solid rgba(59, 130, 246, 0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#60a5fa'
          }}>
            <Lock size={26} />
          </div>
          <h2 style={{ fontSize: '1.375rem', fontWeight: '800', color: '#f8fafc', margin: '0 0 4px 0' }}>
            Đăng Nhập Hệ Thống
          </h2>
          <p style={{ fontSize: '0.8125rem', color: '#94a3b8', margin: 0 }}>
            Kiểm tra và xác thực an ninh theo ngữ cảnh vị trí địa lý
          </p>
        </div>

        {/* Login Form */}
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* Username */}
          <div>
            <label style={{ display: 'block', fontSize: '0.8125rem', fontWeight: '600', color: '#cbd5e1', marginBottom: '6px' }}>
              Tên đăng nhập
            </label>
            <div style={{ position: 'relative' }}>
              <User size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }} />
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Nhập username"
                required
                style={{
                  width: '100%',
                  padding: '10px 12px 10px 38px',
                  background: '#0f141f',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  fontSize: '0.875rem',
                  outline: 'none',
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>

          {/* Password */}
          <div>
            <label style={{ display: 'block', fontSize: '0.8125rem', fontWeight: '600', color: '#cbd5e1', marginBottom: '6px' }}>
              Mật khẩu
            </label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }} />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Nhập mật khẩu"
                required
                style={{
                  width: '100%',
                  padding: '10px 12px 10px 38px',
                  background: '#0f141f',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  fontSize: '0.875rem',
                  outline: 'none',
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>

          {/* IP Address Selection */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
              <label style={{ fontSize: '0.8125rem', fontWeight: '600', color: '#cbd5e1' }}>
                Địa chỉ IP đăng nhập (Vị trí)
              </label>
              <span style={{ fontSize: '0.75rem', color: '#60a5fa' }}>Chọn vị trí thử nghiệm</span>
            </div>

            <div style={{ position: 'relative', marginBottom: '10px' }}>
              <Globe size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }} />
              <input
                type="text"
                value={ipAddress}
                onChange={(e) => setIpAddress(e.target.value)}
                placeholder="Nhập IP (VD: 118.70.128.1)"
                required
                style={{
                  width: '100%',
                  padding: '10px 12px 10px 38px',
                  background: '#0f141f',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#38bdf8',
                  fontFamily: 'monospace',
                  fontSize: '0.875rem',
                  outline: 'none',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            {/* IP Preset Buttons */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              {IP_PRESETS.map((p) => {
                const isSelected = ipAddress === p.ip;
                return (
                  <button
                    key={p.ip}
                    type="button"
                    onClick={() => handleSelectPreset(p.ip)}
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'flex-start',
                      padding: '8px 10px',
                      background: isSelected ? 'rgba(37, 99, 235, 0.2)' : '#161c28',
                      border: `1px solid ${isSelected ? '#3b82f6' : '#283244'}`,
                      borderRadius: '8px',
                      cursor: 'pointer',
                      textAlign: 'left',
                      transition: 'all 0.15s ease'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78125rem', fontWeight: '600', color: isSelected ? '#93c5fd' : '#e2e8f0' }}>
                      <span>{p.flag}</span>
                      <span>{p.label}</span>
                    </div>
                    <span style={{ fontSize: '0.71875rem', color: '#64748b', fontFamily: 'monospace', marginTop: '2px' }}>
                      {p.ip}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
            <button
              type="submit"
              disabled={loading}
              className="btn-primary"
              style={{
                width: '100%',
                padding: '12px',
                fontSize: '0.9375rem',
                fontWeight: '700',
                justifyContent: 'center',
                boxShadow: '0 4px 14px rgba(37, 99, 235, 0.35)',
                cursor: loading ? 'not-allowed' : 'pointer'
              }}
            >
              {loading ? (
                <>
                  <RefreshCw size={16} style={{ animation: 'spin 1s linear infinite' }} />
                  <span>Đang xử lý xác thực...</span>
                </>
              ) : (
                <>
                  <Lock size={16} />
                  <span>Đăng Nhập</span>
                </>
              )}
            </button>

            <button
              type="button"
              onClick={handleLogoutAll}
              disabled={loading}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                padding: '8px',
                background: 'transparent',
                border: '1px dashed #334155',
                borderRadius: '8px',
                color: '#94a3b8',
                fontSize: '0.75rem',
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}
              onMouseOver={(e) => { e.currentTarget.style.color = '#f87171'; e.currentTarget.style.borderColor = '#f87171'; }}
              onMouseOut={(e) => { e.currentTarget.style.color = '#94a3b8'; e.currentTarget.style.borderColor = '#334155'; }}
            >
              <LogOut size={13} />
              <span>Đăng xuất tất cả phiên cũ (Dọn sạch để test lại)</span>
            </button>
          </div>
        </form>

        {/* Logout Notice */}
        {logoutMessage && (
          <div style={{
            marginTop: '16px',
            padding: '10px 14px',
            background: 'rgba(56, 189, 248, 0.1)',
            border: '1px solid rgba(56, 189, 248, 0.3)',
            borderRadius: '8px',
            fontSize: '0.8125rem',
            color: '#38bdf8'
          }}>
            {logoutMessage}
          </div>
        )}

        {/* Result Message Box */}
        {result && (
          <div style={{
            marginTop: '18px',
            padding: '16px',
            borderRadius: '10px',
            background: result.type === 'success'
              ? 'rgba(34, 197, 94, 0.1)'
              : result.type === 'blocked'
              ? 'rgba(239, 68, 68, 0.12)'
              : 'rgba(245, 158, 11, 0.1)',
            border: `1px solid ${
              result.type === 'success'
                ? 'rgba(34, 197, 94, 0.4)'
                : result.type === 'blocked'
                ? 'rgba(239, 68, 68, 0.45)'
                : 'rgba(245, 158, 11, 0.4)'
            }`,
            display: 'flex',
            flexDirection: 'column',
            gap: '8px'
          }}>
            {/* Result Header */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              color: result.type === 'success' ? '#4ade80' : result.type === 'blocked' ? '#f87171' : '#fbbf24',
              fontWeight: '700',
              fontSize: '0.9375rem'
            }}>
              {result.type === 'success' ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
              <span>{result.title}</span>
            </div>

            {/* Subtitle / Reason */}
            {result.subtitle && (
              <p style={{ margin: 0, fontSize: '0.8125rem', color: '#e2e8f0', lineHeight: 1.4 }}>
                {result.subtitle}
              </p>
            )}

            {/* Details for SUCCESS */}
            {result.type === 'success' && (
              <div style={{ fontSize: '0.8125rem', color: '#94a3b8', display: 'flex', flexDirection: 'column', gap: '4px', marginTop: '4px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <MapPin size={13} color="#4ade80" />
                  <span>Vị trí xác thực: <strong style={{ color: '#f1f5f9' }}>{result.data?.city || 'Hanoi'}, {result.data?.country || 'Vietnam'}</strong></span>
                </div>
                <div>Địa chỉ IP: <span className="code-pill">{result.data?.ipAddress || ipAddress}</span></div>
                {onGoToDashboard && (
                  <button
                    onClick={onGoToDashboard}
                    style={{
                      marginTop: '8px',
                      padding: '8px 12px',
                      background: 'rgba(34, 197, 94, 0.2)',
                      border: '1px solid rgba(34, 197, 94, 0.4)',
                      borderRadius: '6px',
                      color: '#4ade80',
                      fontSize: '0.8125rem',
                      fontWeight: '600',
                      cursor: 'pointer'
                    }}
                  >
                    Xem phiên đang mở trên SOC Dashboard →
                  </button>
                )}
              </div>
            )}

            {/* Details for BLOCKED (Impossible Travel) */}
            {result.type === 'blocked' && (
              <div style={{
                marginTop: '6px',
                padding: '10px',
                background: '#121722',
                borderRadius: '8px',
                fontSize: '0.78125rem',
                color: '#94a3b8',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px'
              }}>
                {result.data?.detectionDetails && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Quy tắc vi phạm:</span>
                      <strong style={{ color: '#f87171' }}>{result.data.detectionDetails.ruleTriggered}</strong>
                    </div>
                    {result.data.detectionDetails.metrics && (
                      <>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span>Khoảng cách di chuyển:</span>
                          <strong style={{ color: '#cbd5e1' }}>{result.data.detectionDetails.metrics.distanceKm?.toFixed(1)} km</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span>Vận tốc tính toán:</span>
                          <strong style={{ color: '#f87171' }}>
                            {result.data.detectionDetails.metrics.calculatedSpeedKmh?.toLocaleString('vi-VN', { maximumFractionDigits: 1 })} km/h
                          </strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                          <span>Ngưỡng tối đa cho phép:</span>
                          <span style={{ color: '#64748b' }}>900.0 km/h</span>
                        </div>
                      </>
                    )}
                  </>
                )}

                <div style={{ marginTop: '4px', paddingTop: '6px', borderTop: '1px solid #232c3d', color: '#cbd5e1', fontSize: '0.75rem' }}>
                  🛡️ <em>Sự kiện đã được ghi nhận vào nhật ký an ninh. Bạn có thể mở SOC Dashboard để theo dõi đường nối vi phạm trên bản đồ.</em>
                </div>

                {onGoToDashboard && (
                  <button
                    onClick={onGoToDashboard}
                    style={{
                      marginTop: '6px',
                      padding: '8px 12px',
                      background: 'rgba(239, 68, 68, 0.2)',
                      border: '1px solid rgba(239, 68, 68, 0.4)',
                      borderRadius: '6px',
                      color: '#fca5a5',
                      fontSize: '0.8125rem',
                      fontWeight: '600',
                      cursor: 'pointer'
                    }}
                  >
                    Mở SOC Dashboard xem sự kiện bị chặn →
                  </button>
                )}
              </div>
            )}
          </div>
        )}

      </div>

      {/* Footer Info */}
      <div style={{ marginTop: '20px', fontSize: '0.75rem', color: '#64748b', textAlign: 'center' }}>
        Impossible Travel Detection System • Đại học Phenikaa
      </div>

    </div>
  );
}
