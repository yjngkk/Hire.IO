import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { Search, Eye, Trash2, Calendar, Linkedin,Edit } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/KeycloakProvider";
import  apiService  from "@/config/apiService";
import { 
  Briefcase, 
  MapPin, 
  TrendingUp, 
  CheckCircle, 
  Target, 
  Star, 
  ExternalLink, 
  Share2 
} from 'lucide-react';
interface JobOffer {
  id: number;
  title: string;
  missions: string;
  location: string;
  contractType: string;
  level: string;
  skills: string;
  tone: string;
  createdAt: string;
  status?: string;
  applications?: number;
  views?: number;
  published?: boolean;
  publishedAt?: string;
  applicationLink : string;
}

interface LinkedInResponse {
  success: boolean;
  formId: number | null;
  generatedContent: string | null;
  targetPlatform: string | null;
  modelUsed: string | null;
  wordCount: number | null;
  processingTimeMs: number | null;
  generationTimestamp: string | null;
  error: string | null;
}

export function Jobs() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { keycloak } = useAuth();
  const [searchTerm, setSearchTerm] = useState("");
  const [jobs, setJobs] = useState<JobOffer[]>([]);
  const [loading, setLoading] = useState(true); // Pour gérer l'état de chargement
  const [error, setError] = useState<string | null>(null); // Pour gérer les erreurs
  const [selectedJob, setSelectedJob] = useState<JobOffer | null>(null);
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [publishing, setPublishing] = useState(false);
  const [openPublishForId, setOpenPublishForId] = useState<number | null>(null);
  const [filter, setFilter] = useState<'all' | 'published' | 'unpublished'>('all');

  const filteredJobs = jobs.filter(job => {
    const matchesSearch = job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (job.location && job.location.toLowerCase().includes(searchTerm.toLowerCase()));
    
    if (filter === 'published') {
      return matchesSearch && job.published === true;
    } else if (filter === 'unpublished') {
      return matchesSearch && (job.published === false || job.published === undefined);
    }
    
    return matchesSearch;
  });

  const publishedCount = jobs.filter(job => job.published === true).length;
  const unpublishedCount = jobs.filter(job => job.published === false || job.published === undefined).length;
  useEffect(() => {
  const fetchJobs = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Récupération des données via votre API
      const response = await apiService.get('/forms'); // ou .post selon votre endpoint
      
      // Mise à jour du state avec les données récupérées
      setJobs(response); // ou response selon la structure de votre réponse
      
    } catch (err) {
      console.error('Erreur lors de la récupération des jobs:', err);
      setError('Impossible de charger les données');
      // En cas d'erreur, vous pouvez garder mockJobs comme fallback
      // setJobs(mockJobs);
    } finally {
      setLoading(false);
    }
  };

  fetchJobs();
}, []); // Se déclenche au montage du composant

  const getStatusBadge = (status: string) => {
    const styles = {
      active: "bg-green-100 text-green-800",
      draft: "bg-yellow-100 text-yellow-800",
      closed: "bg-red-100 text-red-800"
    };
    
    const labels = {
      active: "Active",
      draft: "Brouillon",
      closed: "Fermée"
    };

    return (
      <Badge className={styles[status as keyof typeof styles]}>
        {labels[status as keyof typeof labels]}
      </Badge>
    );
  };

  const handleView = (job: JobOffer) => {
    setSelectedJob(job);
  };

  const handleEdit = (jobId: number) => {
       navigate(`/edit-job/${jobId}`);
  };
  const handleDelete = async (jobId: number) => {
  try {
    await apiService.delete(`/forms/${jobId}`);
    setJobs(jobs.filter(job => job.id !== jobId));
    toast({
      title: "Offre supprimée",
      description: "L'offre d'emploi a été supprimée avec succès.",
    });
  } catch (error) {
    console.error("Erreur lors de la suppression:", error);
    toast({
      title: "Erreur",
      description: "Impossible de supprimer l'offre d'emploi.",
      variant: "destructive",
    });
  }
};

  const handlePlatformToggle = (platformId: string) => {
    setSelectedPlatforms(prev => 
      prev.includes(platformId) 
        ? prev.filter(id => id !== platformId)
        : [...prev, platformId]
    );
  };


  const handlePublish = async (job: JobOffer) => {
    setPublishing(true);
    try {
      const response = await apiService.post<LinkedInResponse>(
  `/linkedin/publish/${job.id}`,
  {}
);

      if (response.success) {
        setJobs(jobs.map(j => 
          j.id === job.id 
            ? { ...j, status: "active", published: true, publishedAt: new Date().toISOString() }
            : j
        ));

        toast({
          title: "Offre publiée",
          description: `L'offre "${job.title}" a été publiée sur LinkedIn avec succès.`,
        });
      } else {
        throw new Error(response.error || "Échec de la publication");
      }
    } catch (error: any) {
      const errorMessage = error.response?.error 
        || error.message 
        || "Échec de la publication sur LinkedIn";
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setPublishing(false);
      setOpenPublishForId(null);
    }
  };

  const handleUnpublish = async (job: JobOffer) => {
    setPublishing(true);
    try {
      const response = await apiService.delete<LinkedInResponse>(
        `/linkedin/unpublish/${job.id}`
      );

      if (response.success) {
        setJobs(jobs.map(j => 
          j.id === job.id 
            ? { ...j, published: false, publishedAt: undefined }
            : j
        ));

        toast({
          title: "Publication annulée",
          description: `La publication de l'offre "${job.title}" a été annulée.`,
        });
      } else {
        throw new Error(response.error || "Échec de l'annulation");
      }
    } catch (error: any) {
      const errorMessage = error.response?.error 
        || error.message 
        || "Échec de l'annulation de la publication";
      
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setPublishing(false);
    }
  };

  if (loading) return <div>Chargement...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Offres d'emploi</h1>
          <p className="text-muted-foreground mt-2">
            Gérez vos offres d'emploi et suivez leur performance
          </p>
        </div>
        <Button onClick={() => navigate('/create-job')}>
          Créer une offre
        </Button>
      </div>

      {/* Statistiques */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total des offres</p>
                <p className="text-2xl font-bold">{jobs.length}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Offres publiées</p>
                <p className="text-2xl font-bold text-green-600">{publishedCount}</p>
              </div>
              <Linkedin className="h-8 w-8 text-green-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Offres non publiées</p>
                <p className="text-2xl font-bold text-yellow-600">{unpublishedCount}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
            <div className="flex items-center space-x-2">
              <Search className="h-4 w-4" />
              <Input
                placeholder="Rechercher une offre..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="max-w-sm"
              />
            </div>
            <div className="flex items-center space-x-2">
              <Button
                variant={filter === 'all' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setFilter('all')}
              >
                Toutes ({jobs.length})
              </Button>
              <Button
                variant={filter === 'published' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setFilter('published')}
                className="text-green-600"
              >
                Publiées ({publishedCount})
              </Button>
              <Button
                variant={filter === 'unpublished' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setFilter('unpublished')}
                className="text-yellow-600"
              >
                Non publiées ({unpublishedCount})
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {filteredJobs.map((job) => (
              <Card key={job.id} className="p-4">
                <div className="flex justify-between items-start">
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-semibold">{job.title}</h3>
                      {getStatusBadge("active")}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span>{job.location}</span>
                      <span>{job.contractType}</span>
                      {job.createdAt && (
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          <span>Créé le {new Date(job.createdAt).toLocaleDateString('fr-FR')}</span>
                        </div>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm">
                      <span className="text-blue-600">{job.applications} candidatures</span>
                      <span className="text-muted-foreground">{job.views} vues</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Dialog>
  <DialogTrigger asChild>
    <Button 
      variant="outline" 
      size="sm" 
      onClick={() => handleView(job)}
      className="hover:bg-blue-50 hover:border-blue-300 hover:text-blue-600 transition-all duration-200"
    >
      <Eye className="h-4 w-4" />
    </Button>
  </DialogTrigger>
  <DialogContent className="max-w-3xl max-h-[90vh] overflow-hidden">
    {/* Header avec gradient */}
    <div className="bg-gradient-to-r from-blue-600 to-purple-600 -mx-6 -mt-6 px-6 py-6 mb-6">
      <DialogHeader className="text-white">
        <DialogTitle className="text-2xl font-bold flex items-center gap-3">
          <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center">
            <Briefcase className="w-5 h-5" />
          </div>
          {job.title}
        </DialogTitle>
        <DialogDescription className="text-blue-100 mt-2">
          Aperçu de l'offre d'emploi
        </DialogDescription>
      </DialogHeader>
    </div>

    {/* Content avec scroll */}
    <div className="overflow-y-auto max-h-[60vh] space-y-6">
      {/* Informations principales avec cards */}
      <div className="grid grid-cols-2 gap-4">
        <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-xl border border-blue-100">
          <MapPin className="w-5 h-5 text-blue-600" />
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide">Lieu</p>
            <p className="font-semibold text-gray-900">{job.location}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 p-4 bg-green-50 rounded-xl border border-green-100">
          <Briefcase className="w-5 h-5 text-green-600" />
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide">Contrat</p>
            <p className="font-semibold text-gray-900">{job.contractType}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 p-4 bg-purple-50 rounded-xl border border-purple-100">
          <TrendingUp className="w-5 h-5 text-purple-600" />
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide">Niveau</p>
            <p className="font-semibold text-gray-900">{job.level}</p>
          </div>
        </div>

        {job.status && (
          <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl border border-gray-100">
            <CheckCircle className="w-5 h-5 text-gray-600" />
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide">Statut</p>
              <div className="mt-1">{getStatusBadge(job.status)}</div>
            </div>
          </div>
        )}
      </div>

      {/* Sections détaillées */}
      <div className="space-y-6">
        {/* Missions */}
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-6 rounded-xl border border-blue-100">
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
              <Target className="w-4 h-4 text-blue-600" />
            </div>
            <h4 className="text-lg font-semibold text-gray-900">Missions</h4>
          </div>
          <p className="text-gray-700 leading-relaxed">{job.missions}</p>
        </div>

        {/* Compétences */}
        <div className="bg-gradient-to-r from-green-50 to-emerald-50 p-6 rounded-xl border border-green-100">
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
              <Star className="w-4 h-4 text-green-600" />
            </div>
            <h4 className="text-lg font-semibold text-gray-900">Compétences requises</h4>
          </div>
          <p className="text-gray-700 leading-relaxed">{job.skills}</p>
        </div>

        {/* Lien d'application */}
        <div className="bg-gradient-to-r from-purple-50 to-pink-50 p-6 rounded-xl border border-purple-100">
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
              <ExternalLink className="w-4 h-4 text-purple-600" />
            </div>
            <h4 className="text-lg font-semibold text-gray-900">Lien d'application</h4>
          </div>
          <div className="flex items-center gap-2">
            <p className="text-gray-700 flex-1 bg-white px-3 py-2 rounded-lg border break-all">
              {job.applicationLink}
            </p>
            <button className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2 whitespace-nowrap" onClick={() => window.open(job.applicationLink, '_blank')}>
              <ExternalLink className="w-4 h-4" />
              Ouvrir
            </button>
          </div>
        </div>
      </div>
    </div>
  </DialogContent>
</Dialog>

                    <Button variant="outline" size="sm" onClick={() => handleEdit(job.id)}>
                      <Edit className="h-4 w-4" />
                    </Button>

                    {job.published ? (
                      <div className="flex items-center gap-2">
                        <Badge className="bg-green-100 text-green-800">
                          <Linkedin className="h-3 w-3 mr-1" />
                          Publié
                        </Badge>
                        {job.publishedAt && (
                          <span className="text-xs text-muted-foreground">
                            {new Date(job.publishedAt).toLocaleDateString('fr-FR')}
                          </span>
                        )}
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleUnpublish(job)}
                          disabled={publishing}
                          className="text-red-600 hover:text-red-700"
                        >
                          Annuler
                        </Button>
                      </div>
                    ) : (
                      <Dialog open={openPublishForId === job.id} onOpenChange={(open) => setOpenPublishForId(open ? job.id : null)}>
                        <DialogTrigger asChild>
                          <Button variant="outline" size="sm" className="text-blue-600 hover:text-blue-700">
                            <Linkedin className="h-4 w-4 mr-2" />
                            Publier
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-md">
                          <DialogHeader>
                            <DialogTitle>Confirmer la publication</DialogTitle>
                            <DialogDescription>
                              Publier l'offre "{job.title}" sur LinkedIn
                            </DialogDescription>
                          </DialogHeader>
                          <div className="space-y-4">
                            <div className="flex items-center space-x-3 p-4 rounded-lg border-2 border-blue-200 bg-blue-50">
                              <Linkedin className="h-6 w-6 text-blue-600" />
                              <div>
                                <p className="font-medium">LinkedIn</p>
                                <p className="text-sm text-muted-foreground">
                                  Cette action publiera l'offre sur votre compte LinkedIn
                                </p>
                              </div>
                            </div>
                            <div className="flex space-x-2">
                              <Button 
                                variant="outline" 
                                className="flex-1"
                                onClick={() => setOpenPublishForId(null)}
                              >
                                Annuler
                              </Button>
                              <Button 
                                onClick={() => handlePublish(job)} 
                                className="flex-1 bg-blue-600 hover:bg-blue-700"
                                disabled={publishing}
                              >
                                {publishing ? (
                                  <div className="flex items-center">
                                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Publication...
                                  </div>
                                ) : 'Confirmer'}
                              </Button>
                            </div>
                          </div>
                        </DialogContent>
                      </Dialog>
                    )}

                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="outline" size="sm">
                          <Trash2 className="h-4 w-4 text-red-500" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Supprimer l'offre</AlertDialogTitle>
                          <AlertDialogDescription>
                            Êtes-vous sûr de vouloir supprimer l'offre "{job.title}" ? 
                            Cette action est irréversible.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Annuler</AlertDialogCancel>
                          <AlertDialogAction 
                            onClick={() => handleDelete(job.id)}
                            className="bg-red-600 hover:bg-red-700"
                          >
                            Supprimer
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
