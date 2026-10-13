import React, { useState, useEffect } from 'react';
import { appBlockerApi } from '../api/client';
import { 
  ShieldAlert, 
  Plus, 
  Trash2, 
  Info, 
  Laptop, 
  Lock, 
  CheckCircle2, 
  XCircle,
  Loader2,
  AlertTriangle
} from 'lucide-react';
import Modal from '../components/Modal';

export default function AppBlocker() {
  const [rules, setRules] = useState([]);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);

  // Form state
  const [appName, setAppName] = useState('');
  const [isPermanent, setIsPermanent] = useState(false);
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchRules = async () => {
    setLoading(true);
    try {
      const [rulesData, statusData] = await Promise.all([
        appBlockerApi.getRules(),
        appBlockerApi.getStatus(),
      ]);
      setRules(rulesData || []);
      setStatus(statusData);
    } catch (err) {
      console.error('Failed to load blocker rules:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleAddRule = async (e) => {
    e.preventDefault();
    if (!appName.trim()) return;
    setSaving(true);
    try {
      await appBlockerApi.addRule({
        appName: appName.trim(),
        isPermanent,
        notes: notes.trim(),
      });
      setModalOpen(false);
      setAppName('');
      setNotes('');
      setIsPermanent(false);
      fetchRules();
    } catch (err) {
      alert('Failed to add blocking rule');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteRule = async (id) => {
    if (!window.confirm('Delete this blocking rule?')) return;
    try {
      await appBlockerApi.removeRule(id);
      fetchRules();
    } catch (err) {
      alert('Failed to delete rule');
    }
  };

  return (
    <div className="space-y-6">
      {/* Critical Architecture & Limitation Notice Banner */}
      <div className="p-5 rounded-2xl bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-900/60 flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-700 dark:text-slate-300 space-y-2">
          <p className="font-bold text-slate-900 dark:text-white text-sm">
            Desktop Process Enforcement Architecture
          </p>
          <p>
            Web browser sandboxes strictly prohibit arbitrary local OS process termination (such as Windows <code className="px-1.5 py-0.5 bg-amber-100 dark:bg-amber-900/40 rounded font-mono">TerminateProcess</code> or <code className="px-1.5 py-0.5 bg-amber-100 dark:bg-amber-900/40 rounded font-mono">taskkill</code>).
          </p>
          <p className="font-medium text-amber-900 dark:text-amber-200">
            Rules configured below are synchronized with the DeskWell backend. When your desktop runs the DeskWell Windows Agent companion, it authenticates with the server, fetches these exact rules, and enforces desktop blocking in real-time.
          </p>
        </div>
      </div>

      {/* Enforcement Status Card */}
      <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-slate-600 dark:text-slate-300">
            <Laptop className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
              Enforcement Mode
            </h4>
            <p className="text-xs text-slate-500">
              {status?.statusMessage || 'Desktop companion agent status'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 rounded-full text-xs font-semibold flex items-center gap-1.5 ${
            status?.agentConnected
              ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/50 dark:text-emerald-300'
              : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
          }`}>
            {status?.agentConnected ? (
              <>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                <span>Agent Enforcing</span>
              </>
            ) : (
              <>
                <XCircle className="w-3.5 h-3.5 text-slate-400" />
                <span>Companion Not Connected</span>
              </>
            )}
          </span>
          <button
            onClick={() => setModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Block Rule</span>
          </button>
        </div>
      </div>

      {/* Rules Table */}
      <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-5 shadow-xs">
        <div className="mb-4">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            Configured Blocking Rules ({rules.length})
          </h3>
          <p className="text-xs text-slate-500">
            Applications targeted for suppression during focus mode or permanently
          </p>
        </div>

        {loading ? (
          <div className="py-12 flex justify-center text-xs text-slate-400">
            <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="border-b border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 uppercase font-semibold">
                <tr>
                  <th className="pb-3 pl-2">Target Application</th>
                  <th className="pb-3">Blocking Scope</th>
                  <th className="pb-3">Notes / Purpose</th>
                  <th className="pb-3 text-right pr-2">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-slate-800 dark:text-slate-200">
                {rules.length > 0 ? (
                  rules.map((rule) => (
                    <tr key={rule.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40">
                      <td className="py-3 pl-2 font-semibold flex items-center gap-2">
                        <Lock className="w-3.5 h-3.5 text-rose-500" />
                        <span>{rule.application?.name}</span>
                      </td>
                      <td className="py-3">
                        <span className={`px-2 py-0.5 rounded-sm font-semibold text-[11px] ${
                          rule.isPermanent 
                            ? 'bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300' 
                            : 'bg-purple-100 text-purple-700 dark:bg-purple-950/60 dark:text-purple-300'
                        }`}>
                          {rule.isPermanent ? 'Permanent 24/7' : 'Focus Mode Only'}
                        </span>
                      </td>
                      <td className="py-3 text-slate-500">
                        {rule.notes || 'No description provided'}
                      </td>
                      <td className="py-3 text-right pr-2">
                        <button
                          onClick={() => handleDeleteRule(rule.id)}
                          className="p-1 rounded-md text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"
                          title="Remove Rule"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" className="py-8 text-center text-slate-400">
                      No application blocking rules configured yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Rule Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Configure Application Block Rule">
        <form onSubmit={handleAddRule} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Process Name or Executable
            </label>
            <input
              type="text"
              value={appName}
              onChange={(e) => setAppName(e.target.value)}
              placeholder="e.g. Steam.exe, Discord.exe, chrome.exe"
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Notes (Optional)
            </label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Avoid gaming distractions during work hours"
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>

          <div className="pt-2">
            <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700 dark:text-slate-300">
              <input
                type="checkbox"
                checked={isPermanent}
                onChange={(e) => setIsPermanent(e.target.checked)}
                className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
              />
              <span>Permanent Block (Always block, not just during Focus Mode)</span>
            </label>
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Save Rule
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
