import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../api/client';
import MetricCard from '../components/MetricCard';
import { 
  Clock, 
  CheckCircle2, 
  Coffee, 
  TrendingUp, 
  Activity, 
  AppWindow, 
  PieChart as PieIcon, 
  BarChart2, 
  PlusCircle,
  RefreshCw,
  AlertCircle
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  Tooltip,
  Legend
} from 'recharts';
import { useOutletContext } from 'react-router-dom';
import { useTheme } from '../context/ThemeContext';

function formatHours(seconds) {
  if (!seconds) return '0h 00m';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  return `${h}h ${m.toString().padStart(2, '0')}m`;
}

export default function Dashboard() {
  const { openLogModal } = useOutletContext();
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const tooltipStyle = {
    backgroundColor: isDark ? '#0f172a' : '#ffffff',
    borderColor: isDark ? '#1e293b' : '#e2e8f0',
    color: isDark ? '#f8fafc' : '#0f172a',
    borderRadius: '8px',
    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
    fontSize: '12px'
  };
  const tooltipItemStyle = {
    color: isDark ? '#f8fafc' : '#0f172a'
  };
  const axisStroke = isDark ? '#64748b' : '#94a3b8';

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await dashboardApi.getStats();
      setData(res);
    } catch (err) {
      setError('Unable to load dashboard metrics from backend.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
    const handleUpdate = () => fetchStats();
    window.addEventListener('deskwell_activity_updated', handleUpdate);
    return () => window.removeEventListener('deskwell_activity_updated', handleUpdate);
  }, []);

  if (loading && !data) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] gap-3">
        <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
        <p className="text-sm text-slate-500 dark:text-slate-400">Loading productivity statistics...</p>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="p-6 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 text-center">
        <AlertCircle className="w-10 h-10 text-rose-500 mx-auto mb-2" />
        <h3 className="text-sm font-bold text-rose-900 dark:text-rose-200 mb-1">Failed to Connect to Backend</h3>
        <p className="text-xs text-rose-600 dark:text-rose-400 mb-4">{error}</p>
        <button
          onClick={fetchStats}
          className="px-4 py-2 text-xs font-semibold bg-rose-600 text-white rounded-lg hover:bg-rose-700"
        >
          Retry Connection
        </button>
      </div>
    );
  }

  const {
    totalScreenTimeToday = 0,
    productiveTimeToday = 0,
    idleTimeToday = 0,
    productivityPercentage = 0,
    activeApplicationName = 'DeskWell',
    currentTrackingStatus = 'Web Standalone',
    topApplications = [],
    categoryBreakdown = [],
    hourlyActivity = [],
    weeklyTrend = [],
  } = data || {};

  const hourlyChartData = hourlyActivity
    .filter(h => h.hour >= 6 && h.hour <= 23)
    .map(h => ({
      hour: `${h.hour}:00`,
      minutes: Math.round(h.totalSeconds / 60),
    }));

  const trendChartData = weeklyTrend.map(t => ({
    date: t.date.substring(5), // MM-DD
    screenHours: Math.round((t.screenTimeSeconds / 3600) * 10) / 10,
    productiveHours: Math.round((t.productiveTimeSeconds / 3600) * 10) / 10,
    score: t.productivityScore,
  }));

  const pieColors = categoryBreakdown.map(c => c.color || '#607D8B');

  return (
    <div className="space-y-6">
      {/* Active Tracking Bar */}
      <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-wrap items-center justify-between gap-3 shadow-xs">
        <div className="flex items-center gap-3">
          <div className="w-3 h-3 rounded-full bg-emerald-500 animate-pulse" />
          <div>
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              Current Foreground Application:
            </span>
            <span className="ml-2 text-sm font-bold text-slate-900 dark:text-white">
              {activeApplicationName}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
            Status: {currentTrackingStatus}
          </span>
          <button
            onClick={fetchStats}
            title="Refresh metrics"
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Top 4 Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Screen Time Today"
          value={formatHours(totalScreenTimeToday)}
          subtitle="Total recorded active time"
          icon={Clock}
          color="blue"
        />
        <MetricCard
          title="Productive Time"
          value={formatHours(productiveTimeToday)}
          subtitle="Coding / Development hours"
          icon={CheckCircle2}
          color="emerald"
          badge="Weight 1.0"
          badgeColor="emerald"
        />
        <MetricCard
          title="Idle Time"
          value={formatHours(idleTimeToday)}
          subtitle="Away from keyboard time"
          icon={Coffee}
          color="amber"
        />
        <MetricCard
          title="Productivity Score"
          value={`${productivityPercentage}%`}
          subtitle="Productive / Total screen time"
          icon={TrendingUp}
          color="purple"
          badge={productivityPercentage >= 60 ? 'Healthy' : 'Needs Focus'}
          badgeColor={productivityPercentage >= 60 ? 'emerald' : 'amber'}
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Hourly Activity Bar Chart */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Hourly Activity Today
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Active minutes recorded by hour of day
              </p>
            </div>
            <BarChart2 className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-64">
            {hourlyChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={hourlyChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <XAxis dataKey="hour" stroke={axisStroke} fontSize={11} tickLine={false} />
                  <YAxis stroke={axisStroke} fontSize={11} tickLine={false} />
                  <Tooltip 
                    contentStyle={tooltipStyle}
                    itemStyle={tooltipItemStyle}
                    formatter={(val) => [`${val} mins`, 'Active Time']}
                  />
                  <Bar dataKey="minutes" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No hourly activity data recorded today yet.
              </div>
            )}
          </div>
        </div>

        {/* Category Breakdown Donut */}
        <div className="bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Category Breakdown
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Distribution across app categories
              </p>
            </div>
            <PieIcon className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-52 flex-1 relative">
            {categoryBreakdown.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryBreakdown}
                    dataKey="totalSeconds"
                    nameKey="categoryName"
                    innerRadius={50}
                    outerRadius={75}
                    paddingAngle={3}
                  >
                    {categoryBreakdown.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color || pieColors[index % pieColors.length]} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={tooltipStyle}
                    itemStyle={tooltipItemStyle}
                    formatter={(val, name, props) => [formatHours(val), props.payload.categoryName]}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No category data available.
              </div>
            )}
          </div>

          <div className="space-y-1.5 mt-2 border-t border-slate-100 dark:border-slate-800 pt-3">
            {categoryBreakdown.slice(0, 4).map((cat) => (
              <div key={cat.categoryName} className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: cat.color }} />
                  <span className="text-slate-700 dark:text-slate-300 font-medium truncate max-w-[120px]">
                    {cat.categoryName}
                  </span>
                </div>
                <span className="text-slate-500 dark:text-slate-400 font-semibold">
                  {cat.percentage}%
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 7-Day Trend & Top Applications */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Weekly Trend Area Chart */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                7-Day Productivity Trend
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Daily comparison of screen time vs productive hours
              </p>
            </div>
            <TrendingUp className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-64">
            {trendChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trendChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="screenGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4}/>
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="prodGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.4}/>
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="date" stroke={axisStroke} fontSize={11} tickLine={false} />
                  <YAxis stroke={axisStroke} fontSize={11} tickLine={false} />
                  <Tooltip 
                    contentStyle={tooltipStyle}
                    itemStyle={tooltipItemStyle}
                  />
                  <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                  <Area type="monotone" dataKey="screenHours" name="Screen Time (hrs)" stroke="#3b82f6" fillOpacity={1} fill="url(#screenGrad)" strokeWidth={2} />
                  <Area type="monotone" dataKey="productiveHours" name="Productive (hrs)" stroke="#10b981" fillOpacity={1} fill="url(#prodGrad)" strokeWidth={2} />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No trend data available.
              </div>
            )}
          </div>
        </div>

        {/* Top Applications Card */}
        <div className="bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Most Used Applications
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Ranked by duration today
              </p>
            </div>
            <AppWindow className="w-4 h-4 text-slate-400" />
          </div>

          <div className="flex-1 overflow-y-auto space-y-3">
            {topApplications.length > 0 ? (
              topApplications.slice(0, 5).map((app, idx) => (
                <div key={app.appName} className="flex items-center justify-between text-xs p-2 rounded-lg bg-slate-50 dark:bg-slate-800/50">
                  <div className="flex items-center gap-2.5 min-w-0">
                    <span className="w-5 text-center font-bold text-slate-400">
                      {idx + 1}
                    </span>
                    <div className="min-w-0">
                      <p className="font-semibold text-slate-800 dark:text-slate-200 truncate">
                        {app.appName}
                      </p>
                      <span 
                        className="text-[10px] font-medium px-1.5 py-0.5 rounded-sm"
                        style={{ backgroundColor: `${app.color}20`, color: app.color }}
                      >
                        {app.category}
                      </span>
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <span className="font-bold text-slate-900 dark:text-white">
                      {formatHours(app.totalSeconds)}
                    </span>
                    <p className="text-[10px] text-slate-400">
                      {app.sessionCount} sessions
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 text-slate-400">
                <p className="text-xs mb-2">No application usage recorded today yet.</p>
                <button
                  onClick={openLogModal}
                  className="inline-flex items-center gap-1.5 text-xs text-blue-600 font-semibold hover:underline"
                >
                  <PlusCircle className="w-3.5 h-3.5" />
                  Log first activity
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
