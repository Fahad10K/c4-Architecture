import { useState, useEffect } from 'react'
import { notificationsApi } from '../services/api'

interface Notification {
  id: string
  type: string
  title: string
  message: string
  isRead: boolean
  createdAt: string
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadNotifications()
  }, [])

  const loadNotifications = async () => {
    try {
      const data = await notificationsApi.getAll()
      setNotifications(data.content || data)
    } catch {
      setNotifications([])
    } finally {
      setLoading(false)
    }
  }

  const markAsRead = async (id: string) => {
    try {
      await notificationsApi.markAsRead(id)
      setNotifications(prev => prev.map(n => n.id === id ? {...n, isRead: true} : n))
    } catch { /* ignore */ }
  }

  const markAllAsRead = async () => {
    try {
      await notificationsApi.markAllAsRead()
      setNotifications(prev => prev.map(n => ({...n, isRead: true})))
    } catch { /* ignore */ }
  }

  const getIcon = (type: string) => {
    switch (type) {
      case 'ORDER_UPDATE': return '📦'
      case 'PROMOTION': return '🎉'
      case 'DELIVERY': return '🚗'
      case 'SYSTEM': return '⚙️'
      default: return '🔔'
    }
  }

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <div className="animate-pulse space-y-4">
          {[1,2,3].map(i => <div key={i} className="h-20 bg-gray-200 rounded-lg" />)}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Notifications</h1>
        {notifications.some(n => !n.isRead) && (
          <button onClick={markAllAsRead} className="text-red-600 text-sm font-medium hover:text-red-700">
            Mark all as read
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <div className="text-center py-16">
          <div className="text-6xl mb-4">🔔</div>
          <p className="text-gray-500 text-lg">No notifications yet</p>
          <p className="text-gray-400 text-sm mt-1">You'll receive updates about your orders here</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map(notification => (
            <div key={notification.id}
              onClick={() => !notification.isRead && markAsRead(notification.id)}
              className={`p-4 rounded-lg border cursor-pointer transition-colors ${
                notification.isRead ? 'bg-white border-gray-200' : 'bg-red-50 border-red-200 hover:bg-red-100'
              }`}>
              <div className="flex items-start gap-3">
                <span className="text-2xl">{getIcon(notification.type)}</span>
                <div className="flex-1">
                  <div className="flex justify-between items-start">
                    <h3 className={`font-semibold ${notification.isRead ? 'text-gray-700' : 'text-gray-900'}`}>
                      {notification.title}
                    </h3>
                    {!notification.isRead && <span className="w-2 h-2 bg-red-600 rounded-full mt-2" />}
                  </div>
                  <p className="text-sm text-gray-600 mt-1">{notification.message}</p>
                  <p className="text-xs text-gray-400 mt-2">
                    {new Date(notification.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
