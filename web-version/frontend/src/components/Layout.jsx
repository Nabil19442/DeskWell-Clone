import React, { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import LogActivityModal from './LogActivityModal';

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [logModalOpen, setLogModalOpen] = useState(false);
  const location = useLocation();

  const getPageInfo = () => {
    switch (location.pathname) {
      case '/':
        return {
          title: 'Productivity Dashboard',
          description: 'Real-time overview of screen time, applications, and focus metrics',
        };
      case '/reports':
        return {
          title: 'Analytics & Reports',
          description: 'Historical breakdown of productivity trends and exports',
        };
      case '/focus':
        return {
          title: 'Focus Mode',
          description: 'Server-authoritative Pomodoro sessions and distraction control',
        };
      case '/goals':
        return {
          title: 'Goals & Targets',
          description: 'Track custom screen-time and productive work limits',
        };
      case '/categories':
        return {
          title: 'Application Categories',
          description: 'Productivity weights, auto-categorization rules, and classifications',
        };
      case '/blocker':
        return {
          title: 'App Blocker',
          description: 'Configured application blocking rules for desktop companion enforcement',
        };
      case '/settings':
        return {
          title: 'Settings & Companion',
          description: 'Application preferences, notification limits, and Windows Agent connection',
        };
      default:
        return { title: 'DeskWell', description: 'Digital Wellbeing Application' };
    }
  };

  const { title, description } = getPageInfo();

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex transition-colors duration-150">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 lg:pl-64 flex flex-col min-w-0">
        <Header
          title={title}
          description={description}
          onToggleSidebar={() => setSidebarOpen(prev => !prev)}
          onOpenLogModal={() => setLogModalOpen(true)}
        />

        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
          <Outlet context={{ openLogModal: () => setLogModalOpen(true) }} />
        </main>
      </div>

      <LogActivityModal
        isOpen={logModalOpen}
        onClose={() => setLogModalOpen(false)}
        onActivityLogged={() => {
          // Trigger a custom event so active pages can refresh data
          window.dispatchEvent(new Event('deskwell_activity_updated'));
        }}
      />
    </div>
  );
}
