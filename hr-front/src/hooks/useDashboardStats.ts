// hooks/useDashboardStats.ts
import { useState, useEffect } from 'react';
import apiService from '@/config/apiService';

interface DashboardStats {
  activeJobs: number;
  totalApplications: number;
  totalInterviews: number;
  activeTests: number;
  weeklyApplications: number;
  weeklyJobs: number;
  weeklyInterviews: number;
  weeklyTestsSent: number;
  jobsWeeklyChange: number;
  applicationsWeeklyChange: number;
  interviewsWeeklyChange: number;
  testsWeeklyChange: number;
  loading: boolean;
  error: string | null;
}

interface FormData {
  id: number;
  title: string;
  createdAt: string;
  published: boolean;
  location?: string;
}

interface CandidatData {
  id: number;
  nom: string;
  email: string;
  poste: string;
  processStatus?: string;
  createdAt?: string;
  dateCreation?: string;
}

interface EntretienData {
  id: number;
  statut: string;
  dateHeure: string;
  candidat: {
    nom: string;
    poste: string;
  };
  createdAt?: string;
}

interface TestData {
  id: number;
  name: string;
  createdAt: string;
  exercises?: any[];
}

interface WeeklyTestStats {
  thisWeekSent: number;
  lastWeekSent: number;
  percentageChange: number;
}

interface WeeklyCandidatStats {
  thisWeekCreated: number;
  lastWeekCreated: number;
  percentageChange: number;
}

export const useDashboardStats = () => {
  const [stats, setStats] = useState<DashboardStats>({
    activeJobs: 0,
    totalApplications: 0,
    totalInterviews: 0,
    activeTests: 0,
    weeklyApplications: 0,
    weeklyInterviews: 0,
    weeklyTestsSent: 0,
    weeklyJobs: 0,
    jobsWeeklyChange: 0,
    applicationsWeeklyChange: 0,
    interviewsWeeklyChange: 0,
    testsWeeklyChange: 0,
    loading: true,
    error: null
  });

  const calculateWeeklyData = (items: any[], dateField: string = 'createdAt') => {
    const now = new Date();
    const oneWeekAgo = new Date();
    const twoWeeksAgo = new Date();
    
    oneWeekAgo.setDate(now.getDate() - 7);
    twoWeeksAgo.setDate(now.getDate() - 14);

    const thisWeek = items.filter(item => {
      const itemDate = new Date(item[dateField]);
      return itemDate >= oneWeekAgo && itemDate <= now;
    }).length;

    const lastWeek = items.filter(item => {
      const itemDate = new Date(item[dateField]);
      return itemDate >= twoWeeksAgo && itemDate < oneWeekAgo;
    }).length;

    const change = lastWeek === 0 ? (thisWeek > 0 ? 100 : 0) : 
                  Math.round(((thisWeek - lastWeek) / lastWeek) * 100);

    return { thisWeek, change };
  };

  // NOUVELLE FONCTION pour calculer les entretiens planifiés cette semaine
  const calculateScheduledInterviewsThisWeek = (entretiens: EntretienData[]) => {
    const now = new Date();
    const startOfWeek = new Date(now);
    const endOfWeek = new Date(now);
    
    // Début de la semaine (lundi)
    const dayOfWeek = now.getDay();
    const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    startOfWeek.setDate(now.getDate() + daysToMonday);
    startOfWeek.setHours(0, 0, 0, 0);
    
    // Fin de la semaine (dimanche)
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    endOfWeek.setHours(23, 59, 59, 999);

    // Calculer pour cette semaine
    const thisWeek = entretiens.filter(entretien => {
      const entretienDate = new Date(entretien.dateHeure);
      return entretienDate >= startOfWeek && entretienDate <= endOfWeek &&
             ['prévu', 'en_cours', 'pause'].includes(entretien.statut);
    }).length;

    // Calculer pour la semaine dernière (pour le pourcentage de changement)
    const lastWeekStart = new Date(startOfWeek);
    const lastWeekEnd = new Date(endOfWeek);
    lastWeekStart.setDate(startOfWeek.getDate() - 7);
    lastWeekEnd.setDate(endOfWeek.getDate() - 7);

    const lastWeek = entretiens.filter(entretien => {
      const entretienDate = new Date(entretien.dateHeure);
      return entretienDate >= lastWeekStart && entretienDate <= lastWeekEnd &&
             ['prévu', 'en_cours', 'pause'].includes(entretien.statut);
    }).length;

    const change = lastWeek === 0 ? (thisWeek > 0 ? 100 : 0) : 
                  Math.round(((thisWeek - lastWeek) / lastWeek) * 100);

    return { thisWeek, change };
  };

  const fetchDashboardData = async () => {
    setStats(prev => ({ ...prev, loading: true, error: null }));

    try {
      const [
        publishedForms, 
        allCandidats, 
        allEntretiens, 
        allTests, 
        weeklyTestStats,
        weeklyCandidatStats
      ] = await Promise.all([
        apiService.get<FormData[]>('/forms/published'),
        apiService.get<CandidatData[]>('/candidats'),
        apiService.get<EntretienData[]>('/entretiens'),
        apiService.get<TestData[]>('/tests'),
        apiService.get<WeeklyTestStats>('/test-assignments/weekly-stats'),
        apiService.get<WeeklyCandidatStats>('/candidats/weekly-stats') 
      ]);

      // CHANGEMENT ICI : Utiliser la nouvelle fonction pour les entretiens planifiés
      const scheduledInterviewsWeekly = calculateScheduledInterviewsThisWeek(allEntretiens);

      // Calculate weekly stats for jobs (keep existing logic)  
      const jobsWeekly = calculateWeeklyData(publishedForms, 'createdAt');

      // Count interviews by status
      const scheduledInterviews = allEntretiens.filter(e => 
        ['en_cours', 'prévu', 'pause'].includes(e.statut)
      ).length;

      setStats({
        activeJobs: publishedForms.length,
        totalApplications: allCandidats.length,
        totalInterviews: scheduledInterviews,
        activeTests: allTests.length,
        weeklyApplications: weeklyCandidatStats.thisWeekCreated, 
        weeklyInterviews: scheduledInterviewsWeekly.thisWeek, // CHANGEMENT ICI
        weeklyTestsSent: weeklyTestStats.thisWeekSent,
        weeklyJobs: jobsWeekly.thisWeek,
        jobsWeeklyChange: jobsWeekly.change,
        applicationsWeeklyChange: weeklyCandidatStats.percentageChange,
        interviewsWeeklyChange: scheduledInterviewsWeekly.change, // CHANGEMENT ICI
        testsWeeklyChange: weeklyTestStats.percentageChange,
        loading: false,
        error: null
      });

    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setStats(prev => ({
        ...prev,
        loading: false,
        error: error instanceof Error ? error.message : 'Erreur lors du chargement des données'
      }));
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const refresh = () => {
    fetchDashboardData();
  };

  return { ...stats, refresh };
};