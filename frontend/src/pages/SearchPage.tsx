import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { searchApi } from '../services/api'

interface SearchResult {
  id: string
  name: string
  description: string
  price?: number
  imageUrl?: string
  type: 'menu_item' | 'store'
  storeId?: string
}

export default function SearchPage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SearchResult[]>([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  const handleSearch = async () => {
    if (!query.trim()) return
    setLoading(true)
    setSearched(true)
    try {
      const data = await searchApi.search(query.trim())
      setResults(data.menuItems?.concat(data.stores || []) || [])
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  const popularSearches = ['Margherita', 'Pepperoni', 'Vegan', 'Wings', 'Deals']

  return (
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">Search</h1>

      <div className="flex gap-2 mb-6">
        <input type="text" value={query} onChange={e => setQuery(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSearch()}
          placeholder="Search for pizzas, stores, or cuisines..."
          className="flex-1 border rounded-lg px-4 py-3 text-lg focus:ring-2 focus:ring-red-500 focus:border-transparent" />
        <button onClick={handleSearch} disabled={loading}
          className="bg-red-600 text-white px-6 py-3 rounded-lg hover:bg-red-700 disabled:opacity-50 font-medium">
          {loading ? '...' : 'Search'}
        </button>
      </div>

      {!searched && (
        <div>
          <h2 className="text-sm font-medium text-gray-500 mb-3">Popular searches</h2>
          <div className="flex flex-wrap gap-2">
            {popularSearches.map(term => (
              <button key={term} onClick={() => { setQuery(term); }}
                className="bg-gray-100 px-4 py-2 rounded-full text-sm hover:bg-gray-200 transition-colors">
                {term}
              </button>
            ))}
          </div>
        </div>
      )}

      {searched && !loading && results.length === 0 && (
        <div className="text-center py-12">
          <div className="text-5xl mb-4">🔍</div>
          <p className="text-gray-500 text-lg">No results found for "{query}"</p>
          <p className="text-gray-400 text-sm mt-1">Try a different search term</p>
        </div>
      )}

      {results.length > 0 && (
        <div className="space-y-3">
          {results.map(result => (
            <div key={result.id}
              onClick={() => result.storeId ? navigate(`/stores/${result.storeId}/menu`) : navigate(`/stores`)}
              className="flex items-center gap-4 p-4 bg-white rounded-lg border hover:shadow-md cursor-pointer transition-shadow">
              {result.imageUrl ? (
                <img src={result.imageUrl} alt={result.name} className="w-16 h-16 rounded-lg object-cover" />
              ) : (
                <div className="w-16 h-16 rounded-lg bg-red-100 flex items-center justify-center text-2xl">🍕</div>
              )}
              <div className="flex-1">
                <h3 className="font-semibold">{result.name}</h3>
                <p className="text-sm text-gray-500 line-clamp-1">{result.description}</p>
              </div>
              {result.price && (
                <span className="font-bold text-red-600">${result.price.toFixed(2)}</span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
