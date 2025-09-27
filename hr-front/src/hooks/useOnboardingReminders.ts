// hooks/useOnboardingReminders.ts
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";
import apiService from "@/config/apiService";

interface CandidateFile {
  id: number;
  name: string;
  status: "completed" | "pending" | "missing";
  isRequired: boolean;
}

interface ReminderResponse {
  candidatName: string;
  fileName: string;
  filesSent: number;
}

export function useOnboardingReminders() {
  const { toast } = useToast();
  const [reminderLoading, setReminderLoading] = useState<number | null>(null);

  const sendGlobalReminder = async (
    candidatId: number, 
    employeeName: string,
    onSuccess?: () => Promise<void>
  ) => {
    try {
      setReminderLoading(candidatId);
      const response = await apiService.post<ReminderResponse>(
        `/candidate-files/candidat/${candidatId}/send-reminder`
      );
      
      toast({
        title: "Rappel envoyé",
        description: `Rappel envoyé à ${employeeName} pour ${response.filesSent} document(s)`,
      });

      // Call optional success callback to refresh data
      if (onSuccess) {
        await onSuccess();
      }

    } catch (error) {
      console.error('Erreur lors de l\'envoi du rappel:', error);
      toast({
        title: "Erreur",
        description: "Impossible d'envoyer le rappel",
        variant: "destructive"
      });
    } finally {
      setReminderLoading(null);
    }
  };

  const sendIndividualReminder = async (fileId: number, fileName: string) => {
    try {
      const response = await apiService.post<ReminderResponse>(
        `/candidate-files/${fileId}/send-reminder`
      );
      
      toast({
        title: "Rappel envoyé",
        description: `Rappel envoyé à ${response.candidatName} pour "${response.fileName}"`,
      });
      
    } catch (error) {
      console.error('Erreur lors du rappel:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors de l'envoi du rappel",
        variant: "destructive"
      });
    }
  };

  const requestDocument = async (fileId: number, fileName: string, onSuccess?: () => Promise<void>) => {
    try {
      const response = await apiService.post<ReminderResponse>(
        `/candidate-files/${fileId}/request-with-reminder`
      );
      
      toast({
        title: "Demande envoyée avec succès",
        description: `Email de rappel envoyé à ${response.candidatName} pour "${response.fileName}"`,
      });

      // Call optional success callback to refresh data
      if (onSuccess) {
        await onSuccess();
      }

    } catch (error) {
      console.error('Erreur lors de la demande:', error);
      toast({
        title: "Erreur",
        description: "Erreur lors de l'envoi de la demande",
        variant: "destructive"
      });
    }
  };

  const hasFilesToRemind = (files: CandidateFile[]) => {
    return files.some(file => 
      file.isRequired && (file.status === "missing" || file.status === "pending")
    );
  };

  return {
    sendGlobalReminder,
    sendIndividualReminder,
    requestDocument,
    hasFilesToRemind,
    reminderLoading
  };
}