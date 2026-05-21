import { useState } from 'react'
import { useSelector, useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { RootState } from '../store'
import { clearCart } from '../store/slices/cartSlice'
import { ordersApi } from '../services/api'
import toast from 'react-hot-toast'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const { items, total } = useSelector((state: RootState) => state.cart)
  const [loading, setLoading] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('card')
  const [address, setAddress] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: '',
    instructions: ''
  })
  const [cardDetails, setCardDetails] = useState({
    number: '',
    expiry: '',
    cvc: '',
    name: ''
  })

  const deliveryFee = total > 20 ? 0 : 4.99
  const tax = Math.round(total * 0.08 * 100) / 100
  const grandTotal = Math.round((total + deliveryFee + tax) * 100) / 100

  const handlePlaceOrder = async () => {
    if (!address.street || !address.city) {
      toast.error('Please fill in your delivery address')
      return
    }
    if (paymentMethod === 'card' && (!cardDetails.number || !cardDetails.expiry)) {
      toast.error('Please fill in card details')
      return
    }

    setLoading(true)
    try {
      await ordersApi.create({
        addressId: null,
        specialNotes: address.instructions,
        paymentMethod
      })
      dispatch(clearCart())
      toast.success('Order placed successfully!')
      navigate('/orders')
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to place order')
    } finally {
      setLoading(false)
    }
  }

  if (items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto p-6 text-center">
        <h1 className="text-2xl font-bold mb-4">Checkout</h1>
        <p className="text-gray-500">Your cart is empty. Add items before checkout.</p>
        <button onClick={() => navigate('/stores')} className="mt-4 bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700">
          Browse Stores
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">Checkout</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {/* Delivery Address */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold mb-4">Delivery Address</h2>
            <div className="space-y-3">
              <input type="text" placeholder="Street Address" value={address.street}
                onChange={e => setAddress({...address, street: e.target.value})}
                className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
              <div className="grid grid-cols-2 gap-3">
                <input type="text" placeholder="City" value={address.city}
                  onChange={e => setAddress({...address, city: e.target.value})}
                  className="border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
                <input type="text" placeholder="State" value={address.state}
                  onChange={e => setAddress({...address, state: e.target.value})}
                  className="border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
              </div>
              <input type="text" placeholder="ZIP Code" value={address.zipCode}
                onChange={e => setAddress({...address, zipCode: e.target.value})}
                className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
              <textarea placeholder="Delivery instructions (optional)" value={address.instructions}
                onChange={e => setAddress({...address, instructions: e.target.value})}
                className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" rows={2} />
            </div>
          </div>

          {/* Payment Method */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-lg font-semibold mb-4">Payment Method</h2>
            <div className="space-y-3">
              <label className="flex items-center gap-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
                <input type="radio" name="payment" value="card" checked={paymentMethod === 'card'}
                  onChange={e => setPaymentMethod(e.target.value)} className="text-red-600" />
                <span className="font-medium">Credit / Debit Card</span>
              </label>
              <label className="flex items-center gap-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
                <input type="radio" name="payment" value="cash" checked={paymentMethod === 'cash'}
                  onChange={e => setPaymentMethod(e.target.value)} className="text-red-600" />
                <span className="font-medium">Cash on Delivery</span>
              </label>

              {paymentMethod === 'card' && (
                <div className="mt-4 space-y-3 pl-6">
                  <input type="text" placeholder="Card Number" value={cardDetails.number}
                    onChange={e => setCardDetails({...cardDetails, number: e.target.value})}
                    className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
                  <div className="grid grid-cols-2 gap-3">
                    <input type="text" placeholder="MM/YY" value={cardDetails.expiry}
                      onChange={e => setCardDetails({...cardDetails, expiry: e.target.value})}
                      className="border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
                    <input type="text" placeholder="CVC" value={cardDetails.cvc}
                      onChange={e => setCardDetails({...cardDetails, cvc: e.target.value})}
                      className="border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
                  </div>
                  <input type="text" placeholder="Cardholder Name" value={cardDetails.name}
                    onChange={e => setCardDetails({...cardDetails, name: e.target.value})}
                    className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent" />
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-white rounded-lg shadow p-6 h-fit sticky top-24">
          <h2 className="text-lg font-semibold mb-4">Order Summary</h2>
          <div className="space-y-2 text-sm">
            {items.map(item => (
              <div key={item.id} className="flex justify-between">
                <span>{item.quantity}x {item.name}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            <hr className="my-3" />
            <div className="flex justify-between"><span>Subtotal</span><span>${total.toFixed(2)}</span></div>
            <div className="flex justify-between"><span>Delivery Fee</span><span>{deliveryFee === 0 ? 'FREE' : `$${deliveryFee.toFixed(2)}`}</span></div>
            <div className="flex justify-between"><span>Tax</span><span>${tax.toFixed(2)}</span></div>
            <hr className="my-3" />
            <div className="flex justify-between font-bold text-lg"><span>Total</span><span>${grandTotal.toFixed(2)}</span></div>
          </div>
          <button onClick={handlePlaceOrder} disabled={loading}
            className="w-full mt-6 bg-red-600 text-white py-3 rounded-lg font-semibold hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
            {loading ? 'Processing...' : `Place Order • $${grandTotal.toFixed(2)}`}
          </button>
          <p className="text-xs text-gray-400 text-center mt-3">Secure payment powered by Stripe</p>
        </div>
      </div>
    </div>
  )
}
