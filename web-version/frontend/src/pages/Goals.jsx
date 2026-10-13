import React, { useState, useEffect } from 'react';
import { goalsApi } from '../api/client';
import { 
  Target, 
  Plus, 
  Trash2, 
  Edit2, 
  Clock, 
  CheckCircle2, 
  AlertCircle, 
  ShieldCheck, 
  Loader2,
  Calendar
} from 'lucide-react';
import Modal from '../components/Modal';

export default function Goals() {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState(null);

  // Form state
  const [goalType, setGoalType] = useState('productive_time');
  const [targetHours, setTargetHours] = useState(4);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetchGoals = async () => {
    setLoading(true);
    try {
      const data = await goalsApi.getAll();
      setGoals(data || []);
    } catch (err) {
      console.error('Failed to load goals:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleOpenAdd = () => {
    setEditingGoal(null);
    setGoalType('productive_time');
    setTargetHours(4);
    setError('');
    setModalOpen(true);
  };

  const handleOpenEdit = (goal) => {
    setEditingGoal(goal);
    setGoalType(goal.type);
    setTargetHours(Math.round((goal.targetValue / 60) * 10) / 10);
    setError('');
    setModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this goal?')) return;
    try {
      await goalsApi.delete(id);
      fetchGoals();
    } catch (err) {
      alert('Failed to delete goal');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');

    const targetMinutes = Math.round(targetHours * 60);

    try {
      if (editingGoal) {
        await goalsApi.update(editingGoal.id, {
          type: goalType,
          targetValue: targetMinutes,
          startDate: editingGoal.startDate,
        });
      } else {
        await goalsApi.create({
          type: goalType,
          targetValue: targetMinutes,
          startDate: new Date().toISOString().substring(0, 10),
        });
      }
      setModalOpen(false);
      fetchGoals();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save goal');
    } finally {
      setSaving(false);
    }
  };

  const formatGoalType = (type) => {
    switch (type) {
      case 'productive_time': return 'Daily Productive Time';
      case 'screen_time': return 'Max Daily Screen Time';
      case 'focus_time': return 'Deep Focus Mode Time';
      case 'social_media': return 'Max Communication / Social Time';
      default: return type;
    }
  };

  return (
    <div className="space-y-6">
      {/* Informational Banner */}
      <div className="p-4 rounded-xl bg-blue-50 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900/60 flex items-start gap-3">
        <ShieldCheck className="w-5 h-5 text-blue-600 dark:text-blue-400 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-700 dark:text-slate-300 space-y-1">
          <p className="font-semibold text-slate-900 dark:text-white">
            Genuine Activity-Driven Progress
          </p>
          <p>
            Goal progress is dynamically aggregated from your server usage records today. Background system-wide Windows activity tracking can also be piped in via the authenticated DeskWell Windows companion agent.
          </p>
        </div>
      </div>

      {/* Header with + New Goal */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">
            Productivity Targets
          </h3>
          <p className="text-xs text-slate-500">
            Set healthy thresholds for your digital workday
          </p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-xs"
        >
          <Plus className="w-4 h-4" />
          <span>New Goal</span>
        </button>
      </div>

      {/* Goals Grid */}
      {loading ? (
        <div className="py-20 flex justify-center items-center gap-2 text-xs text-slate-400">
          <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
          <span>Loading goals...</span>
        </div>
      ) : goals.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {goals.map((g) => {
            const currentHours = Math.round((g.currentValue / 60) * 10) / 10;
            const targetH = Math.round((g.targetValue / 60) * 10) / 10;
            const isCompleted = g.isCompleted || (g.currentValue >= g.targetValue);

            return (
              <div
                key={g.id}
                className="p-5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between space-y-4"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                      isCompleted 
                        ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400' 
                        : 'bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400'
                    }`}>
                      <Target className="w-5 h-5" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                        {formatGoalType(g.type)}
                      </h4>
                      <p className="text-[11px] text-slate-500 flex items-center gap-1 mt-0.5">
                        <Calendar className="w-3 h-3" />
                        <span>Daily recurring target</span>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenEdit(g)}
                      className="p-1.5 rounded-md text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                      title="Edit Goal"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(g.id)}
                      className="p-1.5 rounded-md text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"
                      title="Delete Goal"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* Progress Bar & Numerical Target */}
                <div className="space-y-2">
                  <div className="flex items-baseline justify-between text-xs font-semibold">
                    <span className="text-slate-900 dark:text-white">
                      {currentHours}h <span className="text-slate-400 font-normal">/ {targetH}h target</span>
                    </span>
                    <span className={isCompleted ? 'text-emerald-600 dark:text-emerald-400' : 'text-blue-600 dark:text-blue-400'}>
                      {g.progressPercentage}%
                    </span>
                  </div>

                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        isCompleted ? 'bg-emerald-500' : 'bg-blue-600'
                      }`}
                      style={{ width: `${Math.min(100, g.progressPercentage)}%` }}
                    />
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-100 dark:border-slate-800/60 flex items-center justify-between text-[11px] text-slate-500">
                  <span>Status:</span>
                  <span className={`font-semibold ${
                    isCompleted ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-600 dark:text-slate-400'
                  }`}>
                    {isCompleted ? 'Goal Achieved Today' : 'In Progress'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="p-12 text-center rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
          <Target className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-3" />
          <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200 mb-1">
            No Productivity Goals Configured
          </h4>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mb-4">
            Set goals for productive development time or maximum screen time to stay mindful and balanced.
          </p>
          <button
            onClick={handleOpenAdd}
            className="px-4 py-2 text-xs font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Create Your First Goal
          </button>
        </div>
      )}

      {/* Goal Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingGoal ? 'Edit Productivity Goal' : 'Create Productivity Goal'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="p-3 text-xs rounded-lg bg-rose-50 text-rose-600 dark:bg-rose-950/40 dark:text-rose-400 border border-rose-200 dark:border-rose-900">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Goal Type
            </label>
            <select
              value={goalType}
              onChange={(e) => setGoalType(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            >
              <option value="productive_time">Daily Productive Time (Target Minimum)</option>
              <option value="screen_time">Daily Total Screen Time (Max Threshold)</option>
              <option value="focus_time">Pomodoro Focus Time (Target Minimum)</option>
              <option value="social_media">Communication / Messaging (Max Limit)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Target Value (Hours)
            </label>
            <input
              type="number"
              step="0.5"
              min="0.5"
              max="24"
              value={targetHours}
              onChange={(e) => setTargetHours(parseFloat(e.target.value) || 1)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              required
            />
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-60"
            >
              {saving && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{editingGoal ? 'Update Goal' : 'Save Goal'}</span>
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
