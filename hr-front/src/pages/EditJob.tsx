import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useAuth } from "@/KeycloakProvider";
import { useToast } from "@/components/ui/use-toast";
import apiService from '@/config/apiService';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Linkedin,
  Globe,
  Rss,
  Briefcase,
  Loader2,
  Edit,
  Check,
  X,
  ArrowLeft,
} from "lucide-react";
import { useForm } from "../hooks/useForm";

const platforms = [
  {
    id: "linkedin",
    name: "LinkedIn",
    icon: Linkedin,
    color: "text-blue-600",
    description: "Réseau professionnel",
  },
  {
    id: "indeed",
    name: "Indeed",
    icon: Briefcase,
    color: "text-blue-700",
    description: "Site d'emploi",
  },
  {
    id: "website",
    name: "Site web",
    icon: Globe,
    color: "text-green-600",
    description: "Votre site carrières",
  },
  {
    id: "rss",
    name: "Flux RSS",
    icon: Rss,
    color: "text-orange-600",
    description: "Syndication automatique",
  },
];

const cleanGeneratedText = (text: string): string => {
  return (
    text
      // 1. SUPPRIMER LES SECTIONS META (tout ce qui n'est pas le contenu principal)
      .replace(/^\*\*Titre.*?:\*\*\s*/gim, "") // Enlever "**Titre de l'annonce:**"
      .replace(/---+/g, "") // Supprimer tous les séparateurs

      // 2. SUPPRIMER LES SECTIONS DE FIN (tout après certains mots-clés)
      .replace(/\*\*(Mots-clés|Keywords|Tags)\s*:\*\*[\s\S]*$/gim, "")
      .replace(/\*\*(Note|Notes|Remarque)\s*:\*\*[\s\S]*$/gim, "")
      .replace(/\*\*(Conclusion|Pour conclure)\s*:\*\*[\s\S]*$/gim, "")
      .replace(
        /\*\*(Appel à l'action|Call to action|Pour postuler)\s*:\*\*[\s\S]*$/gim,
        ""
      )
      .replace(/\*\*(Contact|Candidature|Application)\s*:\*\*[\s\S]*$/gim, "")

      // 3. NETTOYER LE FORMATAGE EXCESSIF
      .replace(/\*\*(.*?)\*\*/g, "$1") // **texte** → texte
      .replace(/\*(.*?)\*/g, "$1") // *texte* → texte
      .replace(/_{2,}(.*?)_{2,}/g, "$1") // __texte__ → texte
      .replace(/_(.+?)_/g, "$1") // _texte_ → texte

      // 4. SUPPRIMER LES ÉLÉMENTS DE STRUCTURE INUTILES
      .replace(/^#{1,6}\s*/gm, "") // Enlever les # de markdown
      .replace(/^\s*[-•·]\s*/gm, "• ") // Normaliser les puces
      .replace(/^\s*\d+\.\s*/gm, "") // Enlever numérotation (1. 2. 3.)

      // 5. NETTOYER LES CARACTÈRES ET ESPACES
      .replace(/[""]/g, '"') // Normaliser les guillemets
      .replace(/['']/g, "'") // Normaliser les apostrophes
      .replace(/…/g, "...") // Normaliser les ellipses
      .replace(/\s+/g, " ") // Réduire espaces multiples en un seul
      .replace(/\n\s*\n\s*\n+/g, "\n\n") // Max 2 lignes vides consécutives

      // 6. SUPPRIMER LES LIGNES VIDES EN DÉBUT/FIN ET NETTOYER
      .split("\n")
      .map((line) => line.trim()) // Nettoyer chaque ligne
      .filter((line, index, array) => {
        // Supprimer lignes vides en début et fin
        if (index === 0 || index === array.length - 1) {
          return line.length > 0;
        }
        return true;
      })
      .join("\n")
      .trim()

      // 7. NETTOYAGE FINAL - SUPPRIMER LES ARTEFACTS RESTANTS
      .replace(/\n{3,}/g, "\n\n") // Max 2 retours ligne consécutifs
      .replace(/^\s*\n+/g, "") // Supprimer lignes vides au début
      .replace(/\n+\s*$/g, "") // Supprimer lignes vides à la fin

      // 8. SUPPRIMER LES PHRASES GÉNÉRIQUES IA (optionnel)
      .replace(/Cette annonce est optimisée.*$/gim, "")
      .replace(/Les mots-clés.*$/gim, "")
      .replace(/Cette offre.*algorithmes.*$/gim, "")
  );
};

export function EditJob() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const { updateForm, loading, error } = useForm();
  const { keycloak, isAuthenticated, keycloakReady } = useAuth();

  const [jobData, setJobData] = useState({
    title: "",
    missions: "",
    location: "",
    contractType: "",
    level: "",
    skills: "",
    tone: "formel",
    aiModel: "",
  });

  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [generatedOffer, setGeneratedOffer] = useState("");
  const [showGenerated, setShowGenerated] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  const [isEditing, setIsEditing] = useState(false);
  const [editedOffer, setEditedOffer] = useState("");

  // Charger les données de l'offre à modifier
  useEffect(() => {
    const fetchJobData = async () => {
      if (!jobId) return;
      
      try {
        setLoadingData(true);
        // Remplacez par votre appel API réel
        const response = await apiService.get(`/forms/${jobId}`);
        
        if (response) {
          setJobData({
            title: response.title || "",
            missions: response.missions || "",
            location: response.location || "",
            contractType: response.contractType || "",
            level: response.level || "",
            skills: response.skills || "",
            tone: response.tone || "formel",
            aiModel: response.aiModel || "",
          });
          
          // Si l'offre a déjà du contenu généré
          if (response.generatedContent) {
            setGeneratedOffer(response.generatedContent);
            setEditedOffer(response.generatedContent);
            setShowGenerated(true);
          }
          
          // Si l'offre a des plateformes sélectionnées
          if (response.platforms) {
            setSelectedPlatforms(response.platforms);
          }
        }
      } catch (error) {
        console.error("Erreur lors du chargement:", error);
        toast({
          title: "Erreur",
          description: "Impossible de charger les données de l'offre",
          variant: "destructive",
        });
        navigate('/jobs'); // Rediriger vers la liste si erreur
      } finally {
        setLoadingData(false);
      }
    };

    fetchJobData();
  }, [jobId, navigate, toast]);

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleSaveEdit = () => {
    setGeneratedOffer(editedOffer);
    setIsEditing(false);
  };

  const handleCancelEdit = () => {
    setEditedOffer(generatedOffer);
    setIsEditing(false);
  };

  // Fonction pour régénération avec IA
  const handleAISubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      console.log("IA Submit Data:");
      
      const response = await apiService.post('/job-offers/generate', {
        title: jobData.title,
        missions: jobData.missions,
        location: jobData.location,
        contractType: jobData.contractType,
        level: jobData.level,
        skills: jobData.skills,
        tone: jobData.tone,
        targetPlatform: selectedPlatforms[0] || "linkedin",
        preferredModel: jobData.aiModel,
      });

      console.log("reponse", response);

      const optimizedText = response;
      const cleanedText = cleanGeneratedText(optimizedText);
      setGeneratedOffer(cleanedText);
      setEditedOffer(cleanedText);
      setShowGenerated(true);

    } catch (error) {
      console.error("Erreur réseau:", error);
      toast({
        title: "Erreur",
        description: "Erreur lors de la génération avec IA",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Fonction pour mise à jour normale
  const handleNormalSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    try {
      // Mettre à jour l'offre dans le backend
      const updatedForm = await updateForm(Number(jobId), jobData);
      
      if (updatedForm) {
        // Générer le texte d'offre (template simple)
        const mockOffer = `${jobData.title}

Missions principales :
${jobData.missions}

Lieu : ${jobData.location}
Type de contrat : ${jobData.contractType}
Niveau requis : ${jobData.level}

Compétences clés :
${jobData.skills}

Rejoignez notre équipe dynamique et participez à des projets innovants !`;
        
        setGeneratedOffer(mockOffer);
        setEditedOffer(mockOffer);
        setShowGenerated(true);
        
        toast({
          title: "Succès",
          description: "Offre mise à jour avec succès",
        });
      }
    } catch (err) {
      console.error("Erreur lors de la mise à jour:", err);
      toast({
        title: "Erreur",
        description: "Erreur lors de la mise à jour de l'offre",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePlatformToggle = (platformId: string) => {
    setSelectedPlatforms((prev) =>
      prev.includes(platformId)
        ? prev.filter((id) => id !== platformId)
        : [...prev, platformId]
    );
  };

  const handlePreview = () => {
    setShowPreview(true);
  };

  const handleUpdate = async () => {
    setIsSubmitting(true);
    try {
      // Sauvegarder les modifications finales
      const finalData = {
        ...jobData,
        generatedContent: generatedOffer,
        platforms: selectedPlatforms,
      };
      
      await apiService.put(`/job-offers/${jobId}`, finalData);
      
      toast({
        title: "Succès",
        description: "Offre mise à jour avec succès",
      });
      
      navigate('/jobs'); // Rediriger vers la liste
    } catch (err) {
      console.error("Erreur lors de la sauvegarde:", err);
      toast({
        title: "Erreur",
        description: "Erreur lors de la sauvegarde",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(generatedOffer);
    toast({
      title: "Copié",
      description: "Le contenu a été copié dans le presse-papier",
    });
  };

  const optimizeForPlatform = async (platform: string) => {
    try {
      const response = await fetch(
        "http://localhost:8089/api/job-offers/generate",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${keycloak.token}`,
          },
          body: JSON.stringify({
            title: jobData.title,
            missions: jobData.missions,
            location: jobData.location,
            contractType: jobData.contractType,
            level: jobData.level,
            skills: jobData.skills,
            tone: jobData.tone,
            targetPlatform: platform,
            preferredModel: jobData.aiModel,
          }),
        }
      );

      if (response.ok) {
        const optimizedText = await response.text();
        const cleanedText = cleanGeneratedText(optimizedText);
        setGeneratedOffer(cleanedText);
        setEditedOffer(cleanedText);
      }
    } catch (error) {
      console.error("Erreur optimisation:", error);
    }
  };

  // Affichage du loader pendant le chargement initial
  if (loadingData) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin" />
        <span className="ml-2">Chargement de l'offre...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button 
          variant="outline" 
          size="sm" 
          onClick={() => navigate('/jobs')}
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Retour
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Modifier l'offre d'emploi</h1>
          <p className="text-muted-foreground mt-2">
            Modifiez les informations et régénérez votre offre
          </p>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-700">Erreur: {error}</p>
        </div>
      )}

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Informations du poste</CardTitle>
              <CardDescription>Modifiez les informations du poste</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={(e) => e.preventDefault()} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="title">Titre du poste *</Label>
                  <Input
                    id="title"
                    value={jobData.title}
                    onChange={(e) =>
                      setJobData({ ...jobData, title: e.target.value })
                    }
                    placeholder="ex: Développeur Full Stack"
                    required
                    disabled={loading || isSubmitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="missions">Missions principales *</Label>
                  <Textarea
                    id="missions"
                    value={jobData.missions}
                    onChange={(e) =>
                      setJobData({ ...jobData, missions: e.target.value })
                    }
                    placeholder="Décrivez les principales missions du poste..."
                    rows={4}
                    required
                    disabled={loading || isSubmitting}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="location">Lieu *</Label>
                    <Input
                      id="location"
                      value={jobData.location}
                      onChange={(e) =>
                        setJobData({ ...jobData, location: e.target.value })
                      }
                      placeholder="Paris, Remote..."
                      required
                      disabled={loading || isSubmitting}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Type de contrat *</Label>
                    <Select
                      value={jobData.contractType}
                      onValueChange={(value) =>
                        setJobData({ ...jobData, contractType: value })
                      }
                      disabled={loading || isSubmitting}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Choisir" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="cdi">CDI</SelectItem>
                        <SelectItem value="cdd">CDD</SelectItem>
                        <SelectItem value="freelance">Freelance</SelectItem>
                        <SelectItem value="stage">Stage</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Niveau d'expérience *</Label>
                  <Select
                    value={jobData.level}
                    onValueChange={(value) =>
                      setJobData({ ...jobData, level: value })
                    }
                    disabled={loading || isSubmitting}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Choisir le niveau" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="junior">Junior (0-2 ans)</SelectItem>
                      <SelectItem value="confirme">
                        Confirmé (3-5 ans)
                      </SelectItem>
                      <SelectItem value="senior">Senior (5+ ans)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="skills">Compétences clés *</Label>
                  <Textarea
                    id="skills"
                    value={jobData.skills}
                    onChange={(e) =>
                      setJobData({ ...jobData, skills: e.target.value })
                    }
                    placeholder="React, Node.js, PostgreSQL..."
                    rows={3}
                    required
                    disabled={loading || isSubmitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Ton de l'annonce</Label>
                  <Select
                    value={jobData.tone}
                    onValueChange={(value) =>
                      setJobData({ ...jobData, tone: value })
                    }
                    disabled={loading || isSubmitting}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="formel">Formel</SelectItem>
                      <SelectItem value="dynamique">Dynamique</SelectItem>
                      <SelectItem value="startup">Startup</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Modèle IA</Label>
                  <Select
                    value={jobData.aiModel}
                    onValueChange={(value) =>
                      setJobData({ ...jobData, aiModel: value })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="mistral">Mistral AI</SelectItem>
                      <SelectItem value="groq">Groq</SelectItem>
                      <SelectItem value="huggingface">huggingface</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* BOUTONS POUR MODIFICATION */}
                <div className="flex justify-center">
                  
                  <Button 
                    type="button" 
                    onClick={handleNormalSubmit}
                    // variant="outline"
                    className="w-full"
                    disabled={loading || isSubmitting}
                  >
                    {isSubmitting ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Mise à jour...
                      </>
                    ) : (
                      "📝 Mise à jour simple"
                    )}
                  </Button>
                </div>

                {/* DESCRIPTION DES BOUTONS */}
                <div className="text-sm text-muted-foreground space-y-1">
                </div>
              </form>
            </CardContent>
          </Card>

          {/* Platform selection */}
          {showGenerated && (
            <Card>
              <CardHeader>
                <CardTitle>Plateformes de publication</CardTitle>
                <CardDescription>
                  Sélectionnez où vous souhaitez publier cette offre
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-4">
                  {platforms.map((platform) => {
                    const IconComponent = platform.icon;
                    const isSelected = selectedPlatforms.includes(platform.id);

                    return (
                      <div
                        key={platform.id}
                        className={`
                          flex items-center space-x-3 p-4 rounded-lg border-2 cursor-pointer transition-all
                          ${
                            isSelected
                              ? "border-primary bg-primary/5"
                              : "border-border hover:border-primary/50"
                          }
                        `}
                        onClick={() => handlePlatformToggle(platform.id)}
                      >
                        <Checkbox
                          checked={isSelected}
                          onChange={() => handlePlatformToggle(platform.id)}
                        />
                        <IconComponent
                          className={`h-6 w-6 ${platform.color}`}
                        />
                        <div>
                          <div className="font-medium">{platform.name}</div>
                          <div className="text-sm text-muted-foreground">
                            {platform.description}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>

                {selectedPlatforms.length > 0 && (
                  <div className="mt-4 pt-4 border-t">
                    <div className="flex gap-2 flex-wrap">
                      {selectedPlatforms.map((platformId) => {
                        const platform = platforms.find(
                          (p) => p.id === platformId
                        );
                        return platform ? (
                          <Badge key={platformId} variant="secondary">
                            {platform.name}
                          </Badge>
                        ) : null;
                      })}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          )}
        </div>

        {showGenerated && (
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>Offre modifiée</span>
                  <div className="flex gap-2">
                    {!isEditing ? (
                      <Button variant="outline" size="sm" onClick={handleEdit}>
                        <Edit className="h-4 w-4 mr-1" />
                        Modifier
                      </Button>
                    ) : (
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={handleSaveEdit}
                        >
                          <Check className="h-4 w-4 mr-1" />
                          Sauvegarder
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={handleCancelEdit}
                        >
                          <X className="h-4 w-4 mr-1" />
                          Annuler
                        </Button>
                      </div>
                    )}
                  </div>
                </CardTitle>
                <CardDescription>
                  {isEditing
                    ? "Modifiez le texte selon vos besoins"
                    : "Votre offre d'emploi mise à jour"}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {!isEditing ? (
                  <div className="bg-muted p-4 rounded-lg whitespace-pre-line text-sm border">
                    {generatedOffer}
                  </div>
                ) : (
                  <div className="space-y-2">
                    <Label htmlFor="edit-offer">
                      Modifier l'offre d'emploi :
                    </Label>
                    <Textarea
                      id="edit-offer"
                      value={editedOffer}
                      onChange={(e) => setEditedOffer(e.target.value)}
                      rows={20}
                      className="font-mono text-sm"
                      placeholder="Modifiez votre offre d'emploi..."
                    />
                    <div className="text-xs text-muted-foreground">
                      {editedOffer.length} caractères
                    </div>
                  </div>
                )}

                <Separator />

                <div className="space-y-3">
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => optimizeForPlatform("linkedin")}
                      disabled={isSubmitting}
                    >
                      Optimiser pour LinkedIn
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => optimizeForPlatform("indeed")}
                    >
                      Optimiser pour Indeed
                    </Button>
                  </div>

                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      onClick={handlePreview}
                      className="flex-1"
                      disabled={isSubmitting}
                    >
                      Aperçu
                    </Button>
                    <Button
                      variant="outline"
                      onClick={handleCopy}
                      disabled={isSubmitting}
                    >
                      Copier
                    </Button>
                  </div>

                  <Button
                    onClick={handleUpdate}
                    className="w-full"
                    disabled={isSubmitting}
                  >
                    {isSubmitting && (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    )}
                    {isSubmitting
                      ? "Sauvegarde en cours..."
                      : "Sauvegarder les modifications"}
                  </Button>
                </div>
              </CardContent>
            </Card>

            {showPreview && (
              <Card>
                <CardHeader>
                  <CardTitle>Aperçu de publication</CardTitle>
                  <CardDescription>
                    Voici comment votre offre modifiée apparaîtra sur les plateformes
                    sélectionnées
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {selectedPlatforms.map((platformId) => {
                    const platform = platforms.find((p) => p.id === platformId);
                    if (!platform) return null;

                    const IconComponent = platform.icon;

                    return (
                      <div key={platformId} className="border rounded-lg p-4">
                        <div className="flex items-center gap-2 mb-3">
                          <IconComponent
                            className={`h-5 w-5 ${platform.color}`}
                          />
                          <span className="font-medium">{platform.name}</span>
                        </div>
                        <div className="bg-gray-50 p-3 rounded text-sm">
                          <div className="font-semibold mb-2">
                            {jobData.title}
                          </div>
                          <div className="text-gray-600 mb-2">
                            {jobData.location} • {jobData.contractType}
                          </div>
                          <div className="text-gray-700">
                            {jobData.missions.substring(0, 150)}
                            {jobData.missions.length > 150 ? "..." : ""}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </div>
    </div>
  );
}