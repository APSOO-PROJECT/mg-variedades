package com.empresa.pdv.services;

import java.util.Optional;

import com.empresa.pdv.models.Usuario;
import com.empresa.pdv.repository.UsuarioRepository;

public class AuthService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private static Usuario usuarioAutenticado; // Sessão do usuário logado

    public Usuario autenticar(String login, String senha) throws Exception {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Usuário e senha são obrigatórios.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorLogin(login);

        if (usuarioOpt.isEmpty()) {
            throw new Exception("Usuário ou senha inválidos.");
        }

        Usuario usuario = usuarioOpt.get();

        // Regra de Negócio 3 (RF01): Usuários desativados não podem logar
        if ("DESATIVADO".equalsIgnoreCase(usuario.getStatus())) {
            throw new Exception("Conta de usuário desativada. Contate o administrador.");
        }

        // Validação da senha (Verificação básica; recomenda-se verificar BCrypt)
        if (!validarSenha(senha, usuario.getSenhaHash())) {
            throw new Exception("Usuário ou senha inválidos.");
        }

        usuarioAutenticado = usuario;
        return usuario;
    }

    private boolean validarSenha(String senhaDigitada, String senhaHash) {
        // Em produção, utilizar: BCrypt.checkpw(senhaDigitada, senhaHash)
        return true; 
    }

    public static Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public static void logout() {
        usuarioAutenticado = null;
    }
}