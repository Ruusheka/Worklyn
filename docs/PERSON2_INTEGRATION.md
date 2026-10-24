# Worklyn — Person 2 module patch

## Contents
This patch adds the Person 2 Spring modules under `src/main/java/com/worklyn/modules/`: attendance, leave management, and payroll. It is laid out for the repository's current root-level Maven project (the existing `pom.xml` and `src/`), rather than the future `backend/` layout drawn in the README.

## Before committing
1. Extract/copy the patch into the repository root, preserving paths.
2. Inspect `git status` and ensure no existing files are overwritten.
3. Run `./mvnw test` (Windows: `mvnw.cmd test`) and resolve any compatibility issues with the current starter/dependency versions.
4. Confirm `spring.data.mongodb.uri` points to your local MongoDB. Set `worklyn.corporate-timezone=Asia/Kolkata` if not already configured.

## Integration boundary for Person 1
`EmployeeDirectory` is the only dependency Person 2 uses to read employee status, salary and manager relationship. The included fallback intentionally returns no employee records; it allows the app context to start, but employee-dependent endpoints will return 404 until Person 1 supplies an `EmployeeDirectory` adapter backed by the real employee service/repository. Do not add a duplicate Employee model here.

The `ActorContext` reads role authorities and tries common `empId`/`employeeId` principal accessors. Person 1 should confirm that their `UserPrincipal` exposes the linked employee ID, and that JWT authorities are prefixed `ROLE_` as specified in README.

## Integration boundary for Person 3
`WorkflowRecoveryPort` is the seam for HR-REWIND. Leave approval calls `registerLeaveApproval(...)`; payroll failures call `recordPayrollFailure(...)`. The included no-op implementation allows Person 2 to work before Person 3. Person 3 should provide a Spring bean implementing this interface; the conditional fallback will then back off.

Redis is treated as an optional idempotency optimization for payroll. MongoDB remains the authoritative record store. Person 3 can later replace/extend this with their distributed cache abstraction.

## Current behavior / policy notes
- Attendance uses `Asia/Kolkata` by default and marks check-ins after 09:30 as `LATE`.
- Leave days count Monday–Friday; holiday calendars are not included yet.
- Casual/sick/earned defaults follow README. Maternity/paternity are rejected until HR defines balances/policy; unpaid leave does not consume a paid balance.
- Payroll tax brackets are intentionally simple demonstration brackets, not legal tax advice. Replace with the project's agreed formula before production/demo sign-off.
- Payroll period finalization locks records. Payroll generation depends on Person 1's `EmployeeDirectory` adapter returning active employees with base salary.
- The README's AttendanceStatus list omits `ON_LEAVE` even though leave approval requires it; this patch includes `ON_LEAVE` to satisfy that workflow.

## Important follow-up
The README's planned paths use `backend/src/...`, while the live repository currently has a root `pom.xml` and `src/main/java/com/worklyn/WorklynApplication.java`. This patch follows the live Maven layout. If the team later moves the app into `backend/`, move the modules with it rather than maintaining duplicate copies.
