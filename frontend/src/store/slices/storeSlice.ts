import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api from '../../services/api'
import { Store } from '../../types'

interface StoreState {
  stores: Store[]
  selectedStore: Store | null
  loading: boolean
  error: string | null
}

const initialState: StoreState = {
  stores: [],
  selectedStore: null,
  loading: false,
  error: null,
}

export const fetchStores = createAsyncThunk('stores/fetchAll', async (_, { rejectWithValue }) => {
  try {
    const response = await api.get<Store[]>('/stores')
    return response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch stores')
  }
})

export const fetchStoreById = createAsyncThunk('stores/fetchById', async (id: string, { rejectWithValue }) => {
  try {
    const response = await api.get<Store>(`/stores/${id}`)
    return response.data
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch store')
  }
})

const storeSlice = createSlice({
  name: 'stores',
  initialState,
  reducers: {
    clearSelectedStore(state) {
      state.selectedStore = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchStores.pending, (state) => { state.loading = true })
      .addCase(fetchStores.fulfilled, (state, action) => { state.loading = false; state.stores = action.payload })
      .addCase(fetchStores.rejected, (state, action) => { state.loading = false; state.error = action.payload as string })
      .addCase(fetchStoreById.fulfilled, (state, action) => { state.selectedStore = action.payload })
  },
})

export const { clearSelectedStore } = storeSlice.actions
export default storeSlice.reducer
