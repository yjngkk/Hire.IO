import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Plus, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import apiService from "@/config/apiService";

interface Candidat {
  id: number;
  nom: string;
  email: string;
  poste: string;
  notes: string;
  telephone: string;
}

interface OnboardingPlan {
  id: number;
  employeeName: string;
  jobTitle: string;
  employeeEmail: string;
  startDate: string;
  progress: number;
  manager: string;
  status: string;
  expectedEndDate?: string;
  actualEndDate?: string;
  notes?: string;
  candidatId: number;
}

interface CreateOnboardingPlanDialogProps {
  onPlanCreated: (plan: OnboardingPlan) => void;
}

interface FormData {
  candidatId: number | null;
  manager: string;
  startDate: string;
  expectedEndDate: string;
  notes: string;
}

const initialFormData: FormData = {
  candidatId: null,
  manager: "",
  startDate: "",
  expectedEndDate: "",
  notes: ""
};

export function CreateOnboardingPlanDialog({ onPlanCreated }: CreateOnboardingPlanDialogProps) {
  const [open, setOpen] = useState(false);
  const [candidats, setCandidats] = useState<Candidat[]>([]);
  const [formData, setFormData] = useState<FormData>(initialFormData);
  const [loading, setLoading] = useState(false);
  const [candidatsLoading, setCandidatsLoading] = useState(false);
  const { toast } = useToast();

  useEffect(() => {
    if (open) {
      loadCandidatsWithoutOnboarding();
    }
  }, [open]);

  const loadCandidatsWithoutOnboarding = async () => {
  try {
    setCandidatsLoading(true);
    const allCandidats = await apiService.get<Candidat[]>('/candidats');
    
    const candidatsWithoutOnboarding: Candidat[] = [];
    
    for (const candidat of allCandidats) {
      try {
        // Use the exists method for a cleaner approach
        const hasOnboardingPlan = await apiService.exists(`/onboarding-plans/candidat/${candidat.id}`);
        if (!hasOnboardingPlan) {
          candidatsWithoutOnboarding.push(candidat);
        }
      } catch (error: any) {
        console.error(`Unexpected error checking candidat ${candidat.id}:`, error);
      }
    }
    
    setCandidats(candidatsWithoutOnboarding);
  } catch (error) {
    console.error('Erreur lors du chargement des candidats:', error);
    toast({
      title: "Erreur",
      description: "Impossible de charger les candidats disponibles",
      variant: "destructive"
    });
  } finally {
    setCandidatsLoading(false);
  }
};
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.candidatId || !formData.manager || !formData.startDate) {
      toast({
        title: "Erreur",
        description: "Veuillez remplir tous les champs obligatoires",
        variant: "destructive"
      });
      return;
    }

    setLoading(true);

    try {
      const submitData = {
        candidatId: formData.candidatId,
        manager: formData.manager,
        startDate: formData.startDate,
        expectedEndDate: formData.expectedEndDate || null,
        notes: formData.notes || null,
        status: "pending",
        progress: 0
      };

      const newPlan = await apiService.post<OnboardingPlan>('/onboarding-plans', submitData);
      
      // The backend now automatically creates procedures from templates
      // But we can add a fallback check or manual generation if needed
      
      onPlanCreated(newPlan);
      setFormData(initialFormData);
      setOpen(false);
      
      toast({
        title: "Plan créé",
        description: "Le plan d'onboarding et les procédures ont été créés avec succès"
      });
    } catch (error: any) {
      console.error('Erreur lors de la création:', error);
      
      let errorMessage = "Une erreur est survenue lors de la création du plan";
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status === 400) {
        errorMessage = "Données invalides. Vérifiez les informations saisies.";
      } else if (error.response?.status === 409) {
        errorMessage = "Ce candidat a déjà un plan d'onboarding.";
      }
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setFormData(initialFormData);
    setOpen(false);
  };

  // Calculate suggested end date (4 weeks from start date)
  const getSuggestedEndDate = (startDate: string): string => {
    if (!startDate) return "";
    const start = new Date(startDate);
    const end = new Date(start);
    end.setDate(start.getDate() + 28); // 4 weeks
    return end.toISOString().split('T')[0];
  };

  const handleStartDateChange = (value: string) => {
    setFormData({ 
      ...formData, 
      startDate: value,
      expectedEndDate: formData.expectedEndDate || getSuggestedEndDate(value)
    });
  };

  return (
     <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
  <Button className="flex items-center justify-center max-w-full min-w-0 px-2 sm:px-3">
    <Plus className="h-4 w-4 mr-1 sm:mr-2 flex-shrink-0" />
    <span className="truncate text-xs sm:text-sm">
      <span className="hidden sm:inline">Nouveau plan d'onboarding</span>
      <span className="sm:hidden">Nouveau</span>
    </span>
  </Button>
</DialogTrigger>
      <DialogContent className="w-full max-w-[95vw] sm:max-w-[500px] max-h-[90vh] overflow-y-auto mx-auto">
        <DialogHeader className="space-y-2">
          <DialogTitle className="text-lg font-semibold">
            Créer un plan d'onboarding
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground">
            Créez un nouveau plan d'intégration pour un candidat sélectionné. 
            Les procédures seront automatiquement générées à partir des modèles actifs.
          </DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="candidat">Candidat *</Label>
            {candidatsLoading ? (
              <div className="flex items-center space-x-2 p-3 border rounded-md">
                <Loader2 className="h-4 w-4 animate-spin" />
                <span className="text-sm text-muted-foreground">Chargement des candidats...</span>
              </div>
            ) : candidats.length === 0 ? (
              <div className="p-3 border rounded-md text-sm text-muted-foreground">
                Aucun candidat disponible pour l'onboarding
              </div>
            ) : (
              <Select 
                value={formData.candidatId?.toString() || ""} 
                onValueChange={(value) => setFormData({ ...formData, candidatId: parseInt(value) })}
                required
              >
                <SelectTrigger>
                  <SelectValue placeholder="Sélectionner un candidat" />
                </SelectTrigger>
                <SelectContent>
                  {candidats.map((candidat) => (
                    <SelectItem key={candidat.id} value={candidat.id.toString()}>
                      <div className="flex flex-col">
                        <span className="font-medium">{candidat.nom}</span>
                        <span className="text-sm text-muted-foreground">{candidat.poste}</span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="manager">Manager responsable *</Label>
            <Input
              id="manager"
              value={formData.manager}
              onChange={(e) => setFormData({ ...formData, manager: e.target.value })}
              placeholder="Nom du manager responsable"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="startDate">Date de début *</Label>
              <Input
                id="startDate"
                type="date"
                value={formData.startDate}
                onChange={(e) => handleStartDateChange(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="expectedEndDate">Date de fin prévue</Label>
              <Input
                id="expectedEndDate"
                type="date"
                value={formData.expectedEndDate}
                onChange={(e) => setFormData({ ...formData, expectedEndDate: e.target.value })}
                placeholder="Optionnel"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="notes">Notes</Label>
            <Textarea
              id="notes"
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="Notes ou instructions spéciales pour l'onboarding"
              rows={3}
            />
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              disabled={loading}
            >
              Annuler
            </Button>
            <Button 
              type="submit" 
              disabled={loading || candidats.length === 0}
            >
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Créer le plan
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}