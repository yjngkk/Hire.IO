package ma.nexotek.HireCraft.service.ai;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.model.CVScoringResult;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.DecisionType;
import ma.nexotek.HireCraft.model.Form;

@Service
@Slf4j
public class CVScoringService {

    @Autowired
    private CVTextExtractionService cvTextExtractionService;

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${cv.scoring.thresholds.excellent:70}")
    private double excellentThreshold;

    @Value("${cv.scoring.thresholds.good:55}")
    private double goodThreshold;

    @Value("${cv.scoring.thresholds.review:35}")
    private double reviewThreshold;

    /**
     * RÉVOLUTION CV SCORING : Approche multi-perspectives intelligente
     * Cette méthode révolutionne le scoring en analysant le CV sous 5 angles différents
     */
    public CVScoringResult scoreCV(Candidat candidat, Form form) {
        long startTime = System.currentTimeMillis();
        
        log.info("RÉVOLUTION CV SCORING - Candidat: {} | Poste: {}", candidat.getNom(), form.getTitle());

        try {
            String cvText = cvTextExtractionService.extractCVText(candidat);
            String jobText = buildIntelligentJobText(form);
            
            log.info("Analyse démarrée: CV {} chars | Job {} chars", cvText.length(), jobText.length());

            // RÉVOLUTION : 5 perspectives d'analyse indépendantes
            MultiPerspectiveResult analysis = analyzeFromMultiplePerspectives(cvText, jobText, form);

            DecisionType decision = determineSmartDecision(analysis);
            
            CVScoringResult result = buildRevolutionaryResult(candidat, form, analysis, decision, startTime);

            candidat.setCvScore(analysis.finalScore);
            candidat.setProcessStatus(decision.toProcessStatus());

            log.info("RÉVOLUTION TERMINÉE: Final={:.1f}% | Perspectives=[{:.1f}%,{:.1f}%,{:.1f}%,{:.1f}%,{:.1f}%] | Décision={}", 
                    analysis.finalScore, analysis.globalSimilarity, analysis.skillsAlignment, 
                    analysis.experienceRelevance, analysis.educationFit, analysis.contextualMatch, 
                    decision.name());

            return result;

        } catch (Exception e) {
            log.error("Erreur analyse révolutionnaire candidat {}: {}", candidat.getId(), e.getMessage(), e);
            return buildEmergencyResult(candidat, form, e, startTime);
        }
    }

    /**
     * CŒUR DE LA RÉVOLUTION : Analyse multi-perspectives
     * Chaque perspective apporte un éclairage unique sur l'adéquation du candidat
     */
    private MultiPerspectiveResult analyzeFromMultiplePerspectives(String cvText, String jobText, Form form) {
        
        // PERSPECTIVE 1: Similarité globale (vision holistique)
        double globalSimilarity = embeddingService.scoreCvToJob(cvText, jobText);
        log.debug("Perspective Globale: {:.1f}%", globalSimilarity);
        
        // PERSPECTIVE 2: Alignement des compétences (vision technique)
        double skillsAlignment = analyzeSkillsAlignment(cvText, form.getSkills(), jobText);
        log.debug("Perspective Compétences: {:.1f}%", skillsAlignment);
        
        // PERSPECTIVE 3: Pertinence de l'expérience (vision professionnelle)
        double experienceRelevance = analyzeExperienceRelevance(cvText, jobText);
        log.debug("Perspective Expérience: {:.1f}%", experienceRelevance);
        
        // PERSPECTIVE 4: Adéquation formation (vision académique)
        double educationFit = analyzeEducationFit(cvText, jobText, form.getLevel());
        log.debug("Perspective Formation: {:.1f}%", educationFit);
        
        // PERSPECTIVE 5: Correspondance contextuelle (vision sectorielle)
        double contextualMatch = analyzeContextualMatch(cvText, jobText, form.getTitle());
        log.debug("Perspective Contextuelle: {:.1f}%", contextualMatch);
        
        // FUSION INTELLIGENTE DES PERSPECTIVES
        double finalScore = fusePerspecтives(globalSimilarity, skillsAlignment, experienceRelevance, 
                                           educationFit, contextualMatch);
        
        // ANALYSE DE COHÉRENCE
        double confidence = calculatePerspectiveCoherence(globalSimilarity, skillsAlignment, 
                                                        experienceRelevance, educationFit, contextualMatch);
        
        return new MultiPerspectiveResult(finalScore, globalSimilarity, skillsAlignment, 
                                        experienceRelevance, educationFit, contextualMatch, 
                                        confidence, generateInsightfulAnalysis(globalSimilarity, skillsAlignment, 
                                        experienceRelevance, educationFit, contextualMatch));
    }

    /**
     * PERSPECTIVE 2: Alignement des compétences avec intelligence adaptative
     */
    private double analyzeSkillsAlignment(String cvText, String requiredSkills, String jobText) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            // Pas de compétences spécifiques ? Analyser la compatibilité technique générale
            return embeddingService.scoreCvToJob(cvText, 
                "technical skills programming development expertise professional competencies");
        }
        
        String[] skills = requiredSkills.split("[,;\\n]");
        double totalAlignment = 0.0;
        int validSkills = 0;
        List<String> detectedSkills = new ArrayList<>();
        
        for (String skill : skills) {
            skill = skill.trim();
            if (skill.length() < 2) continue;
            
            double skillScore = evaluateSkillWithContext(cvText, skill, jobText);
            
            if (skillScore > 30.0) {
                detectedSkills.add(skill + "(" + Math.round(skillScore) + "%)");
            }
            
            totalAlignment += skillScore;
            validSkills++;
        }
        
        double baseScore = validSkills > 0 ? totalAlignment / validSkills : 50.0;
        
        // Bonus pour couverture complète des compétences
        if (detectedSkills.size() >= validSkills * 0.75) {
            baseScore += 15.0; // Bonus si 75%+ des compétences détectées
        }
        
        log.debug("Compétences détectées: {}", detectedSkills.isEmpty() ? "analyse générale" : 
                 String.join(", ", detectedSkills));
        
        return Math.min(100.0, baseScore);
    }

    /**
     * Évaluation contextuelle d'une compétence
     */
    private double evaluateSkillWithContext(String cvText, String skill, String jobContext) {
        // Approche 1: Recherche directe dans le CV
        double directScore = cvText.toLowerCase().contains(skill.toLowerCase()) ? 80.0 : 0.0;
        
        // Approche 2: Analyse sémantique de la compétence dans le contexte job
        String skillInContext = skill + " " + extractRelevantJobContext(jobContext, skill);
        double semanticScore = embeddingService.scoreCvToJob(cvText, skillInContext);
        
        // Approche 3: Analyse de la compétence seule
        double isolatedScore = embeddingService.scoreCvToJob(cvText, skill);
        
        return Math.max(directScore, Math.max(semanticScore, isolatedScore));
    }

    /**
     * PERSPECTIVE 3: Pertinence de l'expérience avec analyse temporelle
     */
    private double analyzeExperienceRelevance(String cvText, String jobText) {
        // Extraire les contextes d'expérience
        String cvExperienceContext = extractExperienceContext(cvText);
        String jobExperienceContext = extractExperienceContext(jobText);
        
        if (cvExperienceContext.isEmpty() || jobExperienceContext.isEmpty()) {
            // Analyse générale si pas de contexte spécifique
            return embeddingService.scoreCvToJob(cvText, "professional experience work background career");
        }
        
        // Score de correspondance des expériences
        double experienceMatch = embeddingService.scoreCvToJob(cvExperienceContext, jobExperienceContext);
        
        // Bonus pour ancienneté détectée
        double seniorityBonus = analyzeSeniority(cvText, jobText);
        
        return Math.min(100.0, experienceMatch + seniorityBonus);
    }

    /**
     * PERSPECTIVE 4: Adéquation formation avec niveau requis
     */
    private double analyzeEducationFit(String cvText, String jobText, String requiredLevel) {
        String cvEducationContext = extractEducationContext(cvText);
        String jobEducationContext = extractEducationContext(jobText);
        
        double educationMatch = 50.0; // Base neutre
        
        if (!cvEducationContext.isEmpty()) {
            if (!jobEducationContext.isEmpty()) {
                educationMatch = embeddingService.scoreCvToJob(cvEducationContext, jobEducationContext);
            } else {
                // Pas d'exigence éducation spécifique dans le job
                educationMatch = 70.0;
            }
        }
        
        // Ajustement selon niveau requis
        if (requiredLevel != null) {
            double levelBonus = analyzeLevelCompatibility(cvEducationContext, requiredLevel);
            educationMatch = Math.min(100.0, educationMatch + levelBonus);
        }
        
        return educationMatch;
    }

    /**
     * PERSPECTIVE 5: Correspondance contextuelle sectorielle
     */
    private double analyzeContextualMatch(String cvText, String jobText, String jobTitle) {
        // Analyser l'adéquation avec le secteur/domaine d'activité
        String cvDomainContext = extractDomainContext(cvText);
        String jobDomainContext = extractDomainContext(jobText);
        
        double domainMatch = embeddingService.scoreCvToJob(cvDomainContext.isEmpty() ? cvText : cvDomainContext, 
                                                         jobDomainContext.isEmpty() ? jobText : jobDomainContext);
        
        // Bonus pour correspondance titre/profil
        if (jobTitle != null) {
            double titleMatch = embeddingService.scoreCvToJob(cvText, jobTitle);
            domainMatch = Math.max(domainMatch, titleMatch);
        }
        
        return domainMatch;
    }

    /**
     * FUSION INTELLIGENTE : Combinaison pondérée adaptative des perspectives
     */
    private double fusePerspecтives(double global, double skills, double experience, 
                                   double education, double contextual) {
        
        // Pondération adaptative selon la qualité de chaque perspective
        Map<String, Double> weights = calculateAdaptiveWeights(global, skills, experience, education, contextual);
        
        double fusedScore = global * weights.get("global") +
                           skills * weights.get("skills") +
                           experience * weights.get("experience") +
                           education * weights.get("education") +
                           contextual * weights.get("contextual");
        
        // Normalisation finale
        return Math.max(0.0, Math.min(100.0, fusedScore));
    }

    /**
     * Calcul des poids adaptatifs selon la fiabilité de chaque perspective
     */
    private Map<String, Double> calculateAdaptiveWeights(double global, double skills, 
                                                        double experience, double education, double contextual) {
        Map<String, Double> weights = new HashMap<>();
        
        // Poids de base
        weights.put("global", 0.25);
        weights.put("skills", 0.35);    // Plus importante pour le matching technique
        weights.put("experience", 0.20);
        weights.put("education", 0.10);
        weights.put("contextual", 0.10);
        
        // Ajustement selon cohérence des scores
        double[] scores = {global, skills, experience, education, contextual};
        double variance = calculateVariance(scores);
        
        if (variance < 100) { // Scores cohérents
            // Donner plus de poids aux compétences si elles sont cohérentes
            weights.put("skills", 0.40);
            weights.put("global", 0.20);
        } else { // Scores divergents
            // Se rabattre sur la vue globale
            weights.put("global", 0.35);
            weights.put("skills", 0.30);
        }
        
        return weights;
    }

    /**
     * ANALYSE DE COHÉRENCE : Mesurer la fiabilité du scoring
     */
    private double calculatePerspectiveCoherence(double... perspectives) {
        double variance = calculateVariance(perspectives);
        double mean = Arrays.stream(perspectives).average().orElse(50.0);
        
        // Confiance élevée si faible variance et score raisonnable
        double baseConfidence = 90.0;
        
        if (variance > 400) { // Très divergent
            baseConfidence -= 20.0;
        } else if (variance > 200) { // Modérément divergent
            baseConfidence -= 10.0;
        }
        
        // Bonus si score dans gamme raisonnable (pas d'extrêmes)
        if (mean > 20 && mean < 85) {
            baseConfidence += 5.0;
        }
        
        return Math.max(60.0, Math.min(98.0, baseConfidence));
    }

    // MÉTHODES UTILITAIRES INTELLIGENTES

    private String extractExperienceContext(String text) {
        String[] experienceMarkers = {"experience", "expérience", "worked", "travaillé", "employment", "emploi", 
                                    "career", "carrière", "professional", "professionnel"};
        return extractContextByMarkers(text, experienceMarkers, 300);
    }

    private String extractEducationContext(String text) {
        String[] educationMarkers = {"education", "formation", "degree", "diplôme", "university", "université", 
                                   "school", "école", "bachelor", "master", "phd"};
        return extractContextByMarkers(text, educationMarkers, 200);
    }

    private String extractDomainContext(String text) {
        String[] domainMarkers = {"industry", "industrie", "sector", "secteur", "domain", "domaine", 
                                "field", "domain", "specialization", "spécialisation"};
        return extractContextByMarkers(text, domainMarkers, 250);
    }

    private String extractContextByMarkers(String text, String[] markers, int contextSize) {
        String lowerText = text.toLowerCase();
        StringBuilder context = new StringBuilder();
        
        for (String marker : markers) {
            int index = lowerText.indexOf(marker);
            if (index != -1) {
                int start = Math.max(0, index - 50);
                int end = Math.min(text.length(), index + contextSize);
                context.append(text.substring(start, end)).append(" ");
            }
        }
        
        return context.toString().trim();
    }

    private String extractRelevantJobContext(String jobText, String skill) {
        // Extraire le contexte autour de la mention de la compétence dans le job
        String lowerJob = jobText.toLowerCase();
        String lowerSkill = skill.toLowerCase();
        
        int skillIndex = lowerJob.indexOf(lowerSkill);
        if (skillIndex != -1) {
            int start = Math.max(0, skillIndex - 100);
            int end = Math.min(jobText.length(), skillIndex + 100);
            return jobText.substring(start, end);
        }
        
        return "";
    }

    private double analyzeSeniority(String cvText, String jobText) {
        // Analyser les indicateurs d'ancienneté/niveau
        String[] seniorityIndicators = {"senior", "lead", "principal", "manager", "director", 
                                       "expert", "specialist", "architect", "years", "ans"};
        
        int cvSeniorityScore = 0;
        int jobSeniorityScore = 0;
        
        String cvLower = cvText.toLowerCase();
        String jobLower = jobText.toLowerCase();
        
        for (String indicator : seniorityIndicators) {
            if (cvLower.contains(indicator)) cvSeniorityScore++;
            if (jobLower.contains(indicator)) jobSeniorityScore++;
        }
        
        return Math.min(10.0, cvSeniorityScore * 2.0); // Bonus max 10 points
    }

    private double analyzeLevelCompatibility(String cvEducation, String requiredLevel) {
        if (requiredLevel == null || cvEducation.isEmpty()) return 0.0;
        
        String requiredLower = requiredLevel.toLowerCase();
        String cvLower = cvEducation.toLowerCase();
        
        // Bonus selon correspondance niveau
        if (requiredLower.contains("débutant") || requiredLower.contains("junior")) {
            return 5.0; // Accessible à tous
        } else if (requiredLower.contains("confirmé") || requiredLower.contains("intermediate")) {
            return cvLower.contains("bachelor") || cvLower.contains("master") ? 10.0 : 0.0;
        } else if (requiredLower.contains("senior") || requiredLower.contains("expert")) {
            return cvLower.contains("master") || cvLower.contains("phd") ? 15.0 : 5.0;
        }
        
        return 0.0;
    }

    private double calculateVariance(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0.0);
        return Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
    }

    private String buildIntelligentJobText(Form form) {
        StringBuilder intelligent = new StringBuilder();
        
        // Prioriser le contenu généré (description complète)
        if (form.getGeneratedContent() != null && !form.getGeneratedContent().trim().isEmpty()) {
            intelligent.append(form.getGeneratedContent()).append(" ");
        }
        
        // Renforcer avec les éléments critiques
        if (form.getTitle() != null) {
            intelligent.append("Poste: ").append(form.getTitle()).append(". ");
        }
        
        if (form.getSkills() != null && !form.getSkills().trim().isEmpty()) {
            intelligent.append("Compétences clés requises: ").append(form.getSkills()).append(". ");
        }
        
        if (form.getLevel() != null) {
            intelligent.append("Niveau souhaité: ").append(form.getLevel()).append(". ");
        }
        
        return intelligent.toString().trim();
    }

    private String generateInsightfulAnalysis(double global, double skills, double experience, 
                                            double education, double contextual) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append(String.format("Analyse multi-perspectives: Global %.1f%%, Compétences %.1f%%, " +
                                     "Expérience %.1f%%, Formation %.1f%%, Contextuel %.1f%%. ", 
                                     global, skills, experience, education, contextual));
        
        // Identifier la perspective dominante
        double[] scores = {global, skills, experience, education, contextual};
        String[] names = {"Vue globale", "Compétences", "Expérience", "Formation", "Contexte"};
        
        int maxIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIndex]) maxIndex = i;
        }
        
        analysis.append(String.format("Point fort: %s (%.1f%%).", names[maxIndex], scores[maxIndex]));
        
        return analysis.toString();
    }

    private DecisionType determineSmartDecision(MultiPerspectiveResult analysis) {
        double score = analysis.finalScore;
        
        // Décision adaptative selon confiance
        if (analysis.confidence > 85.0) {
            // Confiance élevée - utiliser seuils standards
            if (score >= excellentThreshold) return DecisionType.ACCEPT;
            if (score >= goodThreshold) return DecisionType.INTERVIEW;
            if (score >= reviewThreshold) return DecisionType.REVIEW;
            return DecisionType.REJECT;
        } else {
            // Confiance modérée - être plus conservateur
            if (score >= excellentThreshold + 5) return DecisionType.ACCEPT;
            if (score >= goodThreshold) return DecisionType.INTERVIEW;
            if (score >= reviewThreshold - 5) return DecisionType.REVIEW;
            return DecisionType.REJECT;
        }
    }

    private CVScoringResult buildRevolutionaryResult(Candidat candidat, Form form, 
                                                   MultiPerspectiveResult analysis, DecisionType decision, 
                                                   long startTime) {
        return CVScoringResult.builder()
                .candidatId(candidat.getId())
                .formId(form.getId())
                .finalScore(Math.round(analysis.finalScore * 100.0) / 100.0)
                .semanticScore(Math.round(analysis.globalSimilarity * 100.0) / 100.0)
                .skillsScore(Math.round(analysis.skillsAlignment * 100.0) / 100.0)
                .experienceScore(Math.round(analysis.experienceRelevance * 100.0) / 100.0)
                .aiDecision(decision)
                .confidenceLevel(analysis.confidence)
                .scoringDate(LocalDateTime.now())
                .previousStatus(candidat.getProcessStatus())
                .newStatus(decision.toProcessStatus())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .modelVersion("Revolutionary-MultiPerspective-v4.0")
                .aiAnalysis(analysis.insightfulAnalysis)
                .matchedSkills("Analyse multi-perspectives complétée")
                .missingSkills("")
                .recommendations(generateRevolutionaryRecommendations(analysis, decision))
                .build();
    }

    private String generateRevolutionaryRecommendations(MultiPerspectiveResult analysis, DecisionType decision) {
        StringBuilder recommendations = new StringBuilder();
        
        if (analysis.finalScore >= 75) {
            recommendations.append("Candidat hautement recommandé - Excellence détectée sur plusieurs perspectives");
        } else if (analysis.finalScore >= 60) {
            recommendations.append("Profil très prometteur - Correspondance forte identifiée");
        } else if (analysis.finalScore >= 45) {
            recommendations.append("Candidat intéressant - Potentiel identifié, évaluation approfondie recommandée");
        } else {
            recommendations.append("Correspondance limitée - Considérer selon flexibilité des critères");
        }
        
        // Ajouter insight basé sur la perspective dominante
        if (analysis.skillsAlignment > analysis.globalSimilarity + 10) {
            recommendations.append(" (Fort alignement technique détecté)");
        } else if (analysis.experienceRelevance > analysis.skillsAlignment + 10) {
            recommendations.append(" (Expérience particulièrement pertinente)");
        }
        
        return recommendations.toString();
    }

    private CVScoringResult buildEmergencyResult(Candidat candidat, Form form, Exception error, long startTime) {
        return CVScoringResult.builder()
                .candidatId(candidat.getId())
                .formId(form.getId())
                .finalScore(50.0)
                .semanticScore(50.0)
                .skillsScore(50.0)
                .experienceScore(50.0)
                .aiDecision(DecisionType.REVIEW)
                .confidenceLevel(40.0)
                .scoringDate(LocalDateTime.now())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .modelVersion("Emergency-Recovery-v4.0")
                .aiAnalysis("Analyse d'urgence - Révision manuelle prioritaire: " + error.getMessage())
                .recommendations("Évaluation manuelle immédiate recommandée")
                .build();
    }

    // CLASSE DE DONNÉES POUR LES RÉSULTATS MULTI-PERSPECTIVES
    private static class MultiPerspectiveResult {
        final double finalScore;
        final double globalSimilarity;
        final double skillsAlignment;
        final double experienceRelevance;
        final double educationFit;
        final double contextualMatch;
        final double confidence;
        final String insightfulAnalysis;
        
        MultiPerspectiveResult(double finalScore, double globalSimilarity, double skillsAlignment,
                             double experienceRelevance, double educationFit, double contextualMatch,
                             double confidence, String insightfulAnalysis) {
            this.finalScore = finalScore;
            this.globalSimilarity = globalSimilarity;
            this.skillsAlignment = skillsAlignment;
            this.experienceRelevance = experienceRelevance;
            this.educationFit = educationFit;
            this.contextualMatch = contextualMatch;
            this.confidence = confidence;
            this.insightfulAnalysis = insightfulAnalysis;
        }
    }
}