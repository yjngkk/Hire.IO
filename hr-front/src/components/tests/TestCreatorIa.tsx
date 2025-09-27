import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Wand2, Clock, FileText, RefreshCw, Lightbulb, Target, BookOpen } from "lucide-react";
import { toast } from "sonner";
import  apiService from "@/config/apiService";

interface TestCreatorIaProps {
  onClose?: () => void;
}

export function TestCreatorIa({ onClose }: TestCreatorIaProps) {
  const [aiPrompt, setAiPrompt] = useState("");
  const [generating, setGenerating] = useState(false);
  const [generatedContent, setGeneratedContent] = useState(null);

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

  const handleGenerateWithAI = async () => {
    if (!aiPrompt.trim()) {
      toast.error("Veuillez entrer des consignes pour l'IA");
      return;
    }

    try {
      setGenerating(true);
      
      // Appel à l'API avec apiService
      const data = await apiService.post('/tests/generate-from-message', {
        message: aiPrompt.trim()
      });
      
      // Transformation des données pour correspondre au format attendu
      const formattedContent = {
        testInfo: {
          name: data.title || "Test généré par IA",
          description: `Test généré automatiquement avec ${data.questions?.length || 0} questions`,
          difficulty: data.difficulty || "Non spécifié",
          domain: data.domain || "Général",
          theme: data.theme || "Divers"
        },
        exercises: [{
          id: data.id,
          title: data.title,
          type: data.type,
          difficulty: data.difficulty,
          duration: data.duration,
          totalPoints: data.totalPoints,
          questions: data.questionCount || data.questions?.length || 0,
          description: `${data.type} avec ${data.questions?.length || 0} questions sur ${data.theme}`
        }],
        totalDuration: data.duration,
        totalPoints: data.totalPoints,
        fullTestData: data // Garder les données complètes pour la sauvegarde
      };
      
      setGeneratedContent(formattedContent);
      toast.success("Test généré avec succès par l'IA !");
      
    } catch (error) {
      console.error('Erreur lors de la génération:', error);
      if (error.message?.includes('401') || error.status === 401) {
        toast.error("Session expirée. Veuillez vous reconnecter.");
      } else if (error.message?.includes('400') || error.status === 400) {
        toast.error("Requête invalide. Vérifiez votre message.");
      } else {
        toast.error(`Erreur lors de la génération du test: ${error.message || 'Erreur inconnue'}`);
      }
    } finally {
      setGenerating(false);
    }
  };

  const promptSuggestions = [
    "Créer un QCM sur les concepts de base de Java : variables, boucles, conditions et méthodes. Niveau débutant avec 5 questions.",
    "Générer un test JavaScript ES6+ niveau intermédiaire : async/await, destructuring, arrow functions, 8 questions",
    "QCM Python pour débutants : types de données, listes, dictionnaires, fonctions, 6 questions",
    "Test HTML/CSS : sélecteurs, flexbox, responsive design, niveau débutant, 7 questions"
  ];

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Générer un test avec l'IA</h3>
        <p className="text-sm text-muted-foreground">
          Décrivez le test que vous souhaitez créer et laissez l'IA faire le reste
        </p>
      </div>

      {/* Zone de saisie principale */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Wand2 className="h-4 w-4" />
            Consignes pour l'IA
          </CardTitle>
          <CardDescription>
            Décrivez précisément le type de test que vous souhaitez générer
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Textarea
              placeholder="Ex: Créer un QCM sur les concepts de base de Java : variables, boucles, conditions et méthodes. Niveau débutant avec 5 questions."
              value={aiPrompt}
              onChange={(e) => setAiPrompt(e.target.value)}
              rows={6}
              className="min-h-32"
            />
          </div>

          {/* Suggestions */}
          <div className="space-y-3">
            <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
              <Lightbulb className="h-4 w-4" />
              Suggestions d'idées
            </div>
            <div className="grid gap-2">
              {promptSuggestions.map((suggestion, index) => (
                <div
                  key={index}
                  className="p-3 bg-muted/30 hover:bg-muted/50 rounded-lg cursor-pointer text-sm transition-colors"
                  onClick={() => setAiPrompt(suggestion)}
                >
                  {suggestion}
                </div>
              ))}
            </div>
          </div>

          <Button 
            onClick={handleGenerateWithAI} 
            disabled={!aiPrompt.trim() || generating}
            className="w-full"
            size="lg"
          >
            {generating ? (
              <RefreshCw className="h-5 w-5 mr-2 animate-spin" />
            ) : (
              <Wand2 className="h-5 w-5 mr-2" />
            )}
            {generating ? 'Génération en cours...' : 'Générer le test avec l\'IA'}
          </Button>
        </CardContent>
      </Card>

      {/* Contenu généré */}
      {generatedContent && (
        <div className="space-y-4">
          {/* Informations du test généré */}
          <Card className="border-green-200 bg-green-50/50">
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2 text-green-800">
                <Target className="h-4 w-4" />
                Test généré avec succès
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <h3 className="font-semibold text-lg">{generatedContent.testInfo.name}</h3>
                  <p className="text-sm text-muted-foreground mt-1">
                    {generatedContent.testInfo.description}
                  </p>
                </div>
                
                <div className="flex items-center gap-2 flex-wrap">
                  {getDifficultyBadge(generatedContent.testInfo.difficulty)}
                  <Badge variant="secondary">{generatedContent.testInfo.domain}</Badge>
                  <Badge variant="secondary">{generatedContent.testInfo.theme}</Badge>
                </div>

                <div className="flex items-center gap-4 text-sm font-medium bg-white/60 p-3 rounded-lg">
                  <div className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    <span>{generatedContent.totalDuration} minutes</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <FileText className="h-4 w-4" />
                    <span>{generatedContent.exercises.length} exercices</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <BookOpen className="h-4 w-4" />
                    <span>{generatedContent.totalPoints} points</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}