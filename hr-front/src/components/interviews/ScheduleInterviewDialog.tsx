import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Calendar } from "@/components/ui/calendar";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { Calendar as CalendarIcon, Clock, Mail, Link, Copy } from "lucide-react";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";

interface ScheduleInterviewDialogProps {
  onClose: () => void;
  onSaved?: () => void;
}

export function ScheduleInterviewDialog({ onClose, onSaved }: ScheduleInterviewDialogProps) {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    candidateName: "",
    candidateEmail: "",
    jobTitle: "",
    interviewType: "meet",
    duration: "",
    interviewerEmail: "",
    organizerEmail: "",
    notes: ""
  });
  const [candidates, setCandidates] = useState<any[]>([]);
  const [isLoadingCandidates, setIsLoadingCandidates] = useState(false);
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchCandidates = async () => {
      if (keycloakReady && isAuthenticated) {
        setIsLoadingCandidates(true);
        try {
          const data = await apiService.get("/candidats");
          setCandidates(data);
        } catch (error) {
          console.error("Error fetching candidates:", error);
        } finally {
          setIsLoadingCandidates(false);
        }
      }
    };
    fetchCandidates();
  }, [keycloakReady, isAuthenticated]);
  const [selectedDate, setSelectedDate] = useState<Date>();
  const [availableSlots] = useState([
    "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"
  ]);
  const [selectedSlot, setSelectedSlot] = useState("");
  const [calendarLink, setCalendarLink] = useState("");
  const [isSending, setIsSending] = useState(false);
  const { toast } = useToast();

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const generateCalendarLink = () => {
    if (selectedDate && selectedSlot) {
      const mockLink = `https://calendly.com/interview-${Date.now()}?date=${selectedDate.toISOString().split('T')[0]}&time=${selectedSlot}`;
      setCalendarLink(mockLink);
      setStep(3);
    }
  };

  const copyCalendarLink = () => {
    navigator.clipboard.writeText(calendarLink);
    toast({
      title: "Lien copié",
      description: "Le lien du calendrier a été copié dans le presse-papiers"
    });
  };

  const sendInvitation = async () => {
    if (!selectedDate || !selectedSlot) return;

    setIsSending(true);

    try {
      // Trouver le candidat sélectionné
      const selectedCandidate = candidates.find(c => c.nom === formData.candidateName);
      let candidatId = selectedCandidate ? selectedCandidate.id : null;

      // Si aucun candidat existant, on peut lever une erreur ou empêcher l'envoi
      if (!candidatId) {
        toast({
          variant: "destructive",
          title: "Erreur",
          description: "Veuillez sélectionner un candidat existant."
        });
        setIsSending(false);
        return;
      }

      // Correction du décalage de date : construire la date en local (YYYY-MM-DD)
      const year = selectedDate.getFullYear();
      const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
      const day = String(selectedDate.getDate()).padStart(2, '0');
      const dateString = `${year}-${month}-${day}`;
      const dateTime = `${dateString}T${selectedSlot}:00`;

      const entretien = await apiService.post('/entretiens', {
        candidat: { id: candidatId },
        dateHeure: dateTime,
        duree: formData.duration,
        type: 'meet',
        interviewerEmail: formData.interviewerEmail,
        organizerEmail: formData.organizerEmail,
        notes: formData.notes,
      });

      if (entretien.lienMeet) {
        setCalendarLink(entretien.lienMeet);
      }

      toast({
        title: "Invitation envoyée",
        description: `L'invitation d'entretien a été envoyée à ${formData.candidateEmail}`
      });

      if (typeof onSaved === "function") {
        try {
          onSaved();
        } catch (_) {
          // no-op
        }
      }

      onClose();
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Erreur",
        description: error.message || "Une erreur est survenue lors de la planification."
      });
    } finally {
      setIsSending(false);
    }
  };

  return (
      <div className="space-y-6">
        {step === 1 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Informations du candidat</h3>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="candidateName">Nom du candidat</Label>
                  <Select
                    onValueChange={(value) => {
                      const selectedCandidate = candidates.find(c => c.nom === value);
                      if (selectedCandidate) {
                        setFormData({
                          ...formData,
                          candidateName: selectedCandidate.nom,
                          candidateEmail: selectedCandidate.email || "",
                          jobTitle: selectedCandidate.poste || "",
                          notes: selectedCandidate.notes || ""
                        });
                      }
                    }}
                    value={formData.candidateName}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Sélectionner un candidat" />
                    </SelectTrigger>
                    <SelectContent>
                      {isLoadingCandidates ? (
                        <SelectItem value="loading" disabled>Chargement...</SelectItem>
                      ) : (
                        candidates.map((candidate) => (
                          <SelectItem key={candidate.id} value={candidate.nom}>
                            {candidate.nom}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                  {formData.candidateName && (
                    <Button 
                      variant="link" 
                      size="sm" 
                      className="h-auto p-0 text-muted-foreground"
                      onClick={() => {
                        setFormData({
                          ...formData,
                          candidateName: "",
                          candidateEmail: "",
                          jobTitle: "",
                          notes: ""
                        });
                      }}
                    >
                      Sélectionner un autre candidat
                    </Button>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="candidateEmail">Email du candidat</Label>
                  <Input
                    id="candidateEmail"
                    type="email"
                    value={formData.candidateEmail}
                    onChange={(e) => handleInputChange("candidateEmail", e.target.value)}
                    placeholder="marie.dubois@email.com"
                    readOnly={!!formData.candidateName}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="interviewerEmail">Email de l'interviewer</Label>
                  <Input
                      id="interviewerEmail"
                      type="email"
                      value={formData.interviewerEmail}
                      onChange={(e) => handleInputChange("interviewerEmail", e.target.value)}
                      placeholder="interviewer@email.com"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="organizerEmail">Email de l'organisateur</Label>
                  <Input
                      id="organizerEmail"
                      type="email"
                      value={formData.organizerEmail}
                      onChange={(e) => handleInputChange("organizerEmail", e.target.value)}
                      placeholder="organisateur@email.com"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="jobTitle">Poste concerné</Label>
                <Input
                  id="jobTitle"
                  value={formData.jobTitle}
                  onChange={(e) => handleInputChange("jobTitle", e.target.value)}
                  placeholder="Développeur Full Stack"
                  readOnly={!!formData.candidateName}
                />
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label>Durée</Label>
                  <Select onValueChange={(value) => handleInputChange("duration", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Durée" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="30min">30 minutes</SelectItem>
                      <SelectItem value="45min">45 minutes</SelectItem>
                      <SelectItem value="1h">1 heure</SelectItem>
                      <SelectItem value="1h30">1h30</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
              </div>

              <div className="space-y-2">
                <Label htmlFor="notes">Notes (optionnel)</Label>
                <Textarea
                  id="notes"
                  value={formData.notes}
                  onChange={(e) => handleInputChange("notes", e.target.value)}
                  placeholder="Informations supplémentaires pour l'entretien..."
                  rows={3}
                  readOnly={!!formData.candidateName}
                />
              </div>

              <Button
                  onClick={() => setStep(2)}
                  className="w-full"
                  disabled={!formData.candidateName || !formData.candidateEmail || !formData.jobTitle}
              >
                Suivant : Créer le calendrier
              </Button>
            </div>
        )}

        {step === 2 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Créer le calendrier partagé</h3>

              <div className="grid lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Sélectionner une date</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <Calendar
                        mode="single"
                        selected={selectedDate}
                        onSelect={setSelectedDate}
                        disabled={(date) => date < new Date()}
                        className="rounded-md border"
                    />
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Créneaux disponibles</CardTitle>
                    <CardDescription>
                      {selectedDate
                          ? `Créneaux pour le ${selectedDate.toLocaleDateString('fr-FR')}`
                          : "Sélectionnez d'abord une date"}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {selectedDate ? (
                        <div className="grid grid-cols-2 gap-2">
                          {availableSlots.map((slot) => (
                              <Button
                                  key={slot}
                                  variant={selectedSlot === slot ? "default" : "outline"}
                                  size="sm"
                                  onClick={() => setSelectedSlot(slot)}
                                  className="justify-center"
                              >
                                <Clock className="h-3 w-3 mr-1" />
                                {slot}
                              </Button>
                          ))}
                        </div>
                    ) : (
                        <p className="text-muted-foreground text-sm">
                          Veuillez sélectionner une date pour voir les créneaux disponibles.
                        </p>
                    )}
                  </CardContent>
                </Card>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" onClick={() => setStep(1)}>
                  Retour
                </Button>
                <Button
                    onClick={generateCalendarLink}
                    disabled={!selectedDate || !selectedSlot}
                    className="flex-1"
                >
                  Générer le lien de calendrier
                </Button>
              </div>
            </div>
        )}

        {step === 3 && (
            <div className="space-y-4">
              <h3 className="text-lg font-semibold">Calendrier partagé généré</h3>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Résumé de l'entretien</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Candidat:</span>
                      <p className="font-medium">{formData.candidateName}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Poste:</span>
                      <p className="font-medium">{formData.jobTitle}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Date:</span>
                      <p className="font-medium">{selectedDate?.toLocaleDateString('fr-FR')}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Heure:</span>
                      <p className="font-medium">{selectedSlot} ({formData.duration})</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Badge>meet</Badge>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base flex items-center gap-2">
                    <Link className="h-4 w-4" />
                    Lien du calendrier partagé
                  </CardTitle>
                  <CardDescription>
                    Partagez ce lien avec le candidat pour qu'il puisse confirmer sa disponibilité
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex gap-2">
                    <Input value={calendarLink} readOnly className="flex-1" />
                    <Button variant="outline" size="icon" onClick={copyCalendarLink}>
                      <Copy className="h-4 w-4" />
                    </Button>
                  </div>

                  <div className="p-3 bg-blue-50 rounded-lg text-sm text-blue-800">
                    Le candidat recevra un email avec ce lien et pourra sélectionner un créneau qui lui convient parmi ceux proposés.
                  </div>
                </CardContent>
              </Card>

              <div className="flex gap-2">
                <Button variant="outline" onClick={() => setStep(2)}>
                  Modifier
                </Button>
                <Button
                    onClick={sendInvitation}
                    className="flex-1"
                    disabled={isSending}
                >
                  {isSending ? (
                      <>
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Envoi en cours...
                      </>
                  ) : (
                      <>
                        <Mail className="h-4 w-4 mr-2" />
                        Envoyer l'invitation
                      </>
                  )}
                </Button>
              </div>
            </div>
        )}
      </div>
  );
}