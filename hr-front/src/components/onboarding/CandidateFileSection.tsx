import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { 
  FileText, Upload, Check, AlertCircle, Mail, Eye, Plus, Edit, Trash2,
  RotateCcw, Copy, Move, CheckCircle, XCircle, Clock, FileX, MoreHorizontal,
  ArrowUp, ArrowDown, Download
} from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { useState, useEffect, useRef } from "react";
import apiService from "@/config/apiService";
import { keycloak } from "@/KeycloakProvider";
import { useOnboardingReminders } from "@/hooks/useOnboardingReminders";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';

interface CandidateFileSectionProps {
  employeeName: string;
  candidatId: number;
  onProgressUpdate?: (progress: number) => void;
}

interface CandidateFile {
  id: number;
  name: string;
  description?: string;
  status: "completed" | "pending" | "missing";
  uploadedDate?: string;
  filePath?: string;
  originalFilename?: string;
  fileSize?: number;
  mimeType?: string;
  isRequired: boolean;
  documentType?: string;
  acceptedFormats?: string;
  orderIndex: number;
  templateId?: number;
  templateName?: string;
}

interface FileCompletionStats {
  totalRequired: number;
  completedRequired: number;
  totalFiles: number;
  completedFiles: number;
  pendingFiles: number;
  missingFiles: number;
  completionPercentage: number;
}

interface CustomFileFormData {
  name: string;
  description: string;
  isRequired: boolean;
  documentType: string;
  acceptedFormats: string;
}

interface EditFileFormData {
  id: number;
  name: string;
  description: string;
  isRequired: boolean;
  documentType: string;
  acceptedFormats: string;
}

const documentTypes = [
  { value: "contract", label: "Contrat" },
  { value: "identity", label: "Identité" },
  { value: "banking", label: "Bancaire" },
  { value: "employment", label: "Emploi" },
  { value: "education", label: "Éducation" },
  { value: "social_security", label: "Sécurité sociale" },
  { value: "photo", label: "Photo" },
  { value: "medical", label: "Médical" },
  { value: "legal", label: "Légal" },
  { value: "other", label: "Autre" }
];

export function CandidateFileSection({ employeeName, candidatId, onProgressUpdate }: CandidateFileSectionProps) {
  const { toast } = useToast();
  const [files, setFiles] = useState<CandidateFile[]>([]);
  const [stats, setStats] = useState<FileCompletionStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCustomFileDialogOpen, setIsCustomFileDialogOpen] = useState(false);
  const [isEditFileDialogOpen, setIsEditFileDialogOpen] = useState(false);
  const [deleteFileId, setDeleteFileId] = useState<number | null>(null);

  const [customFileForm, setCustomFileForm] = useState<CustomFileFormData>({
    name: "",
    description: "",
    isRequired: true,
    documentType: "other",
    acceptedFormats: ".pdf,.doc,.docx,.jpg,.jpeg,.png"
  });

  const [editFileForm, setEditFileForm] = useState<EditFileFormData>({
    id: 0,
    name: "",
    description: "",
    isRequired: true,
    documentType: "other",
    acceptedFormats: ".pdf,.doc,.docx,.jpg,.jpeg,.png"
  });

  const previousProgressRef = useRef<number>(-1);
  const currentCandidatRef = useRef<number | null>(null);

  // Use the shared reminder hook
  const { sendGlobalReminder, sendIndividualReminder, requestDocument, hasFilesToRemind } = useOnboardingReminders();

  // Reset state when candidatId changes
  useEffect(() => {
    if (currentCandidatRef.current !== candidatId) {
      currentCandidatRef.current = candidatId;
      previousProgressRef.current = -1;
      setFiles([]);
      setStats(null);
      setLoading(true);
    }
    loadCandidateFiles();
    loadCompletionStats();
  }, [candidatId]);

  // Calculate and report progress whenever files change
  useEffect(() => {
    if (!loading && stats) {
      const completionRate = Math.round(stats.completionPercentage);
      
      if (onProgressUpdate && previousProgressRef.current !== completionRate) {
        previousProgressRef.current = completionRate;
        onProgressUpdate(completionRate);
      }
    }
  }, [stats, onProgressUpdate, loading]);

  const loadCandidateFiles = async () => {
    try {
      setLoading(true);
      const data = await apiService.get<CandidateFile[]>(`/candidate-files/candidat/${candidatId}`);
      setFiles(data);
    } catch (error) {
      console.error('Erreur lors du chargement des fichiers:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors du chargement des fichiers candidat",
        variant: "destructive"
      });
    } finally {
      setLoading(false);
    }
  };

const parseFrenchDate = (dateString: string) => {
  try {
    const [datePart, timePart = "00:00"] = dateString.split(' ');
    const [day, month, year] = datePart.split('/').map(Number);
    const [hours = 0, minutes = 0] = timePart.split(':').map(Number);
    const date = new Date(year, month - 1, day, hours, minutes);
    return isNaN(date.getTime()) ? null : date;
  } catch (error) {
    return null;
  }
};

  const loadCompletionStats = async () => {
    try {
      const data = await apiService.get<FileCompletionStats>(`/candidate-files/candidat/${candidatId}/stats`);
      setStats(data);
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error);
    }
  };

  const refreshData = async () => {
    await loadCandidateFiles();
    await loadCompletionStats();
  };

 const [uploadProgress, setUploadProgress] = useState<{[key: number]: number}>({});

const handleUploadDocument = async (fileId: number, fileName: string) => {
  try {
    const input = document.createElement('input');
    input.type = 'file';
    const candidateFile = files.find(f => f.id === fileId);
    input.accept = candidateFile?.acceptedFormats || '.pdf,.doc,.docx,.jpg,.jpeg,.png';
    
    input.onchange = async (event) => {
      const file = (event.target as HTMLInputElement).files?.[0];
      if (!file) return;

      // File size validation (50MB limit)
      const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB in bytes
      if (file.size > MAX_FILE_SIZE) {
        toast({
          title: "Fichier trop volumineux",
          description: `Le fichier "${file.name}" (${Math.round(file.size / (1024 * 1024))}MB) dépasse la limite de 50MB`,
          variant: "destructive"
        });
        return;
      }

      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await apiService.post(`/candidate-files/${fileId}/upload`, formData);
        
        toast({
          title: "Document uploadé",
          description: `"${fileName}" a été uploadé avec succès pour ${employeeName}`,
        });

        await refreshData();

      } catch (uploadError: any) {
        console.error('Erreur lors de l\'upload:', uploadError);
        
        // Handle specific error types
        if (uploadError?.response?.status === 413) {
          toast({
            title: "Fichier trop volumineux",
            description: "Le fichier dépasse la taille maximale autorisée (50MB)",
            variant: "destructive"
          });
        } else if (uploadError?.response?.status === 400) {
          toast({
            title: "Erreur de validation",
            description: uploadError?.response?.data?.error || "Format de fichier non supporté",
            variant: "destructive"
          });
        } else {
          toast({
            title: "Erreur d'upload",
            description: "Une erreur s'est produite lors de l'upload du document",
            variant: "destructive"
          });
        }
      }
    };

    input.click();

  } catch (error) {
    console.error('Erreur lors de l\'upload:', error);
    toast({
      title: "Erreur",
      description: "Erreur lors de l'initialisation de l'upload",
      variant: "destructive"
    });
  }
};


const handleViewDocument = async (file: CandidateFile) => {
  if (!file.id) {
    toast({
      title: "Erreur",
      description: "Document ID non disponible",
      variant: "destructive"
    });
    return;
  }

  try {
    // Fetch the file first (this ensures authentication works)
    const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/candidate-files/${file.id}/view`, {
      headers: {
        'Authorization': `Bearer ${keycloak.token}`,
      }
    });

    if (!response.ok) {
      // If /view endpoint doesn't exist, try /download
      const downloadResponse = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/candidate-files/${file.id}/download`, {
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
        }
      });
      
      if (!downloadResponse.ok) {
        throw new Error('Cannot access document');
      }
      
      // Use download endpoint but try to display it
      const blob = await downloadResponse.blob();
      const url = URL.createObjectURL(blob);
      const contentType = downloadResponse.headers.get('content-type') || '';
      
      if (contentType.includes('pdf') || contentType.includes('image')) {
        window.open(url, '_blank');
      } else {
        // For other files, show a message and download
        toast({
          title: "Téléchargement",
          description: "Ce type de fichier ne peut pas être affiché dans le navigateur",
        });
        const link = document.createElement('a');
        link.href = url;
        link.download = file.originalFilename || file.name;
        link.click();
      }
      
      setTimeout(() => URL.revokeObjectURL(url), 1000);
      return;
    }

    // If we reach here, /view endpoint exists
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    
    // Open in new tab - browser should display it inline because we used /view endpoint
    const newTab = window.open(url, '_blank');
    
    if (!newTab) {
      toast({
        title: "Popup bloqué",
        description: "Autoriser les popups pour voir le document",
      });
    }
    
    // Clean up
    setTimeout(() => URL.revokeObjectURL(url), 5000);

  } catch (error) {
    console.error('Error viewing document:', error);
    toast({
      title: "Erreur",
      description: "Impossible d'accéder au document. Vérifiez que l'endpoint /view existe.",
      variant: "destructive"
    });
  }
};
  const handleAddCustomFile = async () => {
    try {
      const nextOrderIndex = Math.max(...files.map(f => f.orderIndex), 0) + 1;
      
      const customFileData = {
        ...customFileForm,
        orderIndex: nextOrderIndex
      };

      await apiService.post(`/candidate-files/candidat/${candidatId}/custom`, customFileData);
      
      toast({
        title: "Fichier ajouté",
        description: `"${customFileForm.name}" a été ajouté à la liste des documents`,
      });

      setIsCustomFileDialogOpen(false);
      setCustomFileForm({
        name: "",
        description: "",
        isRequired: true,
        documentType: "other",
        acceptedFormats: ".pdf,.doc,.docx,.jpg,.jpeg,.png"
      });

      await refreshData();

    } catch (error) {
      console.error('Erreur lors de l\'ajout:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors de l'ajout du fichier",
        variant: "destructive"
      });
    }
  };

  const handleEditFile = (file: CandidateFile) => {
    setEditFileForm({
      id: file.id,
      name: file.name,
      description: file.description || "",
      isRequired: file.isRequired,
      documentType: file.documentType || "other",
      acceptedFormats: file.acceptedFormats || ".pdf,.doc,.docx,.jpg,.jpeg,.png"
    });
    setIsEditFileDialogOpen(true);
  };

  const handleUpdateFile = async () => {
    try {
      await apiService.put(`/candidate-files/${editFileForm.id}`, editFileForm);
      
      toast({
        title: "Fichier mis à jour",
        description: `"${editFileForm.name}" a été mis à jour`,
      });

      setIsEditFileDialogOpen(false);
      await refreshData();

    } catch (error) {
      console.error('Erreur lors de la mise à jour:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors de la mise à jour du fichier",
        variant: "destructive"
      });
    }
  };

  const handleDeleteFile = async (fileId: number, fileName: string) => {
    try {
      await apiService.delete(`/candidate-files/${fileId}`);
      
      toast({
        title: "Fichier supprimé",
        description: `"${fileName}" a été supprimé`,
      });

      await refreshData();

    } catch (error) {
      console.error('Erreur lors de la suppression:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors de la suppression du fichier",
        variant: "destructive"
      });
    }
  };

  const handleReorderFiles = async (sourceIndex: number, destinationIndex: number) => {
    const reorderedFiles = Array.from(files);
    const [removed] = reorderedFiles.splice(sourceIndex, 1);
    reorderedFiles.splice(destinationIndex, 0, removed);

    const fileIds = reorderedFiles.map(file => file.id);

    try {
      await apiService.put(`/candidate-files/candidat/${candidatId}/reorder`, fileIds);
      await loadCandidateFiles();
    } catch (error) {
      console.error('Erreur lors du réordonnement:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors du réordonnement des fichiers",
        variant: "destructive"
      });
    }
  };

  const onDragEnd = (result: any) => {
    if (!result.destination) return;
    
    const sourceIndex = result.source.index;
    const destinationIndex = result.destination.index;
    
    if (sourceIndex !== destinationIndex) {
      handleReorderFiles(sourceIndex, destinationIndex);
    }
  };

  const getStatusBadge = (status: string, isRequired: boolean) => {
    const configs = {
      completed: { color: "bg-green-100 text-green-800", icon: CheckCircle, label: "Complété" },
      pending: { color: "bg-yellow-100 text-yellow-800", icon: Clock, label: "En attente" },
      missing: { color: isRequired ? "bg-red-100 text-red-800" : "bg-gray-100 text-gray-800", icon: FileX, label: "Manquant" },
    };
    
    const config = configs[status as keyof typeof configs];
    const Icon = config.icon;
    
    return (
      <Badge className={config.color}>
        <Icon className="h-3 w-3 mr-1" />
        {config.label}
      </Badge>
    );
  };

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return '';
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Dossier administratif - {employeeName}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
            <span className="ml-2">Chargement des documents...</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <FileText className="h-5 w-5" />
          Dossier administratif - {employeeName}
        </CardTitle>
        <CardDescription>
          Suivi des documents requis pour l'intégration
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {stats && (
            <div className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
              <div>
                <p className="font-medium">Progression du dossier</p>
                <p className="text-sm text-muted-foreground">
                  {stats.completedRequired}/{stats.totalRequired} documents requis • {stats.completedFiles}/{stats.totalFiles} total
                </p>
              </div>
              <div className="text-right">
                <div className="flex items-center gap-2">
                  <Progress value={stats.completionPercentage} className="w-24" />
                  <span className="text-sm font-medium">{Math.round(stats.completionPercentage)}%</span>
                </div>
              </div>
            </div>
          )}

          <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="files">
              {(provided) => (
                <div {...provided.droppableProps} ref={provided.innerRef} className="space-y-2">
                  {files.map((file, index) => (
                    <Draggable key={file.id} draggableId={file.id.toString()} index={index}>
                      {(provided, snapshot) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          className={`flex items-center justify-between p-3 border rounded-lg group hover:bg-accent/50 transition-colors ${
                            snapshot.isDragging ? 'shadow-lg bg-accent' : ''
                          }`}
                        >
                          <div className="flex items-center gap-3 flex-1">
                            <div {...provided.dragHandleProps} className="opacity-0 group-hover:opacity-100 transition-opacity">
                              <Move className="h-4 w-4 text-muted-foreground cursor-grab" />
                            </div>
                            <FileText className="h-4 w-4 text-muted-foreground" />
                            <div className="flex-1">
                              <div className="flex items-center gap-2">
                                <span className="font-medium">{file.name}</span>
                                {!file.isRequired && (
                                  <Badge variant="outline" className="text-xs">Optionnel</Badge>
                                )}
                              </div>
                              {file.description && (
                                <p className="text-xs text-muted-foreground mt-1">{file.description}</p>
                              )}
                           {file.uploadedDate && parseFrenchDate(file.uploadedDate) && (
                            <p className="text-xs text-muted-foreground">
                              Reçu le {parseFrenchDate(file.uploadedDate)!.toLocaleDateString('fr-FR')} à {parseFrenchDate(file.uploadedDate)!.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                              {file.fileSize && ` • ${formatFileSize(file.fileSize)}`}
                            </p>
                          )}
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            {getStatusBadge(file.status, file.isRequired)}
                            
                            {/* Action buttons based on status */}
                            {file.status === "missing" && (
                            <>
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => requestDocument(file.id, file.name, refreshData)}
                              >
                                <Mail className="h-3 w-3 mr-1" />
                                Demander
                              </Button>
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => handleUploadDocument(file.id, file.name)}
                              >
                                <Upload className="h-3 w-3 mr-1" />
                                Upload
                              </Button>
                            </>
                          )}
                            
                            {file.status === "pending" && (
                            <>
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => handleUploadDocument(file.id, file.name)}
                              >
                                <Upload className="h-3 w-3 mr-1" />
                                Upload
                              </Button>
                            </>
                          )}

                            {/* More actions dropdown */}
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="h-8 w-8 p-0">
                                  <MoreHorizontal className="h-4 w-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                              <DropdownMenuLabel>Actions</DropdownMenuLabel>
                              
                              {file.filePath && (
                                <DropdownMenuItem onClick={() => handleViewDocument(file)}>
                                  <Eye className="mr-2 h-4 w-4" />
                                  Voir le document
                                </DropdownMenuItem>
                              )}
                              
                              <DropdownMenuItem onClick={() => handleUploadDocument(file.id, file.name)}>
                                <Upload className="mr-2 h-4 w-4" />
                                {file.filePath ? 'Remplacer' : 'Upload'}
                              </DropdownMenuItem>
                              
                              <DropdownMenuSeparator />
                              
                              <DropdownMenuItem onClick={() => handleEditFile(file)}>
                                <Edit className="mr-2 h-4 w-4" />
                                Modifier
                              </DropdownMenuItem>
                                
                                {file.status === "pending" && (
                                  <>
                                    <DropdownMenuSeparator />
                                     <DropdownMenuItem 
                                    onClick={() => sendIndividualReminder(file.id, file.name)}
                                    className="text-blue-600"
                                  >
                                    <Mail className="mr-2 h-4 w-4" />
                                    Envoyer rappel
                                  </DropdownMenuItem>
                                  </>
                                )}
                                                          
                                <DropdownMenuSeparator />
                                <DropdownMenuItem 
                                  onClick={() => setDeleteFileId(file.id)}
                                  className="text-red-600"
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Supprimer
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>

          {/* Action buttons */}
          <div className="flex gap-2 pt-4 border-t">
            <Dialog open={isCustomFileDialogOpen} onOpenChange={setIsCustomFileDialogOpen}>
              <DialogTrigger asChild>
                <Button variant="outline">
                  <Plus className="h-4 w-4 mr-2" />
                  Ajouter fichier
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Ajouter un fichier</DialogTitle>
                  <DialogDescription>
                    Créez un nouveau document spécifique à ce candidat.
                  </DialogDescription>
                </DialogHeader>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Nom du document *</Label>
                    <Input
                      id="name"
                      value={customFileForm.name}
                      onChange={(e) => setCustomFileForm({ ...customFileForm, name: e.target.value })}
                      placeholder="Ex: Contrat spécifique, Document additionnel..."
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                      id="description"
                      value={customFileForm.description}
                      onChange={(e) => setCustomFileForm({ ...customFileForm, description: e.target.value })}
                      placeholder="Description du document"
                      rows={2}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="documentType">Type de document</Label>
                    <Select 
                      value={customFileForm.documentType} 
                      onValueChange={(value) => setCustomFileForm({ ...customFileForm, documentType: value })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Sélectionner un type" />
                      </SelectTrigger>
                      <SelectContent>
                        {documentTypes.map((type) => (
                          <SelectItem key={type.value} value={type.value}>
                            {type.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="acceptedFormats">Formats acceptés</Label>
                    <Input
                      id="acceptedFormats"
                      value={customFileForm.acceptedFormats}
                      onChange={(e) => setCustomFileForm({ ...customFileForm, acceptedFormats: e.target.value })}
                      placeholder=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                    />
                  </div>
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="isRequired"
                      checked={customFileForm.isRequired}
                      onCheckedChange={(checked) => setCustomFileForm({ ...customFileForm, isRequired: checked as boolean })}
                    />
                    <Label htmlFor="isRequired">Document requis</Label>
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    variant="outline"
                    onClick={() => setIsCustomFileDialogOpen(false)}
                  >
                    Annuler
                  </Button>
                  <Button
                    onClick={handleAddCustomFile}
                    disabled={!customFileForm.name.trim()}
                  >
                    Ajouter
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
            </div>
        </div>

        {/* Edit File Dialog */}
        <Dialog open={isEditFileDialogOpen} onOpenChange={setIsEditFileDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Modifier le fichier</DialogTitle>
              <DialogDescription>
                Modifiez les propriétés du fichier.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="editName">Nom du document *</Label>
                <Input
                  id="editName"
                  value={editFileForm.name}
                  onChange={(e) => setEditFileForm({ ...editFileForm, name: e.target.value })}
                  placeholder="Nom du document"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="editDescription">Description</Label>
                <Textarea
                  id="editDescription"
                  value={editFileForm.description}
                  onChange={(e) => setEditFileForm({ ...editFileForm, description: e.target.value })}
                  placeholder="Description du document"
                  rows={2}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="editDocumentType">Type de document</Label>
                <Select 
                  value={editFileForm.documentType} 
                  onValueChange={(value) => setEditFileForm({ ...editFileForm, documentType: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Sélectionner un type" />
                  </SelectTrigger>
                  <SelectContent>
                    {documentTypes.map((type) => (
                      <SelectItem key={type.value} value={type.value}>
                        {type.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="editAcceptedFormats">Formats acceptés</Label>
                <Input
                  id="editAcceptedFormats"
                  value={editFileForm.acceptedFormats}
                  onChange={(e) => setEditFileForm({ ...editFileForm, acceptedFormats: e.target.value })}
                  placeholder=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                />
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="editIsRequired"
                  checked={editFileForm.isRequired}
                  onCheckedChange={(checked) => setEditFileForm({ ...editFileForm, isRequired: checked as boolean })}
                />
                <Label htmlFor="editIsRequired">Document requis</Label>
              </div>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setIsEditFileDialogOpen(false)}
              >
                Annuler
              </Button>
              <Button
                onClick={handleUpdateFile}
                disabled={!editFileForm.name.trim()}
              >
                Mettre à jour
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Delete Single File Confirmation */}
        <AlertDialog open={deleteFileId !== null} onOpenChange={() => setDeleteFileId(null)}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Supprimer ce fichier ?</AlertDialogTitle>
              <AlertDialogDescription>
                Cette action supprimera définitivement ce fichier. 
                Cette action ne peut pas être annulée.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Annuler</AlertDialogCancel>
              <AlertDialogAction 
                onClick={() => {
                  if (deleteFileId) {
                    const file = files.find(f => f.id === deleteFileId);
                    if (file) {
                      handleDeleteFile(deleteFileId, file.name);
                    }
                  }
                  setDeleteFileId(null);
                }}
                className="bg-red-600 hover:bg-red-700"
              >
                Supprimer
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </CardContent>
    </Card>
  );
}