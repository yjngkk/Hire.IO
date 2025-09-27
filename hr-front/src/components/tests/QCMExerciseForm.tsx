import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Save, X, Plus, Trash2, ChevronDown, ChevronUp } from "lucide-react";
import { Exercise, FormExercise, FormQuestion, exerciseApi } from "@/components/api/exerciseApi";

const domains = ["Développement Frontend", "Développement Backend", "Algorithmique", "Base de données", "Soft Skills"];
const themes = ["JavaScript", "React", "Python", "SQL", "Tri", "Personnalité"];
const difficulties = ["Débutant", "Intermédiaire", "Avancé", "Tous niveaux"];

interface QCMExerciseFormProps {
  initialData?: Exercise | null;
  onSave?: (exerciseData: FormExercise) => void;
  onCancel?: () => void;
}

export default function QCMExerciseForm({ initialData, onSave, onCancel }: QCMExerciseFormProps) {
  const [formData, setFormData] = useState<FormExercise>({
    title: "",
    type: "QCM",
    domain: "",
    theme: "",
    difficulty: "",
    duration: 30,
    totalPoints: 0,
    questions: [
      {
        questionText: "",
        answers: [
  { answerText: "", answerIndex: 0 },
  { answerText: "", answerIndex: 1 },
  { answerText: "", answerIndex: 2 },
  { answerText: "", answerIndex: 3 }
],

        correctAnswer: "0",
        points: 5
      }
    ]
  });

  const [showPreview, setShowPreview] = useState(false);
  const [expandedQuestions, setExpandedQuestions] = useState<Record<number, boolean>>({ 0: true });
  const [isLoading, setIsLoading] = useState(false);

  const getAnswerText = (answer: string | any): string => {
  if (!answer) return ''; 
  if (typeof answer === 'string') {
    return answer;
  }
  if (answer && typeof answer === 'object' && answer.answerText) {
    return answer.answerText;
  }
  return '';
};

  useEffect(() => {
    if (initialData) {
      // Convert Exercise format to FormExercise format
      const normalizedQuestions = (initialData.questions || []).map(q => ({
        id: q.id,
        questionText: q.questionText || "",
      answers: q.answers.map((answer, index) => ({
  id: typeof answer === 'object' ? answer.id : undefined,
  answerText: typeof answer === 'object' ? answer.answerText : answer,
  answerIndex: index
})),

        correctAnswer: q.correctAnswer.toString(), 
        points: q.points
      }));

      setFormData({
        id: initialData.id,
        title: initialData.title || "",
        type: initialData.type || "QCM",
        domain: initialData.domain || "",
        theme: initialData.theme || "",
        difficulty: initialData.difficulty || "",
        duration: initialData.duration || 30,
        totalPoints: initialData.totalPoints || 0,
        createdAt: initialData.createdAt,
        updatedAt: initialData.updatedAt,
        questions: normalizedQuestions
      });

      // Set up expanded questions for existing data
      const expanded: Record<number, boolean> = {};
      normalizedQuestions.forEach((_, index) => {
        expanded[index] = index === 0; // Only expand first question by default
      });
      setExpandedQuestions(expanded);
    }
  }, [initialData]);

  // Calculate total points whenever questions change
  useEffect(() => {
    const totalPoints = formData.questions.reduce((sum, q) => sum + (q.points || 0), 0);
    setFormData(prev => ({ ...prev, totalPoints }));
  }, [formData.questions]);

  const handleInputChange = (field: keyof FormExercise, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleQuestionChange = (questionIndex: number, field: keyof FormQuestion, value: string | number) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) => 
        i === questionIndex ? { ...q, [field]: value } : q
      )
    }));
  };

const handleAnswerChange = (questionIndex: number, answerIndex: number, value: string) => {
  setFormData(prev => ({
    ...prev,
    questions: prev.questions.map((q, i) =>
      i === questionIndex
        ? {
            ...q,
            answers: q.answers.map((answer, j) =>
              j === answerIndex
                ? { ...answer, answerText: value }
                : answer
            )
          }
        : q
    )
  }));
};


const addAnswer = (questionIndex: number) => {
  if (formData.questions[questionIndex].answers.length < 6) {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === questionIndex
          ? {
              ...q,
              answers: [
                ...q.answers,
                {
                  answerText: "",
                  answerIndex: q.answers.length
                }
              ]
            }
          : q
      )
    }));
  }
};


 const removeAnswer = (questionIndex: number, answerIndex: number) => {
  if (formData.questions[questionIndex].answers.length > 2) {
    const currentQuestion = formData.questions[questionIndex];
    const currentCorrectAnswer = parseInt(currentQuestion.correctAnswer);
    
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === questionIndex
          ? {
              ...q,
              answers: q.answers
                .filter((_, j) => j !== answerIndex)
                .map((answer, newIndex) => ({
                  ...answer,
                  answerIndex: newIndex
                })),
              correctAnswer: (() => {
                if (currentCorrectAnswer > answerIndex) {
                  return (currentCorrectAnswer - 1).toString();
                } else if (currentCorrectAnswer === answerIndex) {
                  return "0"; 
                } else {
                  return currentCorrectAnswer.toString();
                }
              })()
            }
          : q
      )
    }));
  }
};


const addQuestion = () => {
  const newQuestion: FormQuestion = {
    questionText: "",
    answers: [
      { answerText: "", answerIndex: 0 },
      { answerText: "", answerIndex: 1 },
      { answerText: "", answerIndex: 2 },
      { answerText: "", answerIndex: 3 }
    ],
    correctAnswer: "0",
    points: 5
  };
  
  setFormData(prev => ({
    ...prev,
    questions: [...prev.questions, newQuestion]
  }));
  
  const newIndex = formData.questions.length;
  setExpandedQuestions(prev => ({ ...prev, [newIndex]: true }));
};



  const removeQuestion = (questionIndex: number) => {
    if (formData.questions.length > 1) {
      setFormData(prev => ({
        ...prev,
        questions: prev.questions.filter((_, i) => i !== questionIndex)
      }));
      
      setExpandedQuestions(prev => {
        const updated: Record<number, boolean> = {};
        Object.keys(prev).forEach(key => {
          const index = parseInt(key);
          if (index < questionIndex) {
            updated[index] = prev[index];
          } else if (index > questionIndex) {
            updated[index - 1] = prev[index];
          }
        });
        return updated;
      });
    }
  };

  const toggleQuestionExpansion = (questionIndex: number) => {
    setExpandedQuestions(prev => ({
      ...prev,
      [questionIndex]: !prev[questionIndex]
    }));
  };

  const handleSave = async () => {
    if (!isFormValid()) {
      alert("Veuillez remplir tous les champs obligatoires.");
      return;
    }

    setIsLoading(true);
    try {
      if (onSave) {
        onSave(formData); 
      }
      
      if (!initialData) {
        resetForm();
      }
      
    } catch (error) {
      alert("Erreur lors de l'enregistrement de l'exercice. Veuillez réessayer.");
    } finally {
      setIsLoading(false);
    }
  };

const resetForm = () => {
  setFormData({
    title: "",
    type: "QCM",
    domain: "",
    theme: "",
    difficulty: "",
    duration: 30,
    totalPoints: 0,
    questions: [{
      questionText: "",
      answers: [
        { answerText: "", answerIndex: 0 },
        { answerText: "", answerIndex: 1 },
        { answerText: "", answerIndex: 2 },
        { answerText: "", answerIndex: 3 }
      ],
      correctAnswer: "0",
      points: 5
    }]
  });
  setShowPreview(false);
  setExpandedQuestions({ 0: true });
};

 const handleCancel = () => {
  if (initialData) {
    // Reset to initial data when editing
    const normalizedQuestions = (initialData.questions || []).map(q => ({
      id: q.id,
      questionText: q.questionText || "",
      answers: q.answers.map((answer, index) => ({
        id: typeof answer === 'object' ? answer.id : undefined,
        answerText: typeof answer === 'object' ? answer.answerText : answer,
        answerIndex: typeof answer === 'object' ? answer.answerIndex : index
      })),
      correctAnswer: q.correctAnswer.toString(),
      points: q.points
    }));

    setFormData({
      ...initialData,
      questions: normalizedQuestions
    });
  } else {
    resetForm();
  }

  if (onCancel) {
    onCancel();
  }
};

  const getDifficultyBadge = (difficulty: string) => {
    const styles = {
      "Débutant": "bg-blue-100 text-blue-800",
      "Intermédiaire": "bg-orange-100 text-orange-800",
      "Avancé": "bg-red-100 text-red-800",
      "Tous niveaux": "bg-gray-100 text-gray-800"
    };
    
    return (
      <Badge variant="outline" className={styles[difficulty as keyof typeof styles]}>
        {difficulty}
      </Badge>
    );
  };

  const isFormValid = () => {
  return formData.title && 
         formData.domain && 
         formData.theme && 
         formData.difficulty && 
         formData.questions.length > 0 &&
         formData.questions.every(q => {
           const nonEmptyAnswers = q.answers.filter(answer => 
             answer && answer.answerText && answer.answerText.trim()
           );
           
           return q.questionText && q.questionText.trim() &&
                  nonEmptyAnswers.length >= 2 && 
                  q.correctAnswer !== undefined &&
                  parseInt(q.correctAnswer) >= 0 &&
                  parseInt(q.correctAnswer) < nonEmptyAnswers.length; 
         });
};
  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Header Section */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex justify-between items-start">
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-blue-50 rounded-lg">
                  📚
                </div>
                <div>
                  <h1 className="text-2xl font-semibold text-gray-900">
                    {initialData ? "Modifier l'exercice QCM" : "Créer un exercice QCM"}
                  </h1>
                  <div className="flex items-center gap-4 text-sm text-gray-600 mt-1">
                    <div className="flex items-center gap-1">
                      ❓
                      <span>{formData.questions.length} question{formData.questions.length > 1 ? 's' : ''}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      🎯
                      <span>{formData.totalPoints} points</span>
                    </div>
                    <div className="flex items-center gap-1">
                      ⏱️
                      <span>{formData.duration} min</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Button 
                variant="outline" 
                onClick={() => setShowPreview(!showPreview)}
                className="bg-white hover:bg-blue-50 border-blue-200 text-blue-700"
              >
                {showPreview ? "✏️ Modifier" : "👁️ Aperçu"}
              </Button>
              <Button 
                variant="outline" 
                onClick={handleCancel} 
                disabled={isLoading}
                className="bg-white hover:bg-gray-50 border-gray-300 text-gray-600 hover:text-gray-700"
              >
                <X className="h-4 w-4 mr-2" />
                Annuler
              </Button>
            </div>
          </div>
        </div>

        {!showPreview ? (
          <div className="space-y-8">
            {/* Informations générales */}
            <Card className="border border-gray-200 shadow-sm bg-white">
              <CardHeader className="bg-blue-900 text-white rounded-t-lg">
                <CardTitle className="text-xl flex items-center gap-2">
                  <div className="p-1 bg-white/20 rounded">
                    📋
                  </div>
                  Informations générales
                </CardTitle>
                <CardDescription className="text-blue-100">
                  Définissez les caractéristiques de votre exercice
                </CardDescription>
              </CardHeader>
              <CardContent className="p-6 space-y-6">
                <div className="space-y-3">
                  <Label htmlFor="title" className="text-sm font-semibold text-gray-700">
                    Titre de l'exercice <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="title"
                    required
                    placeholder="Ex: Test JavaScript - Variables et Fonctions"
                    value={formData.title}
                    onChange={(e) => handleInputChange("title", e.target.value)}
                    className="h-12 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                  />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">
                      Domaine <span className="text-red-500">*</span>
                    </Label> 
                    <Select value={formData.domain} onValueChange={(value) => handleInputChange("domain", value)}>
                      <SelectTrigger className="h-12 border-gray-300 focus:border-blue-500">
                        <SelectValue placeholder="Sélectionner un domaine" />
                      </SelectTrigger>
                      <SelectContent>
                        {domains.map((domain) => (
                          <SelectItem key={domain} value={domain}>
                            {domain}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">
                      Thème <span className="text-red-500">*</span>
                    </Label>
                    <Select value={formData.theme} onValueChange={(value) => handleInputChange("theme", value)}>
                      <SelectTrigger className="h-12 border-gray-300 focus:border-blue-500">
                        <SelectValue placeholder="Sélectionner un thème" />
                      </SelectTrigger>
                      <SelectContent>
                        {themes.map((theme) => (
                          <SelectItem key={theme} value={theme}>
                            {theme}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">
                      Niveau <span className="text-red-500">*</span>
                    </Label>
                    <Select value={formData.difficulty} onValueChange={(value) => handleInputChange("difficulty", value)}>
                      <SelectTrigger className="h-12 border-gray-300 focus:border-blue-500">
                        <SelectValue placeholder="Sélectionner le niveau" />
                      </SelectTrigger>
                      <SelectContent>
                        {difficulties.map((difficulty) => (
                          <SelectItem key={difficulty} value={difficulty}>
                            {difficulty}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-3">
                    <Label htmlFor="duration" className="text-sm font-semibold text-gray-700">
                      Durée totale (min)
                    </Label>
                    <Input
                      id="duration"
                      type="number"
                      min="5"
                      max="120"
                      value={formData.duration}
                      onChange={(e) => handleInputChange("duration", parseInt(e.target.value) || 30)}
                      className="h-12 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                    />
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">
                      Points totaux
                    </Label>
                    <div className="h-12 px-4 py-3 border border-gray-300 bg-gray-50 rounded-md flex items-center font-medium text-gray-700">
                      {formData.totalPoints} points
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Questions */}
            <div className="space-y-6">
              <div className="flex justify-between items-center">
                <h2 className="text-2xl font-semibold text-gray-900 flex items-center gap-3">
                  <div className="p-2 bg-gray-100 rounded-lg">
                    ❓
                  </div>
                  Questions
                </h2>
                <Button 
                  onClick={addQuestion} 
                  className="bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Ajouter une question
                </Button>
              </div>

              {formData.questions.map((question, questionIndex) => (
                <Card key={questionIndex} className="border border-gray-200 shadow-sm bg-white overflow-hidden">
                  <CardHeader className="bg-gray-50 border-b border-gray-200">
                    <div className="flex justify-between items-center">
                      <div className="flex items-center gap-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                            <span className="text-sm font-semibold text-blue-600">
                              {questionIndex + 1}
                            </span>
                          </div>
                          <CardTitle className="text-lg text-gray-900">
                            Question {questionIndex + 1}
                          </CardTitle>
                        </div>
                        <Badge className="bg-gray-100 text-gray-700 border-gray-200">
                          {question.points} points
                        </Badge>
                      </div>
                      <div className="flex items-center gap-2">
                        {formData.questions.length > 1 && (
                          <Button 
                            variant="outline" 
                            size="sm"
                            onClick={() => removeQuestion(questionIndex)}
                            className="text-red-600 hover:text-red-700"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        )}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => toggleQuestionExpansion(questionIndex)}
                          className="bg-white hover:bg-gray-50"
                        >
                          {expandedQuestions[questionIndex] ? 
                            <ChevronUp className="h-4 w-4" /> : 
                            <ChevronDown className="h-4 w-4" />
                          }
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  
                  {expandedQuestions[questionIndex] && (
                    <CardContent className="p-6 space-y-6">
                      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                        <div className="lg:col-span-3 space-y-3">
                          <Label className="text-sm font-semibold text-gray-700">
                            Question <span className="text-red-500">*</span>
                          </Label>
                          <Textarea
                            required
                            placeholder="Saisissez votre question ici..."
                            rows={4}
                            value={question.questionText}
                            onChange={(e) => handleQuestionChange(questionIndex, "questionText", e.target.value)}
                            className="border-gray-300 focus:border-blue-500 focus:ring-blue-500 resize-none"
                          />
                        </div>
                        <div className="space-y-3">
                          <Label className="text-sm font-semibold text-gray-700">Points</Label>
                          <Input
                            type="number"
                            min="1"
                            max="20"
                            value={question.points}
                            onChange={(e) => handleQuestionChange(questionIndex, "points", parseInt(e.target.value) || 5)}
                            className="h-12 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                          />
                        </div>
                      </div>

                      <Separator className="bg-gray-200" />

                      <div className="space-y-4">
                        <div className="flex justify-between items-center">
                          <Label className="text-sm font-semibold text-gray-700">
                            Réponses possibles <span className="text-red-500">*</span>
                          </Label>
                          <div className="flex gap-2">
                            {question.answers.length < 6 && (
                              <Button 
                                variant="outline" 
                                size="sm" 
                                onClick={() => addAnswer(questionIndex)}
                                className="bg-white text-blue-600 border-blue-200 hover:bg-blue-50"
                              >
                                <Plus className="h-3 w-3 mr-1" />
                                Ajouter réponse
                              </Button>
                            )}
                          </div>
                        </div>
                        <RadioGroup 
                          value={question.correctAnswer} 
                          onValueChange={(value) => handleQuestionChange(questionIndex, "correctAnswer", value)}
                          className="space-y-4"
                        >
                          {question.answers.map((answer, answerIndex) => (
                            <div key={answerIndex} className="space-y-3">
                              <div className="flex items-center justify-between">
                                <div className="flex items-center space-x-3">
                                  <RadioGroupItem 
                                    value={answerIndex.toString()} 
                                    id={`q${questionIndex}-answer-${answerIndex}`}
                                    className="border-2 border-gray-400 text-blue-600"
                                  />
                                  <Label 
                                    htmlFor={`q${questionIndex}-answer-${answerIndex}`} 
                                    className="text-sm font-semibold text-gray-700 flex items-center gap-2"
                                  >
                                    <span className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center text-xs font-semibold text-blue-600">
                                      {String.fromCharCode(65 + answerIndex)}
                                    </span>
                                    Réponse {String.fromCharCode(65 + answerIndex)}
                                    {question.correctAnswer === answerIndex.toString() && (
                                      <Badge className="bg-green-50 text-green-700 border-green-200 text-xs">
                                        ✓ Correcte
                                      </Badge>
                                    )}
                                  </Label>
                                </div>
                                {question.answers.length > 2 && (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => removeAnswer(questionIndex, answerIndex)}
                                    className="text-red-600 hover:text-red-700"
                                  >
                                    <Trash2 className="red-500 h-3 w-3" />
                                  </Button>
                                )}
                              </div>
                              <Input
                                placeholder={`Saisissez la réponse ${String.fromCharCode(65 + answerIndex)}`}
                                value={answer.answerText}
                                onChange={(e) => handleAnswerChange(questionIndex, answerIndex, e.target.value)}
                                className="ml-9 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
                              />
                            </div>
                          ))}
                        </RadioGroup>
                      </div>
                    </CardContent>
                  )}
                </Card>
              ))}
            </div>
          </div>
        ) : (
          /* Aperçu */
          <Card className="border border-gray-200 shadow-sm bg-white">
            <CardHeader className="bg-blue-600 text-white rounded-t-lg">
              <div className="flex justify-between items-start">
                <div className="space-y-2">
                  <CardTitle className="text-2xl">
                    {formData.title || "Titre de l'exercice"}
                  </CardTitle>
                  <CardDescription className="text-blue-100">
                    Aperçu de votre exercice QCM - {formData.questions.length} question{formData.questions.length > 1 ? 's' : ''}
                  </CardDescription>
                </div>
                <div className="flex flex-col gap-2">
                  <Badge className="bg-white/20 text-white border-white/30">QCM</Badge>
                  {formData.difficulty && getDifficultyBadge(formData.difficulty)}
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-8 space-y-8">
              <div className="flex items-center gap-6 text-sm">
                <div className="flex items-center gap-2 text-gray-600">
                  ⏱️
                  <span className="font-medium">{formData.duration} min</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  🎯
                  <span className="font-medium">{formData.totalPoints} points</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  ❓
                  <span className="font-medium">{formData.questions.length} question{formData.questions.length > 1 ? 's' : ''}</span>
                </div>
              </div>
              
              {formData.domain && formData.theme && (
                <div className="flex items-center gap-2">
                  <Badge variant="secondary" className="bg-blue-50 text-blue-700">{formData.domain}</Badge>
                  <Badge variant="secondary" className="bg-gray-100 text-gray-700">{formData.theme}</Badge>
                </div>
              )}
              
              <Separator className="bg-gray-200" />
              
              {formData.questions.map((question, questionIndex) => (
                <div key={questionIndex} className="space-y-6 p-6 bg-white border border-gray-200 rounded-lg">
                  <div className="flex justify-between items-start">
                    <h4 className="text-lg font-medium text-gray-900 flex items-center gap-2">
                      <span className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-sm font-semibold text-blue-600">
                        {questionIndex + 1}
                      </span>
                      Question {questionIndex + 1}
                    </h4>
                    <Badge className="bg-gray-100 text-gray-700 border-gray-200">
                      {question.points} points
                    </Badge>
                  </div>
                  
                  <p className="text-gray-800 text-base leading-relaxed ml-10">
                    {question.questionText || "Votre question apparaîtra ici..."}
                  </p>
                  
                  <div className="space-y-3 ml-10">
                    {question.answers
                      .filter(answer => answer && answer.answerText && answer.answerText.trim())
                      .map((answer, displayIndex) => {
                        const originalIndex = question.answers.findIndex(a => a === answer);
                        return (
                          <div 
                            key={originalIndex} 
                            className={`p-4 border-2 rounded-lg flex items-center gap-4 transition-all ${
                              question.correctAnswer === originalIndex.toString() 
                                ? 'bg-green-50 border-green-200 shadow-sm' 
                                : 'bg-white border-gray-300 hover:border-gray-400'
                            }`}
                          >
                            <div className={`w-8 h-8 rounded-full border-2 flex items-center justify-center font-bold ${
                              question.correctAnswer === originalIndex.toString() 
                                ? 'border-green-500 bg-green-500 text-white' 
                                : 'border-gray-400 bg-white text-gray-600'
                            }`}>
                              <span className="text-sm">
                                {String.fromCharCode(65 + displayIndex)}
                              </span>
                            </div>
                            <span className={`text-base ${
                              question.correctAnswer === originalIndex.toString() 
                                ? 'font-semibold text-green-800' 
                                : 'text-gray-700'
                            }`}>
                              {answer.answerText}
                            </span>
                            {question.correctAnswer === originalIndex.toString() && (
                              <Badge className="ml-auto bg-green-50 text-green-700 border-green-200">
                                ✓ Réponse correcte
                              </Badge>
                            )}
                          </div>
                        );
                      })}
                  </div>
                  
                  {questionIndex < formData.questions.length - 1 && (
                    <Separator className="mt-8 bg-gray-300" />
                  )}
                </div>
              ))}
            </CardContent>
          </Card>
        )}

        {/* Actions */}
        <div className="flex justify-end gap-4 pb-8">
          <Button 
            onClick={handleSave}
            disabled={!isFormValid() || isLoading}
            className="min-w-40 h-12 bg-blue-600 hover:bg-blue-700 text-white shadow-sm font-medium"
          >
            <Save className="h-5 w-5 mr-2" />
            {isLoading ? "Enregistrement..." : 
             initialData ? "Mettre à jour l'exercice" : "Enregistrer l'exercice"}
          </Button>
        </div>
      </div>
    </div>
  );
}