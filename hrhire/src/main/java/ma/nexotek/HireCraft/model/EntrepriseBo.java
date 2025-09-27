package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "entreprise")
public class EntrepriseBo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String siret;

    @Column(nullable = false)
    private String nom;

    private String adresse;

    private String codePostal;

    private String ville;

    private String email;

    private String tel;

    // Constructeur vide
    public EntrepriseBo() {}

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTéléphone() {
        return tel;
    }

    public void setTéléphone(String téléphone) {
        this.tel = téléphone;
    }
}