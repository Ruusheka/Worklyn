# Worklyn Postman Testing Guide — Person 2 Modules

> **Target Vertical:** Person 2 (Attendance, Leave Management, Payroll)  
> **Base URL:** `http://localhost:8080`  
> **API Version:** `v1`  
> **Database:** MongoDB (`worklyn_db`)  

This guide provides instructions, request bodies, expected responses, and role permissions for testing all Person 2 endpoints using the Postman collection.

---

## 1. Quick Setup & Credentials

### Import Files into Postman
1. **Collection:** [`postman/Worklyn_Person2_Collection.postman_collection.json`](Worklyn_Person2_Collection.postman_collection.json)
2. **Environment (Optional):** [`postman/Worklyn_Person2_Environment.postman_environment.json`](Worklyn_Person2_Environment.postman_environment.json)

*(The collection has embedded variables, so it works even if you keep the environment dropdown set to **No environment**).*

### Authentication Credentials
All Person 2 REST endpoints use HTTP Basic Authentication during local development:

| User | Password | Roles Assigned | Use Case |
|---|---|---|---|
| `emp-001` | `password123` | `EMPLOYEE`, `MANAGER`, `HR`, `ADMIN` | Full-access testing (can execute employee, manager, and HR actions) |

---

## 2. Attendance Module Endpoints

### 2.1 Check-In
* **URL:** `POST {{baseUrl}}/api/v1/attendance/check-in`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Headers:**  
  `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "timestamp": "2026-10-24T09:15:00Z",
    "idempotencyKey": "checkin-20261024"
  }
  ```
* **Status Rules:**
  * Timestamp **before or at 09:30 AM** (corporate timezone `Asia/Kolkata`) &rarr; Status is `PRESENT`.
  * Timestamp **after 09:30 AM** &rarr; Status is `LATE`.
  * If already checked in today or duplicate `idempotencyKey` &rarr; `409 Conflict`.
* **Expected Response (`201 Created`):**
  ```json
  {
    "id": "671...",
    "employeeId": "emp-001",
    "attendanceDate": "2026-10-24",
    "checkInTime": "2026-10-24T09:15:00Z",
    "checkOutTime": null,
    "status": "PRESENT",
    "workDurationMinutes": null,
    "ipAddress": "127.0.0.1",
    "idempotencyKey": "checkin-20261024"
  }
  ```

---

### 2.2 Check-Out
* **URL:** `POST {{baseUrl}}/api/v1/attendance/check-out`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Headers:**  
  `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "timestamp": "2026-10-24T18:00:00Z",
    "idempotencyKey": "checkout-20261024"
  }
  ```
* **Status Rules:**
  * Total duration &lt; 240 minutes (4 hours) &rarr; Status changes to `HALF_DAY`.
  * Total duration &ge; 240 minutes &rarr; Status remains `PRESENT` or `LATE`.
  * Attempting to check out without checking in &rarr; `404 Not Found`.
  * Already checked out &rarr; `409 Conflict`.
* **Expected Response (`200 OK`):**
  ```json
  {
    "id": "671...",
    "employeeId": "emp-001",
    "attendanceDate": "2026-10-24",
    "checkInTime": "2026-10-24T09:15:00Z",
    "checkOutTime": "2026-10-24T18:00:00Z",
    "status": "PRESENT",
    "workDurationMinutes": 525,
    "ipAddress": "127.0.0.1",
    "idempotencyKey": "checkin-20261024"
  }
  ```

---

### 2.3 My Attendance History
* **URL:** `GET {{baseUrl}}/api/v1/attendance/my-history?startDate=2026-10-01&endDate=2026-10-31&page=0&size=20`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Expected Response (`200 OK`):**
  ```json
  {
    "content": [
      {
        "id": "671...",
        "employeeId": "emp-001",
        "attendanceDate": "2026-10-24",
        "checkInTime": "2026-10-24T09:15:00Z",
        "checkOutTime": "2026-10-24T18:00:00Z",
        "status": "PRESENT",
        "workDurationMinutes": 525
      }
    ],
    "page": {
      "size": 20,
      "number": 0,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```

---

### 2.4 Employee Attendance History (Admin / HR / Manager)
* **URL:** `GET {{baseUrl}}/api/v1/attendance/employee/emp-001?startDate=2026-10-01&endDate=2026-10-31`
* **Auth:** `ADMIN`, `HR`, or `MANAGER`
* **Security:** A regular employee attempting to view another employee's history receives `403 Forbidden`.

---

## 3. Leave Management Endpoints

### 3.1 Query Leave Balance
* **URL:** `GET {{baseUrl}}/api/v1/leaves/balance`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Expected Response (`200 OK`):**
  ```json
  {
    "employeeId": "emp-001",
    "year": 2026,
    "annualTotal": 18,
    "annualUsed": 0,
    "annualAvailable": 18,
    "casualTotal": 12,
    "casualUsed": 0,
    "casualAvailable": 12,
    "sickTotal": 12,
    "sickUsed": 0,
    "sickAvailable": 12
  }
  ```

---

### 3.2 Apply for Leave
* **URL:** `POST {{baseUrl}}/api/v1/leaves/apply`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Headers:**  
  `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "leaveType": "CASUAL",
    "startDate": "2026-11-02",
    "endDate": "2026-11-04",
    "reason": "Personal family commitment"
  }
  ```
* **Supported `leaveType` Values:** `ANNUAL`, `CASUAL`, `SICK`, `UNPAID`
* **Validation Rules:**
  * `startDate` cannot be in the past (`400 Bad Request`).
  * `startDate` must be on or before `endDate` (`400 Bad Request`).
  * Only **business days (Monday–Friday)** count toward `totalDays`.
  * Cannot overlap an existing `PENDING` or `APPROVED` leave request (`409 Conflict`).
  * Insufficient available balance (`409 Conflict`).
* **Expected Response (`201 Created`):**
  ```json
  {
    "id": "671a5...",
    "employeeId": "emp-001",
    "leaveType": "CASUAL",
    "startDate": "2026-11-02",
    "endDate": "2026-11-04",
    "totalDays": 3,
    "reason": "Personal family commitment",
    "status": "PENDING",
    "approverId": null,
    "decisionRemarks": null,
    "decisionDate": null,
    "createdAt": "2026-10-24T16:00:00Z"
  }
  ```
*(Note: In Postman, the test script automatically captures `id` into `{{leaveId}}` for subsequent requests).*

---

### 3.3 List My Leave Requests
* **URL:** `GET {{baseUrl}}/api/v1/leaves/my-requests`
* **Auth:** Any authenticated user (`EMPLOYEE`)
* **Expected Response (`200 OK`):** Array of all leave requests sorted by creation date descending.

---

### 3.4 Approve Leave
* **URL:** `POST {{baseUrl}}/api/v1/leaves/{{leaveId}}/approve`
* **Auth:** `HR`, `ADMIN`, or direct `MANAGER`
* **Headers:**  
  `Content-Type: application/json`  
  `Idempotency-Key: leave-appr-001`
* **Request Body:**
  ```json
  {
    "remarks": "Approved by manager"
  }
  ```
* **Automated Side Effects on Approval:**
  1. Deducts `totalDays` from the employee's `LeaveBalance`.
  2. Generates `ON_LEAVE` records in the `Attendance` repository for all weekdays in the range.
  3. Registers a checkpoint with Person 3's `WorkflowRecoveryPort`.
* **Expected Response (`200 OK`):**
  ```json
  {
    "id": "671a5...",
    "employeeId": "emp-001",
    "leaveType": "CASUAL",
    "startDate": "2026-11-02",
    "endDate": "2026-11-04",
    "totalDays": 3,
    "status": "APPROVED",
    "approverId": "emp-001",
    "decisionRemarks": "Approved by manager",
    "decisionDate": "2026-10-24T16:05:00Z"
  }
  ```

---

### 3.5 Reject Leave
* **URL:** `POST {{baseUrl}}/api/v1/leaves/{{leaveId}}/reject`
* **Auth:** `HR`, `ADMIN`, or direct `MANAGER`
* **Headers:**  
  `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "remarks": "High workload during this release sprint"
  }
  ```
* **Rule:** `remarks` cannot be blank (`400 Bad Request`).
* **Expected Response (`200 OK`):** Status changes to `REJECTED`.

---

### 3.6 Cancel Leave
* **URL:** `POST {{baseUrl}}/api/v1/leaves/{{leaveId}}/cancel`
* **Auth Rules:**
  * **If leave is `PENDING`:** Either the applicant (`EMPLOYEE`) or `HR` can cancel.
  * **If leave is `APPROVED`:** Only `HR` or `ADMIN` can cancel.
* **Side Effects on Approved Cancellation:**
  * Restores deducted leave days back to `LeaveBalance`.
  * Removes the synthetic `ON_LEAVE` attendance records from `Attendance`.
* **Expected Response (`200 OK`):** Status changes to `CANCELLED`.

---

## 4. Payroll Module Endpoints

### 4.1 Process Monthly Payroll
* **URL:** `POST {{baseUrl}}/api/v1/payroll/process`
* **Auth:** `HR` or `ADMIN` (Regular employee receives `403 Forbidden`)
* **Headers:**  
  `Content-Type: application/json`  
  `Idempotency-Key: payroll-2026-10`
* **Request Body:**
  ```json
  {
    "period": "2026-10"
  }
  ```
* **Computation Engine Rules:**
  * Period format must strictly be `YYYY-MM` (`400 Bad Request` if invalid).
  * Iterates all `ACTIVE` employees from `EmployeeDirectory`.
  * Determines total working days (excluding weekends) in the month.
  * Tallies `PRESENT`, `LATE`, `HALF_DAY` (0.5), and approved paid leaves.
  * Deducts unpaid leaves.
  * Pro-rates gross salary: `(baseSalary / workingDays) * payableDays`.
  * Calculates progressive tax deduction brackets.
  * Net pay = `max(0, grossPay - taxDeduction)`.
  * Throws `409 Conflict` if the period has already been finalized.
* **Expected Response (`200 OK`):**
  ```json
  {
    "period": "2026-10",
    "processedCount": 1,
    "records": [
      {
        "id": "671...",
        "employeeId": "emp-001",
        "payrollPeriod": "2026-10",
        "baseSalary": 90000.00,
        "workingDays": 22,
        "presentDays": 22,
        "paidLeaveDays": 0,
        "unpaidLeaveDays": 0,
        "grossPay": 90000.00,
        "taxDeduction": 0.00,
        "netPay": 90000.00,
        "status": "PROCESSED",
        "processedAt": "2026-10-24T16:10:00Z",
        "processedBy": "emp-001",
        "idempotencyKey": "payroll-2026-10"
      }
    ],
    "replayed": false
  }
  ```

---

### 4.2 View Period Payroll
* **URL:** `GET {{baseUrl}}/api/v1/payroll/period/2026-10`
* **Auth:** `HR` or `ADMIN`
* **Expected Response (`200 OK`):** Array of all processed payroll records for the period.

---

### 4.3 View Employee Payslip
* **URL:** `GET {{baseUrl}}/api/v1/payroll/payslip/emp-001?period=2026-10`
* **Auth Rules:**
  * An employee can view **their own** payslip.
  * `HR` or `ADMIN` can view **any** employee's payslip.
  * Viewing another employee's payslip without HR/ADMIN role &rarr; `403 Forbidden`.
* **Expected Response (`200 OK`):** Single payslip breakdown matching the generated payroll record.

---

### 4.4 Finalize Payroll Period
* **URL:** `POST {{baseUrl}}/api/v1/payroll/finalize`
* **Auth:** `HR` or `ADMIN`
* **Headers:**  
  `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "period": "2026-10"
  }
  ```
* **Immutability Lock:**
  * Transitions all `PROCESSED` records for the period to `FINALIZED`.
  * After finalization, re-running `POST /process` for this period will return `409 Conflict: Payroll period is finalized`.
* **Expected Response (`200 OK`):**
  ```json
  {
    "message": "Payroll period 2026-10 finalized",
    "period": "2026-10"
  }
  ```

---

## 5. Recommended End-to-End Test Sequence

To verify the full integration workflow in Postman:

1. **Check-In:** Run `03-Attendance -> Check-In (Idempotent)` &rarr; verify `201 Created`.
2. **Check-Out:** Run `03-Attendance -> Check-Out` &rarr; verify `200 OK` and work duration.
3. **Check Balance:** Run `04-Leave-Management -> Query Leave Balance` &rarr; verify `annualAvailable: 18`.
4. **Apply Leave:** Run `04-Leave-Management -> Apply for Leave` &rarr; verify `201 Created` (`leaveId` is saved).
5. **Approve Leave:** Run `04-Leave-Management -> Approve Leave` &rarr; verify `200 OK` (`APPROVED`).
6. **Re-check Balance:** Run `04-Leave-Management -> Query Leave Balance` &rarr; verify available balance decreased.
7. **Process Payroll:** Run `05-Payroll -> Run Monthly Payroll` &rarr; verify salary and days calculation.
8. **View Payslip:** Run `05-Payroll -> View Employee Payslip` &rarr; verify net pay breakdown.
9. **Finalize:** Run `05-Payroll -> Finalize Payroll Period` &rarr; verify period locks.
10. **Idempotency Test:** Re-run `Run Monthly Payroll` &rarr; verify it returns `409 Conflict` (cannot alter finalized period).
