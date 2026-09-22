package com.empresa.pdv.services;

import java.util.List;

import com.empresa.pdv.models.Usuario;
import com.empresa.pdv.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.listarTodos();
    }

    public void cadastrarUsuario(String nome, String login, String senha, String cargo) throws Exception {
        if (nome.isBlank() || login.isBlank() || senha.isBlank()) {
            throw new IllegalArgumentException("Todos os campos obrigatórios devem ser preenchidos.");
        }

        if (usuarioRepository.buscarPorLogin(login).isPresent()) {
            throw new Exception("Já existe um usuário cadastrado com este login.");
        }

        Usuario novoUsuario = new Usuario(null, nome, login, senha, cargo, "ATIVO");
        boolean sucesso = usuarioRepository.salvar(novoUsuario);
        if (!sucesso) {
            throw new Exception("Erro ao salvar o usuário no banco de dados.");
        }
    }

    public void atualizarUsuario(Usuario usuario) throws Exception {
        // Regra de Negócio 2: O admin seed (admin) não pode ser desativado ou rebaixado
        if ("admin".equalsIgnoreCase(usuario.getLogin())) {
            if ("DESATIVADO".equalsIgnoreCase(usuario.getStatus())) {
                throw new Exception("O administrador padrão (seed) não pode ser desativado.");
            }
        }

        boolean sucesso = usuarioRepository.atualizar(usuario);
        if (!sucesso) {
            throw new Exception("Erro ao atualizar os dados do usuário.");
        }
    }
}