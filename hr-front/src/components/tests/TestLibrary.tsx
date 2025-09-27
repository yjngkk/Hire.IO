import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Clock, FileText, Eye, Edit, Trash2, Save, X, Loader2, Plus, Search } from "lucide-react";
import { Test, testApi, TestResponseDTO } from "@/components/api/testApi";
import { Exercise, exerciseApi } from "@/components/api/exerciseApi"; 
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";

// Listes des options de filtres
const domains = ["Tous", "Développement Frontend", "Développement Backend", "Algorithmique", "Base de données", "Soft Skills"];
const themes = ["Tous", "JavaScript", "React", "Python", "SQL", "Tri", "Personnalité"];
const difficulties = ["Tous", "Débutant", "Intermédiaire", "Avancé", "Tous niveaux"];

// Use TestResponseDTO directly since that's what the API returns
interface TestWithExercises extends TestResponseDTO {
  exercises: Exercise[];
}

export function TestLibrary() {
  const [tests, setTests] = useState<TestWithExercises[]>([]);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [viewingTest, setViewingTest] = useState<TestWithExercises | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editedTest, setEditedTest] = useState<TestWithExercises | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [testToDelete, setTestToDelete] = useState<TestWithExercises | null>(null);
  const [isLoadingExerciseDetails, setIsLoadingExerciseDetails] = useState(false);
  const [originalTest, setOriginalTest] = useState<TestWithExercises | null>(null);
  const [pendingExerciseChanges, setPendingExerciseChanges] = useState<{
    toAdd: Exercise[];
    toRemove: number[];
  }>({ toAdd: [], toRemove: [] });
  
  // États pour la gestion des exercices
  const [isAddExerciseDialogOpen, setIsAddExerciseDialogOpen] = useState(false);
  const [availableExercises, setAvailableExercises] = useState<Exercise[]>([]);
  
  // États pour les filtres de recherche d'exercices
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedDomain, setSelectedDomain] = useState("Tous");
  const [selectedTheme, setSelectedTheme] = useState("Tous");
  const [selectedDifficulty, setSelectedDifficulty] = useState("Tous");
  
  const [isLoadingAvailableExercises, setIsLoadingAvailableExercises] = useState(false);
  const [isAddingExercise, setIsAddingExercise] = useState<number | null>(null);
  const [isRemovingExercise, setIsRemovingExercise] = useState<number | null>(null);
  const [exerciseError, setExerciseError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTests = async () => {
      try {
        setIsLoading(true);
        const data = await testApi.getAllTests();
        setTests(data as TestWithExercises[]);
      } catch (error) {
        console.error("Error fetching tests:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTests();
  }, []);

  // Charger les exercices disponibles
  const loadAvailableExercises = async () => {
    if (!viewingTest) return;
    
    try {
      setIsLoadingAvailableExercises(true);
      setExerciseError(null);
      
      const allExercises = await exerciseApi.getAllExercises();
      
      // Filter exercises based on current state (including pending changes)
      const currentExerciseIds = editedTest?.exercises.map(e => e.id).filter(id => id !== undefined) || [];
      const pendingAddIds = pendingExerciseChanges.toAdd.map(e => e.id).filter(id => id !== undefined);
      const exercisesToExclude = [...currentExerciseIds, ...pendingAddIds];
      
      // Include exercises that are marked for removal
      const exercisesToInclude = pendingExerciseChanges.toRemove;
      
      const available = allExercises.filter(e => 
        e.id !== undefined && 
        (!exercisesToExclude.includes(e.id) || exercisesToInclude.includes(e.id))
      );
      
      setAvailableExercises(available);
    } catch (error) {
      setExerciseError("Erreur lors du chargement des exercices disponibles");
      console.error(error);
    } finally {
      setIsLoadingAvailableExercises(false);
    }
  };

  // Fonction pour réinitialiser les filtres
  const resetFilters = () => {
    setSearchTerm("");
    setSelectedDomain("Tous");
    setSelectedTheme("Tous");
    setSelectedDifficulty("Tous");
  };

  // Fonction pour filtrer les exercices disponibles
  const getFilteredAvailableExercises = () => {
    let filtered = availableExercises;

    // Filtrage par terme de recherche (nom)
    if (searchTerm.trim()) {
      filtered = filtered.filter(exercise =>
        exercise.title.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filtrage par domaine
    if (selectedDomain !== "Tous") {
      filtered = filtered.filter(exercise => exercise.domain === selectedDomain);
    }

    // Filtrage par thème
    if (selectedTheme !== "Tous") {
      filtered = filtered.filter(exercise => exercise.theme === selectedTheme);
    }

    // Filtrage par difficulté
    if (selectedDifficulty !== "Tous") {
      filtered = filtered.filter(exercise => exercise.difficulty === selectedDifficulty);
    }

    return filtered;
  };

  // Ajouter un exercice au test
  const handleAddExerciseToTest = async (exercise: Exercise) => {
    if (!exercise.id || !viewingTest || !editedTest) return;
    
    try {
      setIsAddingExercise(exercise.id);
      setExerciseError(null);
      
      // Check if this exercise was previously marked for removal
      const wasMarkedForRemoval = pendingExerciseChanges.toRemove.includes(exercise.id);
      
      if (wasMarkedForRemoval) {
        // Remove from toRemove list instead of adding to toAdd
        setPendingExerciseChanges(prev => ({
          ...prev,
          toRemove: prev.toRemove.filter(id => id !== exercise.id)
        }));
      } else {
        // Add to pending additions
        setPendingExerciseChanges(prev => ({
          ...prev,
          toAdd: [...prev.toAdd, exercise]
        }));
      }
      
      // Update local state immediately for UI
      const updatedExercises = [...editedTest.exercises, exercise];
      const updatedTest = {
        ...editedTest,
        exercises: updatedExercises,
        totalPoints: updatedExercises.reduce((sum, ex) => sum + (ex.totalPoints || 0), 0),
        totalDuration: updatedExercises.reduce((sum, ex) => sum + (ex.duration || 0), 0)
      };
      
      setEditedTest(updatedTest);
      setViewingTest(updatedTest);
      
      // Remove from available exercises
      setAvailableExercises(prev => prev.filter(e => e.id !== exercise.id));
      
    } catch (error: any) {
      setExerciseError(error.message || "Erreur lors de l'ajout de l'exercice");
    } finally {
      setIsAddingExercise(null);
    }
  };

  const handleRemoveExerciseFromTest = async (exerciseId: number) => {
    if (!viewingTest || !editedTest) return;
    
    try {
      setIsRemovingExercise(exerciseId);
      setExerciseError(null);
      
      const exerciseToRemove = editedTest.exercises.find(ex => ex.id === exerciseId);
      if (!exerciseToRemove) return;
      
      // Check if this exercise was in pending additions
      const wasAddedLocally = pendingExerciseChanges.toAdd.some(ex => ex.id === exerciseId);
      
      if (wasAddedLocally) {
        // Remove from toAdd list instead of adding to toRemove
        setPendingExerciseChanges(prev => ({
          ...prev,
          toAdd: prev.toAdd.filter(ex => ex.id !== exerciseId)
        }));
      } else {
        // Add to pending removals
        setPendingExerciseChanges(prev => ({
          ...prev,
          toRemove: [...prev.toRemove, exerciseId]
        }));
      }
      
      // Update local state immediately for UI
      const updatedExercises = editedTest.exercises.filter(ex => ex.id !== exerciseId);
      const updatedTest = {
        ...editedTest,
        exercises: updatedExercises,
        totalPoints: updatedExercises.reduce((sum, ex) => sum + (ex.totalPoints || 0), 0),
        totalDuration: updatedExercises.reduce((sum, ex) => sum + (ex.duration || 0), 0)
      };
      
      setEditedTest(updatedTest);
      setViewingTest(updatedTest);
      
      // If the add exercise dialog is open, reload available exercises
      if (isAddExerciseDialogOpen) {
        await loadAvailableExercises();
      }
      
    } catch (error: any) {
      setExerciseError(error.message || "Erreur lors de la suppression de l'exercice");
    } finally {
      setIsRemovingExercise(null);
    }
  };

  // Function to fetch complete exercise details including questions
  const fetchCompleteExerciseDetails = async (exercises: Exercise[]): Promise<Exercise[]> => {
    try {
      const exerciseDetailsPromises = exercises.map(async (exercise) => {
        if (exercise.id) {
          const completeExercise = await exerciseApi.getExerciseById(exercise.id);
          return completeExercise;
        }
        return exercise;
      });

      const completeExercises = await Promise.all(exerciseDetailsPromises);
      return completeExercises;
    } catch (error) {
      return exercises;
    }
  };

  // Helper functions
  const getAnswerText = (answer: string | any): string => {
    return typeof answer === 'string' ? answer : answer.answerText || '';
  };

  const getExerciseStats = (exercise: Exercise) => {
    const questionCount = exercise.questions?.length || 0;
    const totalPoints = exercise.totalPoints || 0;
    const duration = exercise.duration || 30;
    return { questionCount, totalPoints, duration };
  };

  const getStatusBadge = (status: string) => {
    const styles = {
      "Publié": "bg-green-100 text-green-800",
      "Brouillon": "bg-yellow-100 text-yellow-800",
      "Archivé": "bg-gray-100 text-gray-800"
    };
    
    return (
      <Badge className={styles[status as keyof typeof styles] || "bg-gray-100 text-gray-800"}>
        {status}
      </Badge>
    );
  };

  const getDifficultyBadge = (difficulty: string) => {
    const styles = {
      "Débutant": "bg-blue-100 text-blue-800",
      "Intermédiaire": "bg-orange-100 text-orange-800",
      "Avancé": "bg-red-100 text-red-800",
      "Tous niveaux": "bg-gray-100 text-gray-800"
    };
    
    return (
      <Badge variant="outline" className={styles[difficulty as keyof typeof styles]}>
        {difficulty}
      </Badge>
    );
  };

  const getTypeBadge = (type: string) => {
    const styles = {
      "QCM": "bg-blue-100 text-blue-800",
      "Code": "bg-purple-100 text-purple-800",
      "Pratique": "bg-green-100 text-green-800"
    };
    
    return (
      <Badge className={styles[type as keyof typeof styles] || "bg-gray-100 text-gray-800"}>
        {type}
      </Badge>
    );
  };

  const handleViewTest = async (test: TestWithExercises) => {
    try {
      setIsLoadingExerciseDetails(true);
      setViewingTest(test);
      setIsViewOpen(true);
      setIsEditing(false);

      if (test.exercises && test.exercises.length > 0) {
        const completeExercises = await fetchCompleteExerciseDetails(test.exercises);
        
        const testWithCompleteExercises = {
          ...test,
          exercises: completeExercises
        };
        
        setViewingTest(testWithCompleteExercises);
        setEditedTest({...testWithCompleteExercises});
        
      } else {
        setEditedTest({...test});
      }
    } catch (error) {
      setEditedTest({...test});
    } finally {
      setIsLoadingExerciseDetails(false);
    }
  };
   
  const handleEditTest = async (test: TestWithExercises) => {
    try {
      setIsLoadingExerciseDetails(true);
      setViewingTest(test);
      setIsViewOpen(true);
      setIsEditing(true);

      if (test.exercises && test.exercises.length > 0) {
        const completeExercises = await fetchCompleteExerciseDetails(test.exercises);
        
        const testWithCompleteExercises = {
          ...test,
          exercises: completeExercises
        };
        
        setViewingTest(testWithCompleteExercises);
        setEditedTest({...testWithCompleteExercises});
        setOriginalTest({...testWithCompleteExercises}); // Store original state
        
      } else {
        setEditedTest({...test});
        setOriginalTest({...test}); // Store original state
      }
      
      // Reset pending changes
      setPendingExerciseChanges({ toAdd: [], toRemove: [] });
      
    } catch (error) {
      setEditedTest({...test});
      setOriginalTest({...test});
    } finally {
      setIsLoadingExerciseDetails(false);
    }
  };

  const handleStartEdit = () => {
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    if (originalTest) {
      setEditedTest({...originalTest});
      setViewingTest({...originalTest});
    }
    setPendingExerciseChanges({ toAdd: [], toRemove: [] });
    setIsEditing(false);
  };

  const handleSaveEdit = async () => {
    if (!editedTest || !editedTest.id) return;

    try {
      setIsSaving(true);
      
      // First, apply all pending exercise changes
      let currentTestId = editedTest.id;
      
      // Remove exercises first
      for (const exerciseId of pendingExerciseChanges.toRemove) {
        await testApi.removeExerciseFromTest(currentTestId, exerciseId);
      }
      
      // Add exercises
      for (const exercise of pendingExerciseChanges.toAdd) {
        if (exercise.id) {
          await testApi.addExerciseToTest(currentTestId, exercise.id);
        }
      }
      
      // Then update the test basic information
      const testToUpdate: Test = {
        id: editedTest.id,
        name: editedTest.name,
        description: editedTest.description,
        totalDuration: editedTest.totalDuration,
        difficulty: editedTest.difficulty,
        totalPoints: editedTest.totalPoints,
        status: editedTest.status,
        createdAt: editedTest.createdAt,
        exercises: editedTest.exercises,
        questions: [] 
      };
      
      const updatedTest = await testApi.updateTest(editedTest.id, testToUpdate);
      
      // Fetch complete exercise details for the final result
      let completeExercises: Exercise[] = [];
      if (updatedTest.exercises && updatedTest.exercises.length > 0) {
        completeExercises = await fetchCompleteExerciseDetails(updatedTest.exercises);
      }
      
      const finalTest: TestWithExercises = {
        ...updatedTest,
        exercises: completeExercises
      };
      
      // Update all states with the final result
      setTests(prevTests => 
        prevTests.map(test => 
          test.id === editedTest.id ? finalTest : test
        )
      );
      
      setViewingTest(finalTest);
      setEditedTest(finalTest);
      setOriginalTest(finalTest);
      
      // Reset pending changes
      setPendingExerciseChanges({ toAdd: [], toRemove: [] });
      setIsEditing(false);
      
    } catch (error) {
      console.error("Error saving test:", error);
      alert("Erreur lors de la sauvegarde du test. Veuillez réessayer.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteClick = (test: TestWithExercises) => {
    setTestToDelete(test);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!testToDelete?.id) return;

    try {
      setIsDeleting(true);      
      await testApi.deleteTest(testToDelete.id);
      
      setTests(prevTests => 
        prevTests.filter(test => test.id !== testToDelete.id)
      );
      if (viewingTest?.id === testToDelete.id) {
        setIsViewOpen(false);
        setViewingTest(null);
        setEditedTest(null);
        setIsEditing(false);
      }
      
      setDeleteDialogOpen(false);
      setTestToDelete(null);
      
    } catch (error) {
      alert("Erreur lors de la suppression du test. Veuillez réessayer.");
    } finally {
      setIsDeleting(false);
    }
  };

  const handleFieldChange = (field: keyof TestWithExercises, value: any) => {
    if (editedTest) {
      setEditedTest({
        ...editedTest,
        [field]: value
      });
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="h-8 w-8 animate-spin" />
        <span className="ml-2">Chargement des tests...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold">Bibliothèque de Tests</h3>
          <p className="text-sm text-muted-foreground">
            {tests.length} tests disponibles
          </p>
        </div>
      </div>

      <div className="grid gap-4">
        {tests.map((test) => (
          <Card key={test.id}>
            <CardHeader>
              <div className="flex justify-between items-start">
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <CardTitle className="text-lg">{test.name}</CardTitle>
                    {getStatusBadge(test.status)}
                  </div>
                  <CardDescription>{test.description}</CardDescription>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm"
                      onClick={() => handleViewTest(test)}
                      disabled={isLoadingExerciseDetails}
                    >
                    {isLoadingExerciseDetails ? (
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Eye className="h-4 w-4 mr-2" />
                    )}
                    Voir
                  </Button>
                  
                  <Button variant="outline" size="sm"
                      onClick={() => handleEditTest(test)}
                      disabled={isLoadingExerciseDetails}
                    >
                    {isLoadingExerciseDetails ? (
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Edit className="h-4 w-4 mr-2" />
                    )}
                    Modifier
                  </Button>
                  
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                    onClick={() => handleDeleteClick(test)}
                    disabled={isDeleting}
                  >
                    {isDeleting && testToDelete?.id === test.id ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <Trash2 className="h-4 w-4" />
                    )}
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
                <div className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  <span>{test.totalDuration} min</span>
                </div>
                <div className="flex items-center gap-1">
                  <FileText className="h-3 w-3" />
                  <span>{test.totalPoints} points</span>
                </div>
                <span>Créé le {new Date(test.createdAt || '').toLocaleDateString('fr-FR')}</span>
              </div>
              
              <div className="flex items-center gap-2 flex-wrap">
                {getDifficultyBadge(test.difficulty)}
                {test.exercises && Array.isArray(test.exercises) && test.exercises.length > 0 && (
                  <Badge variant="secondary">
                    {test.exercises.length} exercice{test.exercises.length > 1 ? 's' : ''}
                  </Badge>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirmer la suppression</AlertDialogTitle>
            <AlertDialogDescription>
              Êtes-vous sûr de vouloir supprimer le test "<strong>{testToDelete?.name}</strong>" ? 
              Cette action est irréversible et supprimera définitivement le test et tous ses exercices associés.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>
              Annuler
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
              className="bg-red-600 hover:bg-red-700"
            >
              {isDeleting ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Suppression...
                </>
              ) : (
                <>
                  <Trash2 className="h-4 w-4 mr-2" />
                  Supprimer
                </>
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Dialog d'aperçu/modification du test */}
      {isViewOpen && viewingTest && editedTest && (
        <Dialog open={isViewOpen} onOpenChange={setIsViewOpen}>
          <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <div className="flex justify-between items-start">
                <div>
                  <DialogTitle className="text-xl font-bold">
                    {isEditing ? "Modifier le test" : "Aperçu du test"} - {viewingTest.name}
                  </DialogTitle>
                  <p className="text-muted-foreground text-sm">
                    Créé le {new Date(viewingTest.createdAt || '').toLocaleDateString('fr-FR')}
                  </p>
                </div>
                <div className="flex gap-2">
                  {!isEditing ? (
                    <Button onClick={handleStartEdit} size="sm">
                      <Edit className="h-4 w-4 mr-2" />
                      Modifier
                    </Button>
                  ) : (
                    <>
                      <Button 
                        onClick={handleSaveEdit} 
                        size="sm"
                        disabled={isSaving}
                      >
                        {isSaving ? (
                          <>
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            Sauvegarde...
                          </>
                        ) : (
                          <>
                            <Save className="h-4 w-4 mr-2" />
                            Sauvegarder
                          </>
                        )}
                      </Button>
                      <Button 
                        onClick={handleCancelEdit} 
                        variant="outline" 
                        size="sm"
                        disabled={isSaving}
                      >
                        <X className="h-4 w-4 mr-2" />
                        Annuler
                      </Button>
                    </>
                  )}
                </div>
              </div>
            </DialogHeader>

            {isLoadingExerciseDetails ? (
              <div className="flex justify-center items-center py-8">
                <Loader2 className="h-8 w-8 animate-spin mr-2" />
                <span>Chargement des détails des exercices...</span>
              </div>
            ) : (
              <div className="space-y-6">
                {/* Informations générales du test - Mode éditable */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Informations générales</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      {/* Nom du test */}
                      <div>
                        <p className="text-sm font-medium text-muted-foreground mb-2">Nom du test</p>
                        {isEditing ? (
                          <Input
                            value={editedTest.name}
                            onChange={(e) => handleFieldChange('name', e.target.value)}
                            placeholder="Nom du test..."
                          />
                        ) : (
                          <p className="bg-muted px-3 py-2 rounded text-sm font-medium">{viewingTest.name}</p>
                        )}
                      </div>

                      {/* Description */}
                      <div>
                        <p className="text-sm font-medium text-muted-foreground mb-2">Description</p>
                        {isEditing ? (
                          <Textarea
                            value={editedTest.description}
                            onChange={(e) => handleFieldChange('description', e.target.value)}
                            placeholder="Description du test..."
                            className="min-h-[100px]"
                          />
                        ) : (
                          <p className="bg-muted px-3 py-2 rounded text-sm">{viewingTest.description}</p>
                        )}
                      </div>

                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {/* Difficulté */}
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Difficulté</p>
                          {isEditing ? (
                            <Select 
                              value={editedTest.difficulty} 
                              onValueChange={(value) => handleFieldChange('difficulty', value)}
                            >
                              <SelectTrigger>
                                <SelectValue placeholder="Sélectionner..." />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="Débutant">Débutant</SelectItem>
                                <SelectItem value="Intermédiaire">Intermédiaire</SelectItem>
                                <SelectItem value="Avancé">Avancé</SelectItem>
                                <SelectItem value="Tous niveaux">Tous niveaux</SelectItem>
                              </SelectContent>
                            </Select>
                          ) : (
                            getDifficultyBadge(viewingTest.difficulty)
                          )}
                        </div>

                        {/* Durée totale */}
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Durée totale (min)</p>
                          {isEditing ? (
                            <Input
                              type="number"
                              value={editedTest.totalDuration}
                              onChange={(e) => handleFieldChange('totalDuration', parseInt(e.target.value) || 0)}
                              min="1"
                              max="300"
                            />
                          ) : (
                            <p className="text-sm font-semibold">{viewingTest.totalDuration} minutes</p>
                          )}
                        </div>

                        {/* Points totaux */}
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Points totaux</p>
                          {isEditing ? (
                            <Input
                              type="number"
                              value={editedTest.totalPoints}
                              onChange={(e) => handleFieldChange('totalPoints', parseInt(e.target.value) || 0)}
                              min="1"
                              max="1000"
                            />
                          ) : (
                            <p className="text-sm font-semibold">{viewingTest.totalPoints} pts</p>
                          )}
                        </div>

                        {/* Statut */}
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Statut</p>
                          {isEditing ? (
                            <Select 
                              value={editedTest.status} 
                              onValueChange={(value) => handleFieldChange('status', value)}
                            >
                              <SelectTrigger>
                                <SelectValue placeholder="Sélectionner..." />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="Brouillon">Brouillon</SelectItem>
                                <SelectItem value="Publié">Publié</SelectItem>
                                <SelectItem value="Archivé">Archivé</SelectItem>
                              </SelectContent>
                            </Select>
                          ) : (
                            getStatusBadge(viewingTest.status)
                          )}
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Section Exercices avec gestion ajout/suppression */}
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <h4 className="text-lg font-semibold">
                      Exercices du test ({viewingTest.exercises?.length || 0})
                    </h4>
                    {isEditing && (
                      <Button 
                        onClick={() => {
                          setIsAddExerciseDialogOpen(true);
                          resetFilters(); // Réinitialiser les filtres à l'ouverture
                          loadAvailableExercises();
                        }}
                        size="sm"
                      >
                        <Plus className="h-4 w-4 mr-2" />
                        Ajouter un exercice
                      </Button>
                    )}
                  </div>

                  {/* Message d'erreur pour les exercices */}
                  {exerciseError && (
                    <Alert variant="destructive">
                      <AlertDescription>{exerciseError}</AlertDescription>
                    </Alert>
                  )}
                  
                  {viewingTest.exercises && Array.isArray(viewingTest.exercises) && viewingTest.exercises.length > 0 ? (
                    viewingTest.exercises.map((exercise, exerciseIndex) => {
                      if (!exercise || typeof exercise !== 'object' || !exercise.title) {
                        return null;
                      }
                      
                      const stats = getExerciseStats(exercise);
                      return (
                        <Card key={exercise.id || exerciseIndex} className="border-l-4 border-l-blue-500">
                          <CardHeader>
                            <div className="flex justify-between items-start">
                              <div>
                                <CardTitle className="text-lg">
                                  Exercice {exerciseIndex + 1}: {exercise.title}
                                </CardTitle>
                                <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                                  <span>{stats.duration} min</span>
                                  <span>{stats.totalPoints} points</span>
                                  <span>{stats.questionCount} question{stats.questionCount > 1 ? 's' : ''}</span>
                                </div>
                              </div>
                              <div className="flex items-center gap-2">
                                <div className="flex flex-col gap-2">
                                  {getTypeBadge(exercise.type)}
                                  {getDifficultyBadge(exercise.difficulty)}
                                </div>
                                {isEditing && exercise.id && (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                                    onClick={() => handleRemoveExerciseFromTest(exercise.id!)}
                                    disabled={isRemovingExercise === exercise.id}
                                  >
                                    {isRemovingExercise === exercise.id ? (
                                      <Loader2 className="h-4 w-4 animate-spin" />
                                    ) : (
                                      <Trash2 className="h-4 w-4" />
                                    )}
                                  </Button>
                                )}
                              </div>
                            </div>
                            
                            <div className="flex items-center gap-2 flex-wrap mt-2">
                              <Badge variant="secondary">{exercise.domain}</Badge>
                              <Badge variant="secondary">{exercise.theme}</Badge>
                            </div>
                          </CardHeader>

                          <CardContent>
                            {exercise.questions && exercise.questions.length > 0 ? (
                              <div className="space-y-4">
                                {exercise.questions.map((question, questionIndex) => (
                                  <Card key={questionIndex} className="bg-muted/30">
                                    <CardContent className="p-4">
                                      <div className="space-y-4">
                                        <div className="flex justify-between items-start">
                                          <h5 className="font-semibold">Question {questionIndex + 1}</h5>
                                          <Badge variant="outline">{question.points} points</Badge>
                                        </div>
                                        
                                        <p className="text-gray-700">{question.questionText}</p>
                                        
                                        <div className="space-y-2 ml-4">
                                          {question.answers.map((answer: string | any, answerIndex: number) => {
                                            const answerText = getAnswerText(answer);
                                            return (
                                              <div 
                                                key={answerIndex}
                                                className={`p-3 border rounded-md flex items-center gap-3 ${
                                                  question.correctAnswer === answerIndex 
                                                    ? 'bg-green-50 border-green-200' 
                                                    : 'bg-gray-50'
                                                }`}
                                              >
                                                <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${
                                                  question.correctAnswer === answerIndex
                                                    ? 'border-green-500 bg-green-500 text-white' 
                                                    : 'border-gray-300'
                                                }`}>
                                                  <span className="text-sm font-medium">
                                                    {String.fromCharCode(65 + answerIndex)}
                                                  </span>
                                                </div>
                                                <span className={
                                                  question.correctAnswer === answerIndex
                                                    ? 'font-medium text-green-800' 
                                                    : 'text-gray-700'
                                                }>
                                                  {answerText}
                                                </span>
                                              </div>
                                            );
                                          })}
                                        </div>
                                      </div>
                                    </CardContent>
                                  </Card>
                                ))}
                              </div>
                            ) : (
                              <p className="text-muted-foreground text-center py-4">
                                Aucune question disponible pour cet exercice
                              </p>
                            )}
                          </CardContent>
                        </Card>
                      );
                    })
                  ) : (
                    <Card>
                      <CardContent className="p-8 text-center">
                        <p className="text-muted-foreground">
                          Aucun exercice associé à ce test.
                          {isEditing && " Cliquez sur 'Ajouter un exercice' pour commencer."}
                        </p>
                      </CardContent>
                    </Card>
                  )}
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      )}

      {/* Dialog pour ajouter des exercices avec filtres améliorés */}
      {isAddExerciseDialogOpen && (
        <Dialog open={isAddExerciseDialogOpen} onOpenChange={setIsAddExerciseDialogOpen}>
          <DialogContent className="max-w-5xl max-h-[85vh]">
            <DialogHeader>
              <DialogTitle>Ajouter des exercices au test</DialogTitle>
            </DialogHeader>

            {/* Section des filtres */}
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <CardTitle className="text-base">Filtres de recherche</CardTitle>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    onClick={resetFilters}
                    className="text-xs"
                  >
                    Réinitialiser
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Première ligne : Recherche par nom */}
                <div>
                  <label className="text-sm font-medium mb-2 block">Rechercher par nom</label>
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                    <Input
                      placeholder="Nom de l'exercice..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="pl-10"
                    />
                  </div>
                </div>

                {/* Deuxième ligne : Filtres par catégories */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="text-sm font-medium mb-2 block">Domaine</label>
                    <Select value={selectedDomain} onValueChange={setSelectedDomain}>
                      <SelectTrigger>
                        <SelectValue placeholder="Sélectionner..." />
                      </SelectTrigger>
                      <SelectContent>
                        {domains.map((domain) => (
                          <SelectItem key={domain} value={domain}>
                            {domain}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <label className="text-sm font-medium mb-2 block">Thème</label>
                    <Select value={selectedTheme} onValueChange={setSelectedTheme}>
                      <SelectTrigger>
                        <SelectValue placeholder="Sélectionner..." />
                      </SelectTrigger>
                      <SelectContent>
                        {themes.map((theme) => (
                          <SelectItem key={theme} value={theme}>
                            {theme}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <label className="text-sm font-medium mb-2 block">Difficulté</label>
                    <Select value={selectedDifficulty} onValueChange={setSelectedDifficulty}>
                      <SelectTrigger>
                        <SelectValue placeholder="Sélectionner..." />
                      </SelectTrigger>
                      <SelectContent>
                        {difficulties.map((difficulty) => (
                          <SelectItem key={difficulty} value={difficulty}>
                            {difficulty}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {/* Indicateur du nombre de résultats */}
                <div className="text-sm text-muted-foreground">
                  {getFilteredAvailableExercises().length} exercice{getFilteredAvailableExercises().length > 1 ? 's' : ''} trouvé{getFilteredAvailableExercises().length > 1 ? 's' : ''}
                </div>
              </CardContent>
            </Card>

            {/* Liste des exercices disponibles */}
            <div className="overflow-y-auto max-h-[45vh] space-y-3">
              {isLoadingAvailableExercises ? (
                <div className="flex justify-center items-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin mr-2" />
                  <span>Chargement des exercices...</span>
                </div>
              ) : getFilteredAvailableExercises().length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <div className="space-y-2">
                    <p className="font-medium">Aucun exercice trouvé</p>
                    <p className="text-sm">
                      {searchTerm || selectedDomain !== "Tous" || selectedTheme !== "Tous" || selectedDifficulty !== "Tous" 
                        ? "Essayez de modifier vos critères de recherche"
                        : "Aucun exercice disponible pour ce test"
                      }
                    </p>
                  </div>
                </div>
              ) : (
                getFilteredAvailableExercises().map((exercise) => (
                  <Card key={exercise.id} className="hover:shadow-md transition-shadow">
                    <CardContent className="p-4">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h4 className="font-semibold">{exercise.title}</h4>
                            {getTypeBadge(exercise.type)}
                            {getDifficultyBadge(exercise.difficulty)}
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground mb-2">
                            <span>{exercise.duration} min</span>
                            <span>{exercise.totalPoints} points</span>
                            <span>{exercise.questions?.length || 0} question{(exercise.questions?.length || 0) > 1 ? 's' : ''}</span>
                          </div>
                          <div className="flex items-center gap-2 flex-wrap">
                            <Badge variant="secondary">{exercise.domain}</Badge>
                            <Badge variant="secondary">{exercise.theme}</Badge>
                          </div>
                        </div>
                        <Button
                          size="sm"
                          onClick={() => handleAddExerciseToTest(exercise)}
                          disabled={isAddingExercise === exercise.id}
                          className="shrink-0 ml-4"
                        >
                          {isAddingExercise === exercise.id ? (
                            <>
                              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                              Ajout...
                            </>
                          ) : (
                            <>
                              <Plus className="h-4 w-4 mr-2" />
                              Ajouter
                            </>
                          )}
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>

            <div className="flex justify-end pt-4 border-t">
              <Button 
                variant="outline" 
                onClick={() => {
                  setIsAddExerciseDialogOpen(false);
                  resetFilters();
                }}
              >
                <X className="h-4 w-4 mr-2" />
                Fermer
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}