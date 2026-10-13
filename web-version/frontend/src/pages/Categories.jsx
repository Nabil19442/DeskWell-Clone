import React, { useState, useEffect } from 'react';
import { categoriesApi } from '../api/client';
import { 
  FolderKanban, 
  Edit3, 
  Sparkles, 
  ArrowRight, 
  Loader2, 
  Check, 
  Tag,
  AppWindow,
  SlidersHorizontal
} from 'lucide-react';
import Modal from '../components/Modal';

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Category edit modal
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedCat, setSelectedCat] = useState(null);
  const [catName, setCatName] = useState('');
  const [catColor, setCatColor] = useState('#2196F3');
  const [catWeight, setCatWeight] = useState(0.0);
  const [savingCat, setSavingCat] = useState(false);

  // App assign modal
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [selectedApp, setSelectedApp] = useState(null);
  const [targetCatId, setTargetCatId] = useState('');
  const [assigning, setAssigning] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [catList, appList] = await Promise.all([
        categoriesApi.getAll(),
        categoriesApi.getApps(),
      ]);
      setCategories(catList || []);
      setApps(appList || []);
    } catch (err) {
      console.error('Failed to load category data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleOpenEdit = (cat) => {
    setSelectedCat(cat);
    setCatName(cat.name);
    setCatColor(cat.color || '#607D8B');
    setCatWeight(cat.productivityWeight != null ? cat.productivityWeight : 0.0);
    setEditModalOpen(true);
  };

  const handleSaveCat = async (e) => {
    e.preventDefault();
    setSavingCat(true);
    try {
      await categoriesApi.update(selectedCat.id, {
        name: catName,
        color: catColor,
        productivityWeight: parseFloat(catWeight) || 0.0,
      });
      setEditModalOpen(false);
      fetchData();
    } catch (err) {
      alert('Failed to update category');
    } finally {
      setSavingCat(false);
    }
  };

  const handleOpenAssign = (app) => {
    setSelectedApp(app);
    setTargetCatId(app.category?.id || (categories[0]?.id || ''));
    setAssignModalOpen(true);
  };

  const handleSaveAssign = async (e) => {
    e.preventDefault();
    setAssigning(true);
    try {
      await categoriesApi.assignApp(selectedApp.id, targetCatId);
      setAssignModalOpen(false);
      fetchData();
    } catch (err) {
      alert('Failed to assign category to app');
    } finally {
      setAssigning(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Category Classification Notice */}
      <div className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex items-start gap-3">
        <Sparkles className="w-5 h-5 text-blue-600 dark:text-blue-400 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-600 dark:text-slate-400 space-y-1">
          <p className="font-semibold text-slate-900 dark:text-white">
            Keyword Auto-Categorization & Productivity Weighting
          </p>
          <p>
            DeskWell automatically parses application executable names and window titles against built-in keyword dictionaries. Time spent in categories with a <strong>Productivity Weight &gt; 0</strong> (such as Development at 1.0) directly counts toward your productivity score.
          </p>
        </div>
      </div>

      {/* Categories Grid */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            Available Categories ({categories.length})
          </h3>
        </div>

        {loading ? (
          <div className="py-12 flex justify-center text-xs text-slate-400">
            <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {categories.map((cat) => (
              <div
                key={cat.id}
                className="p-4 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between space-y-3"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-2.5">
                    <span 
                      className="w-4 h-4 rounded-full shadow-xs shrink-0" 
                      style={{ backgroundColor: cat.color }} 
                    />
                    <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                      {cat.name}
                    </h4>
                  </div>
                  <button
                    onClick={() => handleOpenEdit(cat)}
                    className="p-1 rounded-md text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                    title="Edit Category Details"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                  </button>
                </div>

                <div className="space-y-1 text-xs">
                  <div className="flex items-center justify-between text-slate-500">
                    <span>Productivity Weight:</span>
                    <span className="font-bold text-slate-900 dark:text-white">
                      {cat.productivityWeight} ({cat.productivityWeight > 0 ? 'Productive' : 'Neutral'})
                    </span>
                  </div>
                  <div className="flex items-center justify-between text-slate-500">
                    <span>Default Category:</span>
                    <span>{cat.isDefault ? 'System Default' : 'Custom'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Applications Directory & Category Assignments */}
      <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-5 shadow-xs">
        <div className="mb-4">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            Application Assignments ({apps.length})
          </h3>
          <p className="text-xs text-slate-500">
            Override auto-categorization by reassigning any application to a custom category
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 uppercase font-semibold">
              <tr>
                <th className="pb-3 pl-2">Executable / App</th>
                <th className="pb-3">Assigned Category</th>
                <th className="pb-3">Productivity Weight</th>
                <th className="pb-3 text-right pr-2">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60 text-slate-800 dark:text-slate-200">
              {apps.length > 0 ? (
                apps.map((app) => (
                  <tr key={app.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40">
                    <td className="py-3 pl-2 font-semibold flex items-center gap-2">
                      <AppWindow className="w-3.5 h-3.5 text-slate-400" />
                      <span>{app.name}</span>
                    </td>
                    <td className="py-3">
                      <span
                        className="px-2 py-0.5 rounded-sm font-medium text-[11px]"
                        style={{
                          backgroundColor: `${app.category?.color || '#607D8B'}20`,
                          color: app.category?.color || '#607D8B',
                        }}
                      >
                        {app.category?.name || 'Other'}
                      </span>
                    </td>
                    <td className="py-3 text-slate-500 font-medium">
                      {app.category?.productivityWeight ?? 0.0}
                    </td>
                    <td className="py-3 text-right pr-2">
                      <button
                        onClick={() => handleOpenAssign(app)}
                        className="px-2.5 py-1 text-[11px] font-semibold bg-slate-100 dark:bg-slate-800 hover:bg-blue-50 dark:hover:bg-blue-900/30 text-slate-700 dark:text-slate-300 hover:text-blue-600 rounded-md transition-colors"
                      >
                        Reassign
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" className="py-6 text-center text-slate-400">
                    No applications tracked yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit Category Modal */}
      <Modal
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        title={`Edit Category: ${selectedCat?.name}`}
      >
        <form onSubmit={handleSaveCat} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Category Name
            </label>
            <input
              type="text"
              value={catName}
              onChange={(e) => setCatName(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Color (Hex)
            </label>
            <div className="flex items-center gap-3">
              <input
                type="color"
                value={catColor}
                onChange={(e) => setCatColor(e.target.value)}
                className="w-10 h-10 rounded-lg cursor-pointer border-0 bg-transparent"
              />
              <input
                type="text"
                value={catColor}
                onChange={(e) => setCatColor(e.target.value)}
                className="flex-1 px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Productivity Weight (0.0 = Neutral/Distraction, 1.0 = Productive)
            </label>
            <input
              type="number"
              step="0.1"
              min="0"
              max="1"
              value={catWeight}
              onChange={(e) => setCatWeight(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              In DeskWell's scoring system, only categories with weight 1.0 count as Productive Time on the dashboard.
            </p>
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setEditModalOpen(false)}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={savingCat}
              className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Save Changes
            </button>
          </div>
        </form>
      </Modal>

      {/* Assign App Modal */}
      <Modal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        title={`Assign Category: ${selectedApp?.name}`}
      >
        <form onSubmit={handleSaveAssign} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Select Category
            </label>
            <select
              value={targetCatId}
              onChange={(e) => setTargetCatId(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            >
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} (Weight: {c.productivityWeight})
                </option>
              ))}
            </select>
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setAssignModalOpen(false)}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={assigning}
              className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-lg"
            >
              Apply Category
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
