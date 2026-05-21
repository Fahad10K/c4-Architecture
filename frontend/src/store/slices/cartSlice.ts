import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api from '../../services/api'
import { Cart } from '../../types'

interface CartState {
  cart: Cart | null
  loading: boolean
  error: string | null
}

const initialState: CartState = {
  cart: null,
  loading: false,
  error: null,
}

export const fetchCart = createAsyncThunk('cart/fetch', async (_, { rejectWithValue }) => {
  try {
    const response = await api.get<Cart>('/cart')
    return response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch cart')
  }
})

export const addToCart = createAsyncThunk(
  'cart/addItem',
  async (data: { menuItemId: string; quantity: number; storeId: string; customizations?: string; specialInstructions?: string }, { rejectWithValue }) => {
    try {
      const response = await api.post<Cart>('/cart/items', data)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to add item')
    }
  }
)

export const updateCartItem = createAsyncThunk(
  'cart/updateItem',
  async (data: { itemId: string; quantity: number }, { rejectWithValue }) => {
    try {
      const response = await api.put<Cart>(`/cart/items/${data.itemId}`, { quantity: data.quantity })
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update item')
    }
  }
)

export const removeCartItem = createAsyncThunk(
  'cart/removeItem',
  async (itemId: string, { rejectWithValue }) => {
    try {
      const response = await api.delete<Cart>(`/cart/items/${itemId}`)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to remove item')
    }
  }
)

export const applyCoupon = createAsyncThunk(
  'cart/applyCoupon',
  async (code: string, { rejectWithValue }) => {
    try {
      const response = await api.post<Cart>('/cart/coupon', { code })
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Invalid coupon')
    }
  }
)

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    clearCart(state) {
      state.cart = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCart.pending, (state) => { state.loading = true })
      .addCase(fetchCart.fulfilled, (state, action) => { state.loading = false; state.cart = action.payload })
      .addCase(fetchCart.rejected, (state, action) => { state.loading = false; state.error = action.payload as string })
      .addCase(addToCart.fulfilled, (state, action) => { state.cart = action.payload })
      .addCase(updateCartItem.fulfilled, (state, action) => { state.cart = action.payload })
      .addCase(removeCartItem.fulfilled, (state, action) => { state.cart = action.payload })
      .addCase(applyCoupon.fulfilled, (state, action) => { state.cart = action.payload })
      .addCase(applyCoupon.rejected, (state, action) => { state.error = action.payload as string })
  },
})

export const { clearCart } = cartSlice.actions
export default cartSlice.reducer
