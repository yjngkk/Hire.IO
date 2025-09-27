import { useState, useEffect } from 'react';
import { formService } from '../components/offer/formService';
import { FormRequest, FormResponse } from '../components/offer/form';

export const useForm = () => {
  const [forms, setForms] = useState<FormResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createForm = async (formData: FormRequest): Promise<FormResponse | null> => {
    setLoading(true);
    setError(null);
    try {
      const newForm = await formService.createForm(formData);
      setForms(prev => [...prev, newForm]);
      return newForm;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create form');
      return null;
    } finally {
      setLoading(false);
    }
  };

  const fetchForms = async () => {
    setLoading(true);
    setError(null);
    try {
      const fetchedForms = await formService.getAllForms();
      setForms(fetchedForms);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch forms');
    } finally {
      setLoading(false);
    }
  };

  const updateForm = async (id: number, formData: FormRequest): Promise<FormResponse | null> => {
    setLoading(true);
    setError(null);
    try {
      const updatedForm = await formService.updateForm(id, formData);
      setForms(prev => prev.map(form => form.id === id ? updatedForm : form));
      return updatedForm;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update form');
      return null;
    } finally {
      setLoading(false);
    }
  };

  const deleteForm = async (id: number): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      await formService.deleteForm(id);
      setForms(prev => prev.filter(form => form.id !== id));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete form');
      return false;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchForms();
  }, []);

  return {
    forms,
    loading,
    error,
    createForm,
    updateForm,
    deleteForm,
    refreshForms: fetchForms,
  };
};