import { useState, useEffect } from 'react';
import apiService from '@/config/apiService';

export interface UpcomingEvent {
  procedureId?: number;
  entretienId?: number; // Ajout pour les entretiens
  title: string;
  candidatName: string;
  candidatPoste: string;
  dueDate: string;
  deadline?: string;
  completed: boolean;
  responsible: string;
  formattedDueDate: string;
  relativeTimeDescription: string;
  type: 'procedure' | 'entretien'; // Ajout pour distinguer le type d'événement
  status?: string; // Ajout pour le statut de l'entretien
  meetLink?: string; // Ajout pour le lien Meet
}

interface EntretienData {
  id: number;
  statut: string;
  dateHeure: string;
  interviewer: string;
  meetLink?: string;
  candidat: {
    nom: string;
    poste: string;
  };
}

export function useUpcomingEvents(daysAhead: number = 7, limit?: number) {
  const [events, setEvents] = useState<UpcomingEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fonction pour formater la date relative
  const getRelativeTimeDescription = (date: Date): string => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    
    const eventDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    
    if (eventDate < today) {
      return 'En retard';
    } else if (eventDate.getTime() === today.getTime()) {
      return 'Aujourd\'hui';
    } else if (eventDate.getTime() === tomorrow.getTime()) {
      return 'Demain';
    } else {
      const diffTime = eventDate.getTime() - today.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return `Dans ${diffDays} jours`;
    }
  };

  // Fonction pour formater la date
  const formatDate = (date: Date): string => {
    return date.toLocaleDateString('fr-FR', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Fonction pour convertir les entretiens en événements
  const convertInterviewsToEvents = (interviews: EntretienData[]): UpcomingEvent[] => {
    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(now.getDate() + daysAhead);

    return interviews
      .filter(interview => {
        const interviewDate = new Date(interview.dateHeure);
        return interviewDate >= now && interviewDate <= futureDate &&
               ['prévu', 'en_cours', 'pause'].includes(interview.statut);
      })
      .map(interview => {
        const interviewDate = new Date(interview.dateHeure);
        return {
          entretienId: interview.id,
          title: `Entretien - ${interview.candidat.poste}`,
          candidatName: interview.candidat.nom,
          candidatPoste: interview.candidat.poste,
          dueDate: interview.dateHeure,
          completed: false,
          responsible: interview.interviewer || 'Nexotek',
          formattedDueDate: formatDate(interviewDate),
          relativeTimeDescription: getRelativeTimeDescription(interviewDate),
          type: 'entretien' as const,
          status: interview.statut,
          meetLink: interview.meetLink
        };
      });
  };

  const fetchUpcomingEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log(`Fetching upcoming events: daysAhead=${daysAhead}, limit=${limit}`);
      
      // Récupérer les événements de procédures et les entretiens en parallèle
      const [procedureEvents, interviews] = await Promise.all([
        // Récupérer les événements de procédures
        apiService.get<UpcomingEvent[]>(`/procedures/upcoming-events/today`).catch(() => 
          apiService.get<UpcomingEvent[]>('/procedures/upcoming-events').catch(() => [])
        ),
        // Récupérer les entretiens
        apiService.get<EntretienData[]>('/entretiens').catch(() => [])
      ]);

      // Marquer les événements de procédures comme tel
      const formattedProcedureEvents = procedureEvents.map(event => ({
        ...event,
        type: 'procedure' as const
      }));

      // Convertir les entretiens en événements
      const interviewEvents = convertInterviewsToEvents(interviews);

      // Combiner et trier par date
      const allEvents = [...formattedProcedureEvents, ...interviewEvents];
      allEvents.sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());

      // Appliquer la limite si spécifiée
      const limitedEvents = limit ? allEvents.slice(0, limit) : allEvents;
      
      console.log(`Successfully fetched ${limitedEvents.length} upcoming events (${formattedProcedureEvents.length} procedures + ${interviewEvents.length} interviews)`);
      setEvents(limitedEvents);
      
    } catch (err) {
      console.error('Error loading upcoming events:', err);
      
      let errorMessage = 'Failed to load upcoming events';
      
      if (err instanceof Error) {
        if (err.message.includes('404')) {
          errorMessage = 'Upcoming events endpoint not found. Check if Spring Boot server is running.';
        } else if (err.message.includes('500')) {
          errorMessage = 'Server error. Check Spring Boot logs for details.';
        } else if (err.message.includes('connect') || err.message.includes('NetworkError')) {
          errorMessage = 'Cannot connect to server. Is Spring Boot running?';
        } else {
          errorMessage = err.message;
        }
      }
      
      setError(errorMessage);
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUpcomingEvents();
  }, [daysAhead, limit]);

  const refresh = () => {
    fetchUpcomingEvents();
  };

  return {
    events,
    loading,
    error,
    refresh
  };
}