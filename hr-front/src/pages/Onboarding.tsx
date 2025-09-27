import { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { User, Calendar, Loader2, Mail } from "lucide-react";
import { CandidateFileSection } from "@/components/onboarding/CandidateFileSection";
import { CompanyProceduresSection } from "@/components/onboarding/CompanyProceduresSection";
import { TechnicalPlanSection } from "@/components/onboarding/TechnicalPlanSection";
import { OnboardingTimeline } from "@/components/onboarding/OnboardingTimeline";
import { CreateOnboardingPlanDialog } from "@/components/onboarding/CreateOnboardingPlanDialog";
import { useOnboardingReminders } from "@/hooks/useOnboardingReminders";
import apiService from "@/config/apiService";
import { useToast } from "@/hooks/use-toast";

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

interface Candidat {
  id: number;
  nom: string;
  email: string;
  poste: string;
  notes: string;
  telephone: string;
}

interface CandidateFile {
  id: number;
  name: string;
  status: "completed" | "pending" | "missing";
  isRequired: boolean;
}

export function Onboarding() {
  const [selectedPlan, setSelectedPlan] = useState<number | null>(null);
  const [onboardingPlans, setOnboardingPlans] = useState<OnboardingPlan[]>([]);
  const [candidats, setCandidats] = useState<Candidat[]>([]);
  const [files, setFiles] = useState<CandidateFile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { toast } = useToast();
  
  // Use the shared reminder hook
  const { sendGlobalReminder, hasFilesToRemind, reminderLoading } = useOnboardingReminders();

  useEffect(() => {
    loadOnboardingPlans();
    loadCandidats();
  }, []);

  // Load candidate files when selected plan changes
  useEffect(() => {
    if (selectedPlan) {
      const planData = onboardingPlans.find(plan => plan.id === selectedPlan);
      if (planData) {
        loadCandidateFiles(planData.candidatId);
      }
    }
  }, [selectedPlan, onboardingPlans]);

  const loadOnboardingPlans = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // First try to load real onboarding plans from API
      let data: OnboardingPlan[] = [];
      try {
        data = await apiService.get<OnboardingPlan[]>('/onboarding-plans/active');
      } catch (activeError) {
        console.warn('Active onboarding plans endpoint not available, trying all plans:', activeError);
        try {
          data = await apiService.get<OnboardingPlan[]>('/onboarding-plans');
        } catch (allError) {
          console.warn('Onboarding plans API not available yet, will show empty state:', allError);
          data = [];
        }
      }
      
      setOnboardingPlans(data);
      
      if (data.length > 0) {
        setSelectedPlan(data[0].id);
      }
    } catch (error) {
      console.error('Erreur lors du chargement des plans d\'onboarding:', error);
      setError('Erreur lors du chargement des plans d\'onboarding');
      toast({
        title: "Erreur",
        description: "Impossible de charger les plans d'onboarding",
        variant: "destructive"
      });
    } finally {
      setLoading(false);
    }
  };

  const loadCandidats = async () => {
    try {
      const data = await apiService.get<Candidat[]>('/candidats');
      setCandidats(data);
    } catch (error) {
      console.error('Erreur lors du chargement des candidats:', error);
      // Don't show error toast for candidats as it's not critical for onboarding display
    }
  };

  const loadCandidateFiles = async (candidatId: number) => {
    try {
      const data = await apiService.get<CandidateFile[]>(`/candidate-files/candidat/${candidatId}`);
      setFiles(data);
    } catch (error) {
      console.error('Erreur lors du chargement des fichiers:', error);
      setFiles([]);
    }
  };

  const updatePlanProgress = async (planId: number, progress: number) => {
    // First update locally immediately for better UX
    setOnboardingPlans(plans => 
      plans.map(plan => 
        plan.id === planId ? { ...plan, progress } : plan
      )
    );

    try {        
      const updatedPlan = await apiService.patch<OnboardingPlan>(
        `/onboarding-plans/${planId}`, 
        { progress }
      );
      
      setOnboardingPlans(plans => 
        plans.map(plan => 
          plan.id === planId ? updatedPlan : plan
        )
      );
      
      console.log(`Progress updated successfully for plan ${planId}: ${progress}%`);
    } catch (error: any) {
      console.error('Erreur lors de la mise à jour du progrès:', error);
      
      // Revert the local change on error
      const originalPlan = onboardingPlans.find(p => p.id === planId);
      if (originalPlan) {
        setOnboardingPlans(plans => 
          plans.map(plan => 
            plan.id === planId ? { ...plan, progress: originalPlan.progress } : plan
          )
        );
      }

      // Show user-friendly error message based on error type
      let errorMessage = "Impossible de mettre à jour le progrès";
      
      if (error.code === 'ERR_NETWORK' || error.message?.toLowerCase().includes('network')) {
        errorMessage = "Erreur de connexion au serveur - vérifiez que le backend est démarré";
      } else if (error.response?.status === 404) {
        errorMessage = "Plan d'onboarding introuvable";
      } else if (error.response?.status === 400) {
        errorMessage = "Données invalides";
      } else if (error.message?.toLowerCase().includes('cors')) {
        errorMessage = "Erreur CORS - contactez l'administrateur système";
      }

      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive"
      });
    }
  };

  // Use useCallback to prevent recreation on every render
  const handleDocumentProgressUpdate = useCallback((progress: number) => {
    if (selectedPlan) {
      // Get the current progress to avoid unnecessary updates
      const currentPlan = onboardingPlans.find(plan => plan.id === selectedPlan);
      if (currentPlan && currentPlan.progress !== progress) {
        updatePlanProgress(selectedPlan, progress);
      }
    }
  }, [selectedPlan, onboardingPlans]);

  const handlePlanCreated = (newPlan: OnboardingPlan) => {
    setOnboardingPlans(prev => [...prev, newPlan]);
    setSelectedPlan(newPlan.id);
    toast({
      title: "Plan créé",
      description: `Plan d'onboarding créé pour ${newPlan.employeeName}`
    });
  };

  const handleSendGlobalReminder = async () => {
    const selectedPlanData = onboardingPlans.find(plan => plan.id === selectedPlan);
    if (selectedPlanData) {
      await sendGlobalReminder(
        selectedPlanData.candidatId, 
        selectedPlanData.employeeName,
        async () => {
          // Refresh files after sending reminder
          await loadCandidateFiles(selectedPlanData.candidatId);
        }
      );
    }
  };

  // Memoize selectedPlanData to prevent unnecessary re-renders
  const selectedPlanData = onboardingPlans.find(plan => plan.id === selectedPlan);
  const selectedCandidat = candidats.find(c => c.id === selectedPlanData?.candidatId);

  const getStatusBadge = (status: string) => {
    const styles = {
      "in-progress": "bg-blue-100 text-blue-800",
      "completed": "bg-green-100 text-green-800",
      "pending": "bg-yellow-100 text-yellow-800",
      "on-hold": "bg-orange-100 text-orange-800",
      "cancelled": "bg-red-100 text-red-800"
    };
    
    const labels = {
      "in-progress": "En cours",
      "completed": "Terminé",
      "pending": "En attente",
      "on-hold": "En pause",
      "cancelled": "Annulé"
    };

    return (
      <Badge className={`${styles[status as keyof typeof styles]} shrink-0 text-xs`}>
        {labels[status as keyof typeof labels]}
      </Badge>
    );
  };

  if (loading) {
    return (
      <div className="space-y-6 p-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold">Onboarding</h1>
          <p className="text-muted-foreground mt-2">
            Chargement des données...
          </p>
        </div>
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6 p-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold">Onboarding</h1>
          <p className="text-muted-foreground mt-2">
            Une erreur est survenue lors du chargement
          </p>
        </div>
        <Card>
          <CardContent className="text-center py-12">
            <p className="text-red-600 mb-4 break-words">{error}</p>
            <Button onClick={() => {
              setError(null);
              loadOnboardingPlans();
            }}>
              Réessayer
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6 p-4 max-w-full overflow-hidden">
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
        <div className="min-w-0 flex-1">
          <h1 className="text-2xl sm:text-3xl font-bold truncate">Onboarding</h1>
          <p className="text-muted-foreground mt-2 text-sm sm:text-base">
            Suivez l'intégration complète de vos nouveaux collaborateurs
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">
        <Card className="xl:col-span-1">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg sm:text-xl">Plans d'onboarding actifs</CardTitle>
            <CardDescription className="text-xs sm:text-sm">
              Nouveaux collaborateurs en cours d'intégration ({onboardingPlans.length})
            </CardDescription>
          </CardHeader>
          <CardContent className="px-4 pb-4">
            <div className="space-y-4">
              {onboardingPlans.length === 0 ? (
                <div className="text-center py-8 space-y-4">
                  <div className="text-muted-foreground">
                    <p className="font-medium text-sm">Aucun plan d'onboarding actif</p>
                    <p className="text-xs">Créez un plan pour commencer l'intégration d'un candidat</p>
                  </div>
                  <CreateOnboardingPlanDialog onPlanCreated={handlePlanCreated} />
                </div>
              ) : (
                onboardingPlans.map((plan) => (
                  <Card 
                    key={plan.id} 
                    className={`p-3 sm:p-4 cursor-pointer transition-colors ${
                      selectedPlan === plan.id ? 'ring-2 ring-primary' : 'hover:bg-muted/50'
                    }`}
                    onClick={() => setSelectedPlan(plan.id)}
                  >
                    <div className="space-y-3">
                      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-2">
                        <div className="min-w-0 flex-1">
                          <h3 className="font-semibold text-sm sm:text-base truncate">{plan.employeeName}</h3>
                          <p className="text-xs sm:text-sm text-muted-foreground truncate">{plan.jobTitle}</p>
                          <p className="text-xs text-muted-foreground truncate">{plan.employeeEmail}</p>
                        </div>
                        <div className="self-start">
                          {getStatusBadge(plan.status)}
                        </div>
                      </div>
                      
                      <div className="space-y-2">
                        <div className="flex justify-between text-xs sm:text-sm">
                          <span>Progress</span>
                          <span>{plan.progress}%</span>
                        </div>
                        <Progress value={plan.progress} className="h-2" />
                      </div>
                      
                      <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4 text-xs text-muted-foreground">
                        <div className="flex items-center gap-1 min-w-0">
                          <Calendar className="h-3 w-3 shrink-0" />
                          <span className="truncate">{new Date(plan.startDate).toLocaleDateString('fr-FR')}</span>
                        </div>
                        <div className="flex items-center gap-1 min-w-0">
                          <User className="h-3 w-3 shrink-0" />
                          <span className="truncate">{plan.manager}</span>
                        </div>
                      </div>
                    </div>
                  </Card>
                ))
              )}
            </div>
            
            {onboardingPlans.length > 0 && (
              <div className="pt-4 border-t mt-4">
                <CreateOnboardingPlanDialog onPlanCreated={handlePlanCreated} />
              </div>
            )}
          </CardContent>
        </Card>

        <div className="xl:col-span-3">
          {selectedPlanData ? (
            <Tabs defaultValue="administrative" className="space-y-4">
              <div className="flex flex-col lg:flex-row lg:justify-between lg:items-center gap-4">
                <TabsList className="grid w-full lg:w-auto grid-cols-2 h-10">
                  <TabsTrigger value="administrative" className="text-xs sm:text-sm">Administratif</TabsTrigger>
                  <TabsTrigger value="procedures" className="text-xs sm:text-sm">Procédures</TabsTrigger>
                </TabsList>
                <div className="flex flex-col sm:flex-row gap-2 w-full lg:w-auto">
                  {hasFilesToRemind(files) && (
                    <Button 
                      variant="outline" 
                      onClick={handleSendGlobalReminder}
                      disabled={reminderLoading === selectedPlanData.candidatId}
                      className="text-white bg-blue-600 hover:bg-blue-700 text-xs sm:text-sm h-9 px-3"
                    >
                      {reminderLoading === selectedPlanData.candidatId ? (
                        <Loader2 className="h-4 w-4 mr-2 animate-spin shrink-0" />
                      ) : (
                        <Mail className="h-4 w-4 mr-2 shrink-0" />
                      )}
                      <span className="hidden sm:inline">Envoyer un rappel</span>
                      <span className="sm:hidden">Rappel</span>
                    </Button>
                  )}
                              
                  <OnboardingTimeline 
                    employeeName={selectedPlanData.employeeName}
                    jobTitle={selectedPlanData.jobTitle}
                  />
                </div>
              </div>

              <TabsContent value="administrative" className="space-y-4">
                <CandidateFileSection 
                  key={`candidate-${selectedPlanData.candidatId}`} // Force remount when candidate changes
                  employeeName={selectedPlanData.employeeName}
                  candidatId={selectedPlanData.candidatId}
                  onProgressUpdate={handleDocumentProgressUpdate}
                />
              </TabsContent>

              <TabsContent value="procedures" className="space-y-4">
                <CompanyProceduresSection 
                  selectedCandidatId={selectedPlanData.candidatId}
                />
              </TabsContent>

            </Tabs>
          ) : (
            <Card>
              <CardContent className="text-center text-muted-foreground py-12">
                <p className="text-sm sm:text-base break-words px-4">
                  {onboardingPlans.length === 0 
                    ? "Aucun plan d'onboarding disponible. Créez-en un nouveau pour commencer."
                    : "Sélectionnez un plan d'onboarding pour voir les détails"
                  }
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}