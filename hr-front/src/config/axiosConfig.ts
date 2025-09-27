import axios from 'axios';
// import keycloak from './keycloak';
import { keycloak } from '@/KeycloakProvider';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL || "" ,
  timeout: 600000,
  headers: {
    // 'Content-Type': 'application/json',
  },
});

// Intercepteur de requête pour ajouter le token Keycloak
axiosInstance.interceptors.request.use(
  async (config) => {
    console.log('keycloak.authenticated:', keycloak.authenticated);
    
    if (keycloak.authenticated) {
      try {
        await keycloak.updateToken(30);
        config.headers.Authorization = `Bearer ${keycloak.token}`;
      } catch (error) {
        console.error('Erreur lors du rafraîchissement du token:', error);
        keycloak.login();
      }
    }
    if (config.data && !(config.data instanceof FormData) && !config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json';
    }
    return config;
  },
  (error) => Promise.reject(error)
);


axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        await keycloak.updateToken(0); 
        
        originalRequest.headers.Authorization = `Bearer ${keycloak.token}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Impossible de rafraîchir le token:', refreshError);
        
        keycloak.login();
        return Promise.reject(refreshError);
      }
    }
    
    if (error.response?.status === 403) {
      console.warn('Accès refusé - permissions insuffisantes');
    }
    
    return Promise.reject(error);
  }
);

export default axiosInstance;