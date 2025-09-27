// exerciseApi.ts
import apiService from "@/config/apiService";

export interface Answer {
  id?: number;
  answerText: string;
  answerIndex: number;
}

export interface Question {
  id?: number;
  questionText: string;
  answers: Answer[];
  correctAnswer: number;
  points: number;
}

export interface Exercise {
  id?: number;
  title: string;
  type: string;
  domain: string;
  theme: string;
  difficulty: string;
  duration: number;
  totalPoints: number;
  questions: Question[];
  createdAt?: string;
  updatedAt?: string;
}

export interface FormQuestion {
  id?: number;
  questionText: string;
  answers: {
    id?: number;
    answerText: string;
    answerIndex: number;
  }[];
  correctAnswer: string;
  points: number;
}

export interface FormExercise {
  id?: number;
  title: string;
  type: string;
  domain: string;
  theme: string;
  difficulty: string;
  duration: number;
  totalPoints: number;
  questions: FormQuestion[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ExerciseFilters {
  domain?: string;
  theme?: string;
  difficulty?: string;
  type?: string;
  search?: string;
}

// Helper function to transform form data to backend format
const transformFormToBackend = (formData: FormExercise): any => {
  const baseData = {
    title: formData.title?.trim(),
    type: formData.type,
    domain: formData.domain,
    theme: formData.theme,
    difficulty: formData.difficulty,
    duration: Number(formData.duration),
    totalPoints: Number(formData.totalPoints),
    questions: formData.questions.map(question => {
      // Filter out empty answers and create clean answer objects
      const nonEmptyAnswers = question.answers
        .filter(answer => answer && answer.answerText && answer.answerText.trim())
        .map((answer, index) => ({
          // Don't include id for updates - let JPA handle entity management
          answerText: answer.answerText.trim(),
          answerIndex: index // Re-index after filtering
        }));

      // Find the correct answer index in the filtered array
      const originalCorrectIndex = parseInt(question.correctAnswer.toString());
      const originalCorrectAnswer = question.answers[originalCorrectIndex];
      
      // Find the new index of the correct answer in the filtered array
      let newCorrectIndex = 0;
      if (originalCorrectAnswer && originalCorrectAnswer.answerText && originalCorrectAnswer.answerText.trim()) {
        const foundIndex = nonEmptyAnswers.findIndex(answer => 
          answer.answerText === originalCorrectAnswer.answerText.trim()
        );
        newCorrectIndex = foundIndex >= 0 ? foundIndex : 0;
      }

      // Create clean question object without id - let JPA handle entity management
      return {
        questionText: question.questionText?.trim(),
        answers: nonEmptyAnswers,
        correctAnswer: newCorrectIndex,
        points: Number(question.points)
      };
    })
  };

  return formData.id
    ? { ...baseData, id: formData.id }
    : baseData;
};
// Helper function to transform backend data to form format
const transformBackendToForm = (exercise: Exercise): FormExercise => {
  return {
    ...exercise,
    questions: exercise.questions.map(question => ({
      id: question.id,
      questionText: question.questionText || "",
      answers: question.answers.map(answer => ({
        id: answer.id,
        answerText: answer.answerText || "",
        answerIndex: answer.answerIndex
      })),
      correctAnswer: question.correctAnswer.toString(),
      points: question.points
    }))
  };
};

export const exerciseApi = {
  // Create a new exercise
  async createExercise(formData: FormExercise): Promise<Exercise> {
    try {
      
      const { id, createdAt, updatedAt, ...dataForCreation } = formData;
      const backendData = transformFormToBackend(dataForCreation as FormExercise);
      
      const result = await apiService.post<Exercise>('/exercises', backendData);
      
      return result;
    } catch (error: any) {
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Unknown error occurred');
      }
    }
  },

  // Get all exercises
  async getAllExercises(): Promise<Exercise[]> {
    try {
      
      const result = await apiService.get<Exercise[]>('/exercises');
      

      return result;
    } catch (error: any) {

      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch exercises');
      }
    }
  },

  // Get exercise by ID
  async getExerciseById(exerciseId: number): Promise<Exercise> {
    try {

      
      const result = await apiService.get<Exercise>(`/exercises/${exerciseId}`);
      

      return result;
    } catch (error: any) {

      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch exercise');
      }
    }
  },

  // Get exercise by ID for form (with transformation)
  async getExerciseByIdForForm(id: number): Promise<FormExercise> {
    try {
      const exercise = await this.getExerciseById(id);
      return transformBackendToForm(exercise);
    } catch (error) {

      throw error;
    }
  },

  // Update exercise
async updateExercise(id: number, formData: FormExercise): Promise<Exercise> {
  try {

        const backendData = transformFormToBackend(formData);
    backendData.id = id;
    const result = await apiService.put<Exercise>(`/exercises/${id}`, backendData);
    
    return result;
  } catch (error: any) {
    
    let errorMessage = 'Failed to update exercise';
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error;
    } else if (error.response?.data?.details) {
      errorMessage = error.response.data.details;
    } else if (error.response?.status) {
      errorMessage = `HTTP ${error.response.status}: ${error.response.statusText || 'Unknown error'}`;
    } else if (error.message) {
      errorMessage = error.message;
    }

    throw new Error(`Update failed: ${errorMessage}`);
  }
},

  // Delete exercise
  async deleteExercise(id: number): Promise<boolean> {
    try {
      
      await apiService.delete(`/exercises/${id}`);
      
      return true;
    } catch (error: any) {
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to delete exercise');
      }
    }
  },

  // Get filtered exercises
  async getFilteredExercises(filters: ExerciseFilters = {}): Promise<Exercise[]> {
    try {
      
      const params: Record<string, string> = {};

      if (filters.domain && filters.domain !== 'Tous') {
        params.domain = filters.domain;
      }
      if (filters.theme && filters.theme !== 'Tous') {
        params.theme = filters.theme;
      }
      if (filters.difficulty && filters.difficulty !== 'Tous') {
        params.difficulty = filters.difficulty;
      }
      if (filters.type && filters.type !== 'Tous') {
        params.type = filters.type;
      }
      if (filters.search) {
        params.search = filters.search;
      }

      const hasFilters = Object.keys(params).length > 0;
      const endpoint = hasFilters ? '/exercises/filter' : '/exercises';
      
      const result = hasFilters 
        ? await apiService.get<Exercise[]>(endpoint, params)
        : await apiService.get<Exercise[]>(endpoint);
      
      return result;
    } catch (error: any) {
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.response?.status) {
        throw new Error(`HTTP error! status: ${error.response.status}`);
      } else {
        throw new Error(error.message || 'Failed to fetch filtered exercises');
      }
    }
  },
};