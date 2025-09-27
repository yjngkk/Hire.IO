import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { TestLibrary } from "@/components/tests/TestLibrary";
import { ExerciseLibrary } from "@/components/tests/ExerciseLibrary";
import { TestCreator } from "@/components/tests/TestCreator";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Clock, Send, CheckCircle, Loader2, RefreshCw, AlertCircle, Users, XCircle, X, Trophy, Loader } from "lucide-react";
import { useState, useEffect, useCallback } from "react";
import { testApi } from "@/components/api/testApi";
import { TestCreatorIa } from "@/components/tests/TestCreatorIa";
import apiconfig from "@/config/apiService";

export function Tests() {
  // États pour les données
  const [availableTests, setAvailableTests] = useState<any[]>([]);
  const [candidates, setCandidates] = useState<any[]>([]);
  const [sentTests, setSentTests] = useState<any[]>([]);
  
  // États pour le formulaire
  const [selectedTest, setSelectedTest] = useState("");
  const [selectedCandidate, setSelectedCandidate] = useState("");
  const [candidateEmail, setCandidateEmail] = useState("");
  const [message, setMessage] = useState("Bonjour, voici votre test technique. Bonne chance !");
  
  // États pour les chargements et erreurs
  const [isLoadingTests, setIsLoadingTests] = useState(false);
  const [isLoadingCandidates, setIsLoadingCandidates] = useState(false);
  const [isLoadingSentTests, setIsLoadingSentTests] = useState(false);
  const [isSendingTest, setIsSendingTest] = useState(false);
  const [sendError, setSendError] = useState("");
  const [sendSuccess, setSendSuccess] = useState("");
  
  // États pour l'interface
  const [activeTab, setActiveTab] = useState("library");
  const [lastRefresh, setLastRefresh] = useState<number>(Date.now());
  //resultat
  const [showModal, setShowModal] = useState(false);
  const [modalResults, setModalResults] = useState(null);
  const [loadingResults, setLoadingResults] = useState(false);

  // Fonction pour récupérer les tests
  const fetchTests = useCallback(async () => {
    try {
      setIsLoadingTests(true);
      console.log('📄 Fetching tests...');
      const data = await testApi.getAllTests();
      setAvailableTests(data);
      
    } catch (error) {
      console.error('❌ Failed to fetch tests:', error);
      setAvailableTests([]);
    } finally {
      setIsLoadingTests(false);
    }
  }, []);

  const fetchResults = async (test) => {
    try {
      setLoadingResults(true);
      const results = await apiconfig.get(`/test-assignments/candidat/${test.candidat.id}/results`);
      
      setModalResults(results);
    } catch (error) {
      console.error('Erreur lors de la récupération des résultats:', error);
      alert('Erreur lors de la récupération des résultats');
    } finally {
      setLoadingResults(false);
    }
  };

  useEffect(() => {
    // Cette fonction se déclenche chaque fois que modalResults ou loadingResults change
    if (modalResults && !loadingResults) {
      console.log('✅ Données prêtes, ouverture de la modal');
      setShowModal(true);
    }
  }, [modalResults, loadingResults]);

  // Fonction pour récupérer les candidats
  const fetchCandidates = useCallback(async () => {
    try {
      setIsLoadingCandidates(true);
      console.log('📄 Fetching candidates...');
      const response = await apiconfig.get('/candidats');
      setCandidates(response|| []);
      
    } catch (error) {
      console.error('❌ Failed to fetch candidates:', error);
      setCandidates([]);
    } finally {
      setIsLoadingCandidates(false);
    }
  }, []);

  // Fonction pour récupérer les tests envoyés
  const fetchSentTests = useCallback(async () => {
    try {
      setIsLoadingSentTests(true);
      
      // Récupérer tous les tests
      const response = await await apiconfig.get('/test-assignments');
    
      
      console.log('✅ Sent tests fetched:', response);
      setSentTests(response);
      
    } catch (error) {
      console.error('❌ Failed to fetch sent tests:', error);
      setSentTests([]);
    } finally {
      setIsLoadingSentTests(false);
    }
  }, []);

  // Chargement initial
  useEffect(() => {
    fetchTests();
    fetchCandidates();
    fetchSentTests();
  }, [fetchTests, fetchCandidates,fetchSentTests]);

  // Chargement spécifique à l'onglet "send"
  useEffect(() => {
    if (activeTab === "send") {
      console.log('🔄 Switching to send tab...');
      fetchTests();
      fetchCandidates();
      fetchSentTests();
      setLastRefresh(Date.now());
    }
  }, [activeTab, fetchTests, fetchCandidates, fetchSentTests]);

  // Mettre à jour l'email du candidat automatiquement
  useEffect(() => {
    if (selectedCandidate) {
      const candidate = candidates.find((c: any) => c.id.toString() === selectedCandidate);
      if (candidate) {
        setCandidateEmail(candidate.email);
      }
    } else {
      setCandidateEmail("");
    }
  }, [selectedCandidate, candidates]);

  const handleTabChange = (value: string) => {
    console.log('🔄 Tab changed to:', value);
    setActiveTab(value);
  };

  const handleManualRefresh = () => {
    console.log('🔄 Manual refresh triggered');
    fetchTests();
    fetchCandidates();
    if (activeTab === "send") {
      fetchSentTests();
    }
    setLastRefresh(Date.now());
  };

  const getStatusBadge = (status: string) => {
    const styles = {
      SENT: "bg-yellow-100 text-yellow-800",
      STARTED: "bg-blue-100 text-blue-800",
      COMPLETED: "bg-green-100 text-green-800",
      EXPIRED: "bg-red-100 text-red-800"
    };
    
    const labels = {
      SENT: "Envoyé",
      STARTED: "Commencé",
      COMPLETED: "Terminé",
      EXPIRED: "Expiré"
    };
    return (
      <Badge className={styles[status as keyof typeof styles]}>
        {labels[status as keyof typeof labels]}
      </Badge>
    );
  };

  const handleSendTest = async () => {
    if (!selectedTest || !selectedCandidate) {
      setSendError("Veuillez sélectionner un test et un candidat");
      return;
    }

    try {
      setIsSendingTest(true);
      setSendError("");
      setSendSuccess("");

      const requestData = {
        candidatId: parseInt(selectedCandidate),
        testId: parseInt(selectedTest),
        message: message
      };

      console.log('📤 Sending test assignment:', requestData);

      const response = await apiconfig.post('/test-assignments/send', requestData);
      
      console.log('✅ Test sent successfully:', response.data);
      
      const candidateName = candidates.find((c: any) => c.id.toString() === selectedCandidate);
      const testName = availableTests.find((t: any) => t.id?.toString() === selectedTest);
      
      setSendSuccess(
        `Test "${testName?.name}" envoyé avec succès à  ${candidateName?.nom} !`
      );
      
      // Refresh sent tests after successful send
      fetchSentTests();
      
      // Reset form after successful send
      setSelectedTest("");
      setSelectedCandidate("");
      setCandidateEmail("");
      setMessage("Bonjour, voici votre test technique. Bonne chance !");
      
    } catch (error: any) {
      console.error('❌ Failed to send test:', error);
      
      let errorMessage = "Erreur lors de l'envoi du test";
      
      if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      setSendError(errorMessage);
    } finally {
      setIsSendingTest(false);
    }
  };

  const formatCandidateName = (candidat: any) => {
    return `${candidat.prenom} ${candidat.nom}`;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Tests & Évaluations</h1>
        <p className="text-muted-foreground mt-2">
          Gérez votre bibliothèque de tests, créez de nouveaux contenus et suivez les résultats
        </p>
      </div>

      <Tabs value={activeTab} onValueChange={handleTabChange} className="w-full">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="library">Bibliothèque de Tests</TabsTrigger>
          <TabsTrigger value="exercises">Exercices</TabsTrigger>
          <TabsTrigger value="create">Créer un Test</TabsTrigger>
          <TabsTrigger value="create-ia">Générer un Test par IA</TabsTrigger>
          <TabsTrigger value="send">Envoyer & Suivre</TabsTrigger>
        </TabsList>
        
        <TabsContent value="library" className="space-y-6">
          <TestLibrary />
        </TabsContent>
        
        <TabsContent value="exercises" className="space-y-6">
          <ExerciseLibrary />
        </TabsContent>
        
        <TabsContent value="create" className="space-y-6">
          <TestCreator />
        </TabsContent>
        
        <TabsContent value="create-ia" className="space-y-6">
          <TestCreatorIa />
        </TabsContent>
        
        <TabsContent value="send" className="space-y-6">
          <div className="grid lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>Envoyer un test</span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleManualRefresh}
                    disabled={isLoadingTests || isLoadingCandidates}
                    className="h-8 w-8 p-0"
                  >
                    <RefreshCw className={`h-4 w-4 ${(isLoadingTests || isLoadingCandidates) ? 'animate-spin' : ''}`} />
                  </Button>
                </CardTitle>
                <CardDescription>
                  Sélectionnez un test et un candidat
                  <span className="block text-xs text-muted-foreground/70 mt-1">
                    Dernière actualisation: {new Date(lastRefresh).toLocaleTimeString('fr-FR')}
                  </span>
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Messages de succès/erreur */}
                {sendSuccess && (
                  <div className="flex items-center gap-2 p-3 bg-green-50 border border-green-200 rounded-md text-green-800">
                    <CheckCircle className="h-4 w-4" />
                    <span className="text-sm">{sendSuccess}</span>
                  </div>
                )}
                
                {sendError && (
                  <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-md text-red-800">
                    <AlertCircle className="h-4 w-4" />
                    <span className="text-sm">{sendError}</span>
                  </div>
                )}

                {/* Sélection du test */}
                <div className="space-y-2">
                  <label className="text-sm font-medium">Choisir un test *</label>
                  <Select value={selectedTest} onValueChange={setSelectedTest}>
                    <SelectTrigger disabled={isLoadingTests}>
                      <SelectValue placeholder={
                        isLoadingTests 
                          ? "Chargement des tests..." 
                          : availableTests.length === 0 
                            ? "Aucun test publié disponible"
                            : "Sélectionner un test"
                      } />
                    </SelectTrigger>
                    <SelectContent>
                      {isLoadingTests ? (
                        <div className="flex items-center justify-center p-4">
                          <Loader2 className="h-4 w-4 animate-spin mr-2" />
                          <span className="text-sm text-muted-foreground">Chargement...</span>
                        </div>
                      ) : availableTests.length === 0 ? (
                        <div className="p-4 text-center space-y-3">
                          <span className="text-sm text-muted-foreground">
                            Aucun test publié disponible
                          </span>
                          <div className="text-xs text-yellow-700 bg-yellow-100 border border-yellow-200 rounded-md p-2">
                            ⚠️ Des tests existent, mais ils sont encore en <strong>brouillon</strong> ou <strong>archivés</strong>.<br />
                            Modifiez leur statut en <strong>"Publié"</strong> pour les rendre visibles ici.
                          </div>
                          <Button 
                            variant="outline" 
                            size="sm" 
                            onClick={() => fetchTests()}
                            className="w-full"
                          >
                            <RefreshCw className="h-3 w-3 mr-1" />
                            Actualiser
                          </Button>
                        </div>
                      ) : (
                        availableTests.map((test: any) => (
                          <SelectItem key={test.id} value={test.id?.toString() || ""}>
                            <div className="flex flex-col">
                              <span className="font-medium">{test.name}</span>
                            </div>
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                </div>

                {/* Sélection du candidat */}
                <div className="space-y-2">
                  <label className="text-sm font-medium flex items-center gap-2">
                    <Users className="h-4 w-4" />
                    Candidat *
                  </label>
                  <Select value={selectedCandidate} onValueChange={setSelectedCandidate}>
                    <SelectTrigger disabled={isLoadingCandidates}>
                      <SelectValue placeholder={
                        isLoadingCandidates 
                          ? "Chargement des candidats..." 
                          : candidates.length === 0 
                            ? "Aucun candidat disponible"
                            : "Sélectionner un candidat"
                      } />
                    </SelectTrigger>
                    <SelectContent>
                      {isLoadingCandidates ? (
                        <div className="flex items-center justify-center p-4">
                          <Loader2 className="h-4 w-4 animate-spin mr-2" />
                          <span className="text-sm text-muted-foreground">Chargement...</span>
                        </div>
                      ) : candidates.length === 0 ? (
                        <div className="p-4 text-center space-y-3">
                          <span className="text-sm text-muted-foreground">
                            Aucun candidat disponible
                          </span>
                          <Button 
                            variant="outline" 
                            size="sm" 
                            onClick={() => fetchCandidates()}
                            className="w-full"
                          >
                            <RefreshCw className="h-3 w-3 mr-1" />
                            Actualiser
                          </Button>
                        </div>
                      ) : (
                        candidates.map((candidate: any) => (
                          <SelectItem key={candidate.id} value={candidate.id.toString()}>
                            <div className="flex flex-col">
                              <span className="font-medium">{candidate.nom}</span>
                              <span className="text-xs text-muted-foreground">{candidate.email}</span>
                            </div>
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                </div>

                {/* Email du candidat (affiché automatiquement) */}
                <div className="space-y-2">
                  <label className="text-sm font-medium">Email du candidat</label>
                  <Input
                    type="email"
                    value={candidateEmail}
                    onChange={(e) => setCandidateEmail(e.target.value)}
                    placeholder="candidat@example.com"
                    disabled
                    className="bg-muted"
                  />
                </div>

                {/* Message personnalisé */}
                <div className="space-y-2">
                  <label className="text-sm font-medium">Message personnalisé</label>
                  <Textarea
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Message à envoyer au candidat..."
                    rows={3}
                  />
                </div>

                {/* Aperçu du test sélectionné */}
                {selectedTest && availableTests.length > 0 && (
                  <Card className="bg-muted/30">
                    <CardContent className="p-4">
                      <h4 className="font-medium text-sm mb-2">Aperçu du test sélectionné :</h4>
                      {(() => {
                        const test = availableTests.find((t: any) => t.id?.toString() === selectedTest);
                        if (!test) return null;
                        return (
                          <div className="space-y-2 text-sm text-muted-foreground">
                            <div className="flex items-center gap-4">
                              <span className="flex items-center gap-1">
                                <Clock className="h-3 w-3" />
                                {test.totalDuration} min
                              </span>
                              <span>{test.totalPoints} points</span>
                              <Badge variant="outline" className="text-xs">
                                {test.difficulty}
                              </Badge>
                            </div>
                            <p className="text-xs">{test.description}</p>
                            {test.exercises && test.exercises.length > 0 && (
                              <p className="text-xs">
                                {test.exercises.length} exercice{test.exercises.length > 1 ? 's' : ''}
                              </p>
                            )}
                          </div>
                        );
                      })()}
                    </CardContent>
                  </Card>
                )}

                <Button 
                  onClick={handleSendTest}
                  disabled={!selectedTest || !selectedCandidate || isLoadingTests || isLoadingCandidates || isSendingTest}
                  className="w-full"
                >
                  {isSendingTest ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Envoi en cours...
                    </>
                  ) : (
                    <>
                      <Send className="h-4 w-4 mr-2" />
                      Envoyer le test
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>Tests envoyés</span>
                  {isLoadingSentTests && <Loader2 className="h-4 w-4 animate-spin" />}
                </CardTitle>
                <CardDescription>
                  Suivi des tests en cours et terminés
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {isLoadingSentTests ? (
                    <div className="flex items-center justify-center p-8">
                      <Loader2 className="h-6 w-6 animate-spin mr-2" />
                      <span className="text-sm text-muted-foreground">Chargement des tests envoyés...</span>
                    </div>
                  ) : sentTests.length === 0 ? (
                    <div className="text-center p-8 text-muted-foreground">
                      <Send className="h-8 w-8 mx-auto mb-2 opacity-50" />
                      <p className="text-sm">Aucun test envoyé pour le moment</p>
                    </div>
                  ) : (
                    sentTests.map((test: any) => (
                      <Card key={test.id} className="p-4">
                        <div className="flex justify-between items-start">
                          <div className="space-y-2">
                            <div className="flex items-center gap-2">
                              <h3 className="font-semibold">{test.candidat.nom}</h3>
                              {getStatusBadge(test.status)}
                            </div>
                            <p className="text-sm text-muted-foreground">{test.test.name}</p>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                              <span>Envoyé le {new Date(test.sentAt).toLocaleDateString('fr-FR')}</span>
                              {test.validUntil && (
                                <span>Valide jusqu'au {new Date(test.validUntil).toLocaleDateString('fr-FR')}</span>
                              )}
                            </div>
                            {test.completedAt && (
                              <div className="text-sm text-muted-foreground">
                                Terminé le {new Date(test.completedAt).toLocaleDateString('fr-FR')}
                              </div>
                            )}
                            {test.totalScore !== undefined && (
                              <div className="flex items-center gap-2">
                                <span className="text-sm font-medium">Score:</span>
                                <Progress value={test.totalScore} className="w-24" />
                                <span className="text-sm font-semibold">{test.score}/100</span>
                              </div>
                            )}
                          </div>
                          {test.status === "COMPLETED" && (
                            <Button 
                              variant="outline" 
                              size="sm"
                              onClick={() => fetchResults(test)}   
                              disabled={loadingResults}
                            >
                              {loadingResults ? (
                                <>
                                  <Loader className="h-4 w-4 mr-2 animate-spin" />
                                  Chargement...
                                </>
                              ) : (
                                <>
                                  <CheckCircle className="h-4 w-4 mr-2" />
                                  Voir les résultats
                                </>
                              )}
                            </Button>
                          )}
                        </div>
                      </Card>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>

      {/* Modal Popup - MOVED OUTSIDE OF THE MAP FUNCTION */}
      {showModal && modalResults && Array.isArray(modalResults) && modalResults.length > 0 && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-2xl max-w-md w-full mx-4 p-6">
            {/* Header du modal */}
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-gray-800 flex items-center">
                <Trophy className="h-6 w-6 mr-2 text-yellow-500" />
                Résultats du test
              </h2>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* Contenu des résultats - PREMIER ÉLÉMENT SEULEMENT */}
            <div className="space-y-4">
              {(() => {
                const result = modalResults[0]; // Premier (et probablement unique) résultat
                
                return (
                  <>
                    {/* Titre du test */}
                    <div className="text-center">
                      <h3 className="text-lg font-semibold text-gray-800 mb-2">
                        {result.testTitle}
                      </h3>
                      <p className="text-sm text-gray-600">
                        Candidat: {result.candidatName}
                      </p>
                    </div>

                    {/* Score principal */}
                    <div className="text-center p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg">
                      <div className="text-3xl font-bold text-blue-600 mb-1">
                        {result.totalScore}/{result.totalPossibleScore}
                      </div>
                      <div className="text-sm text-gray-600">Points obtenus</div>
                    </div>

                    {/* Grille des détails */}
                    <div className="grid grid-cols-2 gap-3">
                      <div className="text-center p-3 bg-gray-50 rounded-lg">
                        <div className={`text-2xl font-bold ${result.percentage >= 60 ? 'text-green-600' : 'text-red-600'}`}>
                          {result.percentage}%
                        </div>
                        <div className="text-xs text-gray-600">Pourcentage</div>
                      </div>
                      
                      <div className="text-center p-3 bg-gray-50 rounded-lg">
                        <div className="text-2xl font-bold text-green-600">
                          {result.questionResults?.filter(q => q.isCorrect).length || 0}/{result.questionResults?.length || 0}
                        </div>
                        <div className="text-xs text-gray-600">Bonnes réponses</div>
                      </div>
                    </div>

                    {/* Statut */}
                    <div className={`text-center p-4 rounded-lg ${result.percentage >= 60 ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
                      <div className={`text-lg font-bold flex items-center justify-center ${result.percentage >= 60 ? 'text-green-600' : 'text-red-600'}`}>
                        {result.percentage >= 60 ? (
                          <>
                            <CheckCircle className="h-5 w-5 mr-2" />
                            Test Réussi !
                          </>
                        ) : (
                          <>
                            <XCircle className="h-5 w-5 mr-2" />
                            Test Échoué
                          </>
                        )}
                      </div>
                      <div className="text-sm text-gray-600 mt-1">
                        {result.percentage >= 60 ? 'Félicitations pour votre réussite !' : 'Continuez vos efforts !'}
                      </div>
                    </div>
                  </>
                );
              })()}
            </div>

            {/* Footer du modal */}
            <div className="mt-6 flex justify-end">
              <Button
                onClick={() => setShowModal(false)}
                className="px-6"
              >
                Fermer
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}