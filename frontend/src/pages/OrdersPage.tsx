import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Package, Clock, CheckCircle, XCircle } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { fetchOrders } from '../store/slices/orderSlice'

const statusColors: Record<string, string> = {
  PLACED: 'bg-blue-100 text-blue-700',
  CONFIRMED: 'bg-indigo-100 text-indigo-700',
  PREPARING: 'bg-yellow-100 text-yellow-700',
  READY: 'bg-purple-100 text-purple-700',
  OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
}

export default function OrdersPage() {
  const dispatch = useAppDispatch()
  const { orders, loading } = useAppSelector((state) => state.orders)

  useEffect(() => {
    dispatch(fetchOrders())
  }, [dispatch])

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <div className="animate-pulse space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-gray-200 rounded-xl" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>

      {orders.length === 0 ? (
        <div className="text-center py-16">
          <Package className="h-16 w-16 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-700 mb-2">No orders yet</h2>
          <p className="text-gray-500 mb-6">Place your first order to see it here!</p>
          <Link to="/stores" className="btn-primary">Browse Stores</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}/track`} className="card p-5 block hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-3">
                    <h3 className="font-bold text-gray-900">#{order.orderNumber}</h3>
                    <span className={`text-xs font-medium px-2.5 py-0.5 rounded-full ${statusColors[order.status] || 'bg-gray-100 text-gray-700'}`}>
                      {order.status.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">{order.storeName}</p>
                  <p className="text-xs text-gray-400 mt-1">
                    {new Date(order.createdAt).toLocaleDateString()} at {new Date(order.createdAt).toLocaleTimeString()}
                  </p>
                </div>
                <div className="text-right">
                  <span className="font-bold text-lg text-gray-900">${order.total.toFixed(2)}</span>
                  <p className="text-xs text-gray-500">{order.items?.length || 0} items</p>
                </div>
              </div>
              {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                <div className="mt-3 pt-3 border-t border-gray-100 flex items-center gap-2 text-sm text-primary-600">
                  <Clock className="h-4 w-4" />
                  Track Order
                </div>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
