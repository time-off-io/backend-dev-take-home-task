-- Development-only seed data. Loaded exclusively under the `dev` Spring profile
-- (see application-dev.yml). Not applied during integration tests or in production.

INSERT INTO employee (first_name, last_name, email, annual_leave_allowance,
                      remaining_leave_days, active, created_at, updated_at, version)
VALUES ('George', 'Papadopoulos', 'george@example.com', 25, 20.00, TRUE, now(), now(), 0),
       ('Maria', 'Nikolaou', 'maria@example.com', 20, 20.00, TRUE, now(), now(), 0)
ON CONFLICT (email) DO NOTHING;

-- One PENDING and one APPROVED request for George.
INSERT INTO leave_request (employee_id, start_date, end_date, requested_days, status, reason,
                           review_comment, created_at, updated_at, reviewed_at, version)
SELECT e.id, DATE '2026-09-14', DATE '2026-09-18', 5.00, 'PENDING', 'Vacation',
       NULL, now(), now(), NULL, 0
FROM employee e
WHERE e.email = 'george@example.com'
  AND NOT EXISTS (SELECT 1 FROM leave_request lr WHERE lr.employee_id = e.id AND lr.reason = 'Vacation');

INSERT INTO leave_request (employee_id, start_date, end_date, requested_days, status, reason,
                           review_comment, created_at, updated_at, reviewed_at, version)
SELECT e.id, DATE '2026-08-10', DATE '2026-08-14', 5.00, 'APPROVED', 'Summer break',
       'Approved', now(), now(), now(), 0
FROM employee e
WHERE e.email = 'george@example.com'
  AND NOT EXISTS (SELECT 1 FROM leave_request lr WHERE lr.employee_id = e.id AND lr.reason = 'Summer break');
