package com.empresa.pdv.models;

import java.time.LocalDateTime;

public class Usuario {
    private Long id;
    private String nome;
    private String login;
    private String senhaHash;
    private String cargo; // "ADMIN" ou "VENDEDOR"
    private String status; // "ATIVO" ou "DESATIVADO"
    private LocalDateTime dataCadastro;

    public Usuario() {}

    public Usuario(Long id, String nome, String login, String senhaHash, String cargo, String status) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.cargo = cargo;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}