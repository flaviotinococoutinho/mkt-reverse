function App() {
  return (
    <div className="min-h-screen bg-brand-secondary text-text-primary font-sans flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-surface-base p-8 rounded-lg shadow-sm border border-gray-200">
        <h1 className="font-serif text-4xl mb-2 text-brand-primary">O Leilão</h1>
        <p className="text-text-secondary mb-6">Marketplace Reverso MVP</p>
        
        <div className="space-y-4">
          <div className="p-4 bg-surface-subtle rounded border border-gray-100">
            <h2 className="font-medium mb-1">Status do Frontend</h2>
            <p className="text-sm text-text-muted">Inicializado com sucesso.</p>
          </div>
          
          <div className="flex gap-3">
            <button className="flex-1 px-4 py-2 bg-brand-primary text-white rounded hover:bg-gray-800 transition-colors cursor-not-allowed opacity-50">
              Entrar
            </button>
            <button className="flex-1 px-4 py-2 border border-brand-primary text-brand-primary rounded hover:bg-gray-50 transition-colors cursor-not-allowed opacity-50">
              Cadastrar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
