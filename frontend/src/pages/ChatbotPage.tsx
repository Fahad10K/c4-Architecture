import { useState, useRef, useEffect } from 'react'
import { chatbotApi } from '../services/api'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

export default function ChatbotPage() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = async () => {
    if (!input.trim() || loading) return

    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
      timestamp: new Date().toISOString()
    }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)

    try {
      const response = await chatbotApi.sendMessage(input.trim())
      setMessages(prev => [...prev, response])
    } catch {
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: "Sorry, I'm having trouble connecting right now. Please try again.",
        timestamp: new Date().toISOString()
      }])
    } finally {
      setLoading(false)
    }
  }

  const quickActions = [
    'What pizzas do you have?',
    'Track my order',
    'Active promotions?',
    'Delivery time estimate'
  ]

  return (
    <div className="max-w-3xl mx-auto h-[calc(100vh-8rem)] flex flex-col">
      <div className="bg-red-600 text-white p-4 rounded-t-lg">
        <h1 className="text-xl font-bold">Pizza Palace Assistant</h1>
        <p className="text-sm text-red-100">Ask me about menu, orders, delivery, or promotions!</p>
      </div>

      <div className="flex-1 overflow-y-auto bg-gray-50 p-4 space-y-4">
        {messages.length === 0 && (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">🍕</div>
            <h2 className="text-xl font-semibold text-gray-700 mb-2">Welcome to Pizza Palace!</h2>
            <p className="text-gray-500 mb-6">How can I help you today?</p>
            <div className="flex flex-wrap gap-2 justify-center">
              {quickActions.map(action => (
                <button key={action} onClick={() => { setInput(action); }}
                  className="bg-white border border-gray-300 rounded-full px-4 py-2 text-sm hover:bg-red-50 hover:border-red-300 transition-colors">
                  {action}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map(msg => (
          <div key={msg.id} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] rounded-lg px-4 py-3 ${
              msg.role === 'user' ? 'bg-red-600 text-white' : 'bg-white border shadow-sm'
            }`}>
              <p className="whitespace-pre-wrap text-sm">{msg.content}</p>
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex justify-start">
            <div className="bg-white border shadow-sm rounded-lg px-4 py-3">
              <div className="flex space-x-1">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0ms'}} />
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '150ms'}} />
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '300ms'}} />
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="bg-white border-t p-4 rounded-b-lg">
        <div className="flex gap-2">
          <input type="text" value={input} onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && sendMessage()}
            placeholder="Type your message..." disabled={loading}
            className="flex-1 border rounded-lg px-4 py-2 focus:ring-2 focus:ring-red-500 focus:border-transparent disabled:opacity-50" />
          <button onClick={sendMessage} disabled={loading || !input.trim()}
            className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors font-medium">
            Send
          </button>
        </div>
      </div>
    </div>
  )
}
