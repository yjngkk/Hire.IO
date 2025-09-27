package ma.nexotek.HireCraft.service.Onboarding;

import ma.nexotek.HireCraft.model.CandidateFileTemplate;
import ma.nexotek.HireCraft.model.ProcedureTemplate;
import ma.nexotek.HireCraft.repository.CandidateFileTemplateRepository;
import ma.nexotek.HireCraft.repository.ProcedureTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private ProcedureTemplateRepository procedureTemplateRepository;

    @Autowired
    private CandidateFileTemplateRepository candidateFileTemplateRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize procedure templates if database is empty
        if (procedureTemplateRepository.countActiveTemplates() == 0) {
            initializeDefaultProcedureTemplates();
        }

        // Initialize candidate file templates if database is empty
        if (candidateFileTemplateRepository.countActiveTemplates() == 0) {
            initializeDefaultCandidateFileTemplates();
        }
    }

    private void initializeDefaultProcedureTemplates() {
        List<ProcedureTemplate> defaultTemplates = Arrays.asList(
                new ProcedureTemplate(
                        "Accueil et présentation générale",
                        "RH",
                        "J+1",
                        "Tour des locaux, présentation de l'entreprise et de l'équipe, remise du badge d'accès",
                        1
                ),
                new ProcedureTemplate(
                        "Remise du matériel informatique",
                        "IT",
                        "J+1",
                        "Ordinateur portable, téléphone professionnel, accès Wi-Fi",
                        2
                ),
                new ProcedureTemplate(
                        "Création des accès systèmes",
                        "IT",
                        "J+1",
                        "Email professionnel, Slack, outils métier, VPN",
                        3
                ),
                new ProcedureTemplate(
                        "Formation sécurité et conformité",
                        "RH",
                        "J+3",
                        "RGPD, sécurité informatique, règlement intérieur",
                        4
                ),
                new ProcedureTemplate(
                        "Présentation des processus qualité",
                        "Manager",
                        "J+5",
                        "Processus métier, outils de travail, méthodes",
                        5
                ),
                new ProcedureTemplate(
                        "Entretien avec le manager direct",
                        "Manager",
                        "J+7",
                        "Objectifs, attentes, plan de développement",
                        6
                )
        );

        procedureTemplateRepository.saveAll(defaultTemplates);
        System.out.println("✅ " + defaultTemplates.size() + " procedure templates initialized successfully!");
    }

    private void initializeDefaultCandidateFileTemplates() {
        List<CandidateFileTemplate> defaultFileTemplates = Arrays.asList(
                new CandidateFileTemplate(
                        "Contrat de travail signé",
                        "Contrat de travail dûment signé par le candidat et l'employeur",
                        1,
                        true,
                        "document"
                ),
                new CandidateFileTemplate(
                        "Copie pièce d'identité",
                        "Copie lisible de la carte d'identité nationale ou passeport",
                        2,
                        true,
                        "image"
                ),
                new CandidateFileTemplate(
                        "Relevé d'identité bancaire",
                        "RIB pour les virements de salaire",
                        3,
                        true,
                        "document"
                ),
                new CandidateFileTemplate(
                        "Certificat de travail précédent",
                        "Certificat de travail de l'employeur précédent si applicable",
                        4,
                        false,
                        "document"
                ),
                new CandidateFileTemplate(
                        "Diplômes et certifications",
                        "Copies des diplômes et certifications professionnelles",
                        5,
                        true,
                        "document"
                ),
                new CandidateFileTemplate(
                        "Attestation sécurité sociale",
                        "Attestation d'affiliation à la sécurité sociale",
                        6,
                        true,
                        "document"
                ),
                new CandidateFileTemplate(
                        "Photo d'identité",
                        "Photo d'identité récente pour le badge d'accès",
                        7,
                        true,
                        "image"
                )
        );

        candidateFileTemplateRepository.saveAll(defaultFileTemplates);
        System.out.println("✅ " + defaultFileTemplates.size() + " candidate file templates initialized successfully!");
    }
}