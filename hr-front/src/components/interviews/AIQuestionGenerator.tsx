
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useToast } from "@/hooks/use-toast";
import { Sparkles, Copy, RefreshCw } from "lucide-react";

interface AIQuestionGeneratorProps {
  onClose: () => void;
}

export function AIQuestionGenerator({ onClose }: AIQuestionGeneratorProps) {
  const [formData, setFormData] = useState({
    jobTitle: "",
    skills: "",
    experience: "",
    interviewType: ""
  });
  const [generatedQuestions, setGeneratedQuestions] = useState<string[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const { toast } = useToast();

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const generateQuestions = async () => {
    setIsGenerating(true);
    
    // Simuler la génération par IA
    setTimeout(() => {
      const questions = [
        `Parlez-moi de votre expérience en tant que ${formData.jobTitle}`,
        `Comment abordez-vous les défis techniques avec ${formData.skills} ?`,
        `Décrivez un projet complexe que vous avez mené à bien`,
        `Comment restez-vous à jour avec les dernières technologies ?`,
        `Quelle est votre approche pour travailler en équipe ?`,
        `Comment gérez-vous les délais serrés et la pression ?`,
        `Parlez-moi d'une fois où vous avez dû apprendre rapidement une nouvelle technologie`,
        `Quels sont vos objectifs de carrière à court et long terme ?`,
        `Comment debuggez-vous un code complexe ?`,
        `Quelle est votre philosophie en matière de qualité de code ?`
      ];
      
      setGeneratedQuestions(questions);
      setIsGenerating(false);
      
      toast({
        title: "Questions générées",
        description: "10 questions personnalisées ont été créées"
      });
    }, 2000);
  };

  const copyQuestion = (question: string) => {
    navigator.clipboard.writeText(question);
    toast({
      title: "Question copiée",
      description: "La question a été copiée dans le presse-papiers"
    });
  };

  const copyAllQuestions = () => {
    const allQuestions = generatedQuestions.join('\n\n');
    navigator.clipboard.writeText(allQuestions);
    toast({
      title: "Toutes les questions copiées",
      description: "Toutes les questions ont été copiées dans le presse-papiers"
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold mb-4">Générer des questions personnalisées</h3>
        
        <div className="grid grid-cols-2 gap-4 mb-4">
          <div className="space-y-2">
            <Label htmlFor="jobTitle">Poste concerné</Label>
            <Input
              id="jobTitle"
              value={formData.jobTitle}
              onChange={(e) => handleInputChange("jobTitle", e.target.value)}
              placeholder="Développeur Full Stack"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="experience">Niveau d'expérience</Label>
            <Input
              id="experience"
              value={formData.experience}
              onChange={(e) => handleInputChange("experience", e.target.value)}
              placeholder="Junior, Senior, Lead..."
            />
          </div>
        </div>
        
        <div className="space-y-2 mb-4">
          <Label htmlFor="skills">Compétences techniques</Label>
          <Textarea
            id="skills"
            value={formData.skills}
            onChange={(e) => handleInputChange("skills", e.target.value)}
            placeholder="React, Node.js, PostgreSQL, Docker..."
            rows={2}
          />
        </div>
        
        <div className="space-y-2 mb-6">
          <Label htmlFor="interviewType">Type d'entretien</Label>
          <Input
            id="interviewType"
            value={formData.interviewType}
            onChange={(e) => handleInputChange("interviewType", e.target.value)}
            placeholder="Technique, RH, Managérial..."
          />
        </div>
        
        <Button 
          onClick={generateQuestions}
          disabled={!formData.jobTitle || !formData.skills || isGenerating}
          className="w-full"
        >
          <Sparkles className="h-4 w-4 mr-2" />
          {isGenerating ? "Génération en cours..." : "Générer les questions"}
        </Button>
      </div>

      {generatedQuestions.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <div>
                <CardTitle className="text-base">Questions générées</CardTitle>
                <CardDescription>
                  {generatedQuestions.length} questions personnalisées pour votre entretien
                </CardDescription>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={generateQuestions} disabled={isGenerating}>
                  <RefreshCw className="h-3 w-3 mr-1" />
                  Régénérer
                </Button>
                <Button size="sm" variant="outline" onClick={copyAllQuestions}>
                  <Copy className="h-3 w-3 mr-1" />
                  Tout copier
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {generatedQuestions.map((question, index) => (
                <div key={index} className="flex items-start gap-3 p-3 bg-muted/50 rounded-lg">
                  <div className="flex-shrink-0 w-6 h-6 bg-primary/10 text-primary rounded-full flex items-center justify-center text-sm font-medium">
                    {index + 1}
                  </div>
                  <p className="text-sm flex-1">{question}</p>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => copyQuestion(question)}
                    className="flex-shrink-0"
                  >
                    <Copy className="h-3 w-3" />
                  </Button>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
