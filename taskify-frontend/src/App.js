import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Tasks from './pages/Tasks';
import Appointments from './pages/Appointments';
import Dashboard from './pages/Dashboard';
import DashboardLayout from './components/layout/DashboardLayout';

/**
 * Check if user is authenticated
 */
const isAuthenticated = () => {
  return localStorage.getItem('token') !== null;
};

/**
 * Protected Route component
 */
const ProtectedRoute = ({ children }) => {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return <DashboardLayout>{children}</DashboardLayout>;
};

/**
 * Main App component
 */
function App() {
  return (
    <Router>
      <div className="app min-h-screen bg-slate-50">
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={
            isAuthenticated() ? <Navigate to="/dashboard" replace /> : <Login />
          } />
          <Route path="/register" element={
            isAuthenticated() ? <Navigate to="/dashboard" replace /> : <Register />
          } />

          {/* Protected Routes */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />
          <Route path="/tasks" element={
            <ProtectedRoute>
              <Tasks />
            </ProtectedRoute>
          } />
          <Route path="/appointments" element={
            <ProtectedRoute>
              <Appointments />
            </ProtectedRoute>
          } />

          {/* Default Route */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
