import { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { Package, CheckCircle, Truck, ChefHat, Clock } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { fetchOrderById } from '../store/slices/orderSlice'

const steps = [
  { key: 'PLACED', label: 'Order Placed', icon: Clock },
  { key: 'CONFIRMED', label: 'Confirmed', icon: CheckCircle },
  { key: 'PREPARING', label: 'Preparing', icon: ChefHat },
  { key: 'OUT_FOR_DELIVERY', label: 'On the Way', icon: Truck },
  { key: 'DELIVERED', label: 'Delivered', icon: Package },
]

export default function OrderTrackingPage() {
  const { orderId } = useParams<{ orderId: string }>()
  const dispatch = useAppDispatch()
  const { currentOrder } = useAppSelector((state) => state.orders)

  useEffect(() => {
    if (orderId) dispatch(fetchOrderById(orderId))
  }, [orderId, dispatch])

  if (!currentOrder) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/3" />
          <div className="h-48 bg-gray-200 rounded-xl" />
        </div>
      </div>
    )
  }

  const currentStepIdx = steps.findIndex((s) => s.key === currentOrder.status)

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Order #{currentOrder.orderNumber}</h1>
        <p className="text-gray-500 mt-1">{currentOrder.storeName}</p>
      </div>

      {/* Progress Tracker */}
      {currentOrder.status !== 'CANCELLED' && (
        <div className="card p-6 mb-8">
          <div className="flex items-center justify-between">
            {steps.map((step, idx) => {
              const Icon = step.icon
              const isCompleted = idx <= currentStepIdx
              const isCurrent = idx === currentStepIdx
              return (
                <div key={step.key} className="flex flex-col items-center flex-1 relative">
                  {idx > 0 && (
                    <div className={`absolute top-5 right-1/2 w-full h-0.5 -translate-y-1/2 ${idx <= currentStepIdx ? 'bg-primary-500' : 'bg-gray-200'}`} />
                  )}
                  <div className={`relative z-10 w-10 h-10 rounded-full flex items-center justify-center ${isCompleted ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-400'} ${isCurrent ? 'ring-4 ring-primary-100' : ''}`}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <span className={`text-xs mt-2 font-medium text-center ${isCompleted ? 'text-primary-600' : 'text-gray-400'}`}>
                    {step.label}
                  </span>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {currentOrder.status === 'CANCELLED' && (
        <div className="card p-6 mb-8 bg-red-50 border-red-200">
          <p className="text-red-700 font-semibold text-center">This order has been cancelled.</p>
        </div>
      )}

      {/* Order Details */}
      <div className="card p-6">
        <h3 className="font-bold text-gray-900 mb-4">Order Details</h3>
        <div className="space-y-3">
          {currentOrder.items?.map((item) => (
            <div key={item.id} className="flex justify-between items-center">
              <div>
                <span className="font-medium text-gray-900">{item.menuItemName}</span>
                <span className="text-gray-500 ml-2">x{item.quantity}</span>
              </div>
              <span className="font-medium">${item.totalPrice.toFixed(2)}</span>
            </div>
          ))}
        </div>
        <div className="border-t border-gray-100 mt-4 pt-4 space-y-1 text-sm">
          <div className="flex justify-between"><span className="text-gray-600">Subtotal</span><span>${currentOrder.subtotal.toFixed(2)}</span></div>
          <div className="flex justify-between"><span className="text-gray-600">Tax</span><span>${currentOrder.tax.toFixed(2)}</span></div>
          <div className="flex justify-between"><span className="text-gray-600">Delivery</span><span>${currentOrder.deliveryFee.toFixed(2)}</span></div>
          {currentOrder.discount > 0 && (
            <div className="flex justify-between text-green-600"><span>Discount</span><span>-${currentOrder.discount.toFixed(2)}</span></div>
          )}
          <div className="flex justify-between font-bold text-lg pt-2 border-t border-gray-100">
            <span>Total</span><span className="text-primary-600">${currentOrder.total.toFixed(2)}</span>
          </div>
        </div>
        {currentOrder.estimatedDelivery && (
          <p className="text-sm text-gray-500 mt-4">
            Estimated delivery: {new Date(currentOrder.estimatedDelivery).toLocaleTimeString()}
          </p>
        )}
      </div>
    </div>
  )
}
