import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api from '../../services/api'
import { MenuItem, Category } from '../../types'

interface MenuState {
  items: MenuItem[]
  categories: Category[]
  popularItems: MenuItem[]
  loading: boolean
  error: string | null
}

const initialState: MenuState = {
  items: [],
  categories: [],
  popularItems: [],
  loading: false,
  error: null,
}

export const fetchMenuByStore = createAsyncThunk(
  'menu/fetchByStore',
  async (storeId: string, { rejectWithValue }) => {
    try {
      const response = await api.get<MenuItem[]>(`/menu/store/${storeId}`)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch menu')
    }
  }
)

export const fetchPopularItems = createAsyncThunk(
  'menu/fetchPopular',
  async (storeId: string, { rejectWithValue }) => {
    try {
      const response = await api.get<MenuItem[]>(`/menu/store/${storeId}/popular`)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch popular items')
    }
  }
)

export const searchMenu = createAsyncThunk(
  'menu/search',
  async (query: string, { rejectWithValue }) => {
    try {
      const response = await api.get<MenuItem[]>(`/menu/search?q=${query}`)
      return response.data
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Search failed')
    }
  }
)

const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    clearMenu(state) {
      state.items = []
      state.categories = []
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMenuByStore.pending, (state) => { state.loading = true })
      .addCase(fetchMenuByStore.fulfilled, (state, action) => { state.loading = false; state.items = action.payload })
      .addCase(fetchMenuByStore.rejected, (state, action) => { state.loading = false; state.error = action.payload as string })
      .addCase(fetchPopularItems.fulfilled, (state, action) => { state.popularItems = action.payload })
      .addCase(searchMenu.fulfilled, (state, action) => { state.items = action.payload })
  },
})

export const { clearMenu } = menuSlice.actions
export default menuSlice.reducer
