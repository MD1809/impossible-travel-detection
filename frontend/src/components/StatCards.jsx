import React from 'react';
import { Activity, CheckCircle2, ShieldAlert, Users } from 'lucide-react';

export default function StatCards({ summary }) {
  const stats = [
    {
      id: 'total',
      title: 'TỔNG LẦN ĐĂNG NHẬP',
      value: summary?.totalLogins ?? 0,
      icon: Activity,
      iconColor: '#0284c7',
      iconBg: '#e0f2fe',
      subtitle: 'Toàn bộ yêu cầu đăng nhập',
      badge: 'Tất cả',
      badgeClass: 'badge-low'
    },
    {
      id: 'allowed',
      title: 'ĐĂNG NHẬP HỢP LỆ',
      value: summary?.allowedLogins ?? 0,
      icon: CheckCircle2,
      iconColor: '#16a34a',
      iconBg: '#dcfce7',
      subtitle: 'Vận tốc an toàn dưới 900 km/h',
      badge: 'Hợp lệ',
      badgeClass: 'badge-allowed'
    },
    {
      id: 'blocked',
      title: 'IMPOSSIBLE TRAVEL',
      value: summary?.blockedLogins ?? 0,
      icon: ShieldAlert,
      iconColor: '#dc2626',
      iconBg: '#fee2e2',
      subtitle: 'Vượt ngưỡng tốc độ quy định',
      badge: 'Bị chặn',
      badgeClass: 'badge-blocked'
    },
    {
      id: 'active',
      title: 'PHIÊN HOẠT ĐỘNG',
      value: summary?.activeSessions ?? 0,
      icon: Users,
      iconColor: '#2563eb',
      iconBg: '#dbeafe',
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
                  color: '#64748b',
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
                color: '#0f172a',
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
              borderTop: '1px solid #f1f5f9',
              fontSize: '0.75rem'
            }}>
              <span style={{ color: '#64748b' }}>{stat.subtitle}</span>
              <span className={`badge ${stat.badgeClass}`}>{stat.badge}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
