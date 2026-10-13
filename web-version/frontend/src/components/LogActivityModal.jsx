import React, { useState } from 'react';
import Modal from './Modal';
import { activityApi } from '../api/client';
import { Loader2, PlusCircle, CheckCircle } from 'lucide-react';

export default function LogActivityModal({ isOpen, onClose, onActivityLogged }) {
  const [processName, setProcessName] = useState('Code.exe');
  const [windowTitle, setWindowTitle] = useState('');
  const [durationMinutes, setDurationMinutes] = useState(30);
  const [isIdle, setIsIdle] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const quickPresets = [
    { label: 'VS Code', process: 'Code.exe', title: 'DeskWellApp.java' },
    { label: 'IntelliJ', process: 'idea64.exe', title: 'DeskWell - Spring Data JPA' },
    { label: 'Chrome', process: 'chrome.exe', title: 'Documentation - Google Chrome' },
    { label: 'Slack', process: 'slack.exe', title: '#general - Slack' },
    { label: 'Notion', process: 'Notion.exe', title: 'Sprint Backlog - Notion' },
    { label: 'Figma', process: 'Figma.exe', title: 'Dashboard UI Design' },
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!processName.trim()) {
      setError('Process name is required');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await activityApi.record({
        processName: processName.trim(),
        windowTitle: windowTitle.trim() || processName.trim(),
        durationSeconds: durationMinutes * 60,
        isIdle,
      });
      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
        if (onActivityLogged) onActivityLogged();
      }, 700);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to log activity to backend');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Log Activity / Ingestion Record">
      {success ? (
        <div className="py-8 flex flex-col items-center justify-center text-center">
          <CheckCircle className="w-12 h-12 text-emerald-500 mb-2 animate-bounce" />
          <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
            Activity Recorded Successfully!
          </p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="p-3 text-xs rounded-lg bg-rose-50 text-rose-600 dark:bg-rose-950/40 dark:text-rose-400 border border-rose-200 dark:border-rose-900">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Quick Presets
            </label>
            <div className="flex flex-wrap gap-1.5">
              {quickPresets.map((p) => (
                <button
                  type="button"
                  key={p.label}
                  onClick={() => {
                    setProcessName(p.process);
                    setWindowTitle(p.title);
                    setIsIdle(false);
                  }}
                  className={`px-2.5 py-1 text-xs rounded-md border transition-all ${
                    processName === p.process
                      ? 'bg-blue-50 border-blue-400 text-blue-700 dark:bg-blue-900/40 dark:border-blue-500 dark:text-blue-300 font-semibold'
                      : 'bg-slate-50 dark:bg-slate-800/80 border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Process Name (Executable)
            </label>
            <input
              type="text"
              value={processName}
              onChange={(e) => setProcessName(e.target.value)}
              placeholder="e.g. Code.exe or chrome.exe"
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Window Title (Optional)
            </label>
            <input
              type="text"
              value={windowTitle}
              onChange={(e) => setWindowTitle(e.target.value)}
              placeholder="e.g. Feature Implementation - DeskWell"
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Duration (Minutes)
              </label>
              <input
                type="number"
                min="1"
                max="720"
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-hidden focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="flex items-center pt-5">
              <label className="flex items-center gap-2 cursor-pointer text-xs font-medium text-slate-700 dark:text-slate-300">
                <input
                  type="checkbox"
                  checked={isIdle}
                  onChange={(e) => setIsIdle(e.target.checked)}
                  className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                <span>Log as Idle Time</span>
              </label>
            </div>
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-60"
            >
              {loading && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>Save Record</span>
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
}
