
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { Mail, Phone, MessageSquare, Video, ChevronDown, MessageCircle, VideoOff } from "lucide-react";
import apiService from "@/config/apiService";
import { keycloak } from "@/KeycloakProvider";

interface ContactOptionsProps {
  candidateName: string;
  candidateEmail: string;
  candidatePhone?: string;
  candidatId: number;
}

export function ContactOptions({ candidateName, candidateEmail, candidatePhone, candidatId }: ContactOptionsProps) {
  const [emailDialogOpen, setEmailDialogOpen] = useState(false);
  const [meetDialogOpen, setMeetDialogOpen] = useState(false);
  const [emailSubject, setEmailSubject] = useState("");
  const [emailMessage, setEmailMessage] = useState("");
  const [meetDate, setMeetDate] = useState("");
  const [meetTime, setMeetTime] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [smsDialogOpen, setSmsDialogOpen] = useState(false);
  const [smsBody, setSmsBody] = useState("");
  const [smsError, setSmsError] = useState<string | null>(null);
  const [zoomDialogOpen, setZoomDialogOpen] = useState(false);
  const [zoomTopic, setZoomTopic] = useState("");
  const [zoomDateTime, setZoomDateTime] = useState("");

  const handleWhatsApp = () => {
    if (!candidatePhone) {
      toast.error("Numéro de téléphone non disponible pour ce candidat");
      return;
    }

    // Nettoyer le numéro de téléphone (supprimer espaces, tirets, etc.)
    const cleanPhone = candidatePhone.replace(/[\s\-\(\)]/g, '');
    
    // S'assurer que le numéro commence par le code pays
    let phoneNumber = cleanPhone;
    if (!phoneNumber.startsWith('+')) {
      if (phoneNumber.startsWith('0')) {
        // Remplacer le 0 par +33 pour la France
        phoneNumber = '+33' + phoneNumber.substring(1);
      } else if (!phoneNumber.startsWith('33')) {
        // Ajouter +33 si pas de code pays
        phoneNumber = '+33' + phoneNumber;
      } else {
        phoneNumber = '+' + phoneNumber;
      }
    }

    const message = encodeURIComponent(`Bonjour ${candidateName}, nous avons examiné votre candidature et souhaitons échanger avec vous.`);
    const whatsappUrl = `https://wa.me/${phoneNumber.replace('+', '')}?text=${message}`;
    
    window.open(whatsappUrl, '_blank');
    toast.success("Ouverture de WhatsApp");
  };

  const handlePhone = () => {
    if (!candidatePhone) {
      toast.error("Numéro de téléphone non disponible pour ce candidat");
      return;
    }

    // Nettoyer le numéro de téléphone (supprimer espaces, tirets, etc.)
    const cleanPhone = candidatePhone.replace(/[\s\-\(\)]/g, '');
    
    // S'assurer que le numéro commence par le code pays
    let phoneNumber = cleanPhone;
    if (!phoneNumber.startsWith('+')) {
      if (phoneNumber.startsWith('0')) {
        // Remplacer le 0 par +33 pour la France
        phoneNumber = '+33' + phoneNumber.substring(1);
      } else if (!phoneNumber.startsWith('33')) {
        // Ajouter +33 si pas de code pays
        phoneNumber = '+33' + phoneNumber;
      } else {
        phoneNumber = '+' + phoneNumber;
      }
    }

    window.location.href = `tel:${phoneNumber}`;
    toast.success("Ouverture de l'application téléphone");
  };

  const handleWhatsAppCall = () => {
    if (!candidatePhone) {
      toast.error("Numéro de téléphone non disponible pour ce candidat");
      return;
    }

    const cleanPhone = candidatePhone.replace(/[\s\-\(\)]/g, '');
    let phoneNumber = cleanPhone;
    if (phoneNumber.startsWith('+')) {
      phoneNumber = phoneNumber.substring(1);
    } else if (phoneNumber.startsWith('0')) {
      // Convertir 0XXXXXXXXX en 212XXXXXXXXX (Maroc)
      phoneNumber = '212' + phoneNumber.substring(1);
    }

    // Essayer d'abord l'appel direct WhatsApp
    const callUrl = `whatsapp://call?phone=${phoneNumber}`;
    
    // Créer un lien temporaire pour forcer l'ouverture
    const link = document.createElement('a');
    link.href = callUrl;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // Fallback après 1 seconde si l'appel ne fonctionne pas
    setTimeout(() => {
      // Ouvrir WhatsApp sans message pour permettre l'appel manuel
      const whatsappUrl = `https://wa.me/${phoneNumber}`;
      window.open(whatsappUrl, '_blank');
    }, 1000);

    toast.success("Ouverture de WhatsApp pour appel");
  };

  const handleSendEmail = () => {
    if (!emailSubject || !emailMessage) {
      toast.error("Veuillez remplir tous les champs");
      return;
    }
    // Simulation d'envoi d'email
    console.log("Envoi email à:", candidateEmail);
    console.log("Sujet:", emailSubject);
    console.log("Message:", emailMessage);

    toast.success("Email envoyé avec succès");
    setEmailDialogOpen(false);
    setEmailSubject("");
    setEmailMessage("");
  };

  const handleScheduleMeet = async () => {
    if (!meetDate || !meetTime) {
      toast.error("Veuillez sélectionner une date et une heure");
      return;
    }

    const organizerEmail = (keycloak.tokenParsed as any)?.email as string | undefined;
    const interviewerEmail = organizerEmail; // par défaut l'organisateur est aussi l'intervieweur

    if (!organizerEmail) {
      toast.error("Impossible de récupérer l'email de l'organisateur (Keycloak)");
      return;
    }

    const localDateTime = `${meetDate}T${meetTime}:00`;

    const payload = {
      organizerEmail,
      interviewerEmail,
      candidateEmail,
      position: "Technique",
      dateTime: localDateTime,
    } as const;

    try {
      setSubmitting(true);
      const meeting = await apiService.post<any>("/meetings", payload);

      const meetLink: string | undefined = meeting?.meetingLink;
      if (meetLink) {
        window.open(meetLink, '_blank');
      }
      toast.success("Invitation Google Meet envoyée et créée avec succès");
      setMeetDialogOpen(false);
      setMeetDate("");
      setMeetTime("");
    } catch (error: any) {
      const apiMessage = error?.response?.data?.message || error?.message || "Erreur inconnue";
      toast.error(`Échec de la création du Google Meet: ${apiMessage}`);
      console.error("Meeting creation error", error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleScheduleZoom = async () => {
    if (!zoomTopic || !zoomDateTime) {
      toast.error("Veuillez remplir tous les champs");
      return;
    }

    try {
      setSubmitting(true);
      const response = await apiService.post(`/zoom/create-and-send/${candidatId}`, {
        topic: zoomTopic,
        dateTime: zoomDateTime
      });

      if (response) {
        toast.success("Réunion Zoom créée avec succès");
        setZoomDialogOpen(false);
        setZoomTopic("");
        setZoomDateTime("");
      }
    } catch (error: any) {
      const apiMessage = error?.response?.data?.message || error?.message || "Erreur inconnue";
      toast.error(`Échec de la création de la réunion Zoom: ${apiMessage}`);
      console.error("Zoom meeting creation error", error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button size="sm">
            Contacter
            <ChevronDown className="h-4 w-4 ml-2" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-48">
          <DropdownMenuItem onClick={() => setEmailDialogOpen(true)}>
            <Mail className="h-4 w-4 mr-2" />
            Envoyer un email
          </DropdownMenuItem>
         {/*<DropdownMenuItem onClick={handlePhone}>
            <Phone className="h-4 w-4 mr-2" />
            Appeler
          </DropdownMenuItem>}= */}
          <DropdownMenuItem onClick={handleWhatsApp}>
            <MessageSquare className="h-4 w-4 mr-2" />
            WhatsApp
          </DropdownMenuItem>
          <DropdownMenuItem onClick={handleWhatsAppCall}>
            <Phone className="h-4 w-4 mr-2" />
            Appel WhatsApp
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => { setSmsDialogOpen(true); setSmsError(null); }}>
            <MessageCircle className="h-4 w-4 mr-2" />
            SMS
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => setMeetDialogOpen(true)}>
            <Video className="mr-2 h-4 w-4" />
            Planifier un Meet
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => setZoomDialogOpen(true)}>
            <Video className="mr-2 h-4 w-4" />
            Planifier un Zoom
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      {/* Dialog Email */}
      <Dialog open={emailDialogOpen} onOpenChange={setEmailDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Envoyer un email à {candidateName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="email-to">Destinataire</Label>
              <Input id="email-to" value={candidateEmail} disabled />
            </div>
            <div>
              <Label htmlFor="email-subject">Sujet</Label>
              <Input
                id="email-subject"
                value={emailSubject}
                onChange={(e) => setEmailSubject(e.target.value)}
                placeholder="Sujet de votre email"
              />
            </div>
            <div>
              <Label htmlFor="email-message">Message</Label>
              <Textarea
                id="email-message"
                value={emailMessage}
                onChange={(e) => setEmailMessage(e.target.value)}
                placeholder="Votre message..."
                rows={5}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setEmailDialogOpen(false)}>
                Annuler
              </Button>
              <Button onClick={handleSendEmail}>
                Envoyer
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Dialog Google Meet */}
      <Dialog open={meetDialogOpen} onOpenChange={setMeetDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Planifier un Google Meet avec {candidateName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="meet-date">Date</Label>
              <Input
                id="meet-date"
                type="date"
                value={meetDate}
                onChange={(e) => setMeetDate(e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="meet-time">Heure</Label>
              <Input
                id="meet-time"
                type="time"
                value={meetTime}
                onChange={(e) => setMeetTime(e.target.value)}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setMeetDialogOpen(false)}>
                Annuler
              </Button>
              <Button onClick={handleScheduleMeet} disabled={submitting}>
                {submitting ? "Création..." : "Créer et ouvrir Meet"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Dialog SMS */}
      <Dialog open={smsDialogOpen} onOpenChange={setSmsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Envoyer un SMS à {candidateName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="sms-to">Numéro</Label>
              <Input id="sms-to" value={candidatePhone || ""} disabled />
            </div>
            <div>
              <Label htmlFor="sms-body">Message</Label>
              <Textarea
                id="sms-body"
                value={smsBody}
                onChange={(e) => setSmsBody(e.target.value)}
                placeholder="Votre message..."
                rows={4}
              />
              {smsError && (
                <div className="text-sm text-red-600 mt-2">
                  {smsError}
                </div>
              )}
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setSmsDialogOpen(false)}>
                Annuler
              </Button>
              <Button
                onClick={async () => {
                  if (!candidatePhone || !smsBody) {
                    toast.error("Téléphone et message requis");
                    return;
                  }
                  try {
                    setSubmitting(true);
                    
                    // Check authentication
                    if (!keycloak.authenticated) {
                      toast.error("Vous devez être connecté pour envoyer un SMS");
                      return;
                    }
                    
                    // Format phone number to E.164 format
                    let to = candidatePhone.replace(/[\s\-\(\)]/g, '');
                    if (!to.startsWith('+')) {
                      if (to.startsWith('0')) {
                        // Convert 0XXXXXXXXX to +212XXXXXXXXX (Morocco)
                        to = '+212' + to.substring(1);
                      } else if (!to.startsWith('212')) {
                        // Add +212 if no country code
                        to = '+212' + to;
                      } else {
                        to = '+' + to;
                      }
                    }
                    
                    console.log('Sending SMS request:', { to, message: smsBody });
                    console.log('Keycloak token:', keycloak.token ? 'Present' : 'Missing');
                    
                    const response = await apiService.post("/sms", { to, message: smsBody });
                    console.log('SMS response:', response);
                    toast.success("SMS envoyé");
                    setSmsDialogOpen(false);
                    setSmsBody("");
                    setSmsError(null);
                  } catch (e: any) {
                    console.error('SMS error details:', e);
                    console.error('SMS error response:', e?.response);
                    console.error('SMS error status:', e?.response?.status);
                    console.error('SMS error data:', e?.response?.data);
                    const apiMessage: string = e?.response?.data?.error || e?.message || "Erreur";
                    // Friendly messages for common Twilio trial errors
                    if (apiMessage?.toLowerCase().includes('unverified')) {
                      setSmsError("Numéro non vérifié sur Twilio (compte d'essai). Vérifiez le numéro dans Twilio ou passez en compte payant.");
                    } else if (apiMessage?.toLowerCase().includes("from' number") || apiMessage?.toLowerCase().includes('mismatch')) {
                      setSmsError("Le numéro d'envoi Twilio ne correspond pas à votre compte ou n'est pas autorisé.");
                    } else {
                      setSmsError(apiMessage);
                    }
                    toast.error(`Échec de l'envoi du SMS`);
                  } finally {
                    setSubmitting(false);
                  }
                }}
                disabled={submitting}
              >
                {submitting ? "Envoi..." : "Envoyer"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Dialog Zoom */}
      <Dialog open={zoomDialogOpen} onOpenChange={setZoomDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Planifier une réunion Zoom avec {candidateName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="zoom-topic">Sujet de la réunion</Label>
              <Input
                id="zoom-topic"
                value={zoomTopic}
                onChange={(e) => setZoomTopic(e.target.value)}
                placeholder="Ex: Entretien technique"
              />
            </div>
            <div>
              <Label htmlFor="zoom-datetime">Date et heure</Label>
              <Input
                id="zoom-datetime"
                type="datetime-local"
                value={zoomDateTime}
                onChange={(e) => setZoomDateTime(e.target.value)}
                min={new Date().toISOString().slice(0, 16)}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setZoomDialogOpen(false)} disabled={submitting}>
                Annuler
              </Button>
              <Button onClick={handleScheduleZoom} disabled={submitting}>
                {submitting ? 'Création...' : 'Créer la réunion'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
