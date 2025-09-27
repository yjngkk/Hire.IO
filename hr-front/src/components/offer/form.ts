export interface FormRequest {
  title: string;
  missions: string;
  location: string;
  contractType: string; 
  level: string;
  skills: string;
  tone: string;
  categorie:string
}

export interface FormResponse {
  id: number;
  title: string;
  missions: string;
  location: string;
  contractType: string;
  level: string;
  skills: string;
  tone: string;
  createdAt: string;  
  applicationLink: string; 
}
