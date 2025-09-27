import React, { useState, useEffect } from "react";
import { 
  FileText, 
  Download, 
  Mail, 
  Phone, 
  MapPin, 
  Calendar, 
  Loader2, 
  AlertCircle,
  Eye,
  X,
  Maximize2,
  Minimize2
} from "lucide-react";
import { keycloak } from "@/KeycloakProvider"; // Adjust import path
import { useToast } from "@/hooks/use-toast"; // Adjust import path

interface CVViewerProps {
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  candidatePhone?: string;
  candidatePoste?: string;
}

export function CVViewer({ 
  candidateId, 
  candidateName, 
  candidateEmail, 
  candidatePhone, 
  candidatePoste 
}: CVViewerProps) {
  const { toast } = useToast();
  const [isOpen, setIsOpen] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);
  const [error, setError] = useState('');
  const [cvBlobUrl, setCvBlobUrl] = useState<string | null>(null);
  const [isFullscreen, setIsFullscreen] = useState(false);

  // Clean up blob URL when component unmounts or modal closes
  useEffect(() => {
    return () => {
      if (cvBlobUrl) {
        URL.revokeObjectURL(cvBlobUrl);
      }
    };
  }, [cvBlobUrl]);

  useEffect(() => {
    if (!isOpen && cvBlobUrl) {
      URL.revokeObjectURL(cvBlobUrl);
      setCvBlobUrl(null);
    }
  }, [isOpen]);

  // Function to ensure valid token
  const ensureValidToken = async (): Promise<string> => {
    try {
      // Check if keycloak is initialized
      if (!keycloak.authenticated) {
        throw new Error('Non authentifiÃ©');
      }

      // Check if token exists
      if (!keycloak.token) {
        throw new Error('Token manquant');
      }

      // Check if token is expired and refresh if needed
      if (keycloak.isTokenExpired(30)) { // 30 seconds buffer
        const refreshed = await keycloak.updateToken(30);
        if (!refreshed) {
          throw new Error('Impossible de rafraÃ®chir le token');
        }
      }

      return keycloak.token;
    } catch (error) {
      keycloak.login();
      throw error;
    }
  };

  // Function to fetch CV with proper authentication
  const fetchCvBlob = async (): Promise<Blob> => {
    try {
      const token = await ensureValidToken();      
      const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/documents/${candidateId}/cv/download`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

    
      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('Non autorisÃ© - Veuillez vous reconnecter');
        } else if (response.status === 404) {
          throw new Error('CV non trouvÃ© pour ce candidat');
        } else {
          throw new Error(`Erreur HTTP: ${response.status} - ${response.statusText}`);
        }
      }

      const blob = await response.blob();
      
      // Check if blob is empty or very small
      if (blob.size === 0) {
        throw new Error('Le CV existe mais son contenu est vide');
      } else if (blob.size < 100) {
        throw new Error(`Le CV semble corrompu (seulement ${blob.size} bytes)`);
      }

      return blob;
    } catch (error) {
      throw error;
    }
  };

  // Function to download CV
  const downloadCv = async () => {
    setIsDownloading(true);
    setError('');

    try {
      const blob = await fetchCvBlob();
      
      // Create download
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `CV_${candidateName.replace(/\s+/g, '_')}.pdf`;
      document.body.appendChild(link);
      link.click();
      
      // Cleanup
      URL.revokeObjectURL(url);
      document.body.removeChild(link);

      toast({
        title: "CV tÃ©lÃ©chargÃ©",
        description: `Le CV de ${candidateName} a Ã©tÃ© tÃ©lÃ©chargÃ©`,
      });

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Impossible de tÃ©lÃ©charger le CV';
      
      setError(errorMessage);
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    } finally {
      setIsDownloading(false);
    }
  };

  // Function to view CV in new tab
  const viewCvInNewTab = async () => {
    try {
      const blob = await fetchCvBlob();
      const url = URL.createObjectURL(blob);
      
      // Open in new tab
      const newWindow = window.open(url, '_blank');
      
      if (!newWindow) {
        toast({
          title: "Popup bloquÃ©",
          description: "Veuillez autoriser les popups pour voir le CV",
          variant: "default"
        });
      }
      
      // Clean up after some time
      setTimeout(() => URL.revokeObjectURL(url), 10000);

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Impossible d\'ouvrir le CV';
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    }
  };

  // Function to load CV preview
  const loadCvPreview = async () => {
    if (cvBlobUrl) return; // Already loaded

    setIsLoadingPreview(true);
    setError('');

    try {
      const blob = await fetchCvBlob();
      const url = URL.createObjectURL(blob);
      setCvBlobUrl(url);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Impossible de charger l\'aperÃ§u du CV';
      
      setError(errorMessage);
    } finally {
      setIsLoadingPreview(false);
    }
  };

  // Load preview when modal opens
  useEffect(() => {
    if (isOpen) {
      loadCvPreview();
    }
  }, [isOpen]);

  // Close modal and reset fullscreen
  const closeModal = () => {
    setIsOpen(false);
    setIsFullscreen(false);
  };

  return (
    <>
      {/* Trigger Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="inline-flex items-center gap-2 px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
      >
        <FileText className="h-4 w-4" />
        Voir le CV
      </button>

      {/* Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          {/* Overlay */}
          <div 
            className="fixed inset-0 bg-black/50"
            onClick={closeModal}
          />
          
          {/* Modal Content */}
          <div className={`relative bg-white rounded-lg shadow-xl mx-4 overflow-hidden flex flex-col ${
            isFullscreen 
              ? 'w-full h-full max-w-none max-h-none m-0' 
              : 'max-w-6xl w-full max-h-[95vh]'
          }`}>
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b flex-shrink-0 bg-white">
              <h2 className="text-xl font-semibold">CV - {candidateName}</h2>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setIsFullscreen(!isFullscreen)}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-md transition-colors"
                  title={isFullscreen ? "Sortir du plein écran" : "Plein écran"}
                >
                  {isFullscreen ? (
                    <Minimize2 className="h-4 w-4" />
                  ) : (
                    <Maximize2 className="h-4 w-4" />
                  )}
                  {isFullscreen ? 'Réduire' : 'Plein écran'}
                </button>

                <button
                  onClick={downloadCv}
                  disabled={isDownloading}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded-md transition-colors"
                >
                  {isDownloading ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Download className="h-4 w-4" />
                  )}
                  {isDownloading ? 'Téléchargement...' : 'Télécharger PDF'}
                </button>
                
                <button
                  onClick={viewCvInNewTab}
                  className="inline-flex items-center gap-2 px-3 py-1.5 text-sm bg-gray-600 hover:bg-gray-700 text-white rounded-md transition-colors"
                >
                  <Eye className="h-4 w-4" />
                  Nouvel onglet
                </button>
                
                <button
                  onClick={closeModal}
                  className="text-gray-500 hover:text-gray-700 p-1 rounded-full hover:bg-gray-100"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-hidden">
              {/* Error Display */}
              {error && (
                <div className="m-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-700">
                  <AlertCircle className="h-4 w-4" />
                  <span>{error}</span>
                  {error.includes('Non autorisé') && (
                    <button
                      onClick={() => keycloak.login()}
                      className="ml-2 text-sm bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded"
                    >
                      Se reconnecter
                    </button>
                  )}
                </div>
              )}

              {isFullscreen ? (
                // Fullscreen mode - only show PDF viewer
                <div className="h-full relative bg-gray-100">
                  {isLoadingPreview ? (
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="text-center">
                        <Loader2 className="h-12 w-12 animate-spin mx-auto mb-4 text-gray-400" />
                        <p className="text-lg text-gray-600">Chargement du CV...</p>
                      </div>
                    </div>
                  ) : error ? (
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="text-center">
                        <AlertCircle className="h-12 w-12 mx-auto mb-4 text-red-400" />
                        <p className="text-lg text-gray-600 mb-4">Impossible de charger l'aperçu</p>
                        <button
                          onClick={loadCvPreview}
                          className="px-4 py-2 text-blue-600 hover:text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-50"
                        >
                          Réessayer
                        </button>
                      </div>
                    </div>
                  ) : cvBlobUrl ? (
                    <embed
                      src={cvBlobUrl}
                      type="application/pdf"
                      className="w-full h-full"
                      title={`CV de ${candidateName}`}
                    />
                  ) : null}
                </div>
              ) : (
                // Normal mode - show candidate info and CV preview
                <div className="flex flex-col lg:flex-row h-full">
                  {/* Left Panel - Candidate Info */}
                  <div className="lg:w-1/3 border-b lg:border-b-0 lg:border-r border-gray-200 bg-gray-50">
                    <div className="p-6 space-y-6">
                      {/* Personal Information */}
                      <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
                        <div className="p-4 border-b border-gray-200">
                          <h3 className="text-lg font-bold">{candidateName}</h3>
                          {candidatePoste && (
                            <p className="text-md text-gray-600 mt-1">{candidatePoste}</p>
                          )}
                        </div>
                        <div className="p-4">
                          <div className="space-y-3">
                            <div className="flex items-center gap-2">
                              <Mail className="h-4 w-4 text-gray-500 flex-shrink-0" />
                              <span className="text-sm break-all">{candidateEmail}</span>
                            </div>
                            {candidatePhone && (
                              <div className="flex items-center gap-2">
                                <Phone className="h-4 w-4 text-gray-500 flex-shrink-0" />
                                <span className="text-sm">{candidatePhone}</span>
                              </div>
                            )}
                            <div className="flex items-center gap-2">
                              <MapPin className="h-4 w-4 text-gray-500 flex-shrink-0" />
                              <span className="text-sm">Maroc</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Right Panel - CV Preview */}
                  <div className="lg:w-2/3 flex-1 flex flex-col">
                    <div className="p-4 border-b border-gray-200 bg-gray-50">
                      <h3 className="font-semibold flex items-center gap-2">
                        <FileText className="h-5 w-5 text-gray-600" />
                        Aperçu du CV
                      </h3>
                    </div>
                    
                    {/* PDF Viewer - Larger height */}
                    <div className="flex-1 relative min-h-[600px]">
                      {isLoadingPreview ? (
                        <div className="absolute inset-0 flex items-center justify-center bg-gray-50">
                          <div className="text-center">
                            <Loader2 className="h-8 w-8 animate-spin mx-auto mb-2 text-gray-400" />
                            <p className="text-sm text-gray-600">Chargement du CV...</p>
                          </div>
                        </div>
                      ) : error ? (
                        <div className="absolute inset-0 flex items-center justify-center bg-gray-50">
                          <div className="text-center">
                            <AlertCircle className="h-8 w-8 mx-auto mb-2 text-red-400" />
                            <p className="text-sm text-gray-600">Impossible de charger l'aperçu</p>
                            <button
                              onClick={loadCvPreview}
                              className="mt-2 text-sm text-blue-600 hover:text-blue-700"
                            >
                              Réessayer
                            </button>
                          </div>
                        </div>
                      ) : cvBlobUrl ? (
                        <embed
                          src={cvBlobUrl}
                          type="application/pdf"
                          className="w-full h-full"
                          title={`CV de ${candidateName}`}
                        />
                      ) : null}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

// Simplified version - just action buttons (unchanged)
export function CVActions({ candidateId, candidateName }: { candidateId: number, candidateName: string }) {
  const { toast } = useToast();
  const [isDownloading, setIsDownloading] = useState(false);

  const ensureValidToken = async (): Promise<string> => {
    try {
      if (!keycloak.authenticated) {
        throw new Error('Non authentifiÃ©');
      }

      if (!keycloak.token) {
        throw new Error('Token manquant');
      }

      if (keycloak.isTokenExpired(30)) {
        const refreshed = await keycloak.updateToken(30);
        if (!refreshed) {
          throw new Error('Impossible de rafraÃ®chir le token');
        }
      }

      return keycloak.token;
    } catch (error) {
      keycloak.login();
      throw error;
    }
  };

  const downloadCv = async () => {
    setIsDownloading(true);
    
    try {
      const token = await ensureValidToken();
      
      const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/documents/${candidateId}/cv/download`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('Non autorisÃ© - Veuillez vous reconnecter');
        } else if (response.status === 404) {
          throw new Error('CV non trouvÃ©');
        } else {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }
      }

      const blob = await response.blob();
      
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `CV_${candidateName.replace(/\s+/g, '_')}.pdf`;
      document.body.appendChild(link);
      link.click();
      
      URL.revokeObjectURL(url);
      document.body.removeChild(link);

      toast({
        title: "CV tÃ©lÃ©chargÃ©",
        description: `Le CV de ${candidateName} a Ã©tÃ© tÃ©lÃ©chargÃ©`,
      });

    } catch (error) {
      console.error('Erreur:', error);
      const errorMessage = error instanceof Error ? error.message : 'Erreur lors du tÃ©lÃ©chargement du CV';
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    } finally {
      setIsDownloading(false);
    }
  };

  const viewCv = async () => {
    try {
      const token = await ensureValidToken();
      
      const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/documents/${candidateId}/cv/download`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('Non autorisÃ© - Veuillez vous reconnecter');
        } else if (response.status === 404) {
          throw new Error('CV non trouvÃ©');
        } else {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }
      }

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      
      window.open(url, '_blank');
      
      // Clean up after some time
      setTimeout(() => URL.revokeObjectURL(url), 10000);

    } catch (error) {
      console.error('Erreur:', error);
      const errorMessage = error instanceof Error ? error.message : 'Impossible d\'ouvrir le CV';
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    }
  };

  return (
    <div className="flex gap-2">
      <button
        onClick={downloadCv}
        disabled={isDownloading}
        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded transition-colors"
      >
        {isDownloading ? (
          <Loader2 className="h-3 w-3 animate-spin" />
        ) : (
          <Download className="h-3 w-3" />
        )}
        {isDownloading ? 'Téléchargement...' : 'Télécharger'}
      </button>

      <button
        onClick={viewCv}
        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-gray-600 hover:bg-gray-700 text-white rounded transition-colors"
      >
        <Eye className="h-3 w-3" />
        Voir
      </button>
    </div>
  );
}