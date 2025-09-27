import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Search, Plus, Eye, Edit, Trash2, RefreshCw } from "lucide-react";
import QCMExerciseForm from "@/components/tests/QCMExerciseForm";
import { exerciseApi, Exercise } from "@/components/api/exerciseApi";
import DeleteConfirmationDialog from "@/components/tests/DeleteConfirmationDialog";
import { toast } from "sonner";

const domains = ["Tous", "Développement Frontend", "Développement Backend", "Algorithmique", "Base de données", "Soft Skills"];
const themes = ["Tous", "JavaScript", "React", "Python", "SQL", "Tri", "Personnalité"];
const difficulties = ["Tous", "Débutant", "Intermédiaire", "Avancé", "Tous niveaux"];

export function ExerciseLibrary() {
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [filteredExercises, setFilteredExercises] = useState<Exercise[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedDomain, setSelectedDomain] = useState("Tous");
  const [selectedTheme, setSelectedTheme] = useState("Tous");
  const [selectedDifficulty, setSelectedDifficulty] = useState("Tous");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [editingExercise, setEditingExercise] = useState<Exercise | null>(null);
  const [viewingExercise, setViewingExercise] = useState<Exercise | null>(null);

  useEffect(() => {
    loadExercises();
  }, []);

  useEffect(() => {
    filterExercises();
  }, [exercises, searchTerm, selectedDomain, selectedTheme, selectedDifficulty]);

  const getAnswerText = (answer: string | any): string => {
    return typeof answer === 'string' ? answer : answer.answerText || '';
  };
  
  const loadExercises = async () => {
    try {
      setLoading(true);
      const data = await exerciseApi.getAllExercises();
      setExercises(data);
    } catch (error) {
      toast.error("Échec du chargement des exercices");
    } finally {
      setLoading(false);
    }
  };

  const filterExercises = () => {
    let filtered = exercises;

    if (searchTerm) {
      filtered = filtered.filter(exercise => 
        exercise.title.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (selectedDomain !== "Tous") {
      filtered = filtered.filter(exercise => exercise.domain === selectedDomain);
    }

    if (selectedTheme !== "Tous") {
      filtered = filtered.filter(exercise => exercise.theme === selectedTheme);
    }

    if (selectedDifficulty !== "Tous") {
      filtered = filtered.filter(exercise => exercise.difficulty === selectedDifficulty);
    }

    setFilteredExercises(filtered);
  };

  const handleExerciseCreated = async (exerciseData: any) => {
    try {
      await exerciseApi.createExercise(exerciseData);
      setIsFormOpen(false);
      await loadExercises();
      toast.success("Exercice créé avec succès");
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Erreur inconnue";
      toast.error(`Échec de la création: ${errorMessage}`);
    }
  };

  const handleExerciseUpdate = async (exerciseData: any) => {
    if (!editingExercise?.id) {
      toast.error("ID de l'exercice manquant");
      return;
    }
    
    try {
        const updateData = {
        ...exerciseData,
        id: editingExercise.id
      };
      
      await exerciseApi.updateExercise(editingExercise.id, updateData);
      setIsFormOpen(false);
      setEditingExercise(null);
      await loadExercises();
      toast.success("Exercice modifié avec succès");
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Erreur inconnue";
      toast.error(`Échec de la modification: ${errorMessage}`);
    }
  };

  const handleDeleteExercise = async (id: number) => {
    try {
      await exerciseApi.deleteExercise(id);
      await loadExercises();
      toast.success("Exercice supprimé avec succès");
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Erreur inconnue";
      toast.error(`Échec de la suppression: ${errorMessage}`);
    }
  };

 const handleEditExercise = async (exercise: Exercise) => {
  try {
    if (exercise.id) {
      const latestExercise = await exerciseApi.getExerciseById(exercise.id);
      setEditingExercise(latestExercise);
    } else {
      setEditingExercise(exercise);
    }
    setIsFormOpen(true);
  } catch (error) {
    toast.error("Impossible de charger l'exercice pour modification");
  }
};

  const handleViewExercise = (exercise: Exercise) => {
    setViewingExercise(exercise);
    setIsViewOpen(true);
  };

  const handleNewExercise = () => {
    setEditingExercise(null);
    setIsFormOpen(true);
  };

  const handleFormCancel = () => {
    setIsFormOpen(false);
    setEditingExercise(null);
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

  // Fixed function to get exercise stats directly from the exercise object
  const getExerciseStats = (exercise: Exercise) => {
    const questionCount = exercise.questions?.length || 0;
    const totalPoints = exercise.totalPoints || 0;
    const duration = exercise.duration || 30;
    return { questionCount, totalPoints, duration };
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <RefreshCw className="h-6 w-6 animate-spin mr-2" />
        <span>Chargement des exercices...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold">Banque d'exercices</h3>
          <p className="text-sm text-muted-foreground">
            {filteredExercises.length} exercice{filteredExercises.length > 1 ? 's' : ''} trouvé{filteredExercises.length > 1 ? 's' : ''}
          </p>
        </div>
        <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
          <DialogTrigger asChild>
            <Button onClick={handleNewExercise}>
              <Plus className="h-4 w-4 mr-2" />
              Nouvel exercice
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>
                {editingExercise ? "Modifier l'exercice" : "Créer un exercice"}
              </DialogTitle>
            </DialogHeader>
            <QCMExerciseForm 
              initialData={editingExercise}
              onSave={editingExercise ? handleExerciseUpdate : handleExerciseCreated}
              onCancel={handleFormCancel}
            />
          </DialogContent>
        </Dialog>
      </div>

      {/* Filtres */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Filtres</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-4 items-end">
            <div className="flex-1">
              <label className="text-sm font-medium">Rechercher</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Titre de l'exercice..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">Domaine</label>
              <Select value={selectedDomain} onValueChange={setSelectedDomain}>
                <SelectTrigger className="w-48">
                  <SelectValue />
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

            <div className="space-y-2">
              <label className="text-sm font-medium">Thème</label>
              <Select value={selectedTheme} onValueChange={setSelectedTheme}>
                <SelectTrigger className="w-48">
                  <SelectValue />
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

            <div className="space-y-2">
              <label className="text-sm font-medium">Niveau</label>
              <Select value={selectedDifficulty} onValueChange={setSelectedDifficulty}>
                <SelectTrigger className="w-48">
                  <SelectValue />
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
        </CardContent>
      </Card>

      {/* Liste des exercices */}
      <div className="grid gap-4">
        {filteredExercises.map((exercise) => {
          const stats = getExerciseStats(exercise);
          return (
            <Card key={exercise.id}>
              <CardContent className="p-4">
                <div className="flex justify-between items-start">
                  <div className="space-y-2 flex-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-semibold">{exercise.title}</h4>
                      {getTypeBadge(exercise.type)}
                    </div>
                    
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span>{stats.duration} min</span>
                      <span>{stats.totalPoints} points</span>
                      <span>{stats.questionCount} question{stats.questionCount > 1 ? 's' : ''}</span>
                    </div>
                    
                    <div className="flex items-center gap-2 flex-wrap">
                      <Badge variant="secondary">{exercise.domain}</Badge>
                      <Badge variant="secondary">{exercise.theme}</Badge>
                      {getDifficultyBadge(exercise.difficulty)}
                    </div>
                  </div>
                  
                  <div className="flex gap-2">
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleViewExercise(exercise)}
                    >
                      <Eye className="h-4 w-4 mr-2" />
                      Voir
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleEditExercise(exercise)}
                    >
                      <Edit className="h-4 w-4 mr-2" />
                      Modifier
                    </Button>
                    {exercise.id && (
                      <DeleteConfirmationDialog
                        onConfirm={() => handleDeleteExercise(exercise.id!)}
                        trigger={
                          <Button variant="outline" size="sm" className="text-red-600 hover:text-red-700">
                    <Trash2 className="h-4 w-4" />
                  </Button>
                        }
                      />
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          );
        })}
        
        {filteredExercises.length === 0 && !loading && (
          <Card>
            <CardContent className="p-8 text-center">
              <p className="text-muted-foreground">
                Aucun exercice trouvé avec ces critères de recherche.
              </p>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Dialog de visualisation */}
      <Dialog open={isViewOpen} onOpenChange={setIsViewOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Aperçu de l'exercice</DialogTitle>
          </DialogHeader>
          {viewingExercise && (
            <div className="space-y-6">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-xl font-semibold">{viewingExercise.title}</h3>
                  <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                    {(() => {
                      const stats = getExerciseStats(viewingExercise);
                      return (
                        <>
                          <span>{stats.duration} min</span>
                          <span>{stats.totalPoints} points</span>
                          <span>{stats.questionCount} question{stats.questionCount > 1 ? 's' : ''}</span>
                        </>
                      );
                    })()}
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  {getTypeBadge(viewingExercise.type)}
                  {getDifficultyBadge(viewingExercise.difficulty)}
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                <Badge variant="secondary">{viewingExercise.domain}</Badge>
                <Badge variant="secondary">{viewingExercise.theme}</Badge>
              </div>
              
              {viewingExercise.questions && viewingExercise.questions.length > 0 ? (
                <div className="space-y-6">
                  {viewingExercise.questions.map((question, index) => (
                    <Card key={index}>
                      <CardContent className="p-4">
                        <div className="space-y-4">
                          <div className="flex justify-between items-start">
                            <h4 className="font-semibold">Question {index + 1}</h4>
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
                <p className="text-muted-foreground">Aucune question disponible</p>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}