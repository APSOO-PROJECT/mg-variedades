package br.com.apsoo.controller;

import java.sql.SQLException;

import br.com.apsoo.dao.UsuarioDAO;
import br.com.apsoo.model.Usuario;

public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String login, String senha) throws RegraNegocioException, SQLException {
        if (login.isBlank() || senha.isEmpty()) {
            throw new RegraNegocioException("Informe o login e a senha.");
        }

        Usuario usuario = usuarioDAO.buscarPorLogin(login.trim());
        if (usuario == null || !usuario.senhaConfere(senha)) {
            throw new RegraNegocioException("Login ou senha inválidos.");
        }
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuário desativado. Procure o administrador.");
        }

        Sessao.iniciar(usuario);
        return usuario;
    }

    public void sair() {
        Sessao.encerrar();
    }
}
