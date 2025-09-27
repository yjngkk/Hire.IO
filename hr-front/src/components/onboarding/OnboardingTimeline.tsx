
import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, User, FileText, CheckCircle, AlertCircle, Building } from "lucide-react";

interface OnboardingTimelineProps {
  employeeName: string;
  jobTitle: string;
}

export function OnboardingTimeline({ employeeName, jobTitle }: OnboardingTimelineProps) {
  const [isOpen, setIsOpen] = useState(false);

  // Timeline d'onboarding simulée
  const timelineEvents = [
    {
      id: 1,
      type: "documents",
      title: "Documents administratifs",
      description: "Collecte et vérification des documents requis",
      date: "2024-02-01T09:00:00",
      icon: FileText,
      status: "completed"
    },
    {
      id: 2,
      type: "contract",
      title: "Signature du contrat",
      description: "Contrat de travail signé et validé",
      date: "2024-02-01T14:00:00",
      icon: CheckCircle,
      status: "completed"
    },
    {
      id: 3,
      type: "orientation",
      title: "Présentation de l'entreprise",
      description: "Tour des locaux et présentation des équipes",
      date: "2024-02-02T09:00:00",
      icon: Building,
      status: "completed"
    },
    {
      id: 4,
      type: "procedures",
      title: "Formation aux procédures",
      description: "Formation sur les procédures internes et outils",
      date: "2024-02-02T14:00:00",
      icon: User,
      status: "in-progress"
    },
    {
      id: 5,
      type: "technical",
      title: "Formation technique",
      description: `Formation technique spécifique au poste de ${jobTitle}`,
      date: "2024-02-05T09:00:00",
      icon: AlertCircle,
      status: "pending"
    },
    {
      id: 6,
      type: "evaluation",
      title: "Evaluation 30 jours",
      description: "Premier point d'évaluation après 30 jours",
      date: "2024-03-01T10:00:00",
      icon: CheckCircle,
      status: "pending"
    },
    {
      id: 7,
      type: "evaluation",
      title: "Evaluation 90 jours",
      description: "Évaluation finale de la période d'essai",
      date: "2024-05-01T10:00:00",
      icon: CheckCircle,
      status: "pending"
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed": return "bg-green-100 text-green-800";
      case "in-progress": return "bg-blue-100 text-blue-800";
      case "pending": return "bg-yellow-100 text-yellow-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "completed": return "Terminé";
      case "in-progress": return "En cours";
      case "pending": return "À venir";
      default: return "Inconnu";
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button>
          <Clock className="h-4 w-4 mr-2" />
          Voir timeline complète
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Timeline d'onboarding - {employeeName}</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4">
          <div className="text-sm text-muted-foreground mb-6">
            Suivi complet du processus d'intégration pour le poste de {jobTitle}
          </div>
          
          {timelineEvents.map((event, index) => {
            const Icon = event.icon;
            return (
              <Card key={event.id} className="relative">
                <CardContent className="p-4">
                  <div className="flex items-start gap-4">
                    <div className="flex-shrink-0">
                      <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                        <Icon className="h-5 w-5 text-primary" />
                      </div>
                    </div>
                    <div className="flex-1 space-y-2">
                      <div className="flex items-center justify-between">
                        <h3 className="font-semibold">{event.title}</h3>
                        <Badge className={getStatusColor(event.status)}>
                          {getStatusLabel(event.status)}
                        </Badge>
                      </div>
                      <p className="text-muted-foreground text-sm">{event.description}</p>
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Calendar className="h-3 w-3" />
                        <span>
                          {new Date(event.date).toLocaleDateString('fr-FR', {
                            day: 'numeric',
                            month: 'long',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </span>
                      </div>
                    </div>
                  </div>
                </CardContent>
                {/* Ligne de connexion */}
                {index < timelineEvents.length - 1 && (
                  <div className="absolute left-9 top-16 w-0.5 h-4 bg-border"></div>
                )}
              </Card>
            );
          })}
        </div>
        
        <div className="mt-6 p-4 bg-muted/50 rounded-lg">
          <div className="text-sm font-medium mb-2">Progression globale</div>
          <div className="flex gap-4 text-xs text-muted-foreground">
            <div className="flex items-center gap-1">
              <div className="w-2 h-2 bg-green-500 rounded-full"></div>
              <span>3 terminés</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
              <span>1 en cours</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
              <span>3 à venir</span>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
