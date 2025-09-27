import apiService from "@/config/apiService";
import { FormRequest, FormResponse } from "@/components/offer/form";

class FormService {
  
  /**
   * Create a new form
   */
  async createForm(formData: FormRequest): Promise<FormResponse> {
    try {
      console.log('🚀 Creating form:', formData.title);
      
      const requestData = {
        title: formData.title,
        missions: formData.missions,
        location: formData.location,
        contractType: formData.contractType,
        level: formData.level,
        skills: formData.skills,
        tone: formData.tone,
        categorie: formData.categorie
      };

      const result = await apiService.post<FormResponse>('/forms', requestData);
      
      console.log('✅ Form created successfully:', result.id);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to create form:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to create form');
      }
    }
  }

  /**
   * Get all forms
   */
  async getAllForms(): Promise<FormResponse[]> {
    try {
      console.log('📋 Fetching all forms...');
      
      const result = await apiService.get<FormResponse[]>('/forms');
      
      console.log('✅ Forms fetched:', result.length);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to fetch forms:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch forms');
      }
    }
  }

  /**
   * Get form by ID
   */
  async getFormById(id: number): Promise<FormResponse> {
    try {
      console.log('🔍 Fetching form by ID:', id);
      
      const result = await apiService.get<FormResponse>(`/forms/${id}`);
      
      console.log('✅ Form fetched:', result.title);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to fetch form:', error);
      
      if (error.response?.status === 404) {
        throw new Error(`Form with ID ${id} not found`);
      } else if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch form');
      }
    }
  }

  /**
   * Update an existing form
   */
  async updateForm(id: number, formData: FormRequest): Promise<FormResponse> {
    try {
      console.log('🔄 Updating form:', id, formData.title);

      const requestData = {
        title: formData.title,
        missions: formData.missions,
        location: formData.location,
        contractType: formData.contractType,
        level: formData.level,
        skills: formData.skills,
        tone: formData.tone,
      };

      const result = await apiService.put<FormResponse>(`/forms/${id}`, requestData);
      
      console.log('✅ Form updated successfully:', result.id);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to update form:', error);
      
      let errorMessage = 'Failed to update form';
      
      if (error.response?.status === 404) {
        errorMessage = `Form with ID ${id} not found`;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.status) {
        errorMessage = `HTTP ${error.response.status}: ${error.response.statusText || 'Unknown error'}`;
      } else if (error.message) {
        errorMessage = error.message;
      }

      throw new Error(`Update failed: ${errorMessage}`);
    }
  }

  /**
   * Delete a form
   */
  async deleteForm(id: number): Promise<void> {
    try {
      console.log('🗑️ Deleting form:', id);
      
      await apiService.delete(`/forms/${id}`);
      
      console.log('✅ Form deleted successfully:', id);
    } catch (error: any) {
      console.error('❌ Failed to delete form:', error);
      
      if (error.response?.status === 404) {
        throw new Error(`Form with ID ${id} not found`);
      } else if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to delete form');
      }
    }
  }

  /**
   * Search forms by title and/or location
   */
  async searchForms(title?: string, location?: string): Promise<FormResponse[]> {
    try {
      console.log('🔍 Searching forms:', { title, location });
      
      const params: Record<string, string> = {};
      
      if (title && title.trim()) {
        params.title = title.trim();
      }
      if (location && location.trim()) {
        params.location = location.trim();
      }

      const result = await apiService.get<FormResponse[]>('/forms/search', 
        Object.keys(params).length > 0 ? params : {}
      );
      
      console.log('✅ Form search completed:', result.length, 'results');
      return result;
    } catch (error: any) {
      console.error('❌ Failed to search forms:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to search forms');
      }
    }
  }

  /**
   * Get forms by contract type
   */
  async getFormsByContractType(contractType: string): Promise<FormResponse[]> {
    try {
      console.log('🔍 Fetching forms by contract type:', contractType);
      
      const result = await apiService.get<FormResponse[]>('/forms', { contractType });
      
      console.log('✅ Forms fetched by contract type:', result.length);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to fetch forms by contract type:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch forms by contract type');
      }
    }
  }

  /**
   * Get forms by location
   */
  async getFormsByLocation(location: string): Promise<FormResponse[]> {
    try {
      console.log('🔍 Fetching forms by location:', location);
      
      const result = await apiService.get<FormResponse[]>('/forms', { location });
      
      console.log('✅ Forms fetched by location:', result.length);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to fetch forms by location:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch forms by location');
      }
    }
  }

  /**
   * Get forms with pagination
   */
  async getFormsPaginated(page: number = 0, size: number = 10): Promise<{
    content: FormResponse[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    try {
      console.log('📄 Fetching forms with pagination:', { page, size });
      
      const result = await apiService.get('/forms/paginated', { page, size });
      
      console.log('✅ Paginated forms fetched:', result.content?.length || 0, 'of', result.totalElements);
      return result;
    } catch (error: any) {
      console.error('❌ Failed to fetch paginated forms:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch paginated forms');
      }
    }
  }

  /**
   * Advanced search with multiple filters
   */
  async advancedSearch(filters: {
    title?: string;
    location?: string;
    contractType?: string;
    level?: string;
    skills?: string[];
  }): Promise<FormResponse[]> {
    try {
      console.log('🔍 Advanced form search:', filters);
      
      const params: Record<string, any> = {};
      
      if (filters.title && filters.title.trim()) {
        params.title = filters.title.trim();
      }
      if (filters.location && filters.location.trim()) {
        params.location = filters.location.trim();
      }
      if (filters.contractType && filters.contractType.trim()) {
        params.contractType = filters.contractType.trim();
      }
      if (filters.level && filters.level.trim()) {
        params.level = filters.level.trim();
      }
      if (filters.skills && filters.skills.length > 0) {
        params.skills = filters.skills.join(',');
      }

      const result = await apiService.get<FormResponse[]>('/forms/advanced-search', 
        Object.keys(params).length > 0 ? params : {}
      );
      
      console.log('✅ Advanced search completed:', result.length, 'results');
      return result;
    } catch (error: any) {
      console.error('❌ Failed to perform advanced search:', error);
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to perform advanced search');
      }
    }
  }
}

// Export singleton instance
export const formService = new FormService();
export default formService;