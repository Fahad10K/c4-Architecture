export interface User {
  id: string
  email: string
  name: string
  phone: string
  role: string
  avatarUrl?: string
}

export interface Address {
  id: string
  label: string
  street: string
  city: string
  state: string
  zipCode: string
  country: string
  lat: number
  lng: number
  isDefault: boolean
}

export interface Store {
  id: string
  name: string
  description: string
  phone: string
  street: string
  city: string
  state: string
  imageUrl: string
  rating: number
  reviewCount: number
  isActive: boolean
  openTime: string
  closeTime: string
  deliveryRadius: number
  minOrderAmount: number
  deliveryFee: number
  estimatedDeliveryTime: number
}

export interface Category {
  id: string
  name: string
  description: string
  imageUrl?: string
  sortOrder: number
}

export interface MenuItem {
  id: string
  storeId: string
  categoryId: string
  name: string
  description: string
  price: number
  imageUrl?: string
  isAvailable: boolean
  isPopular: boolean
  calories?: number
  preparationTime?: number
  customizations: string
  tags: string
}

export interface CartItem {
  id: string
  menuItemId: string
  menuItemName: string
  menuItemImage?: string
  quantity: number
  unitPrice: number
  totalPrice: number
  customizations?: string
  specialInstructions?: string
}

export interface Cart {
  id: string
  storeId: string
  storeName: string
  items: CartItem[]
  subtotal: number
  tax: number
  deliveryFee: number
  discount: number
  total: number
  couponCode?: string
}

export interface Order {
  id: string
  orderNumber: string
  storeId: string
  storeName: string
  status: string
  items: OrderItem[]
  subtotal: number
  tax: number
  deliveryFee: number
  discount: number
  total: number
  couponCode?: string
  specialNotes?: string
  estimatedDelivery: string
  createdAt: string
}

export interface OrderItem {
  id: string
  menuItemName: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface Delivery {
  id: string
  orderId: string
  driverName: string
  driverPhone: string
  status: string
  currentLat: number
  currentLng: number
  estimatedArrival: string
}

export interface Notification {
  id: string
  title: string
  message: string
  type: string
  isRead: boolean
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}
