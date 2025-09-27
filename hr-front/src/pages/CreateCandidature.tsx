import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useAuth } from "@/KeycloakProvider";
import apiService from '@/config/apiService';
import { useToast } from "@/hooks/use-toast";
import axios from 'axios';



import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  User,
  Mail,
  Phone,
  MapPin,
  Briefcase,
  Upload,
  FileText,
  Loader2,
  Check,
  X,
  Eye,
  Download,
} from "lucide-react";
import { useParams } from "react-router-dom";
import { set } from "date-fns";

export function CreateCandidature() {
  const { keycloak, isAuthenticated, keycloakReady } = useAuth();
  const { toast } = useToast();
  const { encryptedId } = useParams();
  const [candidatData, setCandidatData] = useState({
    nom: "",
    email: "",
    telephone: "",
    poste: "",
    notes: "",
    offreId: ""
  });

  const [cvFile, setCvFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [createdCandidat, setCreatedCandidat] = useState(null);
  const [error, setError] = useState("");

  // Validation du fichier CV
  const validateCvFile = (file) => {
    const allowedTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ];

    if (!allowedTypes.includes(file.type)) {
      setError("Seuls les fichiers PDF, DOC et DOCX sont acceptés");
      return false;
    }

    if (file.size >  2 * 1024 * 1024) { // 10MB
      setError("Le fichier ne doit pas dépasser 2 MB");
      return false;
    }

    setError("");
    return true;
  };
  // Récupérer les informations de l'offre si encryptedId existe
  useEffect(() => {
    if (encryptedId) {
      fetchJobInfo();
    }
  }, [encryptedId]);

  const fetchJobInfo = async () => {
    try {

      const url = `${import.meta.env.VITE_APP_API_BASE_URL}/forms/info/${encryptedId}`;
      console.log("URL appelée:", url);

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'omit'
      });


      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: Offre introuvable`);
      }

      // IMPORTANT: await response.json() pour récupérer les données
      const data = await response.json();
      console.log("Offre récupérée:", data);
      console.log("Titre de l'offre:", data.title);


      // Pré-remplir le poste avec le titre de l'offre
      setCandidatData(prev => ({
        ...prev,
        poste: data.title,
        notes: data.missions,
        offreId: data.id
      }));

    } catch (err) {
      console.error('Erreur récupération offre:', err);
    } finally {
    }
  };

  // Gestion du drag & drop
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (validateCvFile(file)) {
        setCvFile(file);
      }
    }
  };

  const handleFileChange = (e) => {
    console.log("file uploaded");
    
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (validateCvFile(file)) {
        console.log("hiii");
        
        setCvFile(file);
      }
    }
  };

  const removeCvFile = () => {
    setCvFile(null);
    setError("");
  };

  const handleSubmit = async () => {
    if (!cvFile) {
      setError("Veuillez sélectionner un CV");
      return;
    }

    setIsSubmitting(true);
    setError("");

    try {
      // Créer FormData
      const formData = new FormData();
      formData.append('nom', candidatData.nom);
      formData.append('email', candidatData.email);
      formData.append('telephone', candidatData.telephone);
      formData.append('poste', candidatData.poste);
      formData.append('notes', candidatData.notes || '');
      formData.append('offreId', candidatData.offreId);
      formData.append('cv', cvFile);
      const response = await axios.post(
        `${import.meta.env.VITE_APP_API_BASE_URL}/candidats/with-cv`,
        formData,
        {
          headers: {
            'Accept': 'application/json',
          }
        }
      );
      const data = response.data;
      setShowSuccess(false);

      // Reset form
      setCandidatData({
        nom: "",
        email: "",
        telephone: "",
        poste: "",
        notes: "",
        offreId: "",
      });
      setCvFile(null);
      toast({
        title: "✅ Succès",
        description: `Le candidat ${candidatData.nom} pour le poste "${candidatData.poste}" a été créé.`,
      });
      setShowSuccess(true);
      setCreatedCandidat(data);

    } catch (err) {
      console.error("Erreur:", err);
      setError(err.message || "Erreur lors de la création du candidat");

    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setShowSuccess(false);
    setCreatedCandidat(null);
    setError("");
  };

  if (showSuccess && createdCandidat) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-green-600">✅ Candidat créé avec succès !</h1>
          <p className="text-muted-foreground mt-2">
            Le candidat a été ajouté à votre base de données
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Informations du candidat
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Nom</Label>
                <p className="text-lg font-semibold">{createdCandidat.nom}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Poste</Label>
                <p className="text-lg">{createdCandidat.poste}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Email</Label>
                <p>{createdCandidat.email}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Téléphone</Label>
                <p>{createdCandidat.telephone}</p>
              </div>
            </div>

            {createdCandidat.notes && (
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Notes</Label>
                <p className="text-sm bg-muted p-3 rounded-lg">{createdCandidat.notes}</p>
              </div>
            )}

            {createdCandidat.cv && (
              <div>
                <Label className="text-sm font-medium text-muted-foreground">CV</Label>
                <div className="flex items-center gap-2 mt-1">
                  <FileText className="h-4 w-4 text-blue-600" />
                  <span className="text-sm">{createdCandidat.cv.name}</span>
                  <Badge variant="secondary">{(createdCandidat.cv.size / 1024).toFixed(0)} KB</Badge>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Ajouter un candidat</h1>
        <p className="text-muted-foreground mt-2">
          Remplissez les informations du candidat et téléchargez son CV
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-700">Erreur: {error}</p>
        </div>
      )}

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Formulaire d'informations */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Informations personnelles
              </CardTitle>
              <CardDescription>Renseignez les détails du candidat</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="nom" className="flex items-center gap-2">
                    <User className="h-4 w-4" />
                    Nom complet *
                  </Label>
                  <Input
                    id="nom"
                    value={candidatData.nom}
                    onChange={(e) =>
                      setCandidatData({ ...candidatData, nom: e.target.value })
                    }
                    // placeholder="ex: Jean Dupont"
                    required
                    disabled={isSubmitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email" className="flex items-center gap-2">
                    <Mail className="h-4 w-4" />
                    Email *
                  </Label>
                  <Input
                    id="email"
                    type="email"
                    value={candidatData.email}
                    onChange={(e) =>
                      setCandidatData({ ...candidatData, email: e.target.value })
                    }
                    // placeholder="jean.dupont@email.com"
                    required
                    disabled={isSubmitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="telephone" className="flex items-center gap-2">
                    <Phone className="h-4 w-4" />
                    Téléphone *
                  </Label>
                  <Input
                    id="telephone"
                    type="tel"
                    value={candidatData.telephone}
                    onChange={(e) =>
                      setCandidatData({ ...candidatData, telephone: e.target.value })
                    }
                    // placeholder="01 23 45 67 89"
                    required
                    disabled={isSubmitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="poste" className="flex items-center gap-2">
                    <Briefcase className="h-4 w-4" />
                    Poste recherché *
                  </Label>
                  <Input
                    id="poste"
                    value={candidatData.poste}
                    onChange={(e) =>
                      setCandidatData({ ...candidatData, poste: e.target.value })
                    }
                    // placeholder="ex: Développeur Full Stack"
                    required
                    readOnly
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="notes">Notes</Label>
                  <Textarea
                    id="notes"
                    value={candidatData.notes}
                    onChange={(e) =>
                      setCandidatData({ ...candidatData, notes: e.target.value })
                    }
                    // placeholder="Commentaires, observations, profil du candidat..."
                    rows={4}
                    disabled
                  />
                </div>

                <Button
                  onClick={handleSubmit}
                  className="w-full"
                  disabled={isSubmitting || !cvFile}
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Création en cours...
                    </>
                  ) : (
                    <>
                      <Check className="mr-2 h-4 w-4" />
                      Créer le candidat
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Zone de téléchargement CV */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                CV du candidat
              </CardTitle>
              <CardDescription>
                Formats acceptés: PDF, DOC, DOCX (max. 10MB)
              </CardDescription>
            </CardHeader>
            <CardContent>
              {!cvFile ? (
                <div
                  className={`
                    border-2 border-dashed rounded-lg p-8 text-center transition-all cursor-pointer
                    ${dragActive
                      ? 'border-primary bg-primary/5'
                      : 'border-muted-foreground/25 hover:border-primary/50'
                    }
                  `}
                  onDragEnter={handleDrag}
                  onDragLeave={handleDrag}
                  onDragOver={handleDrag}
                  onDrop={handleDrop}
                  onClick={() => document.getElementById('cv-file').click()}
                >
                  <Upload className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                  <div className="space-y-2">
                    <p className="text-lg font-medium">
                      Glissez-déposez votre CV ici
                    </p>
                    <p className="text-sm text-muted-foreground">
                      ou cliquez pour parcourir vos fichiers
                    </p>
                  </div>
                  <input
                    id="cv-file"
                    type="file"
                    accept=".pdf,.doc,.docx"
                    onChange={handleFileChange}
                    className="hidden"
                    disabled={isSubmitting}
                  />
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-green-50 border border-green-200 rounded-lg">
                    <div className="flex items-center gap-3">
                      <FileText className="h-8 w-8 text-green-600" />
                      <div>
                        <p className="font-medium text-green-800">{cvFile.name}</p>
                        <p className="text-sm text-green-600">
                          {(cvFile.size / 1024).toFixed(0)} KB • {cvFile.type.includes('pdf') ? 'PDF' : 'DOC'}
                        </p>
                      </div>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={removeCvFile}
                      disabled={isSubmitting}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>

                  <div className="text-center">
                    <Button
                      variant="outline"
                      onClick={() => document.getElementById('cv-file-replace').click()}
                      disabled={isSubmitting}
                    >
                      <Upload className="mr-2 h-4 w-4" />
                      Remplacer le fichier
                    </Button>
                    <input
                      id="cv-file-replace"
                      type="file"
                      accept=".pdf,.doc,.docx"
                      onChange={handleFileChange}
                      className="hidden"
                      disabled={isSubmitting}
                    />
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Aide */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">💡 Conseils</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground space-y-2">
              <p>• Assurez-vous que toutes les informations sont correctes</p>
              <p>• Le CV doit être au format PDF, DOC ou DOCX</p>
              <p>• Taille maximale: 10MB</p>
              <p>• Les notes peuvent contenir des informations supplémentaires</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}