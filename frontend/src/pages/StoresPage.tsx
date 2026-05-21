import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Star, Clock, MapPin } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { fetchStores } from '../store/slices/storeSlice'

export default function StoresPage() {
  const dispatch = useAppDispatch()
  const { stores, loading } = useAppSelector((state) => state.stores)

  useEffect(() => {
    dispatch(fetchStores())
  }, [dispatch])

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-48 bg-gray-200" />
              <div className="p-5 space-y-3">
                <div className="h-5 bg-gray-200 rounded w-3/4" />
                <div className="h-4 bg-gray-200 rounded w-full" />
                <div className="h-4 bg-gray-200 rounded w-1/2" />
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-12">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Our Stores</h1>
        <p className="text-gray-600 mt-1">Choose a store near you to start ordering</p>
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        {stores.map((store) => (
          <Link key={store.id} to={`/stores/${store.id}/menu`} className="card group hover:shadow-md transition-shadow">
            <div className="h-48 bg-gray-200 overflow-hidden">
              {store.imageUrl && (
                <img src={store.imageUrl} alt={store.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
              )}
            </div>
            <div className="p-5">
              <h3 className="text-lg font-bold text-gray-900 group-hover:text-primary-600 transition-colors">
                {store.name}
              </h3>
              <p className="text-sm text-gray-600 mt-1 line-clamp-2">{store.description}</p>
              <div className="flex items-center gap-4 mt-3 text-sm text-gray-500">
                <span className="flex items-center gap-1">
                  <Star className="h-4 w-4 text-accent-500 fill-accent-500" />
                  {store.rating} ({store.reviewCount})
                </span>
                <span className="flex items-center gap-1">
                  <Clock className="h-4 w-4" />
                  {store.estimatedDeliveryTime} min
                </span>
              </div>
              <div className="flex items-center gap-1 mt-2 text-sm text-gray-500">
                <MapPin className="h-4 w-4" />
                {store.city}, {store.state}
              </div>
              <div className="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
                <span className="text-gray-500">Min: ${store.minOrderAmount}</span>
                <span className="text-gray-500">Delivery: ${store.deliveryFee}</span>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
