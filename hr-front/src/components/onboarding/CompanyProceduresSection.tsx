import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Building, CheckCircle, Clock, User, Plus, Edit, Trash2, Loader2, Calendar as CalendarIcon, CalendarDays } from "lucide-react";
import { useState, useEffect } from "react";
import { format, addDays, addWeeks, startOfDay, isToday, isTomorrow, isPast, setHours, setMinutes } from "date-fns";
import { fr } from "date-fns/locale";
import { cn } from "@/lib/utils";
import apiService from "@/config/apiService";

interface Procedure {
  id: number;
  title: string;
  responsible: string;
  dueDate?: string; // ISO string from backend
  completed: boolean;
  description: string;
  candidatId: number;
  candidatNom: string;
  candidatEmail: string;
  candidatPoste: string;
  status?: 'PENDING' | 'DUE_TODAY' | 'OVERDUE' | 'COMPLETED';
}

interface Candidat {
  id: number;
  nom: string;
  email: string;
  poste: string;
}

interface ProcedureFormData {
  title: string;
  responsible: string;
  dueDate: Date | null;
  dueHour: number;
  dueMinute: number;
  description: string;
  candidatId: number | null;
  customResponsible?: string;
}

interface CompanyProceduresSectionProps {
  selectedCandidatId?: number | null;
}

const initialFormData: ProcedureFormData = {
  title: "",
  responsible: "",
  dueDate: null,
  dueHour: 9,
  dueMinute: 0,
  description: "",
  candidatId: null,
  customResponsible: ""
};

const responsibleOptions = ["RH", "IT", "Manager", "Direction", "Autre"];

// Generate hour options (0-23)
const hourOptions = Array.from({ length: 24 }, (_, i) => i);

// Generate minute options (0-59)
const minuteOptions = Array.from({ length: 60 }, (_, i) => i);

// Common minute intervals for quick selection
const commonMinutes = [0, 15, 30, 45];

// Move ProcedureForm outside the main component
interface ProcedureFormProps {
  formData: ProcedureFormData;
  setFormData: (data: ProcedureFormData) => void;
  onSubmit: (e: React.FormEvent) => void;
  onCancel: () => void;
  submitting: boolean;
  editingProcedure: Procedure | null;
  candidats: Candidat[];
  selectedCandidatId?: number | null;
}

const ProcedureForm: React.FC<ProcedureFormProps> = ({
  formData,
  setFormData,
  onSubmit,
  onCancel,
  submitting,
  editingProcedure,
  candidats,
  selectedCandidatId
}) => {
  const [calendarOpen, setCalendarOpen] = useState(false);

  const handleResponsibleChange = (value: string) => {
    setFormData({ 
      ...formData, 
      responsible: value,
      customResponsible: value === "Autre" ? formData.customResponsible : ""
    });
  };

  const handleCustomResponsibleChange = (value: string) => {
    setFormData({ 
      ...formData, 
      customResponsible: value,
      responsible: value ? value : "Autre"
    });
  };

  const handleDateSelect = (date: Date | undefined) => {
    if (date) {
      setFormData({ 
        ...formData, 
        dueDate: startOfDay(date)
      });
      setCalendarOpen(false);
    }
  };

  const handleHourChange = (hour: string) => {
    setFormData({ 
      ...formData, 
      dueHour: parseInt(hour)
    });
  };

  const handleMinuteChange = (minute: string) => {
    setFormData({ 
      ...formData, 
      dueMinute: parseInt(minute)
    });
  };

  const handleCommonMinuteClick = (minute: number) => {
    setFormData({ 
      ...formData, 
      dueMinute: minute
    });
  };

  const formatDisplayDate = (date: Date | null) => {
    if (!date) return "Sélectionner une date";
    
    if (isToday(date)) return "Aujourd'hui";
    if (isTomorrow(date)) return "Demain";
    
    return format(date, "EEEE dd MMMM yyyy", { locale: fr });
  };

  const getDateStatus = (date: Date | null) => {
    if (!date) return null;
    
    if (isPast(date) && !isToday(date)) return "overdue";
    if (isToday(date)) return "today";
    if (isTomorrow(date)) return "tomorrow";
    return "future";
  };

  const getDateStatusColor = (status: string | null) => {
    switch (status) {
      case "overdue": return "text-red-600";
      case "today": return "text-orange-600";
      case "tomorrow": return "text-yellow-600";
      default: return "text-foreground";
    }
  };

  const getCombinedDateTime = () => {
    if (!formData.dueDate) return null;
    return setMinutes(setHours(formData.dueDate, formData.dueHour), formData.dueMinute);
  };

  const formatTime = (hour: number, minute: number) => {
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
  };

  return (
    <div className="max-h-[70vh] overflow-y-auto pr-2">
      <form onSubmit={onSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="title">Titre *</Label>
          <Input
            id="title"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            placeholder="Titre de la procédure"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="candidat">Candidat *</Label>
          <Select 
            value={formData.candidatId?.toString() || ""} 
            onValueChange={(value) => setFormData({ ...formData, candidatId: parseInt(value) })}
            disabled={!!selectedCandidatId}
          >
            <SelectTrigger>
              <SelectValue placeholder="Sélectionner un candidat" />
            </SelectTrigger>
            <SelectContent>
              {candidats.map((candidat) => (
                <SelectItem key={candidat.id} value={candidat.id.toString()}>
                  {candidat.nom} - {candidat.poste}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {selectedCandidatId && (
            <p className="text-sm text-muted-foreground">
              Procédure pour le candidat sélectionné
            </p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="responsible">Responsable *</Label>
          <Select 
            value={formData.responsible === formData.customResponsible && formData.customResponsible ? "Autre" : formData.responsible} 
            onValueChange={handleResponsibleChange}
          >
            <SelectTrigger>
              <SelectValue placeholder="Sélectionner un responsable" />
            </SelectTrigger>
            <SelectContent>
              {responsibleOptions.map((option) => (
                <SelectItem key={option} value={option}>
                  {option}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          
          {(formData.responsible === "Autre" || (formData.customResponsible && !responsibleOptions.includes(formData.responsible))) && (
            <div className="mt-2">
              <Input
                placeholder="Saisir le responsable personnalisé"
                value={formData.customResponsible}
                onChange={(e) => handleCustomResponsibleChange(e.target.value)}
                className="animate-in slide-in-from-top-2 duration-200"
              />
            </div>
          )}
        </div>

        <div className="space-y-3">
          <Label>Date et heure d'échéance *</Label>
          
          {/* Calendar Date Picker */}
          <div className="space-y-2">
            <Label className="text-sm font-normal">Date</Label>
            <Popover open={calendarOpen} onOpenChange={setCalendarOpen}>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  className={cn(
                    "w-full justify-start text-left font-normal",
                    !formData.dueDate && "text-muted-foreground",
                    getDateStatusColor(getDateStatus(formData.dueDate))
                  )}
                >
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {formatDisplayDate(formData.dueDate)}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                  mode="single"
                  selected={formData.dueDate || undefined}
                  onSelect={handleDateSelect}
                  disabled={(date) => date < startOfDay(new Date())}
                  initialFocus
                />
              </PopoverContent>
            </Popover>
          </div>

          {/* Time Pickers */}
          <div className="space-y-3">
            <Label className="text-sm font-normal">Heure</Label>
            
            {/* Hour and Minute Selectors */}
            <div className="flex items-center gap-3">
              {/* Hour Selector */}
              <div className="flex-1">
                <Label className="text-xs text-muted-foreground">Heure</Label>
                <Select 
                  value={formData.dueHour.toString()} 
                  onValueChange={handleHourChange}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="max-h-48">
                    {hourOptions.map((hour) => (
                      <SelectItem key={hour} value={hour.toString()}>
                        {hour.toString().padStart(2, '0')}h
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="text-2xl font-bold text-muted-foreground pt-5">:</div>

              {/* Minute Selector */}
              <div className="flex-1">
                <Label className="text-xs text-muted-foreground">Minutes</Label>
                <Select 
                  value={formData.dueMinute.toString()} 
                  onValueChange={handleMinuteChange}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="max-h-48">
                    {minuteOptions.map((minute) => (
                      <SelectItem key={minute} value={minute.toString()}>
                        {minute.toString().padStart(2, '0')}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Quick Minute Selection */}
            <div className="space-y-2">
              <Label className="text-xs text-muted-foreground">Raccourcis minutes</Label>
              <div className="flex gap-2">
                {commonMinutes.map((minute) => (
                  <Button
                    key={minute}
                    type="button"
                    variant={formData.dueMinute === minute ? "default" : "outline"}
                    size="sm"
                    onClick={() => handleCommonMinuteClick(minute)}
                    className="px-3 py-1 text-xs"
                  >
                    :{minute.toString().padStart(2, '0')}
                  </Button>
                ))}
              </div>
            </div>
          </div>

          {/* Combined Date & Time Display */}
          {formData.dueDate && (
            <div className="p-3 bg-muted/50 rounded-lg">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">Échéance programmée</p>
                  <p className={cn("text-sm", getDateStatusColor(getDateStatus(formData.dueDate)))}>
                    {formatDisplayDate(formData.dueDate)} à {formatTime(formData.dueHour, formData.dueMinute)}
                  </p>
                </div>
                {getDateStatus(formData.dueDate) === "overdue" && (
                  <Badge variant="destructive" className="text-xs">
                    Date passée
                  </Badge>
                )}
                {getDateStatus(formData.dueDate) === "today" && (
                  <Badge variant="secondary" className="text-xs bg-orange-100 text-orange-800">
                    Aujourd'hui
                  </Badge>
                )}
                {getDateStatus(formData.dueDate) === "tomorrow" && (
                  <Badge variant="secondary" className="text-xs bg-yellow-100 text-yellow-800">
                    Demain
                  </Badge>
                )}
              </div>
            </div>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="description">Description</Label>
          <Textarea
            id="description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Description de la procédure"
            rows={3}
            className="min-h-[80px] resize-none"
          />
        </div>

        <div className="pt-4 border-t bg-background sticky bottom-0">
          <div className="flex justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
            >
              Annuler
            </Button>
            <Button type="submit" disabled={submitting || !formData.dueDate}>
              {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {editingProcedure ? 'Mettre à jour' : 'Ajouter'}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
};

export function CompanyProceduresSection({ selectedCandidatId }: CompanyProceduresSectionProps) {
  const [procedures, setProcedures] = useState<Procedure[]>([]);
  const [candidats, setCandidats] = useState<Candidat[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingProcedure, setEditingProcedure] = useState<Procedure | null>(null);
  const [formData, setFormData] = useState<ProcedureFormData>(initialFormData);
  const [submitting, setSubmitting] = useState(false);

  // Load procedures and candidats on component mount
  useEffect(() => {
    loadProcedures();
    loadCandidats();
  }, [selectedCandidatId]);

  const loadProcedures = async () => {
    try {
      setLoading(true);
      let data: Procedure[];
      
      if (selectedCandidatId) {
        // Load procedures for specific candidat
        data = await apiService.get<Procedure[]>(`/procedures/candidat/${selectedCandidatId}`);
      } else {
        // Load all procedures
        data = await apiService.get<Procedure[]>('/procedures');
      }
      
      setProcedures(data);
    } catch (error) {
      console.error('Erreur lors du chargement des procédures:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadCandidats = async () => {
    try {
      const data = await apiService.get<Candidat[]>('/candidats');
      setCandidats(data);
    } catch (error) {
      console.error('Erreur lors du chargement des candidats:', error);
    }
  };

  const toggleProcedure = async (procedureId: number) => {
    try {
      const updatedProcedure = await apiService.post<Procedure>(`/procedures/${procedureId}/toggle`);
      setProcedures(procedures.map(proc => 
        proc.id === procedureId ? updatedProcedure : proc
      ));
    } catch (error) {
      console.error('Erreur lors de la mise à jour de la procédure:', error);
    }
  };

  const getCombinedDateTime = (date: Date, hour: number, minute: number) => {
    return setMinutes(setHours(date, hour), minute);
  };

// Add this helper function to format local datetime without timezone conversion
const formatLocalDateTime = (date: Date, hour: number, minute: number): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(hour).padStart(2, '0');
  const minutes = String(minute).padStart(2, '0');
  
  // Include seconds - Spring Boot default format requirement
  return `${year}-${month}-${day}T${hours}:${minutes}:00`;
};

// Update handleCreateSubmit
const handleCreateSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setSubmitting(true);

  try {
    // Use local datetime formatting instead of toISOString()
    const localDateTime = formatLocalDateTime(formData.dueDate!, formData.dueHour, formData.dueMinute);
    
    const submitData = {
      title: formData.title,
      responsible: formData.customResponsible || formData.responsible,
      dueDate: localDateTime, // Send as local time
      description: formData.description,
      candidatId: selectedCandidatId || formData.candidatId,
      completed: false
    };

    const newProcedure = await apiService.post<Procedure>('/procedures', submitData);
    setProcedures([...procedures, newProcedure]);
    setIsCreateDialogOpen(false);
    setFormData(initialFormData);
  } catch (error: any) {
    console.error('Erreur lors de la création:', error);
    
    if (error.response?.data?.message) {
      alert(`Erreur: ${error.response.data.message}`);
    } else {
      alert('Une erreur est survenue lors de la création de la procédure.');
    }
  } finally {
    setSubmitting(false);
  }
};

// Update handleEditSubmit  
const handleEditSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  if (!editingProcedure) return;
  
  setSubmitting(true);

  try {
    // Use local datetime formatting instead of toISOString()
    const localDateTime = formatLocalDateTime(formData.dueDate!, formData.dueHour, formData.dueMinute);
    
    const submitData = {
      title: formData.title,
      responsible: formData.customResponsible || formData.responsible,
      deadline: localDateTime, // Send as local time
      dueDate: localDateTime,   // Send as local time
      description: formData.description,
      candidatId: formData.candidatId,
      completed: editingProcedure.completed
    };

    const updatedProcedure = await apiService.put<Procedure>(`/procedures/${editingProcedure.id}`, submitData);
    setProcedures(procedures.map(proc => 
      proc.id === editingProcedure.id ? updatedProcedure : proc
    ));
    setIsEditDialogOpen(false);
    setEditingProcedure(null);
    setFormData(initialFormData);
    console.log('Sending datetime:', formatLocalDateTime(formData.dueDate!, formData.dueHour, formData.dueMinute));
  } catch (error: any) {
    console.error('Erreur lors de la modification:', error);
    
    if (error.response?.data?.message) {
      alert(`Erreur: ${error.response.data.message}`);
    } else {
      alert('Une erreur est survenue lors de la modification de la procédure.');
    }
  } finally {
    setSubmitting(false);
  }
};
const handleEdit = (procedure: Procedure) => {
    setEditingProcedure(procedure);
    
    const isCustomResponsible = !responsibleOptions.includes(procedure.responsible);
    
    // Parse due date and time from procedure
    let dueDate = null;
    let dueHour = 9;
    let dueMinute = 0;
    
    if (procedure.dueDate) {
      const date = new Date(procedure.dueDate);
      dueDate = startOfDay(date);
      dueHour = date.getHours();
      dueMinute = date.getMinutes();
    }
    
    setFormData({
      title: procedure.title,
      responsible: isCustomResponsible ? "Autre" : procedure.responsible,
      dueDate: dueDate,
      dueHour: dueHour,
      dueMinute: dueMinute,
      description: procedure.description,
      candidatId: procedure.candidatId,
      customResponsible: isCustomResponsible ? procedure.responsible : ""
    });
    
    setIsEditDialogOpen(true);
  };

  const handleDelete = async (procedureId: number) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette procédure ?')) {
      try {
        await apiService.delete(`/procedures/${procedureId}`);
        setProcedures(procedures.filter(proc => proc.id !== procedureId));
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
      }
    }
  };

  const handleCreateCancel = () => {
    setFormData(initialFormData);
    setIsCreateDialogOpen(false);
  };

  const handleEditCancel = () => {
    setFormData(initialFormData);
    setEditingProcedure(null);
    setIsEditDialogOpen(false);
  };

  const handleCreateClick = () => {
    const newFormData = { ...initialFormData };
    if (selectedCandidatId) {
      newFormData.candidatId = selectedCandidatId;
    }
    setFormData(newFormData);
    setIsCreateDialogOpen(true);
  };

  const formatProcedureDate = (dueDate: string) => {
const date = new Date(dueDate.replace('T', 'T').replace(/Z?$/, ''));    
    if (isToday(date)) {
      return (
        <div className="flex items-center gap-2 text-orange-600">
          <CalendarIcon className="h-3 w-3" />
          <span className="font-medium">Aujourd'hui</span>
          <span className="text-sm">({format(date, "HH:mm")})</span>
        </div>
      );
    }
    
    if (isTomorrow(date)) {
      return (
        <div className="flex items-center gap-2 text-yellow-600">
          <CalendarIcon className="h-3 w-3" />
          <span className="font-medium">Demain</span>
          <span className="text-sm">({format(date, "HH:mm")})</span>
        </div>
      );
    }
    
    if (isPast(date)) {
      return (
        <div className="flex items-center gap-2 text-red-600">
          <CalendarIcon className="h-3 w-3" />
          <span className="font-medium">En retard</span>
          <span className="text-sm">({format(date, "dd/MM HH:mm")})</span>
        </div>
      );
    }
    
    return (
      <div className="flex items-center gap-2 text-muted-foreground">
        <CalendarIcon className="h-3 w-3" />
        <span>{format(date, "dd MMMM yyyy 'à' HH:mm", { locale: fr })}</span>
      </div>
    );
  };

  const completedCount = procedures.filter(proc => proc.completed).length;

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Building className="h-5 w-5" />
            Procédures entreprise
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin" />
            <span className="ml-2">Chargement des procédures...</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  const titleText = selectedCandidatId 
    ? `Procédures pour ${procedures[0]?.candidatNom || 'ce candidat'}` 
    : "Procédures entreprise";

  const descriptionText = selectedCandidatId
    ? `Étapes d'intégration spécifiques (${completedCount}/${procedures.length} terminées)`
    : `Étapes standardisées d'intégration (${completedCount}/${procedures.length} terminées)`;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Building className="h-5 w-5" />
          {titleText}
        </CardTitle>
        <CardDescription>
          {descriptionText}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {procedures.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              {selectedCandidatId 
                ? "Aucune procédure créée pour ce candidat" 
                : "Aucune procédure trouvée"
              }
            </div>
          ) : (
            procedures.map((procedure) => (
              <div key={procedure.id} className="flex items-start space-x-3 p-3 border rounded-lg group hover:bg-accent/50 transition-colors">
                <Checkbox
                  checked={procedure.completed}
                  onCheckedChange={() => toggleProcedure(procedure.id)}
                  className="mt-1"
                />
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-2">
                    <span className={`font-medium ${procedure.completed ? 'line-through text-muted-foreground' : ''}`}>
                      {procedure.title}
                    </span>
                    <div className="flex items-center gap-1">
                      {procedure.completed && (
                        <CheckCircle className="h-4 w-4 text-green-600" />
                      )}
                      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(procedure)}
                          className="h-8 w-8 p-0 hover:bg-blue-100"
                        >
                          <Edit className="h-4 w-4 text-blue-600" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(procedure.id)}
                          className="h-8 w-8 p-0 hover:bg-red-100"
                        >
                          <Trash2 className="h-4 w-4 text-red-600" />
                        </Button>
                      </div>
                    </div>
                  </div>
                  <p className="text-sm text-muted-foreground mb-2">
                    {procedure.description}
                  </p>
                  <div className="flex items-center gap-4 text-sm text-muted-foreground">
                    {!selectedCandidatId && (
                      <div className="flex items-center gap-1">
                        <User className="h-3 w-3" />
                        <span>Candidat: {procedure.candidatNom}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-1">
                      <User className="h-3 w-3" />
                      <span>Responsable: {procedure.responsible}</span>
                    </div>
                    {procedure.dueDate && formatProcedureDate(procedure.dueDate)}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="flex justify-start pt-4 border-t">
          <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={handleCreateClick}>
                <Plus className="h-4 w-4 mr-2" />
                Ajouter procédure
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[500px] max-h-[90vh]">
              <DialogHeader>
                <DialogTitle>Ajouter une nouvelle procédure</DialogTitle>
                <DialogDescription>
                  {selectedCandidatId 
                    ? "Créez une nouvelle procédure pour ce candidat."
                    : "Créez une nouvelle procédure d'intégration pour l'entreprise."
                  }
                </DialogDescription>
              </DialogHeader>
              <ProcedureForm
                formData={formData}
                setFormData={setFormData}
                onSubmit={handleCreateSubmit}
                onCancel={handleCreateCancel}
                submitting={submitting}
                editingProcedure={null}
                candidats={candidats}
                selectedCandidatId={selectedCandidatId}
              />
            </DialogContent>
          </Dialog>
        </div>

        <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
          <DialogContent className="sm:max-w-[500px] max-h-[90vh]">
            <DialogHeader>
              <DialogTitle>Modifier la procédure</DialogTitle>
              <DialogDescription>
                Modifiez les détails de cette procédure d'intégration.
              </DialogDescription>
            </DialogHeader>
            <ProcedureForm
              formData={formData}
              setFormData={setFormData}
              onSubmit={handleEditSubmit}
              onCancel={handleEditCancel}
              submitting={submitting}
              editingProcedure={editingProcedure}
              candidats={candidats}
              selectedCandidatId={selectedCandidatId}
            />
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  );
}