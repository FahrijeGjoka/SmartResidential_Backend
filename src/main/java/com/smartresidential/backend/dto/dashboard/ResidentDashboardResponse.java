package com.smartresidential.backend.dto.dashboard;

public class ResidentDashboardResponse {

    private long openIssueCount;
    private ResidentDashboardIssue latestIssue;
    private long unreadNotificationCount;
    private ResidentDashboardAnnouncement latestAnnouncement;

    public ResidentDashboardResponse() {
    }

    public long getOpenIssueCount() {
        return openIssueCount;
    }

    public void setOpenIssueCount(long openIssueCount) {
        this.openIssueCount = openIssueCount;
    }

    public ResidentDashboardIssue getLatestIssue() {
        return latestIssue;
    }

    public void setLatestIssue(ResidentDashboardIssue latestIssue) {
        this.latestIssue = latestIssue;
    }

    public long getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(long unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }

    public ResidentDashboardAnnouncement getLatestAnnouncement() {
        return latestAnnouncement;
    }

    public void setLatestAnnouncement(ResidentDashboardAnnouncement latestAnnouncement) {
        this.latestAnnouncement = latestAnnouncement;
    }
}
