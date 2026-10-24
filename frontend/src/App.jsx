import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import { ToastProvider } from './components/common/Toast.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import Layout from './components/common/Layout.jsx';

// Pages
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Employees from './pages/Employees.jsx';
import EmployeeDetails from './pages/EmployeeDetails.jsx';
import Attendance from './pages/Attendance.jsx';
import LeaveManagement from './pages/LeaveManagement.jsx';
import Payroll from './pages/Payroll.jsx';
import Recruitment from './pages/Recruitment.jsx';
import Performance from './pages/Performance.jsx';
import RewindDashboard from './pages/RewindDashboard.jsx';
import Notifications from './pages/Notifications.jsx';
import Settings from './pages/Settings.jsx';
import NotFound from './pages/NotFound.jsx';

function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<Login />} />

      {/* Redirect root → dashboard */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      {/* Protected pages – all share the Layout (sidebar + header) */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout><Dashboard /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/employees"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
            <Layout><Employees /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/employees/:id"
        element={
          <ProtectedRoute>
            <Layout><EmployeeDetails /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/attendance"
        element={
          <ProtectedRoute>
            <Layout><Attendance /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/leave"
        element={
          <ProtectedRoute>
            <Layout><LeaveManagement /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/payroll"
        element={
          <ProtectedRoute>
            <Layout><Payroll /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/recruitment"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR']}>
            <Layout><Recruitment /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/performance"
        element={
          <ProtectedRoute>
            <Layout><Performance /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/rewind"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <Layout><RewindDashboard /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <Layout><Notifications /></Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <Layout><Settings /></Layout>
          </ProtectedRoute>
        }
      />

      {/* 404 */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppRoutes />
      </ToastProvider>
    </AuthProvider>
  );
}
