import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { Badge } from "@/components/ui/badge";
import { Star, ThumbsUp, ThumbsDown, Sparkles } from "lucide-react";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";

interface CreateSummaryDialogProps {
  onClose: () => void;
}

export function CreateSummaryDialog({ onClose }: CreateSummaryDialogProps) {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    candidateName: "",
    jobTitle: "",
    interviewer: "",
    date: "",
    score: "",
    recommendation: "",
    summary: "",
    strengths: "",
    weaknesses: "",
    nextSteps: ""
  });
  const [interviews, setInterviews] = useState<any[]>([]);
  const [isLoadingInterviews, setIsLoadingInterviews] = useState(false);
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();
  useEffect(() => {
    const fetchInterviews = async () => {
      if (keycloakReady && isAuthenticated) {
        setIsLoadingInterviews(true);
        try {
          const data = await apiService.get("/entretiens");
          setInterviews(data);
        } catch (error) {
          console.error("Error fetching interviews:", error);
        } finally {
          setIsLoadingInterviews(false);
        }
      }
    };
    fetchInterviews();
  }, [keycloakReady, isAuthenticated]);
  const [isGenerating, setIsGenerating] = useState(false);
  const { toast } = useToast();

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const generateSummaryWithAI = async () => {
    setIsGenerating(true);
    setTimeout(() => {
      setFormData(prev => ({
        ...prev,
        summary: `Le candidat ${formData.candidateName} a démontré une solide maîtrise technique pour le poste de ${formData.jobTitle}.`,
        strengths: "Excellente maîtrise technique\nBonne communication\nCapacité d'adaptation",
        weaknesses: "Manque d'expérience en leadership\nConnaissances limitées en tests automatisés",
        nextSteps: "Entretien technique avec l'équipe\nVérification des références"
      }));
      setIsGenerating(false);
      toast({
        title: "Résumé généré",
        description: "Le résumé a été généré avec succès"
      });
    }, 2000);
  };

  const saveSummary = async () => {
    try {
      await apiService.post('/resumes', {
        nomCandidat: formData.candidateName,
        poste: formData.jobTitle,
        interviewer: formData.interviewer,
        dateEntretien: formData.date,
        score: Number(formData.score),
        recommandation: formData.recommendation,
        notes: formData.summary,
        pointsForts: formData.strengths,
        pointsAmelioration: formData.weaknesses,
        prochainesEtapes: formData.nextSteps
      });

      toast({
        title: "Résumé sauvegardé",
        description: `Le résumé pour ${formData.candidateName} a été créé avec succès`
      });

      onClose();

      // Recharger la page après un court délai
      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } catch (error: any) {
      toast({
        title: "Erreur",
        description: error.message || "Une erreur est survenue lors de la sauvegarde"
      });
    }
  };

  const getRecommendationBadge = (recommendation: string) => {
    const configs = {
      recommend: { color: "bg-green-100 text-green-800", icon: ThumbsUp, label: "Recommandé" },
      maybe: { color: "bg-yellow-100 text-yellow-800", icon: Star, label: "À reconsidérer" },
      "not-recommend": { color: "bg-red-100 text-red-800", icon: ThumbsDown, label: "Non recommandé" }
    };
    const config = configs[recommendation as keyof typeof configs];
    if (!config) return null;
    const Icon = config.icon;
    return (
      <Badge className={config.color}>
        <Icon className="h-3 w-3 mr-1" />
        {config.label}
      </Badge>
    );
  };

  return (
    <div className="space-y-6">
      {step === 1 && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Informations de base</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label>Nom du candidat</Label>
              <Select
                onValueChange={(value) => {
                  const selectedInterview = interviews.find(i => i.candidat.nom === value);
                  if (selectedInterview) {
                    setFormData(prev => ({
                      ...prev,
                      candidateName: selectedInterview.candidat.nom,
                      jobTitle: selectedInterview.candidat.poste || "",
                      interviewer: selectedInterview.interviewer || "",
                      date: selectedInterview.dateHeure ? selectedInterview.dateHeure.split('T')[0] : ""
                    }));
                  }
                }}
                value={formData.candidateName}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Sélectionner un candidat" />
                </SelectTrigger>
                <SelectContent>
                  {isLoadingInterviews ? (
                    <SelectItem value="loading" disabled>Chargement...</SelectItem>
                  ) : (
                    interviews.map((interview) => (
                      <SelectItem key={interview.id} value={interview.candidat.nom}>
                        {interview.candidat.nom}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Poste</Label>
              <Input 
                value={formData.jobTitle} 
                onChange={(e) => handleInputChange("jobTitle", e.target.value)} 
                readOnly={!!formData.candidateName}
              />
            </div>
           {/* <div>
              <Label>Interviewer</Label>
              <Input
                value={formData.interviewer}
                onChange={(e) => handleInputChange("interviewer", e.target.value)}
                readOnly={!!formData.candidateName}
              />
            </div>*/}
            <div>
              <Label>Date</Label>
              <Input 
                type="date" 
                value={formData.date} 
                onChange={(e) => handleInputChange("date", e.target.value)} 
                readOnly={!!formData.candidateName}
              />
            </div>
            <div>
              <Label>Score</Label>
              <Select onValueChange={(v) => handleInputChange("score", v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Score" />
                </SelectTrigger>
                <SelectContent>
                  {[1,2,3,4,5].map(score => (
                    <SelectItem key={score} value={score.toString()}>{score}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Recommandation</Label>
              <Select onValueChange={(v) => handleInputChange("recommendation", v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Recommandation" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="recommend">Recommandé</SelectItem>
                  <SelectItem value="maybe">À reconsidérer</SelectItem>
                  <SelectItem value="not-recommend">Non recommandé</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="flex gap-2">
            <Button 
              onClick={generateSummaryWithAI} 
              disabled={!formData.candidateName || !formData.jobTitle || isGenerating}
            >
              <Sparkles className="h-4 w-4 mr-2" />
              {isGenerating ? "Génération..." : "Générer avec l'IA"}
            </Button>
            <Button variant="outline" onClick={() => setStep(2)}>
              Saisie manuelle
            </Button>
          </div>
        </div>
      )}

      {step === 2 && (
        <div className="space-y-4">
          <div className="flex justify-between">
            <h3 className="text-lg font-semibold">Détails du résumé</h3>
            {formData.recommendation && getRecommendationBadge(formData.recommendation)}
          </div>
          <div>
            <Label>Résumé général</Label>
            <Textarea 
              value={formData.summary} 
              onChange={(e) => handleInputChange("summary", e.target.value)} 
              rows={3} 
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label>Points forts</Label>
              <Textarea 
                value={formData.strengths} 
                onChange={(e) => handleInputChange("strengths", e.target.value)} 
                rows={4} 
              />
            </div>
            <div>
              <Label>Points d'amélioration</Label>
              <Textarea 
                value={formData.weaknesses} 
                onChange={(e) => handleInputChange("weaknesses", e.target.value)} 
                rows={4} 
              />
            </div>
          </div>
          <div>
            <Label>Prochaines étapes</Label>
            <Textarea 
              value={formData.nextSteps} 
              onChange={(e) => handleInputChange("nextSteps", e.target.value)} 
              rows={2} 
            />
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setStep(1)}>
              Retour
            </Button>
            <Button onClick={saveSummary} className="flex-1">
              Sauvegarder
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}