import { useState, useEffect } from 'react';
import apiService from '@/config/apiService';

interface RecentActivity {
  id: number;
  type: string;
  message: string;
  candidate: string;
  email: string;
  telephone: string;
  time: string;
  status: string;
  timestamp: string;
  poste: string;
  offreId?: number;
  offreTitre?: string;
}

interface PublishedOffer {
  id: number;
  title: string;
  location: string;
  contractType: string;
  level: string;
  createdAt: string;
  publishedAt: string;
  published: boolean;
}

interface ActivityStats {
  candidaturesToday: number;
  candidaturesThisWeek: number;
  candidaturesThisMonth: number;
  totalCandidatures: number;
  offersPublishedToday?: number;
  offersPublishedThisWeek?: number;
  offersPublishedThisMonth?: number;
  lastUpdated: string;
}

// Helper function to convert published offers to activities
const convertOfferToActivity = (offer: PublishedOffer): RecentActivity => {
  const publishedDate = new Date(offer.publishedAt || offer.createdAt);
  const timeAgo = getTimeAgo(publishedDate);
  
  return {
    id: offer.id,
    type: offer.published ? 'offer_published' : 'offer_created',
    message: offer.published 
      ? `Nouvelle offre d'emploi publiée: ${offer.title}`
      : `Nouvelle offre d'emploi créée: ${offer.title}`,
    candidate: offer.title,
    email: '',
    telephone: '',
    time: timeAgo,
    status: offer.published ? 'publié' : 'créé',
    timestamp: offer.publishedAt || offer.createdAt,
    poste: offer.title,
    offreId: offer.id,
    offreTitre: offer.title
  };
};

// Helper function to calculate time ago
const getTimeAgo = (date: Date): string => {
  const now = new Date();
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
  
  if (diffInMinutes < 1) return 'À l\'instant';
  if (diffInMinutes < 60) return `Il y a ${diffInMinutes} min`;
  
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `Il y a ${diffInHours}h`;
  
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) return `Il y a ${diffInDays}j`;
  
  const diffInWeeks = Math.floor(diffInDays / 7);
  if (diffInWeeks < 4) return `Il y a ${diffInWeeks} semaine${diffInWeeks > 1 ? 's' : ''}`;
  
  const diffInMonths = Math.floor(diffInDays / 30);
  return `Il y a ${diffInMonths} mois`;
};

export function useRecentActivities(limit: number = 5, includeOffers: boolean = true) {
  const [activities, setActivities] = useState<RecentActivity[]>([]);
  const [stats, setStats] = useState<ActivityStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchRecentActivities = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log(`🔍 Récupération des ${limit} activités récentes depuis l'API...`);
      
      // Fetch regular activities
      const activitiesPromise = apiService.get('/activities/recent', { limit });
      
      // Fetch recent published offers if includeOffers is true
      const offersPromise = includeOffers 
        ? apiService.get('/forms/published', { limit: Math.ceil(limit / 2) })
        : Promise.resolve([]);
      
      const [activitiesData, offersData] = await Promise.all([activitiesPromise, offersPromise]);
      
      console.log(`✅ ${activitiesData.length} activités récupérées`);
      console.log(`✅ ${offersData.length} offres récupérées`);
      
      // Validate activities data format
      if (!Array.isArray(activitiesData)) {
        throw new Error('Format de données invalide reçu du serveur pour les activités');
      }

      // Validate offers data format
      if (includeOffers && !Array.isArray(offersData)) {
        throw new Error('Format de données invalide reçu du serveur pour les offres');
      }

      // Filter valid activities
      const validActivities = activitiesData.filter(activity => 
        activity.id && activity.candidate && activity.message && activity.timestamp
      );

      // Convert offers to activities and filter valid ones
      let offerActivities: RecentActivity[] = [];
      if (includeOffers && Array.isArray(offersData)) {
        offerActivities = offersData
          .filter(offer => offer.id && offer.title && (offer.publishedAt || offer.createdAt))
          .map(convertOfferToActivity);
      }

      // Combine and sort all activities by timestamp
      const allActivities = [...validActivities, ...offerActivities];
      const sortedActivities = allActivities
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        .slice(0, limit);

      setActivities(sortedActivities);
      console.log(`✅ ${sortedActivities.length} activités combinées affichées`);
      
    } catch (err: any) {
      console.error('❌ Erreur lors de la récupération des activités:', err);
      
      let errorMessage = 'Erreur inconnue';
      
      if (err.response) {
        const status = err.response.status;
        if (status === 404) {
          errorMessage = `Endpoint non trouvé. Vérifiez que les endpoints existent sur le serveur.`;
        } else if (status === 500) {
          errorMessage = `Erreur serveur interne. Vérifiez les logs du backend.`;
        } else if (status === 403) {
          errorMessage = `Accès refusé. Problème d'authentification.`;
        } else {
          errorMessage = `Erreur HTTP ${status}: ${err.response.statusText}`;
        }
      } else if (err.request) {
        errorMessage = `Impossible de contacter le serveur. Vérifiez que le backend est démarré.`;
      } else {
        errorMessage = err.message || 'Erreur de configuration de la requête';
      }
      
      setError(errorMessage);
      setActivities([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchActivityStats = async () => {
  try {
    console.log('📊 Récupération des statistiques d\'activité...');
    
    // Fetch regular stats
    const statsPromise = apiService.get('/activities/stats');
    
    // Fetch published forms to calculate offer stats
    const publishedFormsPromise = includeOffers 
      ? apiService.get('/forms/published')
      : Promise.resolve([]);
    
    const [statsData, publishedForms] = await Promise.all([statsPromise, publishedFormsPromise]);
    
    // Calculate offer stats from published forms data
    let offerStats = {
      offersPublishedToday: 0,
      offersPublishedThisWeek: 0,
      offersPublishedThisMonth: 0,
    };
    
    if (includeOffers && Array.isArray(publishedForms)) {
      const now = new Date();
      const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const weekStart = new Date(todayStart.getTime() - (7 * 24 * 60 * 60 * 1000));
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
      
      offerStats = {
        offersPublishedToday: publishedForms.filter(form => {
          const publishDate = new Date(form.publishedAt || form.createdAt);
          return publishDate >= todayStart;
        }).length,
        
        offersPublishedThisWeek: publishedForms.filter(form => {
          const publishDate = new Date(form.publishedAt || form.createdAt);
          return publishDate >= weekStart;
        }).length,
        
        offersPublishedThisMonth: publishedForms.filter(form => {
          const publishDate = new Date(form.publishedAt || form.createdAt);
          return publishDate >= monthStart;
        }).length,
      };
    }
    
    // Combine stats
    const combinedStats: ActivityStats = {
      ...statsData,
      ...offerStats
    };
    
    setStats(combinedStats);
    console.log('✅ Statistiques récupérées:', combinedStats);
  } catch (err) {
    console.error('❌ Erreur lors de la récupération des statistiques:', err);
    // Don't set error for stats, it's optional
  }
};
  const fetchActivitiesFromLastDays = async (days: number, limitParam: number = 10) => {
    try {
      setLoading(true);
      setError(null);
      
      console.log(`🔍 Récupération des activités des ${days} derniers jours...`);
      
      // Fetch activities from last days
      const activitiesPromise = apiService.get('/activities/last-days', { 
        days, 
        limit: limitParam 
      });
      
      // Fetch offers from last days if includeOffers is true
      const offersPromise = includeOffers 
        ? apiService.get('/forms/published/last-days', { 
            days, 
            limit: Math.ceil(limitParam / 2) 
          }) // You'll need to create this endpoint
        : Promise.resolve([]);
      
      const [activitiesData, offersData] = await Promise.all([activitiesPromise, offersPromise]);
      
      // Process and combine data similar to fetchRecentActivities
      const validActivities = activitiesData.filter(activity => 
        activity.id && activity.candidate && activity.message && activity.timestamp
      );

      let offerActivities: RecentActivity[] = [];
      if (includeOffers && Array.isArray(offersData)) {
        offerActivities = offersData
          .filter(offer => offer.id && offer.title && (offer.publishedAt || offer.createdAt))
          .map(convertOfferToActivity);
      }

      const allActivities = [...validActivities, ...offerActivities];
      const sortedActivities = allActivities
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        .slice(0, limitParam);

      setActivities(sortedActivities);
      console.log(`✅ ${sortedActivities.length} activités des ${days} derniers jours récupérées`);
      
    } catch (err: any) {
      console.error('❌ Erreur:', err);
      setError(err.message || 'Erreur inconnue');
      setActivities([]);
    } finally {
      setLoading(false);
    }
  };

  // Function to refresh only offer-related activities
  const refreshOffers = async () => {
    if (!includeOffers) return;
    
    try {
      console.log('🔄 Actualisation des offres publiées...');
      const offersData = await apiService.get('/forms/published', { limit: Math.ceil(limit / 2) });
      
      if (Array.isArray(offersData)) {
        const offerActivities = offersData
          .filter(offer => offer.id && offer.title && (offer.publishedAt || offer.createdAt))
          .map(convertOfferToActivity);
        
        // Update activities by replacing offer activities with new ones
        setActivities(currentActivities => {
          const nonOfferActivities = currentActivities.filter(
            activity => !['offer_published', 'offer_created', 'published_offer'].includes(activity.type)
          );
          
          const allActivities = [...nonOfferActivities, ...offerActivities];
          return allActivities
            .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
            .slice(0, limit);
        });
        
        console.log('✅ Offres actualisées');
      }
    } catch (err) {
      console.error('❌ Erreur lors de l\'actualisation des offres:', err);
    }
  };

  useEffect(() => {
    // Load initial data
    const loadData = async () => {
      await Promise.all([
        fetchRecentActivities(),
        fetchActivityStats()
      ]);
    };

    loadData();
    
    // Refresh every 2 minutes for real-time data
    const interval = setInterval(() => {
      fetchRecentActivities();
      fetchActivityStats();
    }, 2 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, [limit, includeOffers]);

  return {
    activities,
    stats,
    loading,
    error,
    refresh: fetchRecentActivities,
    refreshStats: fetchActivityStats,
    refreshOffers,
    fetchFromLastDays: fetchActivitiesFromLastDays,
    isEmpty: activities.length === 0 && !loading && !error
  };
}