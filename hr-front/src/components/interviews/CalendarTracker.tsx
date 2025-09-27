import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Clock, Play, Pause, CheckCircle, AlertCircle, Calendar, Bell } from "lucide-react";
import { useState, useEffect } from "react";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";

export function CalendarTracker() {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [interviews, setInterviews] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();
  const [showPauseDialog, setShowPauseDialog] = useState(false);
  const [selectedInterviewId, setSelectedInterviewId] = useState<number | null>(null);
  const [pauseDuration, setPauseDuration] = useState(5); // Durée par défaut de 5 minutes


  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 30000); // Update every 30 seconds
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const fetchInterviews = async () => {
      try {
        const data = await apiService.get("/entretiens");
        
        // Filter for today
        const now = new Date();
        const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const todayEnd = new Date(todayStart.getTime() + 24 * 60 * 60 * 1000);

        const interviewsToday = data.filter((interview: any) => {
          const interviewDate = new Date(interview.dateHeure);
          return interviewDate >= todayStart && interviewDate < todayEnd;
        }).map((interview: any) => {
          const interviewDate = new Date(interview.dateHeure);
          // Parse duration (e.g. "1h", "45min")
          let durationMin = 60;
          if (interview.duree) {
            if (interview.duree.includes('h')) {
              const [h, min] = interview.duree.split('h');
              durationMin = parseInt(h) * 60 + (min ? parseInt(min) : 0);
            } else if (interview.duree.includes('min')) {
              durationMin = parseInt(interview.duree);
            }
          }
          const endDate = new Date(interviewDate.getTime() + durationMin * 60000);
          
          let status = 'in-progress';
          if (interview.statut === 'terminé') {
            status = 'completed';
          } else if (interview.statut === 'pause') {
            status = 'paused';
          } else if (currentTime < interviewDate) {
            status = 'upcoming';
          } else if (currentTime >= endDate && interview.statut !== 'terminé') {
            status = 'overdue';
          }
          
          return {
            ...interview,
            candidateName: interview.candidat?.nom,
            time: interviewDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            duration: durationMin,
            startedAt: status === 'in-progress' ? interviewDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : null,
            expectedEnd: endDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            status,
          };
        });
        setInterviews(interviewsToday);
      } catch (error) {
        console.error("Error fetching interviews:", error);
        setInterviews([]);
      }
    };

    if (keycloakReady && isAuthenticated) {
      fetchInterviews();
    }
  }, [currentTime, keycloakReady, isAuthenticated]);

  const getCurrentTimeString = () => {
    return currentTime.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle className="h-4 w-4 text-green-600" />;
      case "in-progress":
        return <Play className="h-4 w-4 text-blue-600" />;
      case "paused":
        return <Pause className="h-4 w-4 text-orange-600" />;
      case "overdue":
        return <Clock className="h-4 w-4 text-gray-400" />;
      default:
        return <Clock className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusBadge = (status: string) => {
    const styles = {
      completed: "bg-green-100 text-green-800",
      "in-progress": "bg-blue-100 text-blue-800",
      paused: "bg-orange-100 text-orange-800",
      upcoming: "bg-gray-100 text-gray-800",
      overdue: "bg-gray-100 text-gray-800"
    };
    
    const labels = {
      completed: "Terminé",
      "in-progress": "En cours",
      paused: "En pause",
      upcoming: "À venir",
      overdue: "À venir"
    };

    return (
      <Badge className={styles[status as keyof typeof styles]}>
        {labels[status as keyof typeof labels]}
      </Badge>
    );
  };

  const calculateProgress = (interview: any) => {
  if (interview.status !== "in-progress") return 0;
  
  const interviewDate = new Date(interview.dateHeure);
  const endDate = new Date(interviewDate.getTime() + interview.duration * 60000);
  
  const totalDuration = endDate.getTime() - interviewDate.getTime();
  const elapsed = currentTime.getTime() - interviewDate.getTime();
  
  return Math.min((elapsed / totalDuration) * 100, 100);
};

  const activeInterview = interviews.find(i => i.status === "in-progress");
  const activeInterviews = interviews.filter(i => i.status === "in-progress" || i.status === "paused");

  const sendReminder = async (interviewId: number) => {
    setLoading(true);
    try {
      await apiService.post(`/entretiens/${interviewId}/rappel`);
      alert('Rappel envoyé avec succès');
    } catch (error) {
      console.error('Erreur lors de l\'envoi du rappel:', error);
      alert('Erreur lors de l\'envoi du rappel');
    } finally {
      setLoading(false);
    }
  };

  const terminateInterview = async (interviewId: number) => {
    setLoading(true);
    try {
      await apiService.patch(`/entretiens/${interviewId}/terminer`);
      alert('Entretien terminé avec succès');
      setInterviews(prevInterviews => 
        prevInterviews.map(interview => 
          interview.id === interviewId 
            ? { ...interview, status: 'completed' }
            : interview
        )
      );
    } catch (error) {
      console.error('Erreur lors de la terminaison de l\'entretien:', error);
      alert('Erreur lors de la terminaison de l\'entretien');
    } finally {
      setLoading(false);
    }
  };

  const pauseInterview = async (interviewId: number, duration: number = 5) => {
  setLoading(true);
  try {
    await apiService.patch(`/entretiens/${interviewId}/pause?duration=${duration}`);
    alert(`Entretien mis en pause pour ${duration} minutes`);
    setInterviews(prevInterviews => 
      prevInterviews.map(interview => 
        interview.id === interviewId 
          ? { ...interview, status: 'paused' }
          : interview
      )
    );
  } catch (error) {
    console.error('Erreur lors de la mise en pause de l\'entretien:', error);
    alert('Erreur lors de la mise en pause de l\'entretien');
  } finally {
    setLoading(false);
    setShowPauseDialog(false);
  }
};

  const resumeInterview = async (interviewId: number) => {
    setLoading(true);
    try {
      await apiService.patch(`/entretiens/${interviewId}/reprendre`);
      alert('Entretien repris avec succès');
      setInterviews(prevInterviews => 
        prevInterviews.map(interview => 
          interview.id === interviewId 
            ? { ...interview, status: 'in-progress' }
            : interview
        )
      );
    } catch (error) {
      console.error('Erreur lors de la reprise de l\'entretien:', error);
      alert('Erreur lors de la reprise de l\'entretien');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Calendar className="h-5 w-5" />
          Suivi du planning
        </CardTitle>
        <CardDescription>
          Temps réel • {getCurrentTimeString()}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {activeInterviews.length > 0 && (
          <div className="space-y-3">
            <h3 className="font-medium text-blue-900">Entretiens en cours</h3>
            {activeInterviews.map((interview) => (
              <div key={interview.id} className="p-4 border rounded-lg bg-blue-50">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="font-semibold text-blue-900">Entretien en cours</h3>
                  <Badge className="bg-blue-100 text-blue-800">
                    {interview.status === "paused" ? (
                      <Pause className="h-3 w-3 mr-1" />
                    ) : (
                      <Play className="h-3 w-3 mr-1" />
                    )}
                    {interview.status === "paused" ? "En pause" : "En cours"}
                  </Badge>
                </div>
                
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span>{interview.candidateName}</span>
                    <span>{interview.time} - {interview.expectedEnd}</span>
                  </div>
                  
                  <Progress value={calculateProgress(interview)} className="h-2" />
                  
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Commencé à {interview.startedAt}</span>
                    <span>{Math.round(calculateProgress(interview))}% du temps écoulé</span>
                  </div>
                </div>
                
                <div className="flex gap-2 mt-3">
                  {interview.status === 'in-progress' ? (
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={() => {
                        setSelectedInterviewId(interview.id);
                        setShowPauseDialog(true);
                      }}
                      disabled={loading}
                    >
                      <Pause className="h-3 w-3 mr-1" />
                      Suspendre
                    </Button>
                  ) : interview.status === 'paused' ? (
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={() => resumeInterview(interview.id)}
                      disabled={loading}
                    >
                      <Play className="h-3 w-3 mr-1" />
                      Reprendre
                    </Button>
                  ) : null}
                  <Button 
                    size="sm"
                    onClick={() => terminateInterview(interview.id)}
                    disabled={loading}
                  >
                    <CheckCircle className="h-3 w-3 mr-1" />
                    Terminer un entretien
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="space-y-3">
          <h3 className="font-medium">Planning du jour</h3>
          
          {interviews.map((interview) => (
            <div
              key={interview.id}
              className={`flex items-center justify-between p-3 rounded-lg border ${
                interview.status === "in-progress" ? "bg-blue-50 border-blue-200" : 
                interview.status === "paused" ? "bg-orange-50 border-orange-200" :
                interview.status === "completed" ? "bg-green-50 border-green-200" :
                "bg-card"
              }`}
            >
              <div className="flex items-center gap-3">
                {getStatusIcon(interview.status)}
                <div>
                  <p className="font-medium">{interview.candidateName}</p>
                  <p className="text-sm text-muted-foreground">
                    {interview.time} ({interview.duration}min)
                  </p>
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                {getStatusBadge(interview.status)}
                {interview.status === "upcoming" && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      if (interview.meetLink) {
                        window.open(interview.meetLink, "_blank");
                      } else {
                        alert("Aucun lien Meet disponible pour cet entretien.");
                      }
                    }}
                  >
                    Démarrer
                  </Button>
                )}
                {interview.status === "in-progress" && (
                  <Button 
                    size="sm" 
                    variant="outline"
                    onClick={() => sendReminder(interview.id)}
                    disabled={loading}
                  >
                    <Bell className="h-3 w-3 mr-1" />
                    Rappel
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
        
        <div className="pt-4 border-t">
          <div className="grid grid-cols-4 gap-4 text-center text-sm">
            <div>
              <div className="font-semibold text-green-600">{interviews.filter(i => i.status === 'completed').length}</div>
              <div className="text-muted-foreground">Terminé</div>
            </div>
            <div>
              <div className="font-semibold text-blue-600">{interviews.filter(i => i.status === 'in-progress').length}</div>
              <div className="text-muted-foreground">En cours</div>
            </div>
            <div>
              <div className="font-semibold text-orange-600">{interviews.filter(i => i.status === 'paused').length}</div>
              <div className="text-muted-foreground">En pause</div>
            </div>
            <div>
              <div className="font-semibold text-gray-600">{interviews.filter(i => i.status === 'upcoming' || i.status === 'overdue').length}</div>
              <div className="text-muted-foreground">À venir</div>
            </div>
          </div>
        </div>
      </CardContent>
      {/* Dialogue de sélection de durée de pause */}
      <Dialog open={showPauseDialog} onOpenChange={setShowPauseDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Durée de la pause</DialogTitle>
            <DialogDescription>
              Sélectionnez la durée de pause pour cet entretien
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              {[5, 10, 15, 20, 25, 30].map((duration) => (
                <Button
                  key={duration}
                  variant={pauseDuration === duration ? "default" : "outline"}
                  onClick={() => setPauseDuration(duration)}
                >
                  {duration} minutes
                </Button>
              ))}
            </div>
            
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setShowPauseDialog(false)}>
                Annuler
              </Button>
              <Button 
                onClick={() => selectedInterviewId && pauseInterview(selectedInterviewId, pauseDuration)}
                disabled={loading}
              >
                Confirmer
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  );
}