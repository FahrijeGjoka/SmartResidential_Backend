CREATE INDEX IF NOT EXISTS idx_issues_created_by ON issues (created_by);
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues (status);
CREATE INDEX IF NOT EXISTS idx_issues_priority ON issues (priority);
CREATE INDEX IF NOT EXISTS idx_issues_category_id ON issues (category_id);
CREATE INDEX IF NOT EXISTS idx_issues_apartment_id ON issues (apartment_id);
CREATE INDEX IF NOT EXISTS idx_issue_assignments_issue_id ON issue_assignments (issue_id);
CREATE INDEX IF NOT EXISTS idx_issue_assignments_technician_id ON issue_assignments (technician_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications (user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_building_announcements_building_created_at
    ON building_announcements (building_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_maintenance_requests_requested_by ON maintenance_requests (requested_by);
