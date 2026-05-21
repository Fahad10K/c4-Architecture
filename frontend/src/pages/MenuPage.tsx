import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Plus, Search, Star, Clock, Flame } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { fetchMenuByStore } from '../store/slices/menuSlice'
import { fetchStoreById } from '../store/slices/storeSlice'
import { addToCart } from '../store/slices/cartSlice'
import toast from 'react-hot-toast'

export default function MenuPage() {
  const { storeId } = useParams<{ storeId: string }>()
  const dispatch = useAppDispatch()
  const { items, loading } = useAppSelector((state) => state.menu)
  const { selectedStore } = useAppSelector((state) => state.stores)
  const { isAuthenticated } = useAppSelector((state) => state.auth)
  const [search, setSearch] = useState('')

  useEffect(() => {
    if (storeId) {
      dispatch(fetchMenuByStore(storeId))
      dispatch(fetchStoreById(storeId))
    }
  }, [storeId, dispatch])

  const filteredItems = items.filter((item) =>
    item.name.toLowerCase().includes(search.toLowerCase()) ||
    item.description.toLowerCase().includes(search.toLowerCase())
  )

  const handleAddToCart = (menuItemId: string) => {
    if (!isAuthenticated) {
      toast.error('Please sign in to add items to cart')
      return
    }
    dispatch(addToCart({ menuItemId, quantity: 1, storeId: storeId! }))
    toast.success('Added to cart!')
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Store Header */}
      {selectedStore && (
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">{selectedStore.name}</h1>
          <p className="text-gray-600 mt-1">{selectedStore.description}</p>
          <div className="flex items-center gap-4 mt-3 text-sm text-gray-500">
            <span className="flex items-center gap-1">
              <Star className="h-4 w-4 text-accent-500 fill-accent-500" />
              {selectedStore.rating}
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-4 w-4" />
              {selectedStore.estimatedDeliveryTime} min delivery
            </span>
            <span>Min order: ${selectedStore.minOrderAmount}</span>
          </div>
        </div>
      )}

      {/* Search */}
      <div className="relative mb-6">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search menu items..."
          className="input-field pl-10"
        />
      </div>

      {/* Menu Grid */}
      {loading ? (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="card animate-pulse p-4">
              <div className="h-40 bg-gray-200 rounded-lg mb-3" />
              <div className="h-5 bg-gray-200 rounded w-3/4 mb-2" />
              <div className="h-4 bg-gray-200 rounded w-full" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredItems.map((item) => (
            <div key={item.id} className="card group hover:shadow-md transition-shadow">
              {item.imageUrl && (
                <div className="h-44 overflow-hidden">
                  <img src={item.imageUrl} alt={item.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                </div>
              )}
              <div className="p-4">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-bold text-gray-900">{item.name}</h3>
                  {item.isPopular && (
                    <span className="flex items-center gap-1 text-xs bg-accent-100 text-accent-700 px-2 py-0.5 rounded-full font-medium">
                      <Flame className="h-3 w-3" /> Popular
                    </span>
                  )}
                </div>
                <p className="text-sm text-gray-600 mt-1 line-clamp-2">{item.description}</p>
                {item.calories && (
                  <p className="text-xs text-gray-400 mt-1">{item.calories} cal</p>
                )}
                <div className="flex items-center justify-between mt-4">
                  <span className="text-lg font-bold text-primary-600">${item.price.toFixed(2)}</span>
                  <button
                    onClick={() => handleAddToCart(item.id)}
                    disabled={!item.isAvailable}
                    className="flex items-center gap-1 bg-primary-600 hover:bg-primary-700 text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Plus className="h-4 w-4" /> Add
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && filteredItems.length === 0 && (
        <div className="text-center py-16">
          <p className="text-gray-500 text-lg">No menu items found.</p>
        </div>
      )}
    </div>
  )
}
