package ma.nexotek.HireCraft.service.ContentGenerator;

import org.springframework.stereotype.Service;

@Service
public class ContentGeneratorService {

    public String generateJobContent(String title, String skills, String missions, String location,
                                     String contractType, String level, String tone, String applicationLink) {

        StringBuilder content = new StringBuilder();

        // En-tête basé sur le titre
        content.append(generateHeader(title, tone));

        // Section missions si disponible
        if (missions != null && !missions.trim().isEmpty()) {
            content.append(generateMissionsSection(missions, tone));
        }

        // Section compétences requises
        content.append(generateSkillsSection(skills, tone));

        // Section profil recherché
        content.append(generateProfileSection(level, tone));

        // Section informations pratiques
        content.append(generatePracticalInfoSection(location, contractType, tone));

        // Section candidature
        content.append(generateApplicationSection(tone, applicationLink));

        return content.toString();
    }

    private String generateHeader(String title, String tone) {
        StringBuilder header = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            header.append("🚀 OFFRE D'EMPLOI - ").append(title.toUpperCase()).append("\n\n");
            header.append("Nous recherchons un(e) ").append(title).append(" pour rejoindre notre équipe dynamique.\n\n");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            header.append("Hey ! 👋 On cherche un(e) ").append(title).append(" !\n\n");
            header.append("Tu es passionné(e) par le développement ? Cette offre est faite pour toi !\n\n");
        } else {
            header.append("Nous recrutons un(e) ").append(title).append("\n\n");
            header.append("Rejoignez-nous dans cette aventure professionnelle enrichissante.\n\n");
        }

        return header.toString();
    }

    private String generateMissionsSection(String missions, String tone) {
        StringBuilder section = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            section.append("📋 VOS PRINCIPALES MISSIONS :\n");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            section.append("🎯 Ce que tu vas faire au quotidien :\n");
        } else {
            section.append("🔸 Missions principales :\n");
        }

        section.append(missions).append("\n\n");

        return section.toString();
    }

    private String generateSkillsSection(String skills, String tone) {
        StringBuilder section = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            section.append("💼 COMPÉTENCES REQUISES :\n");
            section.append("Pour exceller dans ce poste, vous devrez maîtriser :\n");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            section.append("🛠️ Ton arsenal technique :\n");
            section.append("Voici ce qu'on aimerait que tu maîtrises :\n");
        } else {
            section.append("🔧 Compétences techniques :\n");
            section.append("Les technologies que nous utilisons :\n");
        }

        // Formater les compétences
        String[] skillsArray = skills.split(",");
        for (String skill : skillsArray) {
            section.append("• ").append(skill.trim()).append("\n");
        }
        section.append("\n");

        return section.toString();
    }

    private String generateProfileSection(String level, String tone) {
        StringBuilder section = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            section.append("👤 PROFIL RECHERCHÉ :\n");
            section.append("Niveau d'expérience : ").append(level).append("\n");
            section.append("Nous recherchons une personne motivée, autonome et avec un excellent esprit d'équipe.\n\n");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            section.append("🎭 Le profil qu'on recherche :\n");
            section.append("Niveau : ").append(level).append("\n");
            section.append("Tu es motivé(e), tu aimes apprendre et tu n'as pas peur des défis ? Perfect ! 🎉\n\n");
        } else {
            section.append("👥 Profil souhaité :\n");
            section.append("Expérience : ").append(level).append("\n");
            section.append("Nous valorisons la curiosité, l'autonomie et l'esprit collaboratif.\n\n");
        }

        return section.toString();
    }

    private String generatePracticalInfoSection(String location, String contractType, String tone) {
        StringBuilder section = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            section.append("📍 INFORMATIONS PRATIQUES :\n");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            section.append("📋 Les infos pratiques :\n");
        } else {
            section.append("ℹ️ Détails du poste :\n");
        }

        section.append("• Localisation : ").append(location).append("\n");
        section.append("• Type de contrat : ").append(contractType).append("\n");
        section.append("• Télétravail : Hybride possible\n");
        section.append("• Avantages : Mutuelle, tickets restaurant, CE\n\n");

        return section.toString();
    }

    private String generateApplicationSection(String tone, String applicationLink) {
        StringBuilder section = new StringBuilder();

        if ("Professionnel".equalsIgnoreCase(tone)) {
            section.append("📧 CANDIDATURE :\n");
            section.append("Intéressé(e) par cette opportunité ?\n");
            section.append("Postulez directement via ce lien : ").append(applicationLink).append("\n");
            section.append("Nous étudierons votre candidature avec attention.\n\n");
            section.append("Nous avons hâte de vous rencontrer ! 🤝");
        } else if ("Décontracté".equalsIgnoreCase(tone)) {
            section.append("🚀 Prêt(e) à nous rejoindre ?\n");
            section.append("Clique ici pour postuler : ").append(applicationLink).append("\n");
            section.append("On a hâte de faire ta connaissance ! 😊\n\n");
            section.append("Let's go ! 🎉");
        } else {
            section.append("📨 Pour postuler :\n");
            section.append("Candidatez en ligne : ").append(applicationLink).append("\n");
            section.append("Nous reviendrons vers vous rapidement.\n\n");
            section.append("À bientôt ! ✨");
        }

        return section.toString();
    }

    /**
     * Génère un contenu court pour les réseaux sociaux
     */
    public String generateSocialMediaContent(String title, String skills, String location, String applicationLink) {
        StringBuilder content = new StringBuilder();

        content.append("🔥 NOUS RECRUTONS ! 🔥\n\n");
        content.append("Poste : ").append(title).append(" 💼\n");
        content.append("Lieu : ").append(location).append(" 📍\n");
        content.append("Compétences : ").append(skills).append(" 🛠️\n\n");
        content.append("Postulez ici : ").append(applicationLink).append(" 🚀\n");
        content.append("#Recrutement #").append(title.replaceAll(" ", "")).append(" #Emploi");

        return content.toString();
    }


}