import React from 'react';
import { Activity, CheckCircle2, ShieldAlert, Users, TrendingUp } from 'lucide-react';

export default function StatCards({ summary }) {
  const stats = [
    {
      id: 'total',
      title: 'TỔNG LẦN ĐĂNG NHẬP',
      value: summary?.totalLogins ?? 0,
      icon: Activity,
      color: '#38bdf8',
      bgColor: 'rgba(56, 189, 248, 0.1)',
      borderColor: 'rgba(56, 189, 248, 0.3)',
      subtitle: 'Toàn bộ yêu cầu đăng nhập vào hệ thống',
      badge: 'All Requests'
    },
    {
      id: 'allowed',
      title: 'ĐĂNG NHẬP HỢP LỆ',
      value: summary?.allowedLogins ?? 0,
      icon: CheckCircle2,
      color: '#10b981',
      bgColor: 'rgba(16, 185, 129, 0.1)',
      borderColor: 'rgba(16, 185, 129, 0.3)',
      subtitle: 'Vận tốc an toàn (≤ 900 km/h)',
      badge: 'Allowed (200 OK)'
    },
    {
      id: 'blocked',
      title: 'CẢNH BÁO IMPOSSIBLE TRAVEL',
      value: summary?.blockedLogins ?? 0,
      icon: ShieldAlert,
      color: '#f43f5e',
      bgColor: 'rgba(244, 63, 94, 0.12)',
      borderColor: 'rgba(244, 63, 94, 0.4)',
      glow: '0 0 25px rgba(244, 63, 94, 0.25)',
      subtitle: 'Bị chặn tức thì (> 900 km/h hoặc tức thời)',
      badge: 'Blocked (403 Forbidden)'
    },
    {
      id: 'active',
      title: 'PHIÊN HOẠT ĐỘNG TRỰC TUYẾN',
      value: summary?.activeSessions ?? 0,
      icon: Users,
      color: '#06b6d4',
      bgColor: 'rgba(6, 182, 212, 0.1)',
      borderColor: 'rgba(6, 182, 212, 0.3)',
      subtitle: 'Phiên Active dùng để đối chiếu đồng thời',
      badge: 'Live Sessions'
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
              padding: '20px 22px',
              position: 'relative',
              overflow: 'hidden',
              boxShadow: stat.glow || 'var(--shadow-md)',
              border: `1px solid ${stat.borderColor}`
            }}
          >
            {/* Top decorative accent light */}
            <div style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              height: '3px',
              background: `linear-gradient(90deg, transparent, ${stat.color}, transparent)`
            }} />

            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '14px' }}>
              <div>
                <span style={{
                  fontSize: '0.75rem',
                  fontWeight: '700',
                  letterSpacing: '0.06em',
                  color: '#94a3b8',
                  textTransform: 'uppercase'
                }}>
                  {stat.title}
                </span>
                <div style={{
                  fontSize: '2.25rem',
                  fontWeight: '800',
                  color: '#f8fafc',
                  fontFamily: 'var(--font-mono)',
                  letterSpacing: '-0.03em',
                  marginTop: '4px'
                }}>
                  {stat.value.toLocaleString()}
                </div>
              </div>

              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '12px',
                background: stat.bgColor,
                border: `1px solid ${stat.borderColor}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: stat.color
              }}>
                <IconComponent size={22} />
              </div>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              paddingTop: '12px',
              borderTop: '1px solid rgba(148, 163, 184, 0.1)',
              fontSize: '0.78125rem',
              color: '#94a3b8'
            }}>
              <span>{stat.subtitle}</span>
              <span style={{
                padding: '2px 8px',
                borderRadius: '6px',
                fontSize: '0.7rem',
                fontWeight: '600',
                background: stat.bgColor,
                color: stat.color,
                border: `1px solid ${stat.borderColor}`
              }}>
                {stat.badge}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
