package com.phenikaa.security.impossibletravel.dto.response;

public class DashboardSummaryDto {

    private long totalLogins;
    private long allowedLogins;
    private long blockedLogins;
    private long activeSessions;
    private long securityAlertsCount;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(long totalLogins, long allowedLogins, long blockedLogins, long activeSessions, long securityAlertsCount) {
        this.totalLogins = totalLogins;
        this.allowedLogins = allowedLogins;
        this.blockedLogins = blockedLogins;
        this.activeSessions = activeSessions;
        this.securityAlertsCount = securityAlertsCount;
    }

    public long getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(long totalLogins) {
        this.totalLogins = totalLogins;
    }

    public long getAllowedLogins() {
        return allowedLogins;
    }

    public void setAllowedLogins(long allowedLogins) {
        this.allowedLogins = allowedLogins;
    }

    public long getBlockedLogins() {
        return blockedLogins;
    }

    public void setBlockedLogins(long blockedLogins) {
        this.blockedLogins = blockedLogins;
    }

    public long getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(long activeSessions) {
        this.activeSessions = activeSessions;
    }

    public long getSecurityAlertsCount() {
        return securityAlertsCount;
    }

    public void setSecurityAlertsCount(long securityAlertsCount) {
        this.securityAlertsCount = securityAlertsCount;
    }
}
