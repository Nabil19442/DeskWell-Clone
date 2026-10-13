import React, { useState, useEffect } from 'react';
import { reportsApi, exportsApi } from '../api/client';
import MetricCard from '../components/MetricCard';
import { 
  FileText, 
  FileSpreadsheet, 
  ChevronLeft, 
  ChevronRight, 
  Calendar, 
  Clock, 
  CheckCircle2, 
  TrendingUp, 
  Coffee,
  Download,
  Loader2,
  AlertCircle
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell
} from 'recharts';

function formatHours(seconds) {
  if (!seconds) return '0h 00m';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  return `${h}h ${m.toString().padStart(2, '0')}m`;
}

export default function Reports() {
  const [periodType, setPeriodType] = useState('daily'); // daily, weekly, monthly
  const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().substring(0, 10));
  const [weekOffset, setWeekOffset] = useState(0);
  const [monthOffset, setMonthOffset] = useState(0);

  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchReport = async () => {
    setLoading(true);
    setError('');
    try {
      let data;
      if (periodType === 'daily') {
        data = await reportsApi.getDaily(selectedDate);
      } else if (periodType === 'weekly') {
        data = await reportsApi.getWeekly(weekOffset);
      } else {
        data = await reportsApi.getMonthly(monthOffset);
      }
      setReport(data);
    } catch (err) {
      setError('Unable to load report from backend.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [periodType, selectedDate, weekOffset, monthOffset]);

  const handleExportCsv = () => {
    if (periodType === 'daily') {
      exportsApi.downloadDailyCsv(selectedDate);
    } else {
      exportsApi.downloadWeeklyCsv(weekOffset);
    }
  };

  const handleExportPdf = () => {
    if (periodType === 'daily') {
      exportsApi.downloadDailyPdf(selectedDate);
    } else {
      exportsApi.downloadWeeklyPdf(weekOffset);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Filter and Actions Toolbar */}
      <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex flex-wrap items-center justify-between gap-4 shadow-xs">
        {/* Period Selector Tabs */}
        <div className="flex items-center p-1 bg-slate-100 dark:bg-slate-800 rounded-lg">
          {['daily', 'weekly', 'monthly'].map((type) => (
            <button
              key={type}
              onClick={() => setPeriodType(type)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-md capitalize transition-all ${
                periodType === type
                  ? 'bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-xs'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              {type}
            </button>
          ))}
        </div>

        {/* Date / Offset Navigation */}
        <div className="flex items-center gap-2">
          {periodType === 'daily' && (
            <div className="flex items-center gap-2">
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="px-3 py-1.5 text-xs rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 font-medium focus:outline-hidden focus:ring-2 focus:ring-blue-500"
              />
            </div>
          )}

          {periodType === 'weekly' && (
            <div className="flex items-center gap-1.5 text-xs font-semibold">
              <button
                onClick={() => setWeekOffset((prev) => prev + 1)}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800"
                title="Previous Week"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="px-2 py-1 text-slate-700 dark:text-slate-300">
                {weekOffset === 0 ? 'This Week' : `${weekOffset} week${weekOffset > 1 ? 's' : ''} ago`}
              </span>
              <button
                onClick={() => setWeekOffset((prev) => Math.max(0, prev - 1))}
                disabled={weekOffset === 0}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40"
                title="Next Week"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}

          {periodType === 'monthly' && (
            <div className="flex items-center gap-1.5 text-xs font-semibold">
              <button
                onClick={() => setMonthOffset((prev) => prev + 1)}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800"
                title="Previous Month"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="px-2 py-1 text-slate-700 dark:text-slate-300">
                {monthOffset === 0 ? 'This Month' : `${monthOffset} month${monthOffset > 1 ? 's' : ''} ago`}
              </span>
              <button
                onClick={() => setMonthOffset((prev) => Math.max(0, prev - 1))}
                disabled={monthOffset === 0}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 disabled:opacity-40"
                title="Next Month"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>

        {/* Real Export Actions */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors shadow-2xs"
          >
            <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-600" />
            <span>Export CSV</span>
          </button>
          <button
            onClick={handleExportPdf}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg bg-blue-600 hover:bg-blue-700 text-white transition-colors shadow-xs"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Export PDF</span>
          </button>
        </div>
      </div>

      {loading && !report ? (
        <div className="py-20 flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
          <p className="text-xs text-slate-500">Generating report analytics...</p>
        </div>
      ) : error ? (
        <div className="p-6 rounded-xl bg-rose-50 text-rose-700 dark:bg-rose-950/30 dark:text-rose-300 border border-rose-200 dark:border-rose-900 flex items-center gap-3">
          <AlertCircle className="w-6 h-6 shrink-0" />
          <p className="text-sm font-medium">{error}</p>
        </div>
      ) : report ? (
        <>
          {/* Header Banner */}
          <div className="p-5 rounded-xl bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/40 flex items-center justify-between">
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                {report.title}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                Period: {report.periodLabel}
              </p>
            </div>
            <div className="text-right">
              <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/60 dark:text-blue-300">
                {report.sessionCount} Sessions Recorded
              </span>
            </div>
          </div>

          {/* Metric Summary Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <MetricCard
              title="Total Screen Time"
              value={formatHours(report.totalScreenTimeSeconds)}
              subtitle={periodType !== 'daily' ? `Avg ${formatHours(report.averageDailyScreenTimeSeconds)}/day` : 'Recorded usage'}
              icon={Clock}
              color="blue"
            />
            <MetricCard
              title="Productive Time"
              value={formatHours(report.productiveTimeSeconds)}
              subtitle="Weighted development time"
              icon={CheckCircle2}
              color="emerald"
            />
            <MetricCard
              title="Idle Time"
              value={formatHours(report.idleTimeSeconds)}
              subtitle="Away from keyboard time"
              icon={Coffee}
              color="amber"
            />
            <MetricCard
              title="Productivity Score"
              value={`${report.productivityScore}%`}
              subtitle={periodType !== 'daily' ? 'Period average score' : 'Productive / Total'}
              icon={TrendingUp}
              color="purple"
              badge={report.productivityScore >= 60 ? 'Optimal' : 'Low'}
              badgeColor={report.productivityScore >= 60 ? 'emerald' : 'amber'}
            />
          </div>

          {/* Detailed Applications Table & Category Distribution */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Top Applications Table */}
            <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white mb-1">
                Application Usage Breakdown
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mb-4">
                Detailed metrics for all recorded processes during this period
              </p>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="border-b border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 uppercase font-semibold">
                    <tr>
                      <th className="pb-3 pl-2">Application</th>
                      <th className="pb-3">Category</th>
                      <th className="pb-3 text-center">Sessions</th>
                      <th className="pb-3 text-right pr-2">Duration</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-slate-800 dark:text-slate-200">
                    {report.topApplications && report.topApplications.length > 0 ? (
                      report.topApplications.map((app) => (
                        <tr key={app.appName} className="hover:bg-slate-50 dark:hover:bg-slate-800/40">
                          <td className="py-3 pl-2 font-semibold">
                            {app.appName}
                          </td>
                          <td className="py-3">
                            <span
                              className="px-2 py-0.5 rounded-sm font-medium text-[11px]"
                              style={{ backgroundColor: `${app.color}20`, color: app.color }}
                            >
                              {app.category}
                            </span>
                          </td>
                          <td className="py-3 text-center text-slate-500">
                            {app.sessionCount}
                          </td>
                          <td className="py-3 text-right pr-2 font-bold text-slate-900 dark:text-white">
                            {formatHours(app.totalSeconds)}
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4" className="py-8 text-center text-slate-400">
                          No application usage records found for this period.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Category Breakdown Table */}
            <div className="bg-white dark:bg-slate-900 p-5 rounded-xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white mb-1">
                Category Distribution
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mb-4">
                Total hours and proportion by category
              </p>

              <div className="flex-1 space-y-3">
                {report.categoryBreakdown && report.categoryBreakdown.length > 0 ? (
                  report.categoryBreakdown.map((cat) => (
                    <div key={cat.categoryName} className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40 space-y-1.5">
                      <div className="flex items-center justify-between text-xs">
                        <div className="flex items-center gap-2">
                          <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: cat.color }} />
                          <span className="font-semibold text-slate-800 dark:text-slate-200">
                            {cat.categoryName}
                          </span>
                        </div>
                        <span className="font-bold text-slate-900 dark:text-white">
                          {formatHours(cat.totalSeconds)}
                        </span>
                      </div>
                      <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                        <div
                          className="h-full rounded-full"
                          style={{ width: `${cat.percentage}%`, backgroundColor: cat.color }}
                        />
                      </div>
                      <div className="text-right text-[10px] text-slate-400 font-medium">
                        {cat.percentage}% of tracked time
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 text-center py-8">
                    No categories recorded.
                  </p>
                )}
              </div>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
}
