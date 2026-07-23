-- Single-column indexes for the most common lookups and filters.
CREATE INDEX idx_leave_request_employee_id ON leave_request (employee_id);
CREATE INDEX idx_leave_request_status ON leave_request (status);
CREATE INDEX idx_leave_request_start_date ON leave_request (start_date);
CREATE INDEX idx_leave_request_end_date ON leave_request (end_date);

-- Composite index supporting the overlap check
-- (employee_id + status + date range).
CREATE INDEX idx_leave_request_overlap
    ON leave_request (employee_id, status, start_date, end_date);

-- The employee.email uniqueness is already enforced (and indexed) by uq_employee_email.
