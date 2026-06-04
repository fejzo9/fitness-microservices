import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { PlanTreninga } from './pages/PlanTreninga'
import { Biblioteka } from './pages/Biblioteka'
import { Ishrana } from './pages/Ishrana'
import { Napredak } from './pages/Napredak'
import { Profil } from './pages/Profil'
import { TrenerPanel } from './pages/TrenerPanel'
import { AdminPanel } from './pages/AdminPanel'
import { Login } from './pages/Login'
import { Register } from './pages/Register'
import './styles/index.css'

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-muted-foreground">Učitavanje...</div>
      </div>
    );
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
};
const AdminRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-muted-foreground">Učitavanje...</div>
      </div>
    );
  }
  
  return user?.roleName === 'ADMIN' ? children : <Navigate to="/" />;
};

const TrenerRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-muted-foreground">Učitavanje...</div>
      </div>
    );
  }
  
  return (user?.roleName === 'ADMIN' || user?.roleName === 'TRENER') ? children : <Navigate to="/" />;
};
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Dashboard />} />
            <Route path="plan-treninga" element={<PlanTreninga />} />
            <Route path="biblioteka" element={<Biblioteka />} />
            <Route path="ishrana" element={<Ishrana />} />
            <Route path="napredak" element={<Napredak />} />
            <Route path="profil" element={<Profil />} />
            <Route path="trener-panel" element={<TrenerRoute><TrenerPanel /></TrenerRoute>} />
            <Route path="admin-panel" element={<AdminRoute><AdminPanel /></AdminRoute>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>,
)
