import { useAppSelector } from '../store/hooks'
import { User, Mail, Phone, MapPin } from 'lucide-react'

export default function ProfilePage() {
  const { user } = useAppSelector((state) => state.auth)

  if (!user) return null

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Profile</h1>

      <div className="card p-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
            <User className="h-8 w-8 text-primary-600" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">{user.name}</h2>
            <p className="text-sm text-gray-500 capitalize">{user.role.toLowerCase()}</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
            <Mail className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-xs text-gray-500">Email</p>
              <p className="font-medium text-gray-900">{user.email}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
            <Phone className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-xs text-gray-500">Phone</p>
              <p className="font-medium text-gray-900">{user.phone}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="card p-6 mt-6">
        <h3 className="font-bold text-gray-900 mb-4">Saved Addresses</h3>
        <div className="space-y-3">
          <div className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
            <MapPin className="h-5 w-5 text-primary-500 mt-0.5" />
            <div>
              <p className="font-medium text-gray-900">Home</p>
              <p className="text-sm text-gray-600">123 Main St, New York, NY 10001</p>
            </div>
          </div>
          <div className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
            <MapPin className="h-5 w-5 text-gray-400 mt-0.5" />
            <div>
              <p className="font-medium text-gray-900">Office</p>
              <p className="text-sm text-gray-600">456 Broadway, New York, NY 10002</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
