import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Minus, Plus, Trash2, Tag } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { fetchCart, updateCartItem, removeCartItem, applyCoupon } from '../store/slices/cartSlice'
import { createOrder } from '../store/slices/orderSlice'
import toast from 'react-hot-toast'

export default function CartPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const { cart, loading } = useAppSelector((state) => state.cart)
  const [couponCode, setCouponCode] = useState('')
  const [ordering, setOrdering] = useState(false)

  useEffect(() => {
    dispatch(fetchCart())
  }, [dispatch])

  const handleQuantityChange = (itemId: string, newQty: number) => {
    if (newQty <= 0) {
      dispatch(removeCartItem(itemId))
    } else {
      dispatch(updateCartItem({ itemId, quantity: newQty }))
    }
  }

  const handleApplyCoupon = () => {
    if (couponCode.trim()) {
      dispatch(applyCoupon(couponCode.trim())).then((result) => {
        if (applyCoupon.fulfilled.match(result)) {
          toast.success('Coupon applied!')
        } else {
          toast.error('Invalid coupon code')
        }
      })
    }
  }

  const handlePlaceOrder = async () => {
    setOrdering(true)
    const result = await dispatch(createOrder({ addressId: 'a-001' }))
    setOrdering(false)
    if (createOrder.fulfilled.match(result)) {
      toast.success('Order placed successfully!')
      navigate(`/orders/${result.payload.id}/track`)
    } else {
      toast.error('Failed to place order')
    }
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/4" />
          <div className="h-24 bg-gray-200 rounded" />
          <div className="h-24 bg-gray-200 rounded" />
        </div>
      </div>
    )
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Your Cart is Empty</h1>
        <p className="text-gray-600 mb-6">Browse our stores and add some delicious items!</p>
        <button onClick={() => navigate('/stores')} className="btn-primary">Browse Stores</button>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Your Cart</h1>
      <p className="text-sm text-gray-500 mb-4">From: {cart.storeName}</p>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <div key={item.id} className="card p-4 flex items-center gap-4">
              {item.menuItemImage && (
                <img src={item.menuItemImage} alt={item.menuItemName} className="w-16 h-16 rounded-lg object-cover" />
              )}
              <div className="flex-1">
                <h3 className="font-semibold text-gray-900">{item.menuItemName}</h3>
                <p className="text-sm text-gray-500">${item.unitPrice.toFixed(2)} each</p>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                  className="p-1 rounded-md border border-gray-300 hover:bg-gray-100">
                  <Minus className="h-4 w-4" />
                </button>
                <span className="w-8 text-center font-medium">{item.quantity}</span>
                <button onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                  className="p-1 rounded-md border border-gray-300 hover:bg-gray-100">
                  <Plus className="h-4 w-4" />
                </button>
              </div>
              <span className="font-bold text-gray-900 w-16 text-right">${item.totalPrice.toFixed(2)}</span>
              <button onClick={() => dispatch(removeCartItem(item.id))} className="text-red-500 hover:text-red-700 p-1">
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          ))}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <div className="card p-5 sticky top-20">
            <h3 className="font-bold text-gray-900 mb-4">Order Summary</h3>

            {/* Coupon */}
            <div className="flex gap-2 mb-4">
              <div className="relative flex-1">
                <Tag className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value)}
                  placeholder="Coupon code"
                  className="input-field pl-9 text-sm py-2"
                />
              </div>
              <button onClick={handleApplyCoupon} className="btn-secondary text-sm py-2 px-3">Apply</button>
            </div>

            <div className="space-y-2 text-sm border-t border-gray-100 pt-4">
              <div className="flex justify-between"><span className="text-gray-600">Subtotal</span><span>${cart.subtotal.toFixed(2)}</span></div>
              <div className="flex justify-between"><span className="text-gray-600">Tax</span><span>${cart.tax.toFixed(2)}</span></div>
              <div className="flex justify-between"><span className="text-gray-600">Delivery Fee</span><span>${cart.deliveryFee.toFixed(2)}</span></div>
              {cart.discount > 0 && (
                <div className="flex justify-between text-green-600"><span>Discount</span><span>-${cart.discount.toFixed(2)}</span></div>
              )}
              <div className="flex justify-between font-bold text-lg pt-2 border-t border-gray-100">
                <span>Total</span><span className="text-primary-600">${cart.total.toFixed(2)}</span>
              </div>
            </div>

            <button
              onClick={handlePlaceOrder}
              disabled={ordering}
              className="btn-primary w-full mt-4 disabled:opacity-50"
            >
              {ordering ? 'Placing Order...' : 'Place Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
