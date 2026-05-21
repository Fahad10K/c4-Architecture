import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api from '../../services/api'
import { Order } from '../../types'

interface OrderState {
  orders: Order[]
  currentOrder: Order | null
  loading: boolean
  error: string | null
}

const initialState: OrderState = {
  orders: [],
  currentOrder: null,
  loading: false,
  error: null,
}

export const fetchOrders = createAsyncThunk('orders/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const response = await api.get('/orders')
    return response.data.content || response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch orders')
  }
})

export const fetchOrderById = createAsyncThunk('orders/fetchById', async (id: string, { rejectWithValue }) => {
  try {
    const response = await api.get<Order>(`/orders/${id}`)
    return response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch order')
  }
})

export const createOrder = createAsyncThunk(
  'orders/create',
  async (data: { addressId: string; specialNotes?: string }, { rejectWithValue }) => {
    try {
      const response = await api.post<Order>('/orders', data)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create order')
    }
  }
)

export const cancelOrder = createAsyncThunk('orders/cancel', async (orderId: string, { rejectWithValue }) => {
  try {
    const response = await api.post<Order>(`/orders/${orderId}/cancel`)
    return response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to cancel order')
  }
})

const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    updateOrderStatus(state, action) {
      const { orderId, status } = action.payload
      const order = state.orders.find((o) => o.id === orderId)
      if (order) order.status = status
      if (state.currentOrder?.id === orderId) state.currentOrder.status = status
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.pending, (state) => { state.loading = true })
      .addCase(fetchOrders.fulfilled, (state, action) => { state.loading = false; state.orders = action.payload })
      .addCase(fetchOrders.rejected, (state, action) => { state.loading = false; state.error = action.payload as string })
      .addCase(fetchOrderById.fulfilled, (state, action) => { state.currentOrder = action.payload })
      .addCase(createOrder.fulfilled, (state, action) => { state.orders.unshift(action.payload); state.currentOrder = action.payload })
      .addCase(cancelOrder.fulfilled, (state, action) => {
        const idx = state.orders.findIndex((o) => o.id === action.payload.id)
        if (idx >= 0) state.orders[idx] = action.payload
      })
  },
})

export const { updateOrderStatus } = orderSlice.actions
export default orderSlice.reducer
