import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Clock, CheckCircle, AlertCircle, FileText, User, Code, Monitor, Loader } from 'lucide-react';
import apiconfig from "@/config/apiService";
import { useParams } from 'react-router-dom';

const QCMTest = () => {
  // États
  const { accessToken } = useParams();
  const [testAssignment, setTestAssignment] = useState(null);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [testStarted, setTestStarted] = useState(false);
  const [testSubmitted, setTestSubmitted] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [showResults, setShowResults] = useState(false);
  const [score, setScore] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [questions, setQuestions] = useState([]);
  
  const timerRef = useRef(null);

  // Charger les données depuis l'API
  
    const loadData = async () => {
      try {
        setError(''); // Reset erreur
        
        const response = await apiconfig.get(`/test-assignments/token/${accessToken}`);
        
        setTestAssignment(response);
        
        // Extraire les questions de tous les exercices
        if (response.test?.exercises) {
          const allQuestions = [];
          response.test.exercises.forEach((exercise, index) => {
            if (exercise.questions && Array.isArray(exercise.questions)) {
              exercise.questions.forEach(question => {
                allQuestions.push({
                  ...question,
                  exerciseId: exercise.id,
                  exerciseTitle: exercise.title
                });
              });
            } else {
              console.warn(`⚠️ Exercice ${index} n'a pas de questions valides:`, exercise);
            }
          });
          setQuestions(allQuestions);
        } else {
          console.error("❌ Pas d'exercices dans response.test:", response.test);
        }
        
        // Définir le temps initial
        if (response.test?.totalDuration) {
          setTimeLeft(response.test.totalDuration * 60);
        } else {
          console.warn("⚠️ Pas de totalDuration trouvée");
        }
        
        // Vérifier le statut du test
        if (response.status === 'STARTED') {
          setTestStarted(true);
          if (response.startedAt) {
            const startTime = new Date(response.startedAt);
            const now = new Date();
            const elapsedMinutes = Math.floor((now - startTime) / 60000);
            const remainingMinutes = Math.max(0, response.test.totalDuration - elapsedMinutes);
            setTimeLeft(remainingMinutes * 60);
          }
        }
        
        setLoading(false);
        
      } catch (err) {
        console.error("💥 ERREUR CAPTURÉE:");
        console.error("📋 Erreur complète:", err);
        console.error("🔍 Type d'erreur:", typeof err);
        console.error("🔍 Nom de l'erreur:", err.name);
        console.error("🔍 Message:", err.message);
        console.error("🔍 Stack:", err.stack);
        console.error("🔍 A une propriété 'response'?", !!err.response);
        if (err.response) {
          console.error("🔍 err.response:", err.response);
        }
        
        // Gestion d'erreur améliorée - ne pas accéder à err.response si undefined
        let errorMessage = 'Erreur de chargement';
        
        if (err && typeof err === 'object') {
          if (err.message) {
            errorMessage = err.message;
          } else if (err.response && err.response.data && err.response.data.message) {
            errorMessage = err.response.data.message;
          } else if (err.response && err.response.status) {
            errorMessage = `Erreur HTTP ${err.response.status}`;
          }
        } else if (typeof err === 'string') {
          errorMessage = err;
        }
        
        setError(errorMessage);
        setLoading(false);
      }
    };
    useEffect(() => {
    
    loadData();
  }, []);

  // Timer
  useEffect(() => {
    if (testStarted && !testSubmitted && timeLeft > 0) {
      timerRef.current = setInterval(() => {
        setTimeLeft(prevTime => {
          if (prevTime <= 1) {
            clearInterval(timerRef.current);
            handleAutoSubmit();
            return 0;
          }
          return prevTime - 1;
        });
      }, 1000);
    }

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, [testStarted, testSubmitted, timeLeft]);

  // Commencer le test
  const handleStartTest = async () => {
    try {
      const response = await apiconfig.post(`/test-assignments/start/${accessToken}`);
      setTestStarted(true);
    } catch (err) {
      console.error("❌ Erreur démarrage:", err);
      
      let errorMessage = 'Erreur lors du démarrage du test';
      // if (err && err.message) {
      //   errorMessage = err.message;
      // } else if (err && err.response && err.response.data && err.response.data.message) {
      //   errorMessage = err.response.data.message;
      // }
      if (err.response && err.response.data && err.response.data.message) {
      errorMessage = err.response.data.message; // Message exact du backend
    }
    
    
      
      setError(errorMessage);
    }
  };

  // Sélectionner une réponse
  const handleOptionClick = (questionId, answerIndex) => {
    if (!testSubmitted) {
      setSelectedAnswers(prev => ({
        ...prev,
        [questionId]: answerIndex
      }));
    }
  };

  // Navigation entre questions
  const nextQuestion = () => {
    if (currentQuestion < questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
    }
  };

  const prevQuestion = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion(currentQuestion - 1);
    }
  };

  // Soumettre le test
  const handleSubmitTest = () => {
    if (window.confirm('Êtes-vous sûr de vouloir soumettre votre test ?')) {
      submitTest();
    }
  };

  const handleAutoSubmit = useCallback(() => {
    submitTest();
  }, []);

  const submitTest = async () => {
    try {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }

      // Préparer les réponses pour l'API
      const answers = questions.map(question => ({
        questionId: question.id,
        selectedAnswer: selectedAnswers[question.id] !== undefined ? selectedAnswers[question.id] : null
      }));

      const submitData = {
        accessToken:accessToken ,
        answers
      };

      
      const results = await apiconfig.post('/test-assignments/submit', submitData);
      console.log(results);
      
      
      // Calculer le score local en attendant les résultats de l'API
      let correctAnswers = 0;
      questions.forEach(question => {
        if (selectedAnswers[question.id] === question.correctAnswer) {
          correctAnswers++;
        }
      });

    setScore(correctAnswers);
    if(results.percentage > 50) {
    const dateTime = new Date();
    dateTime.setDate(dateTime.getDate() + 3);
    dateTime.setHours(10, 0, 0, 0);
    
      const entretien = await apiconfig.post('/entretiens', {
      candidat: { id: testAssignment.candidat.id },
      dateHeure: dateTime.toISOString(),
      duree: 60, // 1 heure
      type: 'meet',
      interviewerEmail: testAssignment.candidat.email, 
      organizerEmail: 'rh@company.com', 
      notes: `Test terminé avec un score de ${correctAnswers}/${questions.length}`
    });
    
    console.log('✅ Entretien planifié:', entretien.data);
    }
      setTestSubmitted(true);
      setShowResults(true);
      
    } catch (err) {
      console.error('❌ Erreur soumission:', err);
      
      let errorMessage = 'Erreur lors de la soumission';
      if (err && err.message) {
        errorMessage = err.message;
      } else if (err && err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      }
      
      setError(errorMessage);
    }
  };

  // Formater le temps
  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`;
  };

  // Obtenir la couleur du badge de difficulté
  const getDifficultyColor = (difficulty) => {
    switch (difficulty) {
      case 'Facile': return 'bg-green-100 text-green-800';
      case 'Moyen': return 'bg-yellow-100 text-yellow-800';
      case 'Difficile': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="text-center bg-white p-8 rounded-xl shadow-lg">
          <Loader className="w-12 h-12 text-blue-600 mx-auto mb-4 animate-spin" />
          <p className="text-gray-600 text-lg">Chargement du test...</p>
          <p className="text-gray-400 text-sm mt-2">Récupération des données...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-red-50 to-pink-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center max-w-md">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 mb-2">Erreur</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Réessayer
          </button>
        </div>
      </div>
    );
  }

  // Vérifier que les données sont chargées
  if (!testAssignment || questions.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="text-center bg-white p-8 rounded-xl shadow-lg">
          <AlertCircle className="w-12 h-12 text-orange-500 mx-auto mb-4" />
          <p className="text-gray-600 text-lg">Aucune donnée disponible</p>
        </div>
      </div>
    );
  }

  // Page des résultats
  if (showResults) {   
    return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-8 px-4">
      <div className="max-w-md mx-auto">
        <div className="bg-white rounded-xl shadow-lg p-8 text-center">
          <CheckCircle className="w-20 h-20 text-green-500 mx-auto mb-6" />
          
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            Test Terminé !
          </h1>
          
          <p className="text-lg text-gray-600 mb-2">
            Merci pour avoir terminé le test.
          </p>
          
          <p className="text-gray-500">
            Nous vous contacterons par la suite avec vos résultats.
          </p>
          
          <div className="mt-8 inline-flex items-center px-6 py-3 bg-green-100 text-green-800 rounded-full text-sm font-medium">
            🎉 Merci pour votre participation !
          </div>
        </div>
      </div>
    </div>
  );
  }

  // Page principale du test
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header fixe */}
      <div className="bg-white shadow-sm border-b sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <Code className="w-6 h-6 text-blue-600" />
                <span className="font-bold text-lg text-gray-900">{testAssignment.test.name}</span>
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-medium ${getDifficultyColor(testAssignment.test.difficulty)}`}>
                {testAssignment.test.difficulty}
              </span>
            </div>
            
            {testStarted && (
              <div className="flex items-center space-x-4">
                <div className="text-sm text-gray-600">
                  Question {currentQuestion + 1} / {questions.length}
                </div>
                <div className="flex items-center space-x-2">
                  <Clock className="w-4 h-4 text-gray-600" />
                  <span className={`font-mono text-lg font-semibold ${
                    timeLeft < 300 ? 'text-red-600' : 'text-gray-900'
                  }`}>
                    {formatTime(timeLeft)}
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Informations candidat */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              <User className="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">{testAssignment.candidat.nom}</h2>
              <p className="text-sm text-gray-600">{testAssignment.candidat.poste} • {testAssignment.candidat.email}</p>
            </div>
          </div>
        </div>

        {/* Bouton de démarrage */}
        {!testStarted && (
          <div className="bg-white rounded-xl shadow-lg p-8 text-center">
            <Monitor className="w-16 h-16 text-blue-500 mx-auto mb-6" />
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Prêt à commencer le test ?</h2>
            <div className="text-gray-600 mb-6 space-y-2">
              <p className="text-lg">{testAssignment.test.description}</p>
              <div className="flex justify-center space-x-8 text-sm">
                <div>📝 {questions.length} questions</div>
                <div>⏱️ {testAssignment.test.totalDuration} minutes</div>
                <div>🎯 {testAssignment.test.totalPoints} points</div>
              </div>
            </div>
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
              <p className="text-sm text-yellow-800">
                ⚠️ Une fois commencé, vous ne pourrez pas mettre le test en pause. Assurez-vous d'avoir une connexion internet stable.
              </p>
            </div>
            <button 
              onClick={handleStartTest}
              className="px-8 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-lg font-semibold transform transition-transform hover:scale-105"
            >
              Commencer le test
            </button>
          </div>
        )}

        {/* Interface de test */}
        {testStarted && !testSubmitted && questions.length > 0 && (
          <div className="space-y-6">
            {/* Barre de progression */}
            <div className="bg-white rounded-xl shadow-sm p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">Progression</span>
                <span className="text-sm text-gray-500">{Math.round(((currentQuestion + 1) / questions.length) * 100)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${((currentQuestion + 1) / questions.length) * 100}%` }}
                ></div>
              </div>
            </div>

            {/* Question actuelle */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex items-start justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900 leading-relaxed">
                  {questions[currentQuestion].questionText}
                </h3>
                <div className="text-sm text-blue-600 font-medium bg-blue-50 px-3 py-1 rounded-full">
                  {questions[currentQuestion].points} points
                </div>
              </div>
              
              <div className="space-y-3">
                {questions[currentQuestion].answers
                  .sort((a, b) => a.answerIndex - b.answerIndex)
                  .map((answer, answerIndex) => (
                  <div
                    key={answer.id}
                    className={`p-4 rounded-lg border-2 cursor-pointer transition-all ${
                      selectedAnswers[questions[currentQuestion].id] === answerIndex
                        ? 'bg-blue-50 border-blue-300 shadow-md'
                        : 'bg-gray-50 border-gray-200 hover:bg-gray-100 hover:border-gray-300'
                    }`}
                    onClick={() => handleOptionClick(questions[currentQuestion].id, answerIndex)}
                  >
                    <div className="flex items-center space-x-3">
                      <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${
                        selectedAnswers[questions[currentQuestion].id] === answerIndex
                          ? 'bg-blue-600 border-blue-600'
                          : 'border-gray-300'
                      }`}>
                        {selectedAnswers[questions[currentQuestion].id] === answerIndex && (
                          <div className="w-2 h-2 bg-white rounded-full"></div>
                        )}
                      </div>
                      <span className="font-mono text-sm text-gray-600 min-w-[1.5rem]">
                        {String.fromCharCode(65 + answerIndex)}.
                      </span>
                      <span className="text-gray-900 font-mono text-sm">
                        {answer.answerText}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Navigation */}
            <div className="flex items-center justify-between">
              <button 
                onClick={prevQuestion}
                disabled={currentQuestion === 0}
                className={`px-6 py-3 rounded-lg font-medium ${
                  currentQuestion === 0
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                ← Précédent
              </button>

              <div className="flex space-x-2">
                {questions.map((_, index) => (
                  <button
                    key={index}
                    onClick={() => setCurrentQuestion(index)}
                    className={`w-8 h-8 rounded-full text-sm font-medium ${
                      index === currentQuestion
                        ? 'bg-blue-600 text-white'
                        : selectedAnswers[questions[index].id] !== undefined
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
                    }`}
                  >
                    {index + 1}
                  </button>
                ))}
              </div>

              {currentQuestion === questions.length - 1 ? (
                <button 
                  onClick={handleSubmitTest}
                  className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium"
                >
                  Terminer le test
                </button>
              ) : (
                <button 
                  onClick={nextQuestion}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
                >
                  Suivant →
                </button>
              )}
            </div>

            {/* Résumé des réponses */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h4 className="font-medium text-gray-900 mb-4">Résumé de vos réponses</h4>
              <div className="flex flex-wrap gap-2">
                {questions.map((question, index) => (
                  <div
                    key={question.id}
                    className={`w-10 h-10 rounded-lg flex items-center justify-center text-sm font-medium cursor-pointer ${
                      selectedAnswers[question.id] !== undefined
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-500'
                    }`}
                    onClick={() => setCurrentQuestion(index)}
                  >
                    {index + 1}
                  </div>
                ))}
              </div>
              <p className="text-sm text-gray-600 mt-3">
                {Object.keys(selectedAnswers).length} / {questions.length} questions répondues
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default QCMTest;