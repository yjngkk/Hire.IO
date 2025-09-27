import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Bell, Clock, AlertTriangle, Calendar } from "lucide-react";
import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { InterviewDetailsDialog } from "@/components/interviews/InterviewDetailsDialog";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";

export function InterviewReminders() {
  // Fonction utilitaire pour ouvrir WhatsApp
  const callCandidateWhatsApp = (phone: string) => {
    if (!phone) return;
    window.open(`https://wa.me/${phone}`, '_blank');
  };
  const [currentTime, setCurrentTime] = useState(new Date());
  const [urgentReminders, setUrgentReminders] = useState<any[]>([]);
  const [selectedInterview, setSelectedInterview] = useState<any>(null);
  const [showDetails, setShowDetails] = useState(false);

  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 60000); // Update every minute

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const fetchInterviews = async () => {
      try {
        const data = await apiService.get("/entretiens");
        
        // Filter interviews for today and tomorrow
        const now = new Date();
        const todayStr = now.toISOString().split('T')[0];
        const tomorrow = new Date(now);
        tomorrow.setDate(now.getDate() + 1);
        const tomorrowStr = tomorrow.toISOString().split('T')[0];
        
        const interviewsNext2Days = data.filter((interview: any) => {
          const interviewDate = new Date(interview.dateHeure);
          const interviewDayStr = interviewDate.toISOString().split('T')[0];
          return interviewDayStr === todayStr || interviewDayStr === tomorrowStr;
        }).map((interview: any) => {
          const interviewDate = new Date(interview.dateHeure);
          const diffMs = interviewDate.getTime() - now.getTime();
          const timeUntil = Math.round(diffMs / 60000); // in minutes
          return { ...interview, timeUntil };
        });

        // Group by candidate name
        const grouped: { [key: string]: any[] } = {};
        interviewsNext2Days.forEach((interview: any) => {
          if (!grouped[interview.candidat?.nom]) grouped[interview.candidat?.nom] = [];
          grouped[interview.candidat?.nom].push(interview);
        });

        // Flatten grouped interviews for display
        const groupedList = Object.values(grouped).flat();
        setUrgentReminders(groupedList);
      } catch (error) {
        console.error("Error fetching interviews:", error);
        setUrgentReminders([]);
      }
    };

    if (keycloakReady && isAuthenticated) {
      fetchInterviews();
    }
  }, [currentTime, keycloakReady, isAuthenticated]);

  const formatTimeUntil = (minutes: number) => {
    if (minutes < 60) {
      return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}min` : `${hours}h`;
  };

  const getReminderUrgency = (minutes: number) => {
    if (minutes <= 30) return { color: "bg-red-100 text-red-800", icon: AlertTriangle };
    if (minutes <= 60) return { color: "bg-orange-100 text-orange-800", icon: Clock };
    return { color: "bg-blue-100 text-blue-800", icon: Bell };
  };

  const viewAllDayInterviews = () => {
    // Simuler l'ouverture du calendrier complet du jour
    console.log("Ouverture du calendrier complet du jour");
  };

  const viewInterview = (interview: any) => {
    setSelectedInterview(interview);
    setShowDetails(true);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Bell className="h-5 w-5" />
          Rappels d'entretiens
        </CardTitle>
        <CardDescription>
          Entretiens prévus dans les prochaines heures
        </CardDescription>
      </CardHeader>
      <CardContent>
        {urgentReminders.length === 0 ? (
          <div className="text-center py-8">
            <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground">Aucun entretien prévu aujourd'hui ou demain</p>
          </div>
        ) : (
          <div className="space-y-3">
            {urgentReminders.map((interview) => {
              const urgency = getReminderUrgency(interview.timeUntil);
              const UrgencyIcon = urgency.icon;
              const isMeetType = interview.type === "meet" || interview.type === "phone";
              const isFuture = new Date(interview.dateHeure) > new Date();

              return (
                <div
                  key={interview.id}
                  className="flex items-center justify-between p-3 rounded-lg border bg-card"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-full bg-primary/10">
                      <UrgencyIcon className="h-4 w-4 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium">{interview.candidat?.nom}</p>
                      <p className="text-sm text-muted-foreground">
                        {interview.candidat?.poste} • {new Date(interview.dateHeure).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {isMeetType ? (
                      isFuture ? (
                        <Badge className={urgency.color}>Meet</Badge>
                      ) : (
                        <>
                          <Button
                            size="sm"
                            variant="outline"
                            className="bg-green-500 hover:bg-green-600 text-white border-none flex items-center gap-2"
                            onClick={() => {
                              if (!interview.candidat?.telephone) return;

                              const cleanPhone = interview.candidat.telephone.replace(/[\s\-\(\)]/g, '');
                              let phoneNumber = cleanPhone;
                              if (phoneNumber.startsWith('+')) {
                                phoneNumber = phoneNumber.substring(1);
                              } else if (phoneNumber.startsWith('0')) {
                                // Convertir 0XXXXXXXXX en 212XXXXXXXXX (Maroc)
                                phoneNumber = '212' + phoneNumber.substring(1);
                              }

                              // Essayer d'abord l'appel direct WhatsApp
                              const callUrl = `whatsapp://call?phone=${phoneNumber}`;

                              const link = document.createElement('a');
                              link.href = callUrl;
                              link.target = '_blank';
                              document.body.appendChild(link);
                              link.click();
                              document.body.removeChild(link);

                              // Fallback si ça marche pas
                              setTimeout(() => {
                                const whatsappUrl = `https://wa.me/${phoneNumber}`;
                                window.open(whatsappUrl, '_blank');
                              }, 1000);
                            }}
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="18" height="18">
                              <path
                                fill="white"
                                d="M16 3C9.373 3 4 8.373 4 15c0 2.385.693 4.607 2.01 6.563L4 29l7.646-2.523A12.96 12.96 0 0 0 16 27c6.627 0 12-5.373 12-12S22.627 3 16 3zm0 22c-1.813 0-3.584-.484-5.12-1.402l-.366-.217-4.543 1.498 1.5-4.424-.238-.376C6.484 18.584 6 16.813 6 15c0-5.514 4.486-10 10-10s10 4.486 10 10-4.486 10-10 10zm5.297-7.297c-.297-.149-1.757-.867-2.029-.967-.273-.099-.472-.148-.67.15-.198.297-.767.967-.94 1.166-.173.198-.347.223-.644.075-.297-.149-1.255-.463-2.39-1.477-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.447-.52.149-.173.198-.298.298-.497.099-.198.05-.372-.025-.521-.075-.149-.67-1.617-.917-2.217-.242-.582-.487-.502-.67-.511l-.571-.011c-.198 0-.52.075-.792.372-.272.297-1.04 1.016-1.04 2.479s1.065 2.876 1.213 3.078c.149.198 2.099 3.205 5.088 4.364.712.274 1.267.438 1.701.561.715.228 1.366.196 1.88.119.574-.085 1.757-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"
                              />
                            </svg>
                            Appeler le candidat
                          </Button>

                        </>
                      )
                    ) : (
                      <Badge className={urgency.color}>
                        Dans {formatTimeUntil(interview.timeUntil)}
                      </Badge>
                    )}
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={() => viewInterview(interview)}
                    >
                      Voir
                    </Button>
                  </div>
                </div>
              );
            })}

            <div className="pt-4 border-t">
              <Button 
                variant="outline" 
                className="w-full"
                onClick={viewAllDayInterviews}
              >
                <Calendar className="h-4 w-4 mr-2" />
                Voir tous les entretiens du jour
              </Button>
            </div>
          </div>
        )}
      </CardContent>
      
      {/* Dialog for interview details */}
      <Dialog open={showDetails} onOpenChange={setShowDetails}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Détails de l'entretien</DialogTitle>
          </DialogHeader>
          {selectedInterview && (
            <InterviewDetailsDialog
              interview={selectedInterview}
              onEdit={() => setShowDetails(false)}
              onDelete={() => setShowDetails(false)}
            />
          )}
        </DialogContent>
      </Dialog>
    </Card>
  );
}