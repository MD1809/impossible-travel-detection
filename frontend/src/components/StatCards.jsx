import React from 'react';
import { Activity, CheckCircle2, ShieldAlert, Users } from 'lucide-react';

export default function StatCards({ summary }) {
  const stats = [
    {
      id: 'total',
      title: 'TỔNG LẦN ĐĂNG NHẬP',
      value: summary?.totalLogins ?? 0,
      icon: Activity,
      iconColor: '#60a5fa',
      iconBg: 'rgba(37, 99, 235, 0.15)',
      subtitle: 'Toàn bộ yêu cầu đăng nhập',
      badge: 'Tất cả',
      badgeClass: 'badge-low'
    },
    {
      id: 'allowed',
      title: 'ĐĂNG NHẬP HỢP LỆ',
      value: summary?.allowedLogins ?? 0,
      icon: CheckCircle2,
      iconColor: '#34d399',
      iconBg: 'rgba(16, 185, 129, 0.15)',
      subtitle: 'Tổng truy cập hợp lệ',
      badge: 'Hợp lệ',
      badgeClass: 'badge-allowed'
    },
    {
      id: 'blocked',
      title: 'TRUY CẬP BỊ CHẶN',
      value: summary?.blockedLogins ?? 0,
      icon: ShieldAlert,
      iconColor: '#f87171',
      iconBg: 'rgba(239, 68, 68, 0.15)',
      subtitle: 'Tổng số lần bị chặn',
      badge: 'Bị chặn',
      badgeClass: 'badge-blocked'
    },
    {
      id: 'active',
      title: 'PHIÊN HOẠT ĐỘNG',
      value: summary?.activeSessions ?? 0,
      icon: Users,
      iconColor: '#38bdf8',
      iconBg: 'rgba(56, 189, 248, 0.15)',
      subtitle: 'Phiên đang kết nối hệ thống',
      badge: 'Trực tuyến',
      badgeClass: 'badge-low'
    }
  ];

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
      gap: '20px',
      marginBottom: '28px'
    }}>
      {stats.map((stat) => {
        const IconComponent = stat.icon;
        return (
          <div
            key={stat.id}
            className="glass-card"
            style={{
              padding: '22px 24px',
              position: 'relative',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between'
            }}
          >
            <div>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '14px' }}>
                <span style={{
                  fontSize: '0.75rem',
                  fontWeight: '700',
                  letterSpacing: '0.04em',
                  color: '#94a3b8',
                  textTransform: 'uppercase'
                }}>
                  {stat.title}
                </span>

                <div style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '8px',
                  background: stat.iconBg,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <IconComponent size={20} color={stat.iconColor} />
                </div>
              </div>

              <div style={{
                fontSize: '2.25rem',
                fontWeight: '800',
                color: '#f1f5f9',
                fontFamily: 'var(--font-mono)',
                letterSpacing: '-0.03em',
                lineHeight: 1.1,
                marginBottom: '10px'
              }}>
                {stat.value.toLocaleString()}
              </div>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              paddingTop: '12px',
              borderTop: '1px solid #273142',
              fontSize: '0.75rem'
            }}>
              <span style={{ color: '#94a3b8' }}>{stat.subtitle}</span>
              <span className={`badge ${stat.badgeClass}`}>{stat.badge}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
