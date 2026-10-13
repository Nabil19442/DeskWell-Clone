import React, { useState, useEffect } from 'react';
import { settingsApi } from '../api/client';
import { useTheme } from '../context/ThemeContext';
import { 
  Settings as SettingsIcon, 
  Sun, 
  Moon, 
  Bell, 
  Clock, 
  Shield, 
  Key, 
  Save, 
  Check, 
  Copy, 
  Info,
  Loader2
} from 'lucide-react';

export default function Settings() {
  const { theme, setTheme } = useTheme();
  const [settings, setSettings] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [copiedKey, setCopiedKey] = useState(false);

  // Form fields
  const [frequency, setFrequency] = useState('3');
  const [idleThreshold, setIdleThreshold] = useState('60');
  const [breakReminderEnabled, setBreakReminderEnabled] = useState(true);
  const [breakInterval, setBreakInterval] = useState('60');
  const [screenWarningEnabled, setScreenWarningEnabled] = useState(true);
  const [screenThreshold, setScreenThreshold] = useState('360');

  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const data = await settingsApi.getAll();
        setSettings(data || {});
        setFrequency(data.tracking_frequency_seconds || '3');
        setIdleThreshold(data.idle_threshold_seconds || '60');
        setBreakReminderEnabled(data.break_reminder_enabled !== 'false');
        setBreakInterval(data.break_interval_minutes || '60');
        setScreenWarningEnabled(data.screen_time_warning_enabled !== 'false');
        setScreenThreshold(data.screen_time_warning_threshold || '360');
      } catch (err) {
        console.error('Failed to load settings:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchSettings();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        theme,
        tracking_frequency_seconds: frequency,
        idle_threshold_seconds: idleThreshold,
        break_reminder_enabled: String(breakReminderEnabled),
        break_interval_minutes: breakInterval,
        screen_time_warning_enabled: String(screenWarningEnabled),
        screen_time_warning_threshold: screenThreshold,
      };
      await settingsApi.updateAll(payload);
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 2000);
    } catch (err) {
      alert('Failed to save settings to server');
    } finally {
      setSaving(false);
    }
  };

  const copyApiKey = () => {
    navigator.clipboard.writeText(settings.agent_api_key || 'deskwell-agent-local-token');
    setCopiedKey(true);
    setTimeout(() => setCopiedKey(false), 2000);
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <form onSubmit={handleSave} className="space-y-6">
        {/* Appearance Settings */}
        <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
          <div className="flex items-center gap-2.5 mb-4">
            <Sun className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Appearance & Theme
            </h3>
          </div>

          <div className="grid grid-cols-2 gap-3 max-w-md">
            <button
              id="theme-btn-light"
              type="button"
              onClick={() => setTheme('light')}
              aria-label="Set Light Theme"
              className={`p-4 rounded-xl border flex flex-col items-center gap-2 transition-all cursor-pointer ${
                theme === 'light'
                  ? 'border-blue-600 bg-blue-50/60 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 font-bold shadow-xs ring-2 ring-blue-500/20'
                  : 'border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'
              }`}
            >
              <Sun className="w-5 h-5 text-amber-500" />
              <span className="text-xs">Light Mode</span>
            </button>

            <button
              id="theme-btn-dark"
              type="button"
              onClick={() => setTheme('dark')}
              aria-label="Set Dark Theme"
              className={`p-4 rounded-xl border flex flex-col items-center gap-2 transition-all cursor-pointer ${
                theme === 'dark'
                  ? 'border-blue-500 bg-blue-50/60 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 font-bold shadow-xs ring-2 ring-blue-500/20'
                  : 'border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'
              }`}
            >
              <Moon className="w-5 h-5 text-blue-400" />
              <span className="text-xs">Dark Mode</span>
            </button>
          </div>
        </div>

        {/* Tracking & Idle Detection Parameters */}
        <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
          <div className="flex items-center gap-2.5 mb-4">
            <Clock className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Tracking & Detection Parameters
            </h3>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Polling Frequency (Seconds)
              </label>
              <input
                type="number"
                min="1"
                max="60"
                value={frequency}
                onChange={(e) => setFrequency(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              />
              <p className="text-[11px] text-slate-400 mt-1">
                Sampling interval when ingesting active foreground window states.
              </p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Idle Detection Threshold (Seconds)
              </label>
              <input
                type="number"
                min="10"
                max="600"
                value={idleThreshold}
                onChange={(e) => setIdleThreshold(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              />
              <p className="text-[11px] text-slate-400 mt-1">
                Duration of inactivity before the user is considered away from keyboard.
              </p>
            </div>
          </div>
        </div>

        {/* Wellbeing Reminders & Notification Preferences */}
        <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
          <div className="flex items-center gap-2.5 mb-4">
            <Bell className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Wellbeing & In-App Notifications
            </h3>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40">
              <div>
                <p className="text-xs font-semibold text-slate-900 dark:text-white">
                  Break Reminders
                </p>
                <p className="text-[11px] text-slate-500">
                  Periodic reminder every {breakInterval} minutes to stretch and rest eyes
                </p>
              </div>
              <input
                type="checkbox"
                checked={breakReminderEnabled}
                onChange={(e) => setBreakReminderEnabled(e.target.checked)}
                className="rounded border-slate-300 text-blue-600 focus:ring-blue-500 w-4 h-4"
              />
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40">
              <div>
                <p className="text-xs font-semibold text-slate-900 dark:text-white">
                  Daily Screen Time Limit Warning
                </p>
                <p className="text-[11px] text-slate-500">
                  Alert when total active screen time exceeds {Math.round(screenThreshold / 60)} hours
                </p>
              </div>
              <input
                type="checkbox"
                checked={screenWarningEnabled}
                onChange={(e) => setScreenWarningEnabled(e.target.checked)}
                className="rounded border-slate-300 text-blue-600 focus:ring-blue-500 w-4 h-4"
              />
            </div>
          </div>
        </div>

        {/* Windows Companion Agent & Desktop Integration */}
        <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
          <div className="flex items-center gap-2.5 mb-3">
            <Shield className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              DeskWell Windows Agent Companion
            </h3>
          </div>

          <p className="text-xs text-slate-600 dark:text-slate-400 mb-4 leading-relaxed">
            Standard web browsers cannot access native Windows registry startup keys, minimize to the Windows system tray, or inspect system processes. The optional <strong>DeskWell Windows Companion Agent</strong> runs locally and communicates with your server backend.
          </p>

          <div className="p-3 rounded-lg bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2 min-w-0">
              <Key className="w-4 h-4 text-slate-500 shrink-0" />
              <span className="text-xs font-mono text-slate-800 dark:text-slate-200 truncate">
                {settings.agent_api_key || 'deskwell-agent-local-token'}
              </span>
            </div>
            <button
              type="button"
              onClick={copyApiKey}
              className="flex items-center gap-1 px-2.5 py-1 text-xs font-semibold bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-md text-slate-700 dark:text-slate-200 hover:bg-slate-50"
            >
              {copiedKey ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copiedKey ? 'Copied' : 'Copy'}</span>
            </button>
          </div>
        </div>

        {/* Save Bar */}
        <div className="flex items-center justify-end gap-3 pt-2">
          {savedSuccess && (
            <span className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
              <Check className="w-4 h-4" />
              <span>Settings saved!</span>
            </span>
          )}
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 px-6 py-2.5 text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white rounded-xl shadow-xs transition-colors disabled:opacity-60"
          >
            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
            <span>Save Settings</span>
          </button>
        </div>
      </form>
    </div>
  );
}
