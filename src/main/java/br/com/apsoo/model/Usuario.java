package br.com.apsoo.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

public class Usuario {
    private Integer id;
    private String nome;
    private String login;
    private String senha; // sempre armazenada como hash SHA-256
    private Cargo cargo;
    private StatusCadastro status;
    private boolean seed;
    private LocalDateTime dataCadastro;

    public Usuario() {}

    public Usuario(Integer id, String nome, String login, String senha, Cargo cargo,
                   StatusCadastro status, boolean seed, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.cargo = cargo;
        this.status = status;
        this.seed = seed;
        this.dataCadastro = dataCadastro;
    }

    // Gera o hash que é gravado no banco no lugar da senha em texto
    public static String gerarHash(String senhaPura) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(senhaPura.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean senhaConfere(String senhaPura) {
        return senha != null && senha.equals(gerarHash(senhaPura));
    }

    // Regra de Negócio: usuário desativado não pode realizar login
    public boolean isAtivo() {
        return status == StatusCadastro.ATIVO;
    }

    public boolean isAdmin() {
        return cargo == Cargo.ADMIN;
    }

    // Métodos Getters e Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Cargo getCargo() {
        return cargo;
    }
    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public StatusCadastro getStatus() {
        return status;
    }
    public void setStatus(StatusCadastro status) {
        this.status = status;
    }

    public boolean isSeed() {
        return seed;
    }
    public void setSeed(boolean seed) {
        this.seed = seed;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
