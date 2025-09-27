package ma.nexotek.HireCraft.service;

import ma.nexotek.HireCraft.dto.CandidatDTO;
import ma.nexotek.HireCraft.model.*;
import ma.nexotek.HireCraft.dto.Test.*;
import ma.nexotek.HireCraft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestAssignmentService {
    @Autowired
    private TestAssignmentRepository testAssignmentRepository;

    @Autowired
    private CandidatAnswerRepository candidatAnswerRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private TimelineService timelineService;


    public List<TestAssignmentDto> getAllTestAssignments() {
        List<TestAssignment> assignments = testAssignmentRepository.findAllWithDetails();

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public TestAssignmentResponse sendTestToCandidat(SendTestRequest request) {
        // Validation du candidat
        Candidat candidat = candidatRepository.findById(request.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        // Validation du test
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new RuntimeException("Test non trouvé"));

        // Vérifier si le candidat a déjà ce test en cours
        List<String> statusInProgress = List.of("SENT", "STARTED");
        Optional<TestAssignment> existingAssignment = testAssignmentRepository
                .findByCandidatAndTestAndStatusIn(candidat, test, statusInProgress);

        if (existingAssignment.isPresent()) {
            throw new RuntimeException("Ce candidat a déjà ce test en cours");
        }

        // Générer un token d'accès unique
        String accessToken = UUID.randomUUID().toString();

        // Calculer le score total possible
        Integer totalPossibleScore = calculateTotalPossibleScore(test);

        // Créer l'assignation
        TestAssignment assignment = TestAssignment.builder()
                .candidat(candidat)
                .test(test)
                .status("SENT")
                .accessToken(accessToken)
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(test.getTotalDuration()))
                .totalPossibleScore(totalPossibleScore)
                .build();

        assignment = testAssignmentRepository.save(assignment);

        // Envoyer l'email
        sendTestEmail(candidat, test, assignment, request.getMessage());

        // Construire la réponse
        return TestAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .candidatName(candidat.getNom())
                .candidatEmail(candidat.getEmail())
                .testTitle(test.getName())
                .status(assignment.getStatus())
                .accessToken(assignment.getAccessToken())
                .sentAt(assignment.getSentAt())
                .expiresAt(assignment.getExpiresAt())
                .testUrl(generateTestUrl(accessToken))
                .build();
    }

    @Transactional
    public TestResultResponse submitTest(SubmitTestRequest request) {
        // Récupérer l'assignation
        TestAssignment assignment = testAssignmentRepository.findByAccessToken(request.getAccessToken())
                .orElseThrow(() -> new RuntimeException("Token d'accès invalide"));

        // Vérifier que le test peut être soumis
        boolean canSubmit = "STARTED".equals(assignment.getStatus()) || "SENT".equals(assignment.getStatus());
        if (!canSubmit) {
            throw new RuntimeException("Ce test ne peut plus être soumis");
        }

        // Vérifier l'expiration
        if (assignment.getExpiresAt().isBefore(LocalDateTime.now())) {
            assignment.setStatus("EXPIRED");
            testAssignmentRepository.save(assignment);
            throw new RuntimeException("Le test a expiré");
        }

        // Traiter les réponses et calculer le score
        List<CandidatAnswer> candidatAnswers = new ArrayList<>();
        List<TestResultResponse.QuestionResultDto> questionResults = new ArrayList<>();

        int totalScore = 0;

        for (SubmitTestRequest.QuestionAnswerDto answerDto : request.getAnswers()) {
            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question non trouvée: " + answerDto.getQuestionId()));

            // Vérifier si la réponse est correcte
            boolean isCorrect = question.getCorrectAnswer().equals(answerDto.getSelectedAnswer());
            int pointsEarned = isCorrect ? question.getPoints() : 0;
            totalScore += pointsEarned;

            // Créer la réponse du candidat
            CandidatAnswer candidatAnswer = CandidatAnswer.builder()
                    .testAssignment(assignment)
                    .question(question)
                    .selectedAnswer(answerDto.getSelectedAnswer())
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .pointsPossible(question.getPoints())
                    .answeredAt(LocalDateTime.now())
                    .build();

            candidatAnswers.add(candidatAnswer);

            // Créer le résultat détaillé de la question
            List<String> answerOptions = question.getAnswers().stream()
                    .sorted(Comparator.comparing(Answer::getAnswerIndex))
                    .map(Answer::getAnswerText)
                    .collect(Collectors.toList());

            String selectedAnswerText = getAnswerTextByIndex(answerOptions, answerDto.getSelectedAnswer());
            String correctAnswerText = getAnswerTextByIndex(answerOptions, question.getCorrectAnswer());

            TestResultResponse.QuestionResultDto questionResult = TestResultResponse.QuestionResultDto.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .selectedAnswer(answerDto.getSelectedAnswer())
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .pointsPossible(question.getPoints())
                    .answerOptions(answerOptions)
                    .selectedAnswerText(selectedAnswerText)
                    .correctAnswerText(correctAnswerText)
                    .build();

            questionResults.add(questionResult);
        }

        // Sauvegarder toutes les réponses
        candidatAnswerRepository.saveAll(candidatAnswers);

        // Calculer le pourcentage
        double percentage = assignment.getTotalPossibleScore() > 0
                ? (double) totalScore / assignment.getTotalPossibleScore() * 100
                : 0.0;

        // Mettre à jour l'assignation
        assignment.setStatus("COMPLETED");
        assignment.setCompletedAt(LocalDateTime.now());
        assignment.setScore(totalScore);
        assignment.setPercentage(percentage);

        testAssignmentRepository.save(assignment);
        Candidat candidat = assignment.getCandidat();
        if (assignment.getPercentage()>50) {
            candidat.setProcessStatus("ACCEPTED");
        }
        else {
            candidat.setProcessStatus("REJECTED");
        }
        candidatRepository.save(candidat);


        // Construire la réponse
        return TestResultResponse.builder()
                .assignmentId(assignment.getId())
                .candidatName(assignment.getCandidat().getNom())
                .testTitle(assignment.getTest().getName())
                .totalScore(totalScore)
                .totalPossibleScore(assignment.getTotalPossibleScore())
                .percentage(percentage)
                .status(assignment.getStatus())
                .completedAt(assignment.getCompletedAt())
                .questionResults(questionResults)
                .build();
    }

    public TestResultResponse getTestResults(Long assignmentId) {
        TestAssignment assignment = testAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignation non trouvée"));

        if (!"COMPLETED".equals(assignment.getStatus())) {
            throw new RuntimeException("Le test n'est pas encore terminé");
        }

        List<TestResultResponse.QuestionResultDto> questionResults = assignment.getCandidatAnswers().stream()
                .map(this::mapToQuestionResult)
                .collect(Collectors.toList());

        return TestResultResponse.builder()
                .assignmentId(assignment.getId())
                .candidatName(assignment.getCandidat().getNom())
                .testTitle(assignment.getTest().getName())
                .totalScore(assignment.getScore())
                .totalPossibleScore(assignment.getTotalPossibleScore())
                .percentage(assignment.getPercentage())
                .status(assignment.getStatus())
                .completedAt(assignment.getCompletedAt())
                .questionResults(questionResults)
                .build();
    }

    private TestResultResponse.QuestionResultDto mapToQuestionResult(CandidatAnswer candidatAnswer) {
        Question question = candidatAnswer.getQuestion();

        List<String> answerOptions = question.getAnswers().stream()
                .sorted(Comparator.comparing(Answer::getAnswerIndex))
                .map(Answer::getAnswerText)
                .collect(Collectors.toList());

        String selectedAnswerText = getAnswerTextByIndex(answerOptions, candidatAnswer.getSelectedAnswer());
        String correctAnswerText = getAnswerTextByIndex(answerOptions, question.getCorrectAnswer());

        return TestResultResponse.QuestionResultDto.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .selectedAnswer(candidatAnswer.getSelectedAnswer())
                .correctAnswer(question.getCorrectAnswer())
                .isCorrect(candidatAnswer.getIsCorrect())
                .pointsEarned(candidatAnswer.getPointsEarned())
                .pointsPossible(candidatAnswer.getPointsPossible())
                .answerOptions(answerOptions)
                .selectedAnswerText(selectedAnswerText)
                .correctAnswerText(correctAnswerText)
                .build();
    }

    // Méthode utilitaire pour éviter IndexOutOfBoundsException
    private String getAnswerTextByIndex(List<String> answerOptions, Integer index) {
        if (index != null && index >= 0 && index < answerOptions.size()) {
            return answerOptions.get(index);
        }
        return "Réponse invalide";
    }

    private Integer calculateTotalPossibleScore(Test test) {
        return test.getExercises().stream()
                .flatMap(exercise -> exercise.getQuestions().stream())
                .mapToInt(Question::getPoints)
                .sum();
    }

    private void sendTestEmail(Candidat candidat, Test test,
                               TestAssignment assignment, String customMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(candidat.getEmail());
            message.setSubject("Test technique - " + test.getName());

            String emailBody = buildEmailBody(candidat, test, assignment, customMessage);
            message.setText(emailBody);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    private String buildEmailBody(Candidat candidat, Test test,
                                  TestAssignment assignment, String customMessage) {
        StringBuilder body = new StringBuilder();

        body.append("Bonjour ").append(candidat.getNom()).append(",\n\n");

        if (customMessage != null && !customMessage.trim().isEmpty()) {
            body.append(customMessage).append("\n\n");
        } else {
            body.append("Nous vous invitons à passer le test technique suivant :\n\n");
        }

        body.append("Test: ").append(test.getName()).append("\n");
        body.append("Durée estimée: ").append(test.getTotalDuration()).append(" minutes\n");
        body.append("Date limite: ").append(assignment.getExpiresAt().toLocalDate()).append("\n\n");

        body.append("Pour accéder au test, cliquez sur le lien suivant:\n");
        body.append(generateTestUrl(assignment.getAccessToken())).append("\n\n");

        body.append("Instructions importantes:\n");
        body.append("- Le test doit être complété en une seule session\n");
        body.append("- Assurez-vous d'avoir une connexion internet stable\n");
        body.append("- Le temps est limité, préparez-vous avant de commencer\n\n");

        body.append("Bonne chance!\n\n");
        body.append("L'équipe RH");

        return body.toString();
    }

    private String generateTestUrl(String accessToken) {
        return "https://hire.nexotek.ma/qcm-tests/" + accessToken;
    }

    public List<TestAssignment> getAssignmentsByCandidat(Long candidatId) {
        return testAssignmentRepository.findByCandidatId(candidatId);
    }

    public List<TestAssignment> getPendingAssignments() {
        List<String> pendingStatuses = List.of("SENT", "STARTED");
        return testAssignmentRepository.findByStatusIn(pendingStatuses);
    }

    public TestAssignmentDto getAssignmentByToken(String accessToken) {
        TestAssignment assignment = testAssignmentRepository.findByAccessTokenWithTest(accessToken)
                .orElseThrow(() -> new RuntimeException("Token d'accès invalide"));

        return convertToDto(assignment);
    }


    public void markTestAsStarted(String accessToken) {
        TestAssignment assignment = getAssignmentEntityByToken(accessToken);
        if (assignment.getExpiresAt().isBefore(LocalDateTime.now())) {
            assignment.setStatus("EXPIRED");
            testAssignmentRepository.save(assignment);
            throw new RuntimeException("Le test a expiré");
        }
        if ("SENT".equals(assignment.getStatus())) {
            assignment.setStatus("STARTED");
            assignment.setStartedAt(LocalDateTime.now());
            testAssignmentRepository.save(assignment);
        }
        else {
            throw new RuntimeException("Le test est déjà passé");
        }
    }
    private TestAssignment getAssignmentEntityByToken(String accessToken) {
        return testAssignmentRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new RuntimeException("Test assignment not found for token: " + accessToken));
    }

    public void markExpiredTests() {
        List<TestAssignment> expiredAssignments = testAssignmentRepository
                .findExpiredAssignments(LocalDateTime.now());

        for (TestAssignment assignment : expiredAssignments) {
            assignment.setStatus("EXPIRED");
            testAssignmentRepository.save(assignment);
        }
    }
    private TestAssignmentDto convertToDto(TestAssignment assignment) {
        TestAssignmentDto dto = new TestAssignmentDto();
        dto.setAssignmentId(assignment.getId());
        dto.setStatus(assignment.getStatus());
        dto.setAccessToken(assignment.getAccessToken());
        dto.setSentAt(assignment.getSentAt());
        dto.setStartedAt(assignment.getStartedAt());
        dto.setExpiresAt(assignment.getExpiresAt());

        // Conversion du candidat en DTO (vous devrez adapter selon votre CandidatDTO)
        if (assignment.getCandidat() != null) {
            CandidatDTO candidatDto = new CandidatDTO(assignment.getCandidat());
            dto.setCandidat(candidatDto);
        }
        if (assignment.getTest() != null) {
            dto.setTest(assignment.getTest());
        }




        return dto;
    }

    // Dans TestAssignmentService.java - AJOUTER ces méthodes :


    public Test selectRandomTest(List<Test> allTests) {
        if (allTests == null || allTests.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(allTests.size());
        return allTests.get(randomIndex);
    }

    public List<Test> getTestsByCategorie(String categorie) {
        return testRepository.findByCategorie(categorie);
    }

    public TestAssignmentResponse sendRandomTestToCandidat(SendRandomTestRequest request) {
        // 1. Validation du candidat
        Candidat candidat = candidatRepository.findById(request.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        // 2. Validation de l'offre
        Form offre = formRepository.findById(request.getOffreId())
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // 3. Récupérer les tests de cette catégorie
        List<Test> availableTests = getTestsByCategorie(offre.getCategorie());

        if (availableTests.isEmpty()) {
            throw new RuntimeException("Aucun test disponible pour la catégorie: " + offre.getCategorie());
        }

        // 4. Sélectionner un test aléatoirement
        Test selectedTest = selectRandomTest(availableTests);

        // 5. Vérifier si le candidat a déjà ce test en cours
        List<String> statusInProgress = List.of("SENT", "STARTED");
        Optional<TestAssignment> existingAssignment = testAssignmentRepository
                .findByCandidatAndTestAndStatusIn(candidat, selectedTest, statusInProgress);

        if (existingAssignment.isPresent()) {
            throw new RuntimeException("Ce candidat a déjà ce test en cours");
        }

        // 6. Générer un token d'accès unique
        String accessToken = UUID.randomUUID().toString();

        // 7. Calculer le score total possible
        Integer totalPossibleScore = calculateTotalPossibleScore(selectedTest);

        // 8. Créer l'assignation
        TestAssignment assignment = TestAssignment.builder()
                .candidat(candidat)
                .test(selectedTest)
                .status("SENT")
                .accessToken(accessToken)
                .sentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(selectedTest.getTotalDuration()))
                .totalPossibleScore(totalPossibleScore)
                .build();

        assignment = testAssignmentRepository.save(assignment);

        // 9. Envoyer l'email
        sendTestEmail(candidat, selectedTest, assignment, request.getMessage());
        timelineService.addEmailSent(candidat);
        // 10. Construire la réponse
        return TestAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .candidatName(candidat.getNom())
                .candidatEmail(candidat.getEmail())
                .testTitle(selectedTest.getName())
                .status(assignment.getStatus())
                .accessToken(assignment.getAccessToken())
                .sentAt(assignment.getSentAt())
                .expiresAt(assignment.getExpiresAt())
                .testUrl(generateTestUrl(accessToken))
                .build();
    }
    /**
     * Envoie un email de rejet automatique au candidat
     */
    public void sendRejectionEmail(Candidat candidat, Double cvScore, Long offreId) {
        try {
            // Récupérer les informations de l'offre
            Form offre = formRepository.findById(offreId)
                    .orElse(null);

            String offreTitle = offre != null ? offre.getTitle() : "l'offre";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(candidat.getEmail());
            message.setSubject("Candidature pour " + offreTitle);

            String emailBody = buildRejectionEmailMessage(candidat, offreTitle, cvScore);
            message.setText(emailBody);
            mailSender.send(message);
            System.out.println("Email de rejet envoyé à " + candidat.getNom() +
                    " (" + candidat.getEmail() + ")");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de rejet: " + e.getMessage());
            // Ne pas bloquer le processus si l'email échoue
        }
    }

    /**
     * Construit le message d'email de rejet
     */
    private String buildRejectionEmailMessage(Candidat candidat, String offreTitle, Double cvScore) {
        return String.format("""
        Bonjour %s,
        
        Nous vous remercions pour votre candidature pour le poste de %s.
        
        Après examen de votre profil, nous regrettons de vous informer que 
        votre candidature ne correspond pas entièrement aux critères recherchés 
        pour ce poste à ce moment.
        
        Nous vous encourageons à consulter nos autres offres d'emploi qui 
        pourraient mieux correspondre à votre profil sur notre site web.
        
        Nous vous souhaitons bonne chance dans vos recherches professionnelles.
        
        Cordialement,
        L'équipe RH
        
        ---
        Cet email a été envoyé automatiquement. Merci de ne pas y répondre.
        """,
                candidat.getNom(),
                offreTitle
        );
    }

    // Add this method to your TestAssignmentService.java

    public Map<String, Integer> getWeeklyTestStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        // Get test assignments sent this week
        List<TestAssignment> thisWeekAssignments = testAssignmentRepository
                .findBySentAtBetween(oneWeekAgo, now);

        // Get test assignments sent last week for comparison
        List<TestAssignment> lastWeekAssignments = testAssignmentRepository
                .findBySentAtBetween(twoWeeksAgo, oneWeekAgo);

        int thisWeekCount = thisWeekAssignments.size();
        int lastWeekCount = lastWeekAssignments.size();

        // Calculate percentage change
        int percentageChange = 0;
        if (lastWeekCount > 0) {
            percentageChange = Math.round(((float)(thisWeekCount - lastWeekCount) / lastWeekCount) * 100);
        } else if (thisWeekCount > 0) {
            percentageChange = 100; // If no tests last week but some this week, it's 100% increase
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("thisWeekSent", thisWeekCount);
        stats.put("lastWeekSent", lastWeekCount);
        stats.put("percentageChange", percentageChange);

        return stats;
    }
}
