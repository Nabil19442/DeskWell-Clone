import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Reports from './pages/Reports';
import FocusMode from './pages/FocusMode';
import Goals from './pages/Goals';
import Categories from './pages/Categories';
import AppBlocker from './pages/AppBlocker';
import Settings from './pages/Settings';

export default function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="reports" element={<Reports />} />
            <Route path="focus" element={<FocusMode />} />
            <Route path="goals" element={<Goals />} />
            <Route path="categories" element={<Categories />} />
            <Route path="blocker" element={<AppBlocker />} />
            <Route path="settings" element={<Settings />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}
