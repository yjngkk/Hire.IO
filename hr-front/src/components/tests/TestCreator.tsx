import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Plus, Wand2, X, Clock, FileText, RefreshCw } from "lucide-react";
import { exerciseApi, Exercise } from "@/components/api/exerciseApi";
import { Test,testApi } from "@/components/api/testApi";
import { toast } from "sonner";
import { Description } from "@radix-ui/react-toast";
interface TestCreatorProps {
  mode?: 'create' | 'edit';
  initialTest?: Test | null;
  onClose?: () => void;
}

export function TestCreator({ mode = 'create', initialTest, onClose }: TestCreatorProps) {

// export function TestCreator() {
  // Existing state
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [testName, setTestName] = useState("");
  const [testDescription, setTestDescription] = useState("");
  const [selectedExercises, setSelectedExercises] = useState<Exercise[]>([]);
  const [aiPrompt, setAiPrompt] = useState("");
  const [testDifficulty, setTestDifficulty] = useState("");
  const [testCategory, setTestCategory] = useState("");
  const [showPreview, setShowPreview] = useState(false);

  // New state for loading exercises
  const [availableExercises, setAvailableExercises] = useState<Exercise[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // Helper function to get auth headers (same as in exerciseApi.ts)
  const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  };
const categoriesList = [
    "Développement",
    "Sécurité",
    "Management", 
    "Design",
    "Marketing",
    "Ressources Humaines",
    "Finance",
    "Commercial",
    "Support Technique",
    "Qualité",
    "Data Science",
    "DevOps"
  ];
  useEffect(() => {
    
    loadExercises();
  }, []);
  useEffect(() => {
  if (mode === 'edit' && initialTest) {
    setTestName(initialTest.name || "");
    setTestDescription(initialTest.description || "");
    setTestDifficulty(initialTest.difficulty || "");
    setSelectedExercises(initialTest.exercises || []);
  }
}, [mode, initialTest]);


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

  const addExercise = (exercise: Exercise) => {
    if (!selectedExercises.find(e => e.id === exercise.id)) {
      setSelectedExercises([...selectedExercises, exercise]);
    }
  };

  const removeExercise = (exerciseId: number) => {
    setSelectedExercises(selectedExercises.filter(e => e.id !== exerciseId));
  };

  // Helper function to get exercise stats
  const getExerciseStats = (exercise: Exercise) => {
    const questionCount = exercise.questions?.length || 0;
    const totalPoints = exercise.totalPoints || 0;
    const duration = exercise.duration || 30;
    return { questionCount, totalPoints, duration };
  };

  const totalDuration = selectedExercises.reduce((sum, ex) => {
    const stats = getExerciseStats(ex);
    return sum + stats.duration;
  }, 0);

  const totalPoints = selectedExercises.reduce((sum, ex) => {
    const stats = getExerciseStats(ex);
    return sum + stats.totalPoints;
  }, 0);

  const handleGenerateWithAI = () => {
    //Implement AI generation here
  };

const handleSaveTest = async () => {
  if (!testName.trim()) {
    toast.error("Veuillez entrer un nom pour le test");
    return;
  }

  if (selectedExercises.length === 0) {
    toast.error("Veuillez sélectionner au moins un exercice");
    return;
  }

  // Create test data in the format expected by the Test interface
  const testData = {
    name: testName.trim(),
    description: testDescription.trim(),
    difficulty: testDifficulty,
    totalDuration,
    totalPoints,
    status: "Brouillon",
    exercises: selectedExercises,
    questions: [] ,
    categorie :testCategory
  };
  
  try {
    setSaving(true);
    // Use the new API method that handles DTO conversion internally
    const savedTest = await testApi.createTest(testData); 
    console.log("saved");
    
    console.log(savedTest);
    
    toast.success("Test créé avec succès!");
    
    // Reset form
    setTestName("");
    setTestDescription("");
    setTestDifficulty("");
    setSelectedExercises([]);
    
    if (onClose) {
      onClose();
    }
    
  } catch (error) {
    if (error.message.includes('Authentication token not found')) {
      toast.error("Session expirée. Veuillez vous reconnecter.");
    } else if (error.message.includes('401')) {
      toast.error("Non autorisé. Veuillez vous reconnecter.");
    } else {
      toast.error(`Erreur lors de la sauvegarde du test: ${error.message}`);
    }
  } finally {
    setSaving(false);
  }
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

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Créer un nouveau test</h3>
        <p className="text-sm text-muted-foreground">
          Composez votre test en sélectionnant des exercices existants ou en utilisant l'IA
        </p>
      </div>

      {/* Informations du test */}
      <Card>
  <CardHeader>
    <CardTitle className="text-base">Informations générales</CardTitle>
  </CardHeader>
  <CardContent className="space-y-4">
    <div className="grid grid-cols-2 gap-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">
          Nom du test *
        </label>
        <Input
          placeholder="Entrez le nom du test"
          value={testName}
          onChange={(e) => setTestName(e.target.value)}
          required
          className={!testName ? "border-black-500" : ""}
        />
        
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">Catégorie *</label>
        <Select
          value={testCategory} 
          onValueChange={setTestCategory}
          required
        >
          <SelectTrigger className={!testCategory ? "border-white-500" : ""}>
            <SelectValue placeholder="Sélectionnez une catégorie" />
          </SelectTrigger>
          <SelectContent>
            {categoriesList.map((category) => (
              <SelectItem key={category} value={category}>
                {category}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        
      </div>
    </div>
    
    <div className="grid grid-cols-2 gap-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">Description *</label>
        <Textarea
          placeholder="Description du test"
          value={testDescription}
          onChange={(e) => setTestDescription(e.target.value)}
          rows={3}
          required
          className={!testDescription ? "border-black-500" : ""}
        />
        
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">Niveau de difficulté *</label>
        <Select 
          value={testDifficulty} 
          onValueChange={setTestDifficulty}
          required
        >
          <SelectTrigger className={!testDifficulty ? "border-black-500" : ""}>
            <SelectValue placeholder="Sélectionnez un niveau" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="beginner">Débutant</SelectItem>
            <SelectItem value="intermediate">Intermédiaire</SelectItem>
            <SelectItem value="advanced">Avancé</SelectItem>
            <SelectItem value="all">Tous niveaux</SelectItem>
          </SelectContent>
        </Select>
       
      </div>
    </div>
  </CardContent>
</Card>

      {/* Création du contenu */}
      <Tabs defaultValue="compose" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="compose">Composer avec exercices existants</TabsTrigger>
          <TabsTrigger value="generate">Générer avec l'IA</TabsTrigger>
        </TabsList>
        
        <TabsContent value="compose" className="space-y-4">
          <div className="grid lg:grid-cols-2 gap-6">
            {/* Exercices disponibles */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Exercices disponibles</CardTitle>
                <CardDescription>
                  {loading ? 'Chargement...' : `${exercises.length} exercice${exercises.length > 1 ? 's' : ''} disponible${exercises.length > 1 ? 's' : ''}`}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-3 max-h-96 overflow-y-auto">
                {loading ? (
                  <div className="flex items-center justify-center p-4">
                    <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                    <span className="text-sm">Chargement des exercices...</span>
                  </div>
                ) : exercises.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-8">
                    Aucun exercice disponible. Créez d'abord des exercices dans la banque d'exercices.
                  </p>
                ) : (
                  exercises.map((exercise) => {
                    const stats = getExerciseStats(exercise);
                    const isSelected = selectedExercises.some(e => e.id === exercise.id);
                    
                    return (
                      <div
                        key={exercise.id}
                        className={`p-3 border rounded-lg cursor-pointer transition-colors ${
                          isSelected 
                            ? 'bg-blue-50 border-blue-200' 
                            : 'hover:bg-muted/50'
                        }`}
                        onClick={() => !isSelected && addExercise(exercise)}
                      >
                        <div className="flex justify-between items-start">
                          <div className="space-y-1 flex-1">
                            <h4 className="font-medium text-sm">{exercise.title}</h4>
                            <div className="flex items-center gap-2 text-xs text-muted-foreground">
                              <Clock className="h-3 w-3" />
                              <span>{stats.duration} min</span>
                              <span>•</span>
                              <span>{stats.totalPoints} pts</span>
                              <span>•</span>
                              <span>{stats.questionCount} question{stats.questionCount > 1 ? 's' : ''}</span>
                            </div>
                            <div className="flex gap-1 flex-wrap">
                              {getTypeBadge(exercise.type)}
                              {getDifficultyBadge(exercise.difficulty)}
                              <Badge variant="secondary" className="text-xs">{exercise.domain}</Badge>
                              <Badge variant="secondary" className="text-xs">{exercise.theme}</Badge>
                            </div>
                          </div>
                          <Button 
                            size="sm" 
                            variant="ghost"
                            disabled={isSelected}
                            className={isSelected ? 'text-blue-600' : ''}
                          >
                            {isSelected ? '✓' : <Plus className="h-3 w-3" />}
                          </Button>
                        </div>
                      </div>
                    );
                  })
                )}
              </CardContent>
            </Card>

            {/* Test en cours de création */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Test en cours de création</CardTitle>
                <CardDescription>
                  {selectedExercises.length} exercice{selectedExercises.length > 1 ? 's' : ''} • {totalDuration} min • {totalPoints} points
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-3 max-h-96 overflow-y-auto">
                {selectedExercises.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-8">
                    Aucun exercice sélectionné. Cliquez sur les exercices disponibles pour les ajouter.
                  </p>
                ) : (
                  selectedExercises.map((exercise) => {
                    const stats = getExerciseStats(exercise);
                    
                    return (
                      <div key={exercise.id} className="p-3 border rounded-lg bg-muted/20">
                        <div className="flex justify-between items-start">
                          <div className="space-y-1 flex-1">
                            <h4 className="font-medium text-sm">{exercise.title}</h4>
                            <div className="flex items-center gap-2 text-xs text-muted-foreground">
                              <Clock className="h-3 w-3" />
                              <span>{stats.duration} min</span>
                              <span>•</span>
                              <span>{stats.totalPoints} pts</span>
                              <span>•</span>
                              <span>{stats.questionCount} question{stats.questionCount > 1 ? 's' : ''}</span>
                            </div>
                            <div className="flex gap-1 flex-wrap">
                              {getTypeBadge(exercise.type)}
                              {getDifficultyBadge(exercise.difficulty)}
                              <Badge variant="secondary" className="text-xs">{exercise.domain}</Badge>
                              <Badge variant="secondary" className="text-xs">{exercise.theme}</Badge>
                            </div>
                          </div>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => removeExercise(exercise.id)}
                            className="text-red-600 hover:text-red-700"
                          >
                            <X className="h-3 w-3" />
                          </Button>
                        </div>
                      </div>
                    );
                  })
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>
        
        <TabsContent value="generate" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Génération automatique avec l'IA</CardTitle>
              <CardDescription>
                Décrivez le type de test que vous souhaitez créer et l'IA générera les exercices appropriés
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Consignes pour l'IA</label>
                <Textarea
                  placeholder="Ex: Créer un test de 45 minutes sur React avec 3 exercices de code et 2 QCM, niveau intermédiaire à avancé, couvrant les hooks, la gestion d'état et les performances..."
                  value={aiPrompt}
                  onChange={(e) => setAiPrompt(e.target.value)}
                  rows={4}
                />
              </div>
              
              <Button onClick={handleGenerateWithAI} disabled={!aiPrompt.trim()}>
                <Wand2 className="h-4 w-4 mr-2" />
                Générer avec l'IA
              </Button>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Actions */}
      <div className="flex justify-end gap-3">
        <Button variant="outline" disabled={saving}>
          Sauvegarder en brouillon
        </Button>
        <Button 
          onClick={handleSaveTest} 
          disabled={!testName || selectedExercises.length === 0 || !testCategory || !testDifficulty || saving  }
        >
          {saving ? (
            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
          ) : (
            <FileText className="h-4 w-4 mr-2" />
          )}
          {saving ? 'Création...' : 'Créer le test'}
        </Button>
      </div>
    </div>
  );
}