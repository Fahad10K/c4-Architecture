import { Link } from 'react-router-dom'
import { MapPin, Clock, Star, ArrowRight } from 'lucide-react'

export default function HomePage() {
  return (
    <div>
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary-600 via-primary-700 to-primary-900 text-white overflow-hidden">
        <div className="absolute inset-0 bg-black/20" />
        <div className="relative max-w-7xl mx-auto px-4 py-24 sm:py-32">
          <div className="max-w-2xl">
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold leading-tight">
              Fresh Pizza,<br />Delivered to Your Door
            </h1>
            <p className="mt-6 text-lg sm:text-xl text-primary-100 leading-relaxed">
              Order from the best local pizzerias. Hot, fresh, and delivered fast with real-time tracking.
            </p>
            <div className="mt-8 flex flex-wrap gap-4">
              <Link to="/stores" className="inline-flex items-center gap-2 bg-white text-primary-700 font-bold py-3 px-8 rounded-lg hover:bg-gray-100 transition-colors shadow-lg">
                Order Now <ArrowRight className="h-5 w-5" />
              </Link>
              <Link to="/register" className="inline-flex items-center gap-2 border-2 border-white text-white font-bold py-3 px-8 rounded-lg hover:bg-white/10 transition-colors">
                Create Account
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center p-6">
              <div className="w-14 h-14 bg-primary-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <MapPin className="h-7 w-7 text-primary-600" />
              </div>
              <h3 className="text-lg font-bold text-gray-900 mb-2">Nearby Stores</h3>
              <p className="text-gray-600">Find the closest pizza stores in your area for faster delivery.</p>
            </div>
            <div className="text-center p-6">
              <div className="w-14 h-14 bg-accent-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Clock className="h-7 w-7 text-accent-600" />
              </div>
              <h3 className="text-lg font-bold text-gray-900 mb-2">Fast Delivery</h3>
              <p className="text-gray-600">Average delivery in 30 minutes with real-time GPS tracking.</p>
            </div>
            <div className="text-center p-6">
              <div className="w-14 h-14 bg-green-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Star className="h-7 w-7 text-green-600" />
              </div>
              <h3 className="text-lg font-bold text-gray-900 mb-2">Top Quality</h3>
              <p className="text-gray-600">Hand-crafted pizzas with premium ingredients from top-rated stores.</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Ready to Order?</h2>
          <p className="text-lg text-gray-600 mb-8">Browse our partner stores and find your perfect pizza today.</p>
          <Link to="/stores" className="btn-primary text-lg py-3 px-10">
            View Stores
          </Link>
        </div>
      </section>
    </div>
  )
}
