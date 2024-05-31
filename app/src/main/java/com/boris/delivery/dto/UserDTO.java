package com.boris.delivery.dto;

public class UserDTO {
    private String email;
    private String motDePasse;
    private String telephone;
    private String immatriculation;
    private String id;
    private long role;

    public UserDTO(String id, String email, String motDePasse, String telephone, String immatriculation, long role) {
        this.id = id;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.immatriculation = immatriculation;
        this.role = role;
    }

    public UserDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public long getRole() {
        return role;
    }

    public void setRole(long role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", immatriculation='" + immatriculation + '\'' +
                ", role=" + role +'\'' +
                ", id=" + id +
                '}';
    }
}
