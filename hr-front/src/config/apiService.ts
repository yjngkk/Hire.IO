import axiosInstance from './axiosConfig';

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
  lastUpdated: string;
}

interface OfferStats {
  publishedToday: number;
  publishedThisWeek: number;
  publishedThisMonth: number;
  totalPublished: number;
  createdToday: number;
  createdThisWeek: number;
  createdThisMonth: number;
  totalCreated: number;
  lastUpdated: string;
}

class ApiService {
  // === EXISTING ACTIVITY METHODS ===
  
  async getRecentActivities(limit: number = 5): Promise<RecentActivity[]> {
    try {
      console.log(`🔍 API: Récupération de ${limit} activités récentes...`);
      const response = await axiosInstance.get<RecentActivity[]>('/api/activities/recent', {
        params: { limit }
      });
      console.log(`✅ API: ${response.data.length} activités récupérées`);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des activités récentes');
      this.handleActivityError(error, '/api/activities/recent', 'GET');
      throw error;
    }
  }

  async getActivitiesFromLastDays(days: number = 7, limit: number = 10): Promise<RecentActivity[]> {
    try {
      console.log(`🔍 API: Récupération des activités des ${days} derniers jours (limite: ${limit})...`);
      const response = await axiosInstance.get<RecentActivity[]>('/api/activities/last-days', {
        params: { days, limit }
      });
      console.log(`✅ API: ${response.data.length} activités récupérées`);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des activités par période');
      this.handleActivityError(error, '/api/activities/last-days', 'GET');
      throw error;
    }
  }

  async getActivityStats(): Promise<ActivityStats> {
    try {
      console.log('📊 API: Récupération des statistiques d\'activité...');
      const response = await axiosInstance.get<ActivityStats>('/api/activities/stats');
      console.log('✅ API: Statistiques récupérées:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des statistiques');
      this.handleActivityError(error, '/api/activities/stats', 'GET');
      throw error;
    }
  }

  async checkActivityHealthStatus(): Promise<{ status: string; message: string; timestamp: string }> {
    try {
      const response = await axiosInstance.get('/api/activities/health');
      return response.data;
    } catch (error) {
      this.handleActivityError(error, '/api/activities/health', 'GET');
      throw error;
    }
  }

  // === NEW OFFER METHODS ===
  
  async getPublishedOffers(limit: number = 5): Promise<PublishedOffer[]> {
    try {
      console.log(`🔍 API: Récupération de ${limit} offres publiées...`);
      const response = await axiosInstance.get<PublishedOffer[]>('/api/forms/published', {
        params: { limit }
      });
      console.log(`✅ API: ${response.data.length} offres publiées récupérées`);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des offres publiées');
      this.handleOfferError(error, '/api/forms/published', 'GET');
      throw error;
    }
  }

  async getPublishedOffersFromLastDays(days: number = 7, limit: number = 10): Promise<PublishedOffer[]> {
    try {
      console.log(`🔍 API: Récupération des offres publiées des ${days} derniers jours (limite: ${limit})...`);
      const response = await axiosInstance.get<PublishedOffer[]>('/api/forms/published/last-days', {
        params: { days, limit }
      });
      console.log(`✅ API: ${response.data.length} offres publiées récupérées`);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des offres publiées par période');
      this.handleOfferError(error, '/api/forms/published/last-days', 'GET');
      throw error;
    }
  }

  

  async getAllForms(): Promise<PublishedOffer[]> {
    try {
      console.log('🔍 API: Récupération de toutes les offres...');
      const response = await axiosInstance.get<PublishedOffer[]>('/api/forms');
      console.log(`✅ API: ${response.data.length} offres récupérées`);
      return response.data;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération de toutes les offres');
      this.handleOfferError(error, '/api/forms', 'GET');
      throw error;
    }
  }

  async getFormById(id: number): Promise<PublishedOffer> {
    try {
      console.log(`🔍 API: Récupération de l'offre ${id}...`);
      const response = await axiosInstance.get<PublishedOffer>(`/api/forms/${id}`);
      console.log('✅ API: Offre récupérée:', response.data);
      return response.data;
    } catch (error) {
      console.error(`❌ API: Erreur lors de la récupération de l'offre ${id}`);
      this.handleOfferError(error, `/api/forms/${id}`, 'GET');
      throw error;
    }
  }

  // === COMBINED ACTIVITIES AND OFFERS ===
  
  async getCombinedRecentActivities(limit: number = 10, includeOffers: boolean = true): Promise<RecentActivity[]> {
    try {
      console.log(`🔍 API: Récupération des activités combinées (limite: ${limit}, inclure offres: ${includeOffers})...`);
      
      const activitiesPromise = this.getRecentActivities(Math.ceil(limit * 0.7)); // 70% activities
      const offersPromise = includeOffers 
        ? this.getPublishedOffers(Math.ceil(limit * 0.3)) // 30% offers
        : Promise.resolve([]);
      
      const [activities, offers] = await Promise.all([activitiesPromise, offersPromise]);
      
      // Convert offers to activities (this logic should match the frontend conversion)
      const offerActivities: RecentActivity[] = offers.map(offer => ({
        id: offer.id,
        type: 'offer_published',
        message: `Nouvelle offre d'emploi publiée: ${offer.title}`,
        candidate: offer.title,
        email: '',
        telephone: '',
        time: this.getTimeAgo(new Date(offer.publishedAt || offer.createdAt)),
        status: 'publié',
        timestamp: offer.publishedAt || offer.createdAt,
        poste: offer.title,
        offreId: offer.id,
        offreTitre: offer.title
      }));
      
      // Combine and sort by timestamp
      const combined = [...activities, ...offerActivities]
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        .slice(0, limit);
      
      console.log(`✅ API: ${combined.length} activités combinées récupérées`);
      return combined;
    } catch (error) {
      console.error('❌ API: Erreur lors de la récupération des activités combinées');
      throw error;
    }
  }

  // === GENERIC HTTP METHODS ===

  async get<T = any>(endpoint: string, params: Record<string, any> = {}): Promise<T> {
    try {
      const response = await axiosInstance.get<T>(endpoint, { params });
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'GET');
      throw error;
    }
  }

  async getSilent<T = any>(endpoint: string, params: Record<string, any> = {}): Promise<T> {
    try {
      const response = await axiosInstance.get<T>(endpoint, { params });
      return response.data;
    } catch (error) {
      if (error.response?.status !== 404) {
        this.handleError(error, endpoint, 'GET');
      }
      throw error;
    }
  }

  async post<T = any>(endpoint: string, data: any = {}): Promise<T> {
    try {
      const response = await axiosInstance.post<T>(endpoint, data);
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'POST');
      throw error;
    }
  }

  async put<T = any>(endpoint: string, data: any = {}): Promise<T> {
    try {
      const response = await axiosInstance.put<T>(endpoint, data);
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'PUT');
      throw error;
    }
  }

  async patch<T = any>(endpoint: string, data: any = {}): Promise<T> {
    try {
      const response = await axiosInstance.patch<T>(endpoint, data);
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'PATCH');
      throw error;
    }
  }

  async delete<T = any>(endpoint: string, params: Record<string, any> = {}): Promise<T> {
    try {
      const response = await axiosInstance.delete<T>(endpoint, { params });
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'DELETE');
      throw error;
    }
  }

  async uploadFile<T = any>(
    endpoint: string,
    file: File | Blob,
    additionalData: Record<string, any> = {}
  ): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);

    Object.keys(additionalData).forEach((key) => {
      formData.append(key, additionalData[key]);
    });

    try {
      const response = await axiosInstance.post<T>(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(error, endpoint, 'UPLOAD');
      throw error;
    }
  }

  async exists(endpoint: string, params: Record<string, any> = {}): Promise<boolean> {
    try {
      await this.getSilent(endpoint, params);
      return true;
    } catch (error) {
      if (error.response?.status === 404) {
        return false;
      }
      throw error;
    }
  }

  // === UTILITY METHODS ===
  
  private getTimeAgo(date: Date): string {
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
  }

  // === ERROR HANDLING ===
  
  private handleActivityError(error: any, endpoint: string, method: string) {
    console.error(`❌ Erreur ${method} sur ${endpoint}`);

    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      
      console.error('🔍 Détails de l\'erreur:', {
        status,
        statusText: error.response.statusText,
        data,
        headers: error.response.headers,
      });

      if (endpoint.includes('/api/activities')) {
        if (status === 404) {
          console.error('💡 Solution: Vérifiez que le contrôleur ActivityController est bien déployé');
        } else if (status === 500) {
          console.error('💡 Solution: Vérifiez les logs Spring Boot et la connexion à la base de données');
        } else if (status === 403) {
          console.error('💡 Solution: Problème d\'authentification ou de CORS');
        }
      }
    } else if (error.request) {
      console.error('🔌 Aucune réponse reçue du serveur:', error.request);
      console.error('💡 Solution: Vérifiez que le serveur Spring Boot est démarré sur le bon port');
    } else {
      console.error('⚙️ Erreur lors de la configuration de la requête:', error.message);
    }
  }

  private handleOfferError(error: any, endpoint: string, method: string) {
    console.error(`❌ Erreur ${method} sur ${endpoint}`);

    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      
      console.error('🔍 Détails de l\'erreur:', {
        status,
        statusText: error.response.statusText,
        data,
        headers: error.response.headers,
      });

      if (endpoint.includes('/api/forms')) {
        if (status === 404) {
          console.error('💡 Solution: Vérifiez que le contrôleur FormController est bien déployé');
        } else if (status === 500) {
          console.error('💡 Solution: Vérifiez les logs Spring Boot et la connexion à la base de données');
        } else if (status === 403) {
          console.error('💡 Solution: Problème d\'authentification ou de CORS');
        }
      }
    } else if (error.request) {
      console.error('🔌 Aucune réponse reçue du serveur:', error.request);
      console.error('💡 Solution: Vérifiez que le serveur Spring Boot est démarré sur le bon port');
    } else {
      console.error('⚙️ Erreur lors de la configuration de la requête:', error.message);
    }
  }

  private handleError(error: any, endpoint: string, method: string) {
    console.error(`❌ Erreur ${method} sur ${endpoint}`);

    if (error.response) {
      console.error('Réponse erreur API:', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers,
      });
    } else if (error.request) {
      console.error('Aucune réponse reçue:', error.request);
    } else {
      console.error('Erreur lors de la configuration de la requête:', error.message);
    }
  }
}

// Export d'une instance unique
const apiService = new ApiService();
export default apiService;