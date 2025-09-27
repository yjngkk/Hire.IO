
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { FileText, Star, ThumbsUp, ThumbsDown, Edit, Plus } from "lucide-react";

import { useState, useEffect } from "react";
import { useAuth } from "@/KeycloakProvider";
import apiService from "../../config/apiService";


export function InterviewSummaries() {
  const [summaries, setSummaries] = useState<any[]>([]);
  const [selectedSummary, setSelectedSummary] = useState<any | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [editedSummary, setEditedSummary] = useState("");

  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  useEffect(() => {
    if (!keycloakReady || !isAuthenticated) return;
    
    const loadSummaries = async () => {
      try {
        const data = await apiService.get("/resumes");
        setSummaries(data);
      } catch (error) {
        console.error("Error loading summaries:", error);
      }
    };

    loadSummaries();
  }, [keycloakReady, isAuthenticated]);

  const getScoreColor = (score: number) => {
    if (score >= 4) return "text-green-600";
    if (score >= 3) return "text-yellow-600";
    return "text-red-600";
  };

  const getRecommendationBadge = (recommendation: string) => {
    const styles = {
      recommend: "bg-green-100 text-green-800",
      maybe: "bg-yellow-100 text-yellow-800",
      "not-recommend": "bg-red-100 text-red-800"
    };

    const labels = {
      recommend: "Recommandé",
      maybe: "À reconsidérer",
      "not-recommend": "Non recommandé"
    };

    const icons = {
      recommend: ThumbsUp,
      maybe: Star,
      "not-recommend": ThumbsDown
    };

    const Icon = icons[recommendation as keyof typeof icons];
    return (
      <Badge className={styles[recommendation as keyof typeof styles] || styles["maybe"]}>
        {Icon && <Icon className="h-3 w-3 mr-1" />}
        {labels[recommendation as keyof typeof labels] || recommendation}
      </Badge>
    );
  };

  const safeArray = (val: any) => Array.isArray(val) ? val : (val ? [val] : []);

  const openSummaryDialog = async (summary: any) => {
    try {
      const data = await apiService.get(`/resumes/${summary.id || summary._id}`);
      setSelectedSummary(data);
      setEditedSummary(data.notes || data.summary || "");
      setEditMode(false);
    } catch (error) {
      console.error("Error loading summary details:", error);
      setSelectedSummary(summary); // fallback
      setEditedSummary(summary.notes || summary.summary || "");
      setEditMode(false);
    }
  };

  const deleteSummary = async (summary: any) => {
    const id = summary.id || summary._id;
    if (!id) {
      alert('Impossible de supprimer : identifiant manquant.');
      return;
    }
    if (!window.confirm('Voulez-vous vraiment supprimer ce résumé ?')) return;
    
    try {
      await apiService.delete(`/resumes/${id}`);
      setSummaries(summaries.filter(s => (s.id || s._id) !== id));
      if (selectedSummary && (selectedSummary.id || selectedSummary._id) === id) {
        setSelectedSummary(null);
      }
    } catch (error) {
      console.error("Error deleting summary:", error);
      alert('Erreur lors de la suppression');
    }
  };

  return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Résumés d'entretiens
          </CardTitle>
          <CardDescription>
            Consultez et modifiez les résumés de vos entretiens passés
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {summaries.length === 0 ? (
                <div className="text-center text-muted-foreground py-8">
                  Aucun résumé d'entretien trouvé.
                </div>
            ) : (
                summaries.map((summary, idx) => (
                    <Card key={summary.id || summary._id || idx} className="p-4">
                      <div className="space-y-3">
                        <div className="flex justify-between items-start">
                          <div className="flex-1">
                            <h3 className="font-semibold">{summary.nomCandidat}</h3>
                            <p className="text-sm text-muted-foreground">
                              {summary.poste} • {summary.dateEntretien ? new Date(summary.dateEntretien).toLocaleDateString('fr-FR') : ''} • {summary.interviewer}
                            </p>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className="text-right">
                              <div className={`text-lg font-bold ${getScoreColor(summary.score)}`}>
                                {summary.score}/5
                              </div>
                            </div>
                            {getRecommendationBadge(summary.recommandation)}
                          </div>
                        </div>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {summary.notes}
                        </p>
                        <div className="flex gap-2">
                          <Dialog>
                            <DialogTrigger asChild>
                              <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => openSummaryDialog(summary)}
                              >
                                Voir
                              </Button>
                            </DialogTrigger>
                            <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
                              <DialogHeader>
                                <DialogTitle>
                                  Résumé d'entretien - {selectedSummary?.nomCandidat}
                                </DialogTitle>
                              </DialogHeader>
                              {selectedSummary && (
                                  <div className="space-y-6">
                                    <div className="grid grid-cols-2 gap-4 p-4 bg-muted/50 rounded-lg">
                                      <div>
                                        <span className="text-sm text-muted-foreground">Candidat:</span>
                                        <p className="font-medium">{selectedSummary.nomCandidat}</p>
                                      </div>
                                      <div>
                                        <span className="text-sm text-muted-foreground">Poste:</span>
                                        <p className="font-medium">{selectedSummary.poste}</p>
                                      </div>
                                      <div>
                                        <span className="text-sm text-muted-foreground">Date:</span>
                                        <p className="font-medium">{selectedSummary.dateEntretien ? new Date(selectedSummary.dateEntretien).toLocaleDateString('fr-FR') : ''}</p>
                                      </div>
                                     {/* <div>
                                        <span className="text-sm text-muted-foreground">Interviewer:</span>
                                        <p className="font-medium">{selectedSummary.interviewer}</p>
                                      </div>*/}
                                    </div>
                                    <div className="flex items-center justify-between">
                                      <div className="flex items-center gap-4">
                                        <div>
                                          <span className="text-sm text-muted-foreground">Score global:</span>
                                          <p className={`text-2xl font-bold ${getScoreColor(selectedSummary.score)}`}>
                                            {selectedSummary.score}/5
                                          </p>
                                        </div>
                                        <div>
                                          <span className="text-sm text-muted-foreground">Recommandation:</span>
                                          <div className="mt-1">
                                            {getRecommendationBadge(selectedSummary.recommandation)}
                                          </div>
                                        </div>
                                      </div>
                                      <Button
                                          variant="outline"
                                          size="sm"
                                          onClick={() => setEditMode(!editMode)}
                                      >
                                        <Edit className="h-4 w-4 mr-2" />
                                        {editMode ? "Annuler" : "Modifier"}
                                      </Button>
                                    </div>
                                    <div>
                                      <h3 className="font-semibold mb-2">Résumé général</h3>
                                      {editMode ? (
                                          <div className="space-y-2">
                                            <Textarea
                                                value={editedSummary}
                                                onChange={(e) => setEditedSummary(e.target.value)}
                                                rows={4}
                                                className="w-full"
                                            />
                                            <Button size="sm">Sauvegarder</Button>
                                          </div>
                                      ) : (
                                          <p className="text-sm leading-relaxed">{selectedSummary.notes}</p>
                                      )}
                                    </div>
                                    <div className="grid lg:grid-cols-2 gap-4">
                                      <div>
                                        <h3 className="font-semibold mb-2 text-green-700">Points forts</h3>
                                        <ul className="space-y-1">
                                          {safeArray(selectedSummary.pointsForts || selectedSummary.pointsforts).map((strength: string, index: number) => (
                                              <li key={index} className="text-sm flex items-center gap-2">
                                                <div className="w-1.5 h-1.5 bg-green-500 rounded-full" />
                                                {strength}
                                              </li>
                                          ))}
                                        </ul>
                                      </div>
                                      <div>
                                        <h3 className="font-semibold mb-2 text-orange-700">Points d'amélioration</h3>
                                        <ul className="space-y-1">
                                          {safeArray(selectedSummary.pointsAmelioration || selectedSummary.pointsamelioration).map((weakness: string, index: number) => (
                                              <li key={index} className="text-sm flex items-center gap-2">
                                                <div className="w-1.5 h-1.5 bg-orange-500 rounded-full" />
                                                {weakness}
                                              </li>
                                          ))}
                                        </ul>
                                      </div>
                                    </div>
                                    <div className="p-4 bg-blue-50 rounded-lg">
                                      <h3 className="font-semibold mb-2 text-blue-900">Prochaines étapes</h3>
                                      <p className="text-sm text-blue-800">{selectedSummary.prochainesEtapes}</p>
                                    </div>
                                  </div>
                              )}
                            </DialogContent>
                          </Dialog>
                          <Button size="sm" variant="outline" onClick={() => deleteSummary(summary)} color="red">
                            Supprimer
                          </Button>
                          {/*  <Button size="sm" variant="outline">
                            <Edit className="h-3 w-3 mr-1" />
                            Modifier
                          </Button>*/}
                        </div>
                      </div>
                    </Card>
                ))
            )}
            <Button variant="outline" className="w-full">
              <Plus className="h-4 w-4 mr-2" />
              Créer un nouveau résumé
            </Button>
          </div>
        </CardContent>
      </Card>

  );
}
