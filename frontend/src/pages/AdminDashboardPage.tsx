import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminApi } from '../services/api'

export default function AdminDashboardPage() {
  const navigate = useNavigate()
  const [dashboard, setDashboard] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      const data = await adminApi.getDashboard()
      setDashboard(data)
    } catch {
      setDashboard(null)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto p-6">
        <div className="animate-pulse grid grid-cols-1 md:grid-cols-4 gap-4">
          {[1,2,3,4].map(i => <div key={i} className="h-28 bg-gray-200 rounded-lg" />)}
        </div>
      </div>
    )
  }

  const stats = [
    { label: 'Total Orders', value: dashboard?.totalOrders || 0, icon: '📦', color: 'bg-blue-50 text-blue-700' },
    { label: 'Total Users', value: dashboard?.totalUsers || 0, icon: '👥', color: 'bg-green-50 text-green-700' },
    { label: 'Total Stores', value: dashboard?.totalStores || 0, icon: '🏪', color: 'bg-purple-50 text-purple-700' },
    { label: 'Today Orders', value: dashboard?.todayOrders || 0, icon: '📋', color: 'bg-orange-50 text-orange-700' },
    { label: 'Revenue', value: `$${(dashboard?.revenue || 0).toFixed(2)}`, icon: '💰', color: 'bg-emerald-50 text-emerald-700' },
    { label: 'Active Orders', value: dashboard?.activeOrders || 0, icon: '🔥', color: 'bg-red-50 text-red-700' },
    { label: 'Delivered', value: dashboard?.deliveredOrders || 0, icon: '✅', color: 'bg-teal-50 text-teal-700' },
    { label: 'Cancelled', value: dashboard?.cancelledOrders || 0, icon: '❌', color: 'bg-gray-50 text-gray-700' },
  ]

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Admin Dashboard</h1>
        <div className="flex gap-3">
          <button onClick={() => navigate('/admin/users')} className="bg-white border px-4 py-2 rounded-lg text-sm hover:bg-gray-50">
            Manage Users
          </button>
          <button onClick={() => navigate('/admin/analytics')} className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700">
            Analytics
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map(stat => (
          <div key={stat.label} className={`${stat.color} rounded-lg p-4`}>
            <div className="flex items-center justify-between">
              <span className="text-2xl">{stat.icon}</span>
            </div>
            <p className="text-2xl font-bold mt-2">{stat.value}</p>
            <p className="text-sm opacity-80">{stat.label}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Quick Actions</h2>
          <div className="space-y-3">
            <button onClick={() => navigate('/admin/users')} className="w-full text-left p-3 rounded-lg border hover:bg-gray-50 flex items-center gap-3">
              <span>👥</span><span>User & Role Management</span>
            </button>
            <button onClick={() => navigate('/admin/analytics')} className="w-full text-left p-3 rounded-lg border hover:bg-gray-50 flex items-center gap-3">
              <span>📊</span><span>Reports & Analytics</span>
            </button>
            <button onClick={() => navigate('/orders')} className="w-full text-left p-3 rounded-lg border hover:bg-gray-50 flex items-center gap-3">
              <span>📦</span><span>Order Management</span>
            </button>
            <button onClick={() => navigate('/stores')} className="w-full text-left p-3 rounded-lg border hover:bg-gray-50 flex items-center gap-3">
              <span>🏪</span><span>Store Management</span>
            </button>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">System Status</h2>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <span className="text-sm font-medium">API Gateway</span>
              <span className="text-xs bg-green-200 text-green-800 px-2 py-1 rounded-full">Healthy</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <span className="text-sm font-medium">Database</span>
              <span className="text-xs bg-green-200 text-green-800 px-2 py-1 rounded-full">Connected</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <span className="text-sm font-medium">Payment Gateway</span>
              <span className="text-xs bg-green-200 text-green-800 px-2 py-1 rounded-full">Active</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <span className="text-sm font-medium">Notification Service</span>
              <span className="text-xs bg-green-200 text-green-800 px-2 py-1 rounded-full">Running</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
