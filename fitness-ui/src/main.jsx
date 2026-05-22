import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { Layout } from './Layout'
import { Dashboard } from './Dashboard'
import { PlanTreninga } from './PlanTreninga'
import { Biblioteka } from './Biblioteka'
import { Ishrana } from './Ishrana'
import { Napredak } from './Napredak'
import { Profil } from './Profil'
import { TrenerPanel } from './TrenerPanel'
import { AdminPanel } from './AdminPanel'
import { Login } from './Login'
import { Register } from './Register'
import './index.css'

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" />;
};
const AdminRoute = ({ children }) => {
  const { user } = useAuth();
  return user?.roleName === 'ADMIN' ? children : <Navigate to="/" />;
};

const TrenerRoute = ({ children }) => {
  const { user } = useAuth();
  return (user?.roleName === 'ADMIN' || user?.roleName === 'TRENER') ? children : <Navigate to="/" />;
};
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="trener-panel" element={<TrenerRoute><TrenerPanel /></TrenerRoute>} />
          <Route path="admin-panel" element={<AdminRoute><AdminPanel /></AdminRoute>} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Dashboard />} />
            <Route path="plan-treninga" element={<PlanTreninga />} />
            <Route path="biblioteka" element={<Biblioteka />} />
            <Route path="ishrana" element={<Ishrana />} />
            <Route path="napredak" element={<Napredak />} />
            <Route path="profil" element={<Profil />} />
            <Route path="trener-panel" element={<TrenerPanel />} />
            <Route path="admin-panel" element={<AdminPanel />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>,
)
