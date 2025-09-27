
import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, User, FileText, Mail, Phone, Video } from "lucide-react";
import apiconfig from "@/config/apiService";

interface CandidateTimelineProps {
  candidateName: string;
  candidateId: number;
}

export function CandidateTimeline({ candidateName ,candidateId }: CandidateTimelineProps) {
  const [isOpen, setIsOpen] = useState(false);
    const [timelineEvents, setTimelineEvents] = useState([]);

  const fetchTimelineEvents = async () => {
    try {
      const response = await apiconfig.get(`/timeline/candidat/${candidateId}`);
      console.log(response);
      setTimelineEvents(response);
    } catch (err) {
      console.log('Impossible de charger la timeline');
      
    } 
  };
  useEffect(() => {
    if (isOpen && candidateId) {
      fetchTimelineEvents();
    }
  }, [isOpen, candidateId]);
  const getIconByEventType = (eventType: string) => {
    switch (eventType) {
      case "CANDIDATURE_RECEIVED":
        return FileText;
      case "CV_REVIEWED":
        return User;
      case "EMAIL_SENT":
        return Mail;
      case "INTERVIEW_SCHEDULED":
        return Phone;
      case "TECHNICAL_INTERVIEW":
        return Video;
      default:
        return FileText;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "COMPLETED": return "bg-green-100 text-green-800";
      case "SCHEDULED": return "bg-blue-100 text-blue-800";
      case "PENDING": return "bg-yellow-100 text-yellow-800";
      case "CANCELLED": return "bg-red-100 text-yellow-800"
      default: return "bg-gray-100 text-gray-800";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "COMPLETED": return "Terminé";
      case "SCHEDULED": return "Planifié";
      case "PENDING": return "En attente";
      case "CANCELLED": return "Annulé";

      default: return "Inconnu";
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          <Clock className="h-4 w-4 mr-2" />
          Timeline 
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Timeline - {candidateName}</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4">
          {timelineEvents.map((event, index) => {
            const Icon = getIconByEventType(event.eventType);

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
                        <Badge className={getStatusColor(event?.status)}>
                          {getStatusLabel(event.status)}
                        </Badge>
                      </div>
                      <p className="text-muted-foreground text-sm">{event.description}</p>
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Calendar className="h-3 w-3" />
                        <span>
                          {event.formattedDate}
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
      </DialogContent>
    </Dialog>
  );
}
