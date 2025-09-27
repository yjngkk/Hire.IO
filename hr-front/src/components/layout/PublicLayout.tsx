import React from 'react';

function PublicLayout({ children }) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header public avec style bleu */}
      <header className="bg-white shadow-sm border-b border-blue-100">
        <div className="container mx-auto px-4 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">H</span>
              </div>
              <div>
                <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                  Hire.IO
                </h1>
                <span className="text-sm text-gray-600">Plateforme RH Moderne</span>
              </div>
            </div>
            <div className="hidden sm:flex items-center space-x-2">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              <span className="text-sm text-gray-600 font-medium">
                Candidature en ligne
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Contenu principal avec style */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
          {/* Ajout d'une zone d'introduction stylée */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center px-4 py-2 bg-blue-100 text-blue-800 rounded-full text-sm font-medium mb-4">
              ✨ Rejoignez notre équipe
            </div>
            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              Postulez en quelques minutes
            </h2>
            <p className="text-gray-600 max-w-2xl mx-auto">
              Remplissez le formulaire ci-dessous pour soumettre votre candidature. 
              Notre équipe RH vous contactera dans les plus brefs délais.
            </p>
          </div>
          
          {/* Contenu du formulaire avec style card */}
          <div className="bg-white rounded-2xl shadow-lg border border-blue-100 overflow-hidden">
            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-4">
              <h3 className="text-white font-semibold text-lg">Informations de candidature</h3>
              <p className="text-blue-100 text-sm">Tous les champs marqués d'un * sont obligatoires</p>
            </div>
            <div className="p-6">
              {children}
            </div>
          </div>
        </div>
      </main>

      {/* Footer stylé */}
      <footer className="bg-white border-t border-blue-100 mt-16">
        <div className="container mx-auto px-4 py-8">
          <div className="text-center">
            <div className="flex items-center justify-center space-x-3 mb-4">
              <div className="w-6 h-6 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-md flex items-center justify-center">
                <span className="text-white font-bold text-xs">H</span>
              </div>
              <span className="font-semibold text-gray-800">Hire.IO</span>
            </div>
            <p className="text-sm text-gray-600">
              © 2024 Hire.IO - Plateforme RH Moderne
            </p>
            <div className="flex items-center justify-center space-x-6 mt-4 text-xs text-gray-500">
              <span>🔒 Données sécurisées</span>
              <span>⚡ Traitement rapide</span>
              <span>💼 Équipe professionnelle</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default PublicLayout;