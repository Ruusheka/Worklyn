/**
 * Mock Data Store for Worklyn Frontend
 * Conforms to REST API Reference in README Section 14
 */

export const INITIAL_USERS = [
  {
    id: "6714a8f9c102b34a11000101",
    email: "hr.admin@worklyn.io",
    password: "Password@123",
    firstName: "Sarah",
    lastName: "Jenkins",
    roles: ["ROLE_ADMIN", "ROLE_HR"],
    employeeId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
    designation: "VP of People Operations",
    department: "HUMAN_RESOURCES"
  },
  {
    id: "6714a8f9c102b34a11000103",
    email: "david.chen@worklyn.io",
    password: "Password@123",
    firstName: "David",
    lastName: "Chen",
    roles: ["ROLE_EMPLOYEE"],
    employeeId: "6714a8f9c102b34a11000104",
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
    designation: "Backend Engineer",
    department: "ENGINEERING"
  },
  {
    id: "6714a8f9c102b34a11000105",
    email: "elena.rostova@worklyn.io",
    password: "Password@123",
    firstName: "Elena",
    lastName: "Rostova",
    roles: ["ROLE_MANAGER", "ROLE_EMPLOYEE"],
    employeeId: "6714a8f9c102b34a11000106",
    avatar: "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
    designation: "Engineering Lead",
    department: "ENGINEERING"
  }
];

export const INITIAL_EMPLOYEES = [
  {
    id: "6714a8f9c102b34a11000102",
    employeeCode: "EMP-1002",
    userId: "6714a8f9c102b34a11000101",
    firstName: "Sarah",
    lastName: "Jenkins",
    email: "hr.admin@worklyn.io",
    phone: "+1 (555) 234-5678",
    department: "HUMAN_RESOURCES",
    designation: "VP of People Operations",
    dateOfJoining: "2023-01-15",
    status: "ACTIVE",
    baseSalary: 125000.00,
    managerId: null,
    avatar: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
    address: "452 Lexington Ave, New York, NY",
    emergencyContact: "Robert Jenkins (+1 555-901-2345)"
  },
  {
    id: "6714a8f9c102b34a11000104",
    employeeCode: "EMP-2045",
    userId: "6714a8f9c102b34a11000103",
    firstName: "David",
    lastName: "Chen",
    email: "david.chen@worklyn.io",
    phone: "+1 (555) 345-6789",
    department: "ENGINEERING",
    designation: "Backend Engineer",
    dateOfJoining: "2024-03-01",
    status: "ACTIVE",
    baseSalary: 95000.00,
    managerId: "6714a8f9c102b34a11000106",
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
    address: "108 Market St, San Francisco, CA",
    emergencyContact: "Mei Chen (+1 555-888-9900)"
  },
  {
    id: "6714a8f9c102b34a11000106",
    employeeCode: "EMP-3088",
    userId: "6714a8f9c102b34a11000105",
    firstName: "Elena",
    lastName: "Rostova",
    email: "elena.rostova@worklyn.io",
    phone: "+1 (555) 456-7890",
    department: "ENGINEERING",
    designation: "Engineering Lead",
    dateOfJoining: "2022-06-10",
    status: "ACTIVE",
    baseSalary: 140000.00,
    managerId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
    address: "742 Evergreen Terr, Seattle, WA",
    emergencyContact: "Alex Rostova (+1 555-777-6655)"
  },
  {
    id: "6714a8f9c102b34a11000107",
    employeeCode: "EMP-4102",
    userId: null,
    firstName: "Priya",
    lastName: "Sharma",
    email: "priya.sharma@worklyn.io",
    phone: "+1 (555) 567-8901",
    department: "DESIGN",
    designation: "Senior UI/UX Designer",
    dateOfJoining: "2023-09-18",
    status: "ACTIVE",
    baseSalary: 105000.00,
    managerId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
    address: "223 Austin Blvd, Austin, TX",
    emergencyContact: "Aarav Sharma (+1 555-444-3322)"
  },
  {
    id: "6714a8f9c102b34a11000108",
    employeeCode: "EMP-5201",
    userId: null,
    firstName: "Carlos",
    lastName: "Mendez",
    email: "carlos.mendez@worklyn.io",
    phone: "+1 (555) 678-9012",
    department: "PRODUCT",
    designation: "Senior Product Manager",
    dateOfJoining: "2023-04-12",
    status: "ACTIVE",
    baseSalary: 130000.00,
    managerId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80",
    address: "88 Ocean Drive, Miami, FL",
    emergencyContact: "Maria Mendez (+1 555-222-1133)"
  },
  {
    id: "6714a8f9c102b34a11000109",
    employeeCode: "EMP-6110",
    userId: null,
    firstName: "Aisha",
    lastName: "Bello",
    email: "aisha.bello@worklyn.io",
    phone: "+1 (555) 789-0123",
    department: "FINANCE",
    designation: "Financial Analyst",
    dateOfJoining: "2024-01-08",
    status: "ACTIVE",
    baseSalary: 92000.00,
    managerId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=150&auto=format&fit=crop&q=80",
    address: "310 Michigan Ave, Chicago, IL",
    emergencyContact: "Tunde Bello (+1 555-333-8877)"
  },
  {
    id: "6714a8f9c102b34a11000110",
    employeeCode: "EMP-7023",
    userId: null,
    firstName: "Liam",
    lastName: "O'Connor",
    email: "liam.oconnor@worklyn.io",
    phone: "+1 (555) 890-1234",
    department: "MARKETING",
    designation: "Growth Marketing Lead",
    dateOfJoining: "2026-08-01",
    status: "PROBATION",
    baseSalary: 88000.00,
    managerId: "6714a8f9c102b34a11000102",
    avatar: "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150&auto=format&fit=crop&q=80",
    address: "65 Cambridge St, Boston, MA",
    emergencyContact: "Siobhan O'Connor (+1 555-666-4433)"
  }
];

export const INITIAL_ATTENDANCE = [
  {
    id: "att_101",
    employeeId: "6714a8f9c102b34a11000104",
    employeeName: "David Chen",
    employeeCode: "EMP-2045",
    department: "ENGINEERING",
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: `${new Date().toISOString().split('T')[0]}T08:58:12.000Z`,
    checkOutTime: null,
    durationMinutes: null,
    status: "PRESENT",
    ipAddress: "192.168.1.14"
  },
  {
    id: "att_102",
    employeeId: "6714a8f9c102b34a11000102",
    employeeName: "Sarah Jenkins",
    employeeCode: "EMP-1002",
    department: "HUMAN_RESOURCES",
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: `${new Date().toISOString().split('T')[0]}T08:42:00.000Z`,
    checkOutTime: null,
    durationMinutes: null,
    status: "PRESENT",
    ipAddress: "192.168.1.10"
  },
  {
    id: "att_103",
    employeeId: "6714a8f9c102b34a11000106",
    employeeName: "Elena Rostova",
    employeeCode: "EMP-3088",
    department: "ENGINEERING",
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: `${new Date().toISOString().split('T')[0]}T09:15:30.000Z`,
    checkOutTime: null,
    durationMinutes: null,
    status: "LATE",
    ipAddress: "192.168.1.22"
  },
  {
    id: "att_104",
    employeeId: "6714a8f9c102b34a11000107",
    employeeName: "Priya Sharma",
    employeeCode: "EMP-4102",
    department: "DESIGN",
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: `${new Date().toISOString().split('T')[0]}T08:50:00.000Z`,
    checkOutTime: null,
    durationMinutes: null,
    status: "PRESENT",
    ipAddress: "192.168.1.45"
  },
  {
    id: "att_105",
    employeeId: "6714a8f9c102b34a11000108",
    employeeName: "Carlos Mendez",
    employeeCode: "EMP-5201",
    department: "PRODUCT",
    attendanceDate: new Date().toISOString().split('T')[0],
    checkInTime: null,
    checkOutTime: null,
    durationMinutes: null,
    status: "ON_LEAVE",
    ipAddress: null
  },
  {
    id: "att_099",
    employeeId: "6714a8f9c102b34a11000104",
    employeeName: "David Chen",
    employeeCode: "EMP-2045",
    department: "ENGINEERING",
    attendanceDate: "2026-10-23",
    checkInTime: "2026-10-23T08:55:00.000Z",
    checkOutTime: "2026-10-23T17:35:00.000Z",
    durationMinutes: 520,
    status: "PRESENT",
    ipAddress: "192.168.1.14"
  },
  {
    id: "att_098",
    employeeId: "6714a8f9c102b34a11000104",
    employeeName: "David Chen",
    employeeCode: "EMP-2045",
    department: "ENGINEERING",
    attendanceDate: "2026-10-22",
    checkInTime: "2026-10-22T09:12:00.000Z",
    checkOutTime: "2026-10-22T17:45:00.000Z",
    durationMinutes: 513,
    status: "LATE",
    ipAddress: "192.168.1.14"
  },
  {
    id: "att_097",
    employeeId: "6714a8f9c102b34a11000104",
    employeeName: "David Chen",
    employeeCode: "EMP-2045",
    department: "ENGINEERING",
    attendanceDate: "2026-10-21",
    checkInTime: "2026-10-21T08:50:00.000Z",
    checkOutTime: "2026-10-21T13:00:00.000Z",
    durationMinutes: 250,
    status: "HALF_DAY",
    ipAddress: "192.168.1.14"
  }
];

export const INITIAL_LEAVE_BALANCES = {
  casual: { total: 12, used: 4, remaining: 8 },
  sick: { total: 10, used: 2, remaining: 8 },
  earned: { total: 15, used: 5, remaining: 10 },
  year: 2026
};

export const INITIAL_LEAVE_REQUESTS = [
  {
    id: "leave_001",
    employeeId: "6714a8f9c102b34a11000104",
    employeeName: "David Chen",
    employeeCode: "EMP-2045",
    department: "ENGINEERING",
    leaveType: "CASUAL",
    startDate: "2026-11-05",
    endDate: "2026-11-06",
    daysCount: 2,
    reason: "Family gathering and travel",
    status: "PENDING",
    appliedAt: "2026-10-22T11:20:00.000Z",
    managerRemarks: null
  },
  {
    id: "leave_002",
    employeeId: "6714a8f9c102b34a11000108",
    employeeName: "Carlos Mendez",
    employeeCode: "EMP-5201",
    department: "PRODUCT",
    leaveType: "EARNED",
    startDate: "2026-10-24",
    endDate: "2026-10-28",
    daysCount: 5,
    reason: "Annual vacation trip",
    status: "APPROVED",
    appliedAt: "2026-10-15T09:00:00.000Z",
    managerRemarks: "Approved. Handover documentation received."
  },
  {
    id: "leave_003",
    employeeId: "6714a8f9c102b34a11000107",
    employeeName: "Priya Sharma",
    employeeCode: "EMP-4102",
    department: "DESIGN",
    leaveType: "SICK",
    startDate: "2026-10-18",
    endDate: "2026-10-19",
    daysCount: 2,
    reason: "Flu and doctor consultation",
    status: "APPROVED",
    appliedAt: "2026-10-18T08:15:00.000Z",
    managerRemarks: "Get well soon."
  },
  {
    id: "leave_004",
    employeeId: "6714a8f9c102b34a11000110",
    employeeName: "Liam O'Connor",
    employeeCode: "EMP-7023",
    department: "MARKETING",
    leaveType: "CASUAL",
    startDate: "2026-10-10",
    endDate: "2026-10-12",
    daysCount: 3,
    reason: "Personal appointment",
    status: "REJECTED",
    appliedAt: "2026-10-08T14:30:00.000Z",
    managerRemarks: "High priority product launch milestone during these dates."
  }
];

export const INITIAL_PAYROLL_PERIODS = [
  {
    period: "2026-10",
    status: "PROCESSING",
    totalEmployees: 7,
    processedEmployees: 7,
    totalGross: 647000.00,
    totalDeductions: 58230.00,
    totalNetDisbursement: 588770.00,
    processedAt: "2026-10-24T06:00:00.000Z"
  },
  {
    period: "2026-09",
    status: "FINALIZED",
    totalEmployees: 7,
    processedEmployees: 7,
    totalGross: 647000.00,
    totalDeductions: 57400.00,
    totalNetDisbursement: 589600.00,
    processedAt: "2026-09-30T18:00:00.000Z"
  }
];

export const INITIAL_PAYSLIPS = [
  {
    id: "pay_2026_10_001",
    employeeId: "6714a8f9c102b34a11000104",
    employeeCode: "EMP-2045",
    employeeName: "David Chen",
    designation: "Backend Engineer",
    department: "ENGINEERING",
    period: "2026-10",
    payDate: "2026-10-31",
    presentDays: 22,
    workingDays: 22,
    leaveDaysDeducted: 0,
    earnings: {
      basicPay: 55000.00,
      hra: 22000.00,
      specialAllowance: 18000.00,
      bonus: 0.00,
      grossPay: 95000.00
    },
    deductions: {
      providentFund: 6600.00,
      professionalTax: 200.00,
      incomeTaxTds: 8500.00,
      unpaidLeaveDeduction: 0.00,
      totalDeductions: 15300.00
    },
    netPay: 79700.00,
    bankAccountMasked: "•••• •••• •••• 4092",
    status: "GENERATED"
  }
];

export const INITIAL_REWIND_WORKFLOWS = [
  {
    workflowId: "6714a930c102",
    workflowType: "LEAVE_APPROVAL_CASCADE",
    status: "FAILED",
    initiatedBy: "elena.rostova@worklyn.io",
    initiatedAt: "2026-10-23T14:32:00.000Z",
    failedStepId: "STEP_ADJUST_PAYROLL",
    failureReason: "HTTP Connection timeout calling external payroll service",
    targetEntity: "EMP-2045",
    steps: [
      { id: "STEP_VALIDATE_BALANCE", name: "Validate Leave Balance", status: "COMPLETED", durationMs: 45, dependsOn: [] },
      { id: "STEP_DEDUCT_LEAVE_BALANCE", name: "Deduct Leave Balance", status: "COMPLETED", durationMs: 120, dependsOn: ["STEP_VALIDATE_BALANCE"] },
      { id: "STEP_MARK_ATTENDANCE", name: "Mark Attendance Calendar", status: "COMPLETED", durationMs: 95, dependsOn: ["STEP_DEDUCT_LEAVE_BALANCE"] },
      { id: "STEP_ADJUST_PAYROLL", name: "Recalculate Period Payroll", status: "FAILED", durationMs: 5002, dependsOn: ["STEP_MARK_ATTENDANCE"], retryCount: 1, maxRetries: 3 },
      { id: "STEP_SEND_NOTIFICATIONS", name: "Dispatch Email Notification", status: "PENDING", durationMs: 0, dependsOn: ["STEP_ADJUST_PAYROLL"] }
    ]
  },
  {
    workflowId: "6714b140c103",
    workflowType: "BATCH_PAYROLL_RUN",
    status: "COMPLETED",
    initiatedBy: "hr.admin@worklyn.io",
    initiatedAt: "2026-10-20T09:00:00.000Z",
    failedStepId: null,
    failureReason: null,
    targetEntity: "BATCH-2026-10",
    steps: [
      { id: "STEP_AGGREGATE_HOURS", name: "Aggregate Attendance Hours", status: "COMPLETED", durationMs: 240, dependsOn: [] },
      { id: "STEP_CALCULATE_TAXES", name: "Calculate Statutory Deductions", status: "COMPLETED", durationMs: 180, dependsOn: ["STEP_AGGREGATE_HOURS"] },
      { id: "STEP_GENERATE_PAYSLIPS", name: "Generate Immutable Payslips", status: "COMPLETED", durationMs: 310, dependsOn: ["STEP_CALCULATE_TAXES"] },
      { id: "STEP_NOTIFY_EMPLOYEES", name: "Trigger Employee Alerts", status: "COMPLETED", durationMs: 80, dependsOn: ["STEP_GENERATE_PAYSLIPS"] }
    ]
  },
  {
    workflowId: "6714c890c104",
    workflowType: "EMPLOYEE_OFFBOARDING",
    status: "REQUIRES_MANUAL_REVIEW",
    initiatedBy: "hr.admin@worklyn.io",
    initiatedAt: "2026-10-21T16:15:00.000Z",
    failedStepId: "STEP_REVOKE_SSO",
    failureReason: "External IdP provider returned 409 Conflict: Active token revoke timeout",
    targetEntity: "EMP-0981",
    steps: [
      { id: "STEP_FREEZE_SALARY", name: "Freeze Salary Disbursement", status: "COMPLETED", durationMs: 65, dependsOn: [] },
      { id: "STEP_CALCULATE_GRATUITY", name: "Compute Settlement Package", status: "COMPLETED", durationMs: 110, dependsOn: ["STEP_FREEZE_SALARY"] },
      { id: "STEP_REVOKE_SSO", name: "Revoke SSO & Corporate Access", status: "FAILED", durationMs: 3000, dependsOn: ["STEP_FREEZE_SALARY"] },
      { id: "STEP_ARCHIVE_RECORDS", name: "Archive Compliance Documents", status: "BLOCKED", durationMs: 0, dependsOn: ["STEP_REVOKE_SSO"] }
    ]
  }
];

export const INITIAL_RECRUITMENT = {
  jobs: [
    {
      id: "job_01",
      title: "Senior Full-Stack Engineer",
      department: "ENGINEERING",
      location: "San Francisco, CA (Hybrid)",
      type: "Full-Time",
      openings: 2,
      applicantsCount: 38,
      status: "OPEN",
      postedDate: "2026-10-01"
    },
    {
      id: "job_02",
      title: "Product Marketing Manager",
      department: "MARKETING",
      location: "New York, NY (Remote)",
      type: "Full-Time",
      openings: 1,
      applicantsCount: 24,
      status: "OPEN",
      postedDate: "2026-10-10"
    },
    {
      id: "job_03",
      title: "Staff Data Engineer",
      department: "DATA_PLATFORM",
      location: "Remote (US/Canada)",
      type: "Full-Time",
      openings: 1,
      applicantsCount: 16,
      status: "REVIEWING",
      postedDate: "2026-09-28"
    },
    {
      id: "job_04",
      title: "HR Operations Associate",
      department: "HUMAN_RESOURCES",
      location: "New York, NY (On-site)",
      type: "Full-Time",
      openings: 1,
      applicantsCount: 42,
      status: "INTERVIEWING",
      postedDate: "2026-10-05"
    }
  ],
  candidates: [
    {
      id: "cand_01",
      name: "Marcus Vance",
      email: "marcus.v@example.com",
      role: "Senior Full-Stack Engineer",
      stage: "TECHNICAL_INTERVIEW",
      rating: 4.8,
      appliedDate: "2026-10-12",
      status: "SHORTLISTED"
    },
    {
      id: "cand_02",
      name: "Sophia Lindqvist",
      email: "sophia.l@example.com",
      role: "Senior Full-Stack Engineer",
      stage: "FINAL_ROUND",
      rating: 4.9,
      appliedDate: "2026-10-08",
      status: "OFFER_EXTENDED"
    },
    {
      id: "cand_03",
      name: "Jamal Edwards",
      email: "j.edwards@example.com",
      role: "Product Marketing Manager",
      stage: "INITIAL_SCREEN",
      rating: 4.2,
      appliedDate: "2026-10-18",
      status: "APPLIED"
    },
    {
      id: "cand_04",
      name: "Clara Zhang",
      email: "clara.z@example.com",
      role: "Staff Data Engineer",
      stage: "TECHNICAL_INTERVIEW",
      rating: 4.6,
      appliedDate: "2026-10-14",
      status: "SHORTLISTED"
    }
  ]
};

export const INITIAL_NOTIFICATIONS = [
  {
    id: "notif_01",
    category: "Leave",
    title: "New Leave Application",
    description: "David Chen submitted a Casual Leave request for 2 days (Nov 5 - Nov 6).",
    timestamp: "10 minutes ago",
    read: false,
    link: "/leave"
  },
  {
    id: "notif_02",
    category: "HR",
    title: "HR-REWIND Workflow Alert",
    description: "Leave Approval Cascade (#6714a930c102) step STEP_ADJUST_PAYROLL failed transiently.",
    timestamp: "1 hour ago",
    read: false,
    link: "/rewind"
  },
  {
    id: "notif_03",
    category: "Payroll",
    title: "October Payroll Calculated",
    description: "Batch payroll calculation for period 2026-10 completed. Total net disbursement: $588,770.",
    timestamp: "4 hours ago",
    read: true,
    link: "/payroll"
  },
  {
    id: "notif_04",
    category: "Recruitment",
    title: "Candidate Shortlisted",
    description: "Marcus Vance has been moved to Technical Interview for Senior Full-Stack Engineer.",
    timestamp: "1 day ago",
    read: true,
    link: "/recruitment"
  }
];

export const INITIAL_PERFORMANCE = {
  goals: [
    {
      id: "goal_01",
      title: "Deliver Worklyn Core Microservices Architecture",
      category: "Technical Excellence",
      progress: 85,
      targetDate: "2026-11-15",
      status: "ON_TRACK"
    },
    {
      id: "goal_02",
      title: "Implement Zero-Downtime HR-REWIND DAG Engine",
      category: "Resilience",
      progress: 92,
      targetDate: "2026-11-01",
      status: "ON_TRACK"
    },
    {
      id: "goal_03",
      title: "Scale Token Bucket Rate Limiter to 10k RPS",
      category: "Performance",
      progress: 70,
      targetDate: "2026-11-30",
      status: "ATTENTION_NEEDED"
    }
  ],
  reviews: [
    {
      id: "rev_01",
      reviewer: "Elena Rostova",
      reviewerRole: "Engineering Lead",
      cycle: "Q3 2026 Performance Review",
      rating: "Exceeds Expectations",
      date: "2026-10-01",
      feedback: "Exceptional architecture rigor, thorough distributed resilience testing, and collaborative cross-team integration."
    }
  ]
};
