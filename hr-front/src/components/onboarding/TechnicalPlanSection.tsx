import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Code, Sparkles, FileText, Brain } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface TechnicalPlanSectionProps {
  employeeName: string;
  jobTitle: string;
  candidatId: number; // Added this prop
}

const defaultTechnicalPlan = [
  {
    id: 1,
    title: "Configuration de l'environnement de développement",
    description: "Installation et configuration des outils de développement",
    duration: "J+1 à J+2",
    completed: false
  },
  {
    id: 2,
    title: "Familiarisation avec la stack technique",
    description: "Découverte des technologies utilisées dans l'équipe",
    duration: "J+3 à J+5",
    completed: false
  },
  {
    id: 3,
    title: "Premier projet d'intégration",
    description: "Petit projet pour se familiariser avec le code existant",
    duration: "J+7 à J+14",
    completed: false
  }
];

export function TechnicalPlanSection({ employeeName, jobTitle, candidatId }: TechnicalPlanSectionProps) {
  const [technicalPlan, setTechnicalPlan] = useState(defaultTechnicalPlan);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedPlan, setGeneratedPlan] = useState("");
  const { toast } = useToast();

  const generateTechnicalPlan = async () => {
    setIsGenerating(true);
    
    // Simulation de génération IA basée sur CV et entretien
    // You can now use candidatId to fetch specific candidate data if needed
    setTimeout(() => {
      const aiGeneratedPlan = `Plan technique personnalisé pour ${employeeName} - ${jobTitle}
Semaine 1 : Mise en place
- Configuration de l'environnement React/TypeScript
- Installation des outils : VS Code, Git, Docker
- Accès aux repositories et documentation technique

Semaine 2 : Découverte technique
- Formation sur l'architecture microservices
- Présentation de la stack : React, Node.js, PostgreSQL
- Review du code existant avec un senior

Semaine 3 : Premier développement
- Implémentation d'une feature simple
- Tests unitaires et intégration continue
- Code review et bonnes pratiques

Semaine 4 : Montée en compétences
- Projet d'intégration plus complexe
- Participation aux réunions techniques
- Point d'étape avec le tech lead`;

      setGeneratedPlan(aiGeneratedPlan);
      setIsGenerating(false);
      
      toast({
        title: "Plan technique généré",
        description: "Plan personnalisé créé avec succès"
      });
    }, 2000);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Code className="h-5 w-5" />
          Plan technique - {employeeName}
        </CardTitle>
        <CardDescription>
          Formation technique personnalisée pour {jobTitle}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex gap-2">
            <Dialog>
              <DialogTrigger asChild>
                <Button className="flex-1">
                  <Sparkles className="h-4 w-4 mr-2" />
                  Générer avec l'IA
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-4xl">
                <DialogHeader>
                  <DialogTitle>Génération automatique du plan technique</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div className="p-4 bg-blue-50 rounded-lg">
                    <h4 className="font-medium mb-2">Sources utilisées :</h4>
                    <div className="flex gap-2">
                      <Badge variant="secondary">
                        <FileText className="h-3 w-3 mr-1" />
                        CV du candidat
                      </Badge>
                      <Badge variant="secondary">
                        <Brain className="h-3 w-3 mr-1" />
                        Résumé d'entretien
                      </Badge>
                    </div>
                  </div>
                  
                  <Button 
                    onClick={generateTechnicalPlan}
                    disabled={isGenerating}
                    className="w-full"
                  >
                    <Sparkles className="h-4 w-4 mr-2" />
                    {isGenerating ? "Génération en cours..." : "Générer le plan technique"}
                  </Button>

                  {generatedPlan && (
                    <div className="space-y-3">
                      <h4 className="font-medium">Plan généré :</h4>
                      <Textarea
                        value={generatedPlan}
                        onChange={(e) => setGeneratedPlan(e.target.value)}
                        rows={15}
                        className="font-mono text-sm"
                      />
                      <Button>Valider et appliquer</Button>
                    </div>
                  )}
                </div>
              </DialogContent>
            </Dialog>
            
            <Button variant="outline">
              Saisie manuelle
            </Button>
          </div>

          <div className="space-y-3">
            <h4 className="font-medium">Plan technique actuel</h4>
            {technicalPlan.map((step) => (
              <div key={step.id} className="flex items-start gap-3 p-3 border rounded-lg">
                <div className="flex-shrink-0 w-6 h-6 bg-primary/10 text-primary rounded-full flex items-center justify-center text-sm font-medium mt-1">
                  {step.id}
                </div>
                <div className="flex-1">
                  <h5 className="font-medium">{step.title}</h5>
                  <p className="text-sm text-muted-foreground mb-1">{step.description}</p>
                  <Badge variant="outline" className="text-xs">
                    {step.duration}
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}