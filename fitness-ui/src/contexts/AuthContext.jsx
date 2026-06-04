import { createContext, useContext, useState, useEffect } from 'react';
import { api, setLogoutCallback } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData && userData !== 'undefined' && userData !== 'null') {
      setIsAuthenticated(true);
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error('Error parsing user data:', error);
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = (token, refreshToken, userData) => {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));
    setIsAuthenticated(true);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
  };

  // Registruj logout callback u axios interceptoru čim je dostupan
  useEffect(() => {
    setLogoutCallback(logout);
  }, []);

  const refreshUser = async () => {
    if (user?.id) {
      try {
        const updatedUser = await api.getUserById(user.id);
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
      } catch (error) {
        console.error('Error refreshing user data:', error);
      }
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout, loading, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
