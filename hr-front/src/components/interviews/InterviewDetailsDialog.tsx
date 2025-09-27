import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, User, Video, Phone, MapPin, Edit, Trash, Copy } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";

interface InterviewDetailsDialogProps {
  interview: any;
  onEdit: () => void;
  onDelete: () => void;
}

export function InterviewDetailsDialog({ interview, onEdit, onDelete }: InterviewDetailsDialogProps) {
  const { toast } = useToast();
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  const getInterviewTypeIcon = (type: string) => {
    const icons = {
      video: Video,
      phone: Phone,
      onsite: MapPin
    };
    return icons[type as keyof typeof icons] || Video;
  };

  const getInterviewTypeBadge = (type: string) => {
    const styles = {
      video: "bg-blue-100 text-blue-800",
      phone: "bg-green-100 text-green-800",
      onsite: "bg-purple-100 text-purple-800"
    };
    
    const labels = {
      video: "Visioconférence",
      phone: "Meet",
      onsite: "Présentiel"
    };

    const Icon = getInterviewTypeIcon(type);

    return (
      <Badge className={styles[type as keyof typeof styles]}>
        <Icon className="h-3 w-3 mr-1" />
        {labels[type as keyof typeof labels]}
      </Badge>
    );
  };

  const joinVideoCall = () => {
    toast({
      title: "Rejoindre l'appel",
      description: "Ouverture de la visioconférence..."
    });
    window.open("https://meet.google.com/sample-meeting", "_blank");
  };

  const callCandidate = () => {
    toast({
      title: "Appel en cours",
      description: `Appel de ${interview.candidateName}...`
    });
  };

  const handleDelete = async () => {
    if (window.confirm("Voulez-vous vraiment annuler cet entretien ?")) {
      try {
        await apiService.delete(`/entretiens/${interview.id}`);
        toast({ 
          title: "Entretien annulé", 
          description: "L'entretien a bien été supprimé." 
        });
        onDelete();
      } catch (error) {
        toast({ 
          title: "Erreur", 
          description: error instanceof Error ? error.message : "Erreur lors de la suppression" 
        });
      }
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex justify-between items-start">
            <div>
              <CardTitle className="text-xl">{interview.candidateName}</CardTitle>
              <CardDescription className="text-base mt-1">
                Entretien pour le poste de {interview.jobTitle}
              </CardDescription>
            </div>
            {(interview.type === "phone" || interview.type === "meet") ? (
              new Date(interview.date) > new Date() ? (
                getInterviewTypeBadge(interview.type)
              ) : (
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    className="bg-green-500 hover:bg-green-600 text-white border-none flex items-center gap-2"
                    onClick={() => {
                      if (!interview.candidat?.telephone) return;
                      const cleanPhone = interview.candidat.telephone.replace(/\s|\-|\(|\)/g, '');
                      let phoneNumber = cleanPhone;
                      if (phoneNumber.startsWith('+')) {
                        phoneNumber = phoneNumber.substring(1);
                      } else if (phoneNumber.startsWith('0')) {
                        phoneNumber = '212' + phoneNumber.substring(1);
                      }
                      const callUrl = `whatsapp://call?phone=${phoneNumber}`;
                      const link = document.createElement('a');
                      link.href = callUrl;
                      link.target = '_blank';
                      document.body.appendChild(link);
                      link.click();
                      document.body.removeChild(link);
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
                    Appel WhatsApp
                  </Button>
                </div>
              )
            ) : (
              getInterviewTypeBadge(interview.type)
            )}
          </div>
          {(interview.type === "meet" || interview.type === "phone") && interview.meetLink && (
            <div className="flex items-center gap-2 mt-2">
              <Button
                asChild
                className="bg-green-600 hover:bg-green-700 text-white"
                size="sm"
              >
                <a
                  href={interview.meetLink}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center"
                >
                  <Video className="h-4 w-4 mr-1" />
                  Rejoindre la réunion
                </a>
              </Button>
              <Button
                size="icon"
                variant="outline"
                onClick={() => {
                  navigator.clipboard.writeText(interview.meetLink);
                  toast({
                    title: "Lien copié",
                    description: "Le lien de la réunion a été copié dans le presse-papiers"
                  });
                }}
                title="Copier le lien"
              >
                <Copy className="h-4 w-4" />
              </Button>
            </div>
          )}
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm">
                {interview.dateHeure
                  ? new Date(interview.dateHeure).toLocaleDateString('fr-FR', {
                      weekday: 'long',
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric'
                    })
                  : ""}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm">
                {interview.dateHeure
                  ? new Date(interview.dateHeure).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                  : ""}
                {interview.duration && interview.duration !== "undefined" ? ` (${interview.duration})` : ""}
              </span>
            </div>
            <div className="flex items-center gap-2 col-span-2">
              <User className="h-4 w-4 text-muted-foreground" />
              {interview.candidat && (
                <>
                  <span className="text-sm font-medium">{interview.candidat.nom}</span>
                  {interview.candidat.email && (
                    <span className="text-xs text-muted-foreground ml-2">{interview.candidat.email}</span>
                  )}
                  {interview.candidat.poste && (
                    <span className="text-xs text-muted-foreground ml-2">({interview.candidat.poste})</span>
                  )}
                </>
              )}
            </div>
           {/* <div className="flex items-center gap-2 col-span-2">
              <User className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm">Interviewer: {interview.interviewer}</span>
            </div>*/}
          </div>
          
          {interview.notes && (
            <div className="p-3 bg-muted/50 rounded-lg">
              <h4 className="font-medium text-sm mb-1">Notes</h4>
              <p className="text-sm text-muted-foreground">{interview.notes}</p>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="flex gap-2">
        {interview.type === "video" && (
          <Button onClick={joinVideoCall} className="flex-1">
            <Video className="h-4 w-4 mr-2" />
            Rejoindre l'appel vidéo
          </Button>
        )}

        <Button variant="outline" onClick={onEdit}>
          <Edit className="h-4 w-4 mr-2" />
          Modifier
        </Button>
        <Button variant="outline" onClick={handleDelete}>
          <Trash className="h-4 w-4 mr-2" />
          Annuler
        </Button>
      </div>
    </div>
  );
}