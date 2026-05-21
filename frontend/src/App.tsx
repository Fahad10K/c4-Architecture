import { Routes, Route } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import StoresPage from './pages/StoresPage'
import MenuPage from './pages/MenuPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrdersPage from './pages/OrdersPage'
import OrderTrackingPage from './pages/OrderTrackingPage'
import ProfilePage from './pages/ProfilePage'
import ChatbotPage from './pages/ChatbotPage'
import NotificationsPage from './pages/NotificationsPage'
import SearchPage from './pages/SearchPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import AdminAnalyticsPage from './pages/AdminAnalyticsPage'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  return (
    <>
      <Toaster position="top-right" />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="stores" element={<StoresPage />} />
          <Route path="stores/:storeId/menu" element={<MenuPage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
          <Route path="checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
          <Route path="orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
          <Route path="orders/:orderId/track" element={<ProtectedRoute><OrderTrackingPage /></ProtectedRoute>} />
          <Route path="profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
          <Route path="notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
          <Route path="chatbot" element={<ProtectedRoute><ChatbotPage /></ProtectedRoute>} />
          <Route path="admin" element={<ProtectedRoute><AdminDashboardPage /></ProtectedRoute>} />
          <Route path="admin/analytics" element={<ProtectedRoute><AdminAnalyticsPage /></ProtectedRoute>} />
        </Route>
      </Routes>
    </>
  )
}

export default App
