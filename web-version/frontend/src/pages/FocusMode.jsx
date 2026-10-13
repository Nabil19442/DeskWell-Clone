import React, { useState, useEffect, useRef } from 'react';
import { focusApi } from '../api/client';
import { 
  Play, 
  Pause, 
  RotateCcw, 
  SkipForward, 
  Sliders, 
  Clock, 
  CheckCircle2, 
  Flame, 
  AlertCircle,
  Coffee,
  Volume2
} from 'lucide-react';
import Modal from '../components/Modal';

export default function FocusMode() {
  const [sessionState, setSessionState] = useState(null);
  const [remainingSeconds, setRemainingSeconds] = useState(25 * 60);
  const [loading, setLoading] = useState(true);
  const [configModalOpen, setConfigModalOpen] = useState(false);
  const [history, setHistory] = useState([]);

  // Config parameters
  const [focusMinutes, setFocusMinutes] = useState(25);
  const [breakMinutes, setBreakMinutes] = useState(5);
  const [longBreakMinutes, setLongBreakMinutes] = useState(15);
  const [sessionsBeforeLongBreak, setSessionsBeforeLongBreak] = useState(4);

  const timerRef = useRef(null);

  // Sync state with server
  const fetchActiveState = async () => {
    try {
      const data = await focusApi.getActive();
      setSessionState(data);
      setFocusMinutes(data.focusDurationMinutes || 25);
      setBreakMinutes(data.breakDurationMinutes || 5);
      setLongBreakMinutes(data.longBreakDurationMinutes || 15);
      setSessionsBeforeLongBreak(data.sessionsBeforeLongBreak || 4);

      if (data.state === 'FOCUS' || data.state === 'BREAK') {
        if (data.targetEndEpochMillis) {
          const diff = Math.max(0, Math.floor((data.targetEndEpochMillis - Date.now()) / 1000));
          setRemainingSeconds(diff);
        } else {
          setRemainingSeconds(data.remainingSeconds || 0);
        }
      } else if (data.state === 'PAUSED') {
        setRemainingSeconds(data.remainingSeconds || 0);
      } else {
        setRemainingSeconds((data.focusDurationMinutes || 25) * 60);
      }
    } catch (err) {
      console.error('Error fetching focus state:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchHistory = async () => {
    try {
      const data = await focusApi.getHistory();
      setHistory(data || []);
    } catch (err) {
      console.error('Error fetching history:', err);
    }
  };

  useEffect(() => {
    fetchActiveState();
    fetchHistory();
  }, []);

  // Server-authoritative local tick with periodic background sync
  useEffect(() => {
    if (timerRef.current) clearInterval(timerRef.current);

    if (sessionState?.state === 'FOCUS' || sessionState?.state === 'BREAK') {
      timerRef.current = setInterval(() => {
        if (sessionState.targetEndEpochMillis) {
          const diff = Math.max(0, Math.floor((sessionState.targetEndEpochMillis - Date.now()) / 1000));
          setRemainingSeconds(diff);
          if (diff <= 0) {
            // Re-sync with server when timer expires
            fetchActiveState();
            fetchHistory();
          }
        } else {
          setRemainingSeconds((prev) => {
            if (prev <= 1) {
              fetchActiveState();
              fetchHistory();
              return 0;
            }
            return prev - 1;
          });
        }
      }, 1000);
    }

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [sessionState?.state, sessionState?.targetEndEpochMillis]);

  // Periodic resync with server every 10 seconds to guard against drift
  useEffect(() => {
    const syncInterval = setInterval(() => {
      if (sessionState?.state === 'FOCUS' || sessionState?.state === 'BREAK') {
        fetchActiveState();
      }
    }, 10000);
    return () => clearInterval(syncInterval);
  }, [sessionState?.state]);

  const handleStart = async () => {
    try {
      const res = await focusApi.start({
        focusDurationMinutes: focusMinutes,
        breakDurationMinutes: breakMinutes,
        longBreakDurationMinutes: longBreakMinutes,
        sessionsBeforeLongBreak: sessionsBeforeLongBreak,
      });
      setSessionState(res);
      setRemainingSeconds(res.remainingSeconds);
      fetchHistory();
    } catch (err) {
      console.error('Failed to start focus session:', err);
    }
  };

  const handlePause = async () => {
    try {
      const res = await focusApi.pause();
      setSessionState(res);
      setRemainingSeconds(res.remainingSeconds);
    } catch (err) {
      console.error('Failed to pause focus session:', err);
    }
  };

  const handleResume = async () => {
    try {
      const res = await focusApi.resume();
      setSessionState(res);
      setRemainingSeconds(res.remainingSeconds);
    } catch (err) {
      console.error('Failed to resume focus session:', err);
    }
  };

  const handleStop = async () => {
    try {
      const res = await focusApi.stop();
      setSessionState(res);
      setRemainingSeconds(focusMinutes * 60);
      fetchHistory();
    } catch (err) {
      console.error('Failed to stop focus session:', err);
    }
  };

  const handleSkipBreak = async () => {
    try {
      const res = await focusApi.skipBreak();
      setSessionState(res);
      setRemainingSeconds(focusMinutes * 60);
    } catch (err) {
      console.error('Failed to skip break:', err);
    }
  };

  const formatTimer = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const isRunning = sessionState?.state === 'FOCUS' || sessionState?.state === 'BREAK';
  const isPaused = sessionState?.state === 'PAUSED';
  const isOnBreak = sessionState?.state === 'BREAK';

  const totalDuration = isOnBreak ? breakMinutes * 60 : focusMinutes * 60;
  const progressPercent = Math.min(100, Math.max(0, ((totalDuration - remainingSeconds) / totalDuration) * 100));

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Top Banner with Today Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-medium">Completed Today</span>
            <p className="text-lg font-bold text-slate-900 dark:text-white">
              {sessionState?.completedSessionsToday || 0} Sessions
            </p>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-medium">Total Focus Time</span>
            <p className="text-lg font-bold text-slate-900 dark:text-white">
              {sessionState?.totalFocusMinutesToday || 0} Minutes
            </p>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 flex items-center justify-center">
            <Flame className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs text-slate-500 font-medium">Streak & Sessions</span>
            <p className="text-lg font-bold text-slate-900 dark:text-white">
              Cycle: {((sessionState?.completedSessionsToday || 0) % sessionsBeforeLongBreak) + 1}/{sessionsBeforeLongBreak}
            </p>
          </div>
        </div>
      </div>

      {/* Main Focus Mode Card */}
      <div className="p-8 sm:p-12 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center relative overflow-hidden">
        {/* State Badge */}
        <div className="mb-6 flex items-center gap-2">
          <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${
            isOnBreak
              ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/50 dark:text-amber-300'
              : isRunning
              ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300'
              : isPaused
              ? 'bg-purple-100 text-purple-700 dark:bg-purple-900/50 dark:text-purple-300'
              : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
          }`}>
            {isOnBreak ? 'Break Time' : isRunning ? 'Deep Focus Session' : isPaused ? 'Session Paused' : 'Ready to Focus'}
          </span>
          <button
            onClick={() => setConfigModalOpen(true)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            title="Configure Durations"
          >
            <Sliders className="w-4 h-4" />
          </button>
        </div>

        {/* Circular Progress Display */}
        <div className="relative w-64 h-64 sm:w-72 sm:h-72 flex items-center justify-center mb-8">
          <svg className="w-full h-full -rotate-90">
            <circle
              cx="50%"
              cy="50%"
              r="44%"
              className="stroke-slate-100 dark:stroke-slate-800"
              strokeWidth="10"
              fill="transparent"
            />
            <circle
              cx="50%"
              cy="50%"
              r="44%"
              className={`transition-all duration-1000 ease-linear ${
                isOnBreak ? 'stroke-amber-500' : 'stroke-blue-600'
              }`}
              strokeWidth="10"
              strokeDasharray={2 * Math.PI * 125}
              strokeDashoffset={2 * Math.PI * 125 * (1 - progressPercent / 100)}
              strokeLinecap="round"
              fill="transparent"
            />
          </svg>

          <div className="absolute flex flex-col items-center">
            <span className="text-5xl sm:text-6xl font-black text-slate-900 dark:text-white tracking-tight font-mono">
              {formatTimer(remainingSeconds)}
            </span>
            <span className="text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-widest mt-2">
              {isOnBreak ? 'Rest & Recharge' : 'Stay Immersed'}
            </span>
          </div>
        </div>

        {/* Controls */}
        <div className="flex items-center gap-3">
          {!isRunning && !isPaused ? (
            <button
              onClick={handleStart}
              className="flex items-center gap-2 px-8 py-3.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-md shadow-blue-500/20 transition-all hover:scale-105"
            >
              <Play className="w-5 h-5 fill-current" />
              <span>Start Focus Session</span>
            </button>
          ) : isRunning ? (
            <div className="flex items-center gap-3">
              <button
                onClick={handlePause}
                className="flex items-center gap-2 px-6 py-3 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-sm shadow-md transition-all"
              >
                <Pause className="w-4 h-4 fill-current" />
                <span>Pause</span>
              </button>
              {isOnBreak ? (
                <button
                  onClick={handleSkipBreak}
                  className="flex items-center gap-2 px-5 py-3 rounded-xl border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 font-bold text-sm"
                >
                  <SkipForward className="w-4 h-4" />
                  <span>Skip Break</span>
                </button>
              ) : null}
              <button
                onClick={handleStop}
                className="flex items-center gap-2 px-5 py-3 rounded-xl border border-rose-200 dark:border-rose-900 hover:bg-rose-50 dark:hover:bg-rose-950/30 text-rose-600 dark:text-rose-400 font-bold text-sm"
              >
                <RotateCcw className="w-4 h-4" />
                <span>Stop</span>
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <button
                onClick={handleResume}
                className="flex items-center gap-2 px-6 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm shadow-md transition-all"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>Resume</span>
              </button>
              <button
                onClick={handleStop}
                className="flex items-center gap-2 px-5 py-3 rounded-xl border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 font-bold text-sm"
              >
                <RotateCcw className="w-4 h-4" />
                <span>Reset</span>
              </button>
            </div>
          )}
        </div>

        <p className="mt-6 text-[11px] text-slate-400 max-w-sm">
          Server-authoritative timer: You can safely close or refresh your browser tab without interrupting or extending the active session.
        </p>
      </div>

      {/* Focus Session Configuration Modal */}
      <Modal isOpen={configModalOpen} onClose={() => setConfigModalOpen(false)} title="Focus Timer Durations">
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Focus Duration (Minutes)
            </label>
            <input
              type="number"
              min="5"
              max="120"
              value={focusMinutes}
              onChange={(e) => setFocusMinutes(Math.max(5, parseInt(e.target.value) || 25))}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Short Break (Minutes)
            </label>
            <input
              type="number"
              min="1"
              max="30"
              value={breakMinutes}
              onChange={(e) => setBreakMinutes(Math.max(1, parseInt(e.target.value) || 5))}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Long Break (Minutes)
            </label>
            <input
              type="number"
              min="5"
              max="60"
              value={longBreakMinutes}
              onChange={(e) => setLongBreakMinutes(Math.max(5, parseInt(e.target.value) || 15))}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>

          <div className="pt-2 flex justify-end">
            <button
              onClick={() => {
                if (!isRunning && !isPaused) {
                  setRemainingSeconds(focusMinutes * 60);
                }
                setConfigModalOpen(false);
              }}
              className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Save Configuration
            </button>
          </div>
        </div>
      </Modal>

      {/* History Log */}
      <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-5 shadow-xs">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white mb-1">
          Recent Focus Sessions
        </h3>
        <p className="text-xs text-slate-500 mb-4">
          History of tracked deep work and breaks
        </p>

        <div className="space-y-2">
          {history.length > 0 ? (
            history.slice(0, 6).map((h) => (
              <div key={h.id} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40 text-xs">
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${h.completed ? 'bg-emerald-500' : 'bg-slate-400'}`} />
                  <div>
                    <span className="font-semibold text-slate-900 dark:text-white">
                      {h.durationMinutes} min Session
                    </span>
                    <p className="text-[10px] text-slate-400">
                      Started: {h.startTime?.replace('T', ' ').substring(0, 16)}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <span className={`px-2 py-0.5 rounded-full font-bold text-[10px] ${
                    h.completed 
                      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/50 dark:text-emerald-300' 
                      : 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-300'
                  }`}>
                    {h.completed ? 'Completed' : 'Stopped Early'}
                  </span>
                  <p className="text-[10px] text-slate-400 mt-0.5">
                    {h.interruptions} interruptions
                  </p>
                </div>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-400 text-center py-6">
              No previous focus sessions recorded yet.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
