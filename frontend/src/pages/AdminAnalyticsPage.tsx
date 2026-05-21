import { useState, useEffect } from 'react'
import { adminApi } from '../services/api'

export default function AdminAnalyticsPage() {
  const [reports, setReports] = useState<any>(null)
  const [period, setPeriod] = useState('weekly')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadReports()
  }, [period])

  const loadReports = async () => {
    setLoading(true)
    try {
      const data = await adminApi.getReports(period)
      setReports(data)
    } catch {
      setReports(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Analytics & Reports</h1>
        <div className="flex gap-2">
          {['daily', 'weekly', 'monthly', 'yearly'].map(p => (
            <button key={p} onClick={() => setPeriod(p)}
              className={`px-4 py-2 rounded-lg text-sm capitalize ${
                period === p ? 'bg-red-600 text-white' : 'bg-white border hover:bg-gray-50'
              }`}>
              {p}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="animate-pulse space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[1,2,3].map(i => <div key={i} className="h-28 bg-gray-200 rounded-lg" />)}
          </div>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">Total Orders</p>
              <p className="text-3xl font-bold text-gray-900">{reports?.totalOrders || 0}</p>
              <p className="text-xs text-gray-400 mt-1">Period: {period}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">Revenue</p>
              <p className="text-3xl font-bold text-green-600">${(reports?.revenue || 0).toFixed(2)}</p>
              <p className="text-xs text-gray-400 mt-1">Period: {period}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-6">
              <p className="text-sm text-gray-500">Avg Order Value</p>
              <p className="text-3xl font-bold text-blue-600">${(reports?.avgOrderValue || 0).toFixed(2)}</p>
              <p className="text-xs text-gray-400 mt-1">Period: {period}</p>
            </div>
          </div>

          {reports?.topStores && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-semibold mb-4">Top Stores</h2>
              <div className="space-y-3">
                {reports.topStores.map((store: any, i: number) => (
                  <div key={store.id || i} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-gray-400 w-6">#{i + 1}</span>
                      <span className="font-medium">{store.name}</span>
                    </div>
                    <span className="text-sm text-yellow-600">⭐ {store.rating || 'N/A'}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="mt-6 bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold mb-2">Report Details</h2>
            <p className="text-sm text-gray-500">Generated: {reports?.generatedAt || 'N/A'}</p>
            <p className="text-sm text-gray-500">New Users: {reports?.newUsers || 0}</p>
          </div>
        </>
      )}
    </div>
  )
}
