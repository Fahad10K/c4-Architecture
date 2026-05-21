import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, User, Pizza, LogOut, Package } from 'lucide-react'
import { useAppSelector, useAppDispatch } from '../store/hooks'
import { logout } from '../store/slices/authSlice'

export default function Navbar() {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth)
  const { cart } = useAppSelector((state) => state.cart)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const handleLogout = () => {
    dispatch(logout())
    navigate('/')
  }

  const cartItemCount = cart?.items?.reduce((acc, item) => acc + item.quantity, 0) || 0

  return (
    <nav className="bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="flex items-center gap-2">
            <Pizza className="h-8 w-8 text-primary-600" />
            <span className="text-xl font-bold text-gray-900">Pizza Palace</span>
          </Link>

          <div className="hidden md:flex items-center gap-6">
            <Link to="/stores" className="text-gray-600 hover:text-primary-600 font-medium transition-colors">
              Stores
            </Link>
            {isAuthenticated && (
              <Link to="/orders" className="text-gray-600 hover:text-primary-600 font-medium transition-colors flex items-center gap-1">
                <Package className="h-4 w-4" />
                Orders
              </Link>
            )}
          </div>

          <div className="flex items-center gap-4">
            {isAuthenticated && (
              <Link to="/cart" className="relative p-2 text-gray-600 hover:text-primary-600 transition-colors">
                <ShoppingCart className="h-6 w-6" />
                {cartItemCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
                    {cartItemCount}
                  </span>
                )}
              </Link>
            )}

            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                <Link to="/profile" className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors">
                  <User className="h-5 w-5" />
                  <span className="hidden sm:inline text-sm font-medium">{user?.name}</span>
                </Link>
                <button onClick={handleLogout} className="p-2 text-gray-400 hover:text-primary-600 transition-colors" title="Logout">
                  <LogOut className="h-5 w-5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login" className="btn-secondary text-sm py-2 px-4">Sign In</Link>
                <Link to="/register" className="btn-primary text-sm py-2 px-4">Sign Up</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
