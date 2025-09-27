// testApi.ts
import apiService from "@/config/apiService";
import { Exercise, Question } from "./exerciseApi";

// Define DTOs to match backend
export interface TestRequestDTO {
  name: string;
  description: string;
  totalDuration: number;
  difficulty: string;
  totalPoints: number;
  status: string;
  categorie:string;
  exerciseIds: number[]; // Changed from exercises to exerciseIds
}

export interface TestResponseDTO {
  id: number;
  name: string;
  description: string;
  totalDuration: number;
  difficulty: string;
  totalPoints: number;
  status: string;
  categorie:string;
  createdAt: string;
  exercises: Exercise[]; // Full exercise objects in response
}

// Keep the original Test interface for frontend use
export interface Test {
  id?: number;
  name: string;
  description: string;
  totalDuration: number;
  questions: Question[];
  difficulty: string;
  totalPoints: number;
  createdAt?: string;
  status: string;
  categorie:string;
  exercises: Exercise[];
}

export const testApi = {
  // Get all tests
  async getAllTests(): Promise<TestResponseDTO[]> {
    try {
      
      const result = await apiService.get<TestResponseDTO[]>('/tests');
      
      return result;
    } catch (error: any) {
      
      let errorMessage = 'Failed to fetch tests';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  // Create a new test
  async createTest(testData: Omit<Test, 'id' | 'createdAt'>): Promise<TestResponseDTO> {
    try {

      // Convert Test to TestRequestDTO
      const requestDTO: TestRequestDTO = {
        name: testData.name,
        description: testData.description,
        totalDuration: testData.totalDuration,
        difficulty: testData.difficulty,
        totalPoints: testData.totalPoints,
        status: testData.status,
        categorie:testData.categorie,
        exerciseIds: testData.exercises
          .map(ex => ex.id)
          .filter(id => id !== undefined) as number[]
      };
      console.log("DTO");
      console.log(requestDTO);
      const result = await apiService.post<TestResponseDTO>('/tests', requestDTO);
       console.log("result");
      console.log(result);
      return result;
    } catch (error: any) {
      
      let errorMessage = 'Failed to create test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  // Update an existing test
  async updateTest(testId: number, testData: Test): Promise<TestResponseDTO> {
    try {

      // Convert Test to TestRequestDTO
      const requestDTO: TestRequestDTO = {
        name: testData.name,
        description: testData.description,
        totalDuration: testData.totalDuration,
        difficulty: testData.difficulty,
        totalPoints: testData.totalPoints,
        status: testData.status,
        categorie:testData.categorie,
        exerciseIds: testData.exercises
          .map(ex => ex.id)
          .filter(id => id !== undefined) as number[]
      };
      const result = await apiService.put<TestResponseDTO>(`/tests/${testId}`, requestDTO);
      
      return result;
    } catch (error: any) {
      
      let errorMessage = 'Failed to update test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  // Delete a test
  async deleteTest(testId: number): Promise<void> {
    try {
      
      await apiService.delete(`/tests/${testId}`);
      
    } catch (error: any) {
      
      let errorMessage = 'Failed to delete test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  // Get test by ID
  async getTestById(testId: number): Promise<TestResponseDTO> {
    try {
      
      const result = await apiService.get<TestResponseDTO>(`/tests/${testId}`);
      
      return result;
    } catch (error: any) {
      
      let errorMessage = 'Failed to fetch test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  async addExerciseToTest(testId: number, exerciseId: number): Promise<TestResponseDTO> {
    try {
      const result = await apiService.post<TestResponseDTO>(
        `/tests/${testId}/exercises/${exerciseId}`
      );
      return result;
    } catch (error: any) {
      let errorMessage = 'Failed to add exercise to test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status === 409) {
        errorMessage = 'Cet exercice est déjà présent dans le test';
      } else if (error.response?.status === 404) {
        errorMessage = 'Test ou exercice introuvable';
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  },

  // Supprimer un exercice d'un test
  async removeExerciseFromTest(testId: number, exerciseId: number): Promise<TestResponseDTO> {
    try {
      const result = await apiService.delete<TestResponseDTO>(
        `/tests/${testId}/exercises/${exerciseId}`
      );
      return result;
    } catch (error: any) {
      let errorMessage = 'Failed to remove exercise from test';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.status === 404) {
        errorMessage = 'Test ou exercice introuvable';
      } else if (error.response?.status) {
        errorMessage = `HTTP error! status: ${error.response.status}`;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      throw new Error(errorMessage);
    }
  }

};