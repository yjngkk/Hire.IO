
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Calendar, Clock, Video, Plus, Bell, AlertTriangle, Sparkles } from "lucide-react";
import { InterviewHistory } from "@/components/interviews/InterviewHistory";
import { ScheduleInterviewDialog } from "@/components/interviews/ScheduleInterviewDialog";
import { InterviewReminders } from "@/components/interviews/InterviewReminders";
import { CalendarTracker } from "@/components/interviews/CalendarTracker";
import { InterviewSummaries } from "@/components/interviews/InterviewSummaries";
import { CreateSummaryDialog } from "@/components/interviews/CreateSummaryDialog";
import { AIQuestionGenerator } from "@/components/interviews/AIQuestionGenerator";
import { InterviewDetailsDialog } from "@/components/interviews/InterviewDetailsDialog";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../config/apiService";


const suggestedQuestions = [
  "Parlez-moi de votre expérience avec React et JavaScript",
  "Comment gérez-vous les projets en équipe ?",
  "Quelles sont vos motivations pour ce poste ?",
  "Comment restez-vous à jour avec les nouvelles technologies ?",
  "Décrivez un défi technique que vous avez résolu récemment"
];

export function Interviews() {
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();
  const [showQuestionGenerator, setShowQuestionGenerator] = useState(false);
  const [showAIQuestionGenerator, setShowAIQuestionGenerator] = useState(false);
  const [showCreateSummary, setShowCreateSummary] = useState(false);
  const [selectedInterview, setSelectedInterview] = useState<any>(null);
  const [showInterviewDetails, setShowInterviewDetails] = useState(false);
  const [jobTitle, setJobTitle] = useState("");
  const [skills, setSkills] = useState("");
  const [generatedQuestions, setGeneratedQuestions] = useState<string[]>([]);
  const [showScheduleDialog, setShowScheduleDialog] = useState(false);
  const [upcoming, setUpcoming] = useState<any[]>([]);
  const [meetings, setMeetings] = useState<any[]>([]);
  const [zoomMeetings, setZoomMeetings] = useState<any[]>([]);
  const { toast } = useToast();

  const fetchUpcoming = async () => {
    if (!keycloakReady || !isAuthenticated) return;
    try {
      const data = await apiService.get("/entretiens");
      const now = new Date();
      const upcomingList = data.filter((interview: any) => {
        const interviewDate = new Date(interview.dateHeure);
        return interviewDate > now;
      }).map((interview: any) => {
        const interviewDate = new Date(interview.dateHeure);
        return {
          ...interview,
          candidateName: interview.candidat?.nom,
          jobTitle: interview.candidat?.poste,
          date: interviewDate,
          time: interviewDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          duration: interview.duree,
          interviewer: interview.interviewer,
          type: interview.type,
          notes: interview.notes,
        };
      });
      setUpcoming(upcomingList);
    } catch (error) {
      console.error("Error fetching upcoming interviews:", error);
      setUpcoming([]);
    }
  };

  const fetchMeetings = async () => {
    if (!keycloakReady || !isAuthenticated) return;
    try {
      const list = await apiService.get("/meetings/all");
      const now = new Date();
      const future = (list || []).filter((m: any) => {
        if (!m?.dateTime) return false;
        const d = new Date(m.dateTime);
        return d.getTime() > now.getTime();
      }).map((m: any) => ({
        id: m.id,
        candidateEmail: m.candidateEmail,
        interviewerEmail: m.interviewerEmail,
        organizerEmail: m.organizerEmail,
        meetingLink: m.meetingLink,
        status: m.status,
        date: new Date(m.dateTime),
      }));
      setMeetings(future);
    } catch (error) {
      console.error("Error fetching meetings:", error);
      setMeetings([]);
    }
  };

  useEffect(() => {
    if (!keycloakReady || !isAuthenticated) return;
    fetchUpcoming();
    fetchMeetings();
    fetchZoomMeetings();
  }, [keycloakReady, isAuthenticated]);

    const fetchZoomMeetings = async () => {
      if (!keycloakReady || !isAuthenticated) return;
      try {
        const data = await apiService.get("/zoom/local-meetings");
        const now = new Date();
        const upcomingZoomMeetings = (data || [])
          .filter((meeting: any) => {
            if (!meeting?.startTime) return false;
            const meetingDate = new Date(meeting.startTime);
            return meetingDate.getTime() > now.getTime();
          })
          .map((meeting: any) => ({
            meetingId: meeting.meetingId,
            topic: meeting.topic,
            joinUrl: meeting.joinUrl,
            startTime: meeting.startTime,
            meetingDate: new Date(meeting.startTime),
          }));
        setZoomMeetings(upcomingZoomMeetings);
      } catch (error) {
        console.error("Error fetching Zoom meetings:", error);
        setZoomMeetings([]);
      }
    };


  const generateQuestions = () => {
    // Simuler la génération de questions personnalisées
    const mockGenerated = [
      `Comment aborderiez-vous le développement d'une application ${jobTitle} ?`,
      `Expliquez votre expérience avec ${skills}`,
      "Quels sont vos objectifs de carrière à long terme ?",
      "Comment gérez-vous la pression et les délais serrés ?",
      "Donnez un exemple de collaboration réussie en équipe"
    ];
    setGeneratedQuestions(mockGenerated);
  };

  const getInterviewTypeBadge = (type: string) => {
    const styles = {
      video: "bg-blue-100 text-blue-800",
      phone: "bg-green-100 text-green-800",
      onsite: "bg-purple-100 text-purple-800"
    };
    
    const labels = {
      video: "Visio",
      phone: "Meet",
      onsite: "Présentiel"
    };

    return (
      <Badge className={styles[type as keyof typeof styles]}>
        {labels[type as keyof typeof labels]}
      </Badge>
    );
  };

  const viewInterviewDetails = (interview: any) => {
    setSelectedInterview(interview);
    setShowInterviewDetails(true);
  };

  const editInterview = (interview: any) => {
    toast({
      title: "Modification d'entretien",
      description: `Ouverture de l'édition pour ${interview.candidateName}`
    });
    setShowInterviewDetails(false);
    // Ici on pourrait ouvrir le formulaire de modification
  };

  const deleteInterview = (interview: any) => {
    toast({
      title: "Entretien annulé",
      description: `L'entretien avec ${interview.candidateName} a été annulé`
    });
    setShowInterviewDetails(false);
    fetchUpcoming();
    fetchMeetings();
  };

  const joinMeet = (meeting: any) => {
    if (meeting?.meetingLink) {
      window.open(meeting.meetingLink, '_blank');
    }
  };

  const joinVideoCall = (interview: any) => {
    toast({
      title: "Rejoindre l'appel",
      description: `Connexion à l'entretien avec ${interview.candidateName}`
    });
    // Simuler l'ouverture d'un lien de visioconférence
  };

  const viewAllDayInterviews = () => {
    toast({
      title: "Calendrier du jour",
      description: "Affichage de tous les entretiens de la journée"
    });
  };

  if (!keycloakReady) {
    return <div>Chargement de l'authentification...</div>;
  }
  if (!isAuthenticated) {
    return <div>Veuillez vous connecter pour accéder aux entretiens.</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Entretiens</h1>
          <p className="text-muted-foreground mt-2">
            Planifiez et gérez vos entretiens avec les candidats
          </p>
        </div>
        <div className="flex gap-2">
          <Dialog open={showCreateSummary} onOpenChange={setShowCreateSummary}>
            <DialogTrigger asChild>
              <Button variant="outline">
                <Plus className="h-4 w-4 mr-2" />
                Créer un résumé
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Créer un nouveau résumé d'entretien</DialogTitle>
              </DialogHeader>
              <CreateSummaryDialog onClose={() => setShowCreateSummary(false)} />
            </DialogContent>
          </Dialog>
          
          <Dialog open={showScheduleDialog} onOpenChange={setShowScheduleDialog}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Planifier un entretien
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Planifier un nouvel entretien</DialogTitle>
              </DialogHeader>
              <ScheduleInterviewDialog 
                onClose={() => setShowScheduleDialog(false)} 
                onSaved={() => {
                  fetchUpcoming();
                  fetchMeetings();
                }}
              />
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {/* Rappels et suivi en temps réel */}
      <div className="grid lg:grid-cols-2 gap-6">
        <InterviewReminders />
        <CalendarTracker />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Entretiens à venir</CardTitle>
            <CardDescription>
              Vos prochains rendez-vous avec les candidats
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {upcoming.length === 0 ? (
                <div className="text-center text-muted-foreground py-8">
                  Aucun entretien à venir
                </div>
              ) : (
                <>
                  {upcoming.map((interview) => (
                    <Card key={interview.id} className="p-4">
                      <div className="space-y-3">
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="font-semibold">{interview.candidateName}</h3>
                            <p className="text-sm text-muted-foreground">{interview.jobTitle}</p>
                          </div>
                          {getInterviewTypeBadge(interview.type)}
                        </div>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            <span>{interview.date.toLocaleDateString('fr-FR')}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            <span>{interview.time} ({interview.duration})</span>
                          </div>
                        </div>
                        {/*<div className="text-sm">
                          <span className="text-muted-foreground">Entretien mené par: </span>
                          <span className="font-medium">{interview.interviewer}</span>
                        </div>*/}
                        <div className="flex gap-2 pt-2">
                          <Button 
                            size="sm" 
                            variant="outline"
                            onClick={() => viewInterviewDetails(interview)}
                          >
                            Voir
                          </Button>
                        </div>
                      </div>
                    </Card>
                  ))}
                </>
              )}

            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Meetings Google Meet</CardTitle>
            <CardDescription>
              Événements planifiés via Google Calendar
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {meetings.length === 0 ? (
                <div className="text-center text-muted-foreground py-8">
                  Aucun meeting planifié
                </div>
              ) : (
                <>
                  {meetings.map((m) => (
                    <Card key={m.id} className="p-4">
                      <div className="space-y-3">
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="font-semibold">{m.candidateEmail}</h3>
                            <p className="text-sm text-muted-foreground">Organisateur: {m.organizerEmail}</p>
                          </div>
                          <Badge className="bg-blue-100 text-blue-800">{m.status || 'scheduled'}</Badge>
                        </div>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            <span>{m.date.toLocaleDateString('fr-FR')}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            <span>{m.date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                          </div>
                        </div>
                        <div className="flex gap-2 pt-2">
                          <Button size="sm" onClick={() => joinMeet(m)}>
                            <Video className="h-4 w-4 mr-2" />
                            Rejoindre
                          </Button>
                        </div>
                      </div>
                    </Card>
                  ))}
                </>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Zoom Meetings */}
      <Card className="border-2 border-blue-300 shadow-lg">
        <CardHeader>
          <div className="flex items-center gap-2">
            <Video className="h-6 w-6 text-blue-500" />
            <CardTitle className="text-blue-700">Réunions Zoom</CardTitle>
          </div>
          <CardDescription>
            Vos prochaines réunions planifiées sur Zoom
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {zoomMeetings.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                Aucune réunion Zoom à venir
              </div>
            ) : (
              <>
                {zoomMeetings.map((meeting, idx) => (
                  <Card key={meeting.meetingId || idx} className="p-4 border border-blue-200 bg-blue-50/40">
                    <div className="space-y-3">
                      <div className="flex justify-between items-center">
                        <div className="flex items-center gap-2">
                          <Video className="h-5 w-5 text-blue-500" />
                          <h3 className="font-bold text-lg text-blue-700">{meeting.topic}</h3>
                        </div>
                        <Badge className="bg-blue-200 text-blue-800 shadow">Zoom</Badge>
                      </div>
                      <p className="text-sm text-blue-900 font-semibold">
                        {meeting.meetingId ? `ID: ${meeting.meetingId}` : ''}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3 text-blue-500" />
                          <span className="font-medium text-blue-700">{meeting.meetingDate.toLocaleDateString('fr-FR')}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3 text-blue-500" />
                          <span className="font-medium text-blue-700">{meeting.meetingDate.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                      </div>
                      <div className="flex gap-2 pt-2">
                        <Button 
                          size="sm" 
                          className="bg-blue-500 text-white hover:bg-blue-600"
                          onClick={() => window.open(meeting.joinUrl, '_blank')}
                        >
                          <Video className="h-4 w-4 mr-2" />
                          Rejoindre la réunion
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          className="border-blue-300 text-blue-700 hover:bg-blue-100"
                          onClick={() => navigator.clipboard.writeText(meeting.joinUrl)}
                        >
                          Copier le lien
                        </Button>
                      </div>
                    </div>
                  </Card>
                ))}
              </>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Historique des entretiens */}
      <InterviewHistory />

      {/* Résumés des entretiens */}
      <InterviewSummaries />

      {/* Dialog pour les détails d'entretien */}
      <Dialog open={showInterviewDetails} onOpenChange={setShowInterviewDetails}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Détails de l'entretien</DialogTitle>
          </DialogHeader>
          {selectedInterview && (
            <InterviewDetailsDialog
              interview={selectedInterview}
              onEdit={() => editInterview(selectedInterview)}
              onDelete={() => deleteInterview(selectedInterview)}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
