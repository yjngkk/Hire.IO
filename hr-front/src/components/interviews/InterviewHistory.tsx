import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, User, FileText } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";

const getStatusBadge = (status: string) => {
  const styles: Record<string, string> = {
    terminé: "bg-green-100 text-green-800",
    annulé: "bg-yellow-100 text-yellow-800",
    absent: "bg-red-100 text-red-800",
    reprogrammé: "bg-blue-100 text-blue-800",
    prévu: "bg-gray-100 text-gray-800",
    en_cours: "bg-indigo-100 text-indigo-800",
  };

  const labels: Record<string, string> = {
    terminé: "Terminé",
    annulé: "Annulé",
    absent: "Absent",
    reprogrammé: "Reprogrammé",
    prévu: "Prévu",
    en_cours: "En cours",
  };

  return (
    <Badge className={styles[status] || "bg-gray-100 text-gray-800"}>
      {labels[status] || status}
    </Badge>
  );
};

const getScoreColor = (score: number) => {
  if (score >= 4) return "text-green-600";
  if (score >= 3) return "text-yellow-600";
  return "text-red-600";
};

type Entretien = {
  id: number;
  candidat: { nom: string };
  dateHeure: string;
  duree: string;
  type: string;
  interviewer: string;
  statut: string;
  notes?: string;
  score?: number;
};

export function InterviewHistory() {
  const [entretiens, setEntretiens] = useState<Entretien[]>([]);
  const [selectedEntretien, setSelectedEntretien] = useState<Entretien | null>(null);
  const [showDetails, setShowDetails] = useState(false);

  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data: Entretien[] = await apiService.get("/entretiens");
        const now = new Date();
        const passed = data.filter((e) => new Date(e.dateHeure) < now);
        setEntretiens(passed);
      } catch (err) {
        console.error("Erreur API :", err);
      }
    };

    if (keycloakReady && isAuthenticated) {
      fetchData();
    }
  }, [keycloakReady, isAuthenticated]);

  const openDetails = (entretien: Entretien) => {
    setSelectedEntretien(entretien);
    setShowDetails(true);
  };

  const closeDetails = () => {
    setShowDetails(false);
    setSelectedEntretien(null);
  };

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Historique des entretiens</CardTitle>
          <CardDescription>
            Consultez tous vos <strong>entretiens passés</strong> avec leur statut et évaluation.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {entretiens.length === 0 ? (
              <p className="text-muted-foreground text-sm">Aucun entretien passé trouvé.</p>
            ) : (
              entretiens.map((interview) => {
                const date = new Date(interview.dateHeure);
                return (
                  <Card key={interview.id} className="p-4">
                    <div className="space-y-3">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <div className="flex items-center gap-3">
                            <h3 className="font-semibold">{interview.candidat?.nom}</h3>
                            {getStatusBadge(interview.statut)}
                          </div>
                          <p className="text-sm text-muted-foreground">
                            {interview.type === "meet" ? "Entretien en ligne" : "Présentiel"}
                          </p>
                        </div>
                        {interview.score !== undefined && (
                          <div className="text-right">
                            <div className={`text-lg font-bold ${getScoreColor(interview.score)}`}>
                              {interview.score}/5
                            </div>
                            <div className="text-xs text-muted-foreground">Score</div>
                          </div>
                        )}
                      </div>

                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          <span>{date.toLocaleDateString("fr-FR")}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          <span>
                            {date.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" })} ({interview.duree})
                          </span>
                        </div>
                        <div className="flex items-center gap-1">
                          <User className="h-3 w-3" />
                          <span>{interview.interviewer}</span>
                        </div>
                      </div>

                      {interview.notes && (
                        <div className="p-3 bg-muted/50 rounded-lg">
                          <div className="flex items-start gap-2">
                            <FileText className="h-4 w-4 mt-0.5 text-muted-foreground" />
                            <p className="text-sm">{interview.notes}</p>
                          </div>
                        </div>
                      )}

                      <div className="flex gap-2 pt-2">
                        <Button size="sm" variant="outline" onClick={() => openDetails(interview)}>
                          Voir détails
                        </Button>
                      </div>
                    </div>
                  </Card>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>

      {/* Modal details */}
      <Dialog open={showDetails} onOpenChange={setShowDetails}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Détails de l'entretien</DialogTitle>
            <DialogDescription>
              Informations complètes sur l'entretien sélectionné.
            </DialogDescription>
          </DialogHeader>

          {selectedEntretien && (
            <div className="space-y-3 text-sm">
              <p>
                <strong>Nom du candidat :</strong> {selectedEntretien.candidat.nom}
              </p>
              <p>
                <strong>Type :</strong>{" "}
                {selectedEntretien.type === "meet" ? "Entretien en ligne" : "Présentiel"}
              </p>
             <p>
                <strong>Date :</strong>{" "}
                {new Date(selectedEntretien.dateHeure).toLocaleDateString("fr-FR", {
                  weekday: 'long',
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric'
                })}
                {" à "}
                {new Date(selectedEntretien.dateHeure).toLocaleTimeString("fr-FR", {
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </p>
              <p>
                <strong>Durée :</strong> {selectedEntretien.duree}
              </p>
              <p>
                <strong>Interviewer :</strong> {selectedEntretien.interviewer}
              </p>
              <div className="flex items-center gap-2">
                <strong>Statut :</strong>
                {getStatusBadge(selectedEntretien.statut)}
              </div>
              {selectedEntretien.score !== undefined && (
                <p>
                  <strong>Score :</strong>{" "}
                  <span className={getScoreColor(selectedEntretien.score)}>
                    {selectedEntretien.score}/5
                  </span>
                </p>
              )}
              {selectedEntretien.notes && (
                <p>
                  <strong>Notes :</strong> {selectedEntretien.notes}
                </p>
              )}
            </div>
          )}

          <div className="mt-4 flex justify-end">
            <Button onClick={closeDetails}>Fermer</Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}