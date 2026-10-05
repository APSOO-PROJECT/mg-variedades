package br.com.apsoo.controller;

import java.sql.SQLException;
import java.util.List;

import br.com.apsoo.dao.UsuarioDAO;
import br.com.apsoo.model.Cargo;
import br.com.apsoo.model.StatusCadastro;
import br.com.apsoo.model.Usuario;

// Casos de uso Gerenciar Vendedor e Redefinir Senha
public class UsuarioController {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listar() throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return usuarioDAO.listarTodos();
    }

    // Cadastra (id nulo) ou altera um usuário; senhaPura vazia na alteração mantém a senha atual
    public void salvar(Usuario usuario, String senhaPura) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        validarDados(usuario);

        Usuario existente = usuarioDAO.buscarPorLogin(usuario.getLogin());
        if (existente != null && !existente.getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Já existe um usuário com este login.");
        }

        if (usuario.getId() == null) {
            validarSenha(senhaPura);
            usuario.setSenha(Usuario.gerarHash(senhaPura));
            usuario.setStatus(StatusCadastro.ATIVO);
            usuarioDAO.incluir(usuario);
            return;
        }

        Usuario atual = usuarioDAO.buscarPorId(usuario.getId());
        validarAlteracao(atual, usuario);

        boolean trocarSenha = senhaPura != null && !senhaPura.isEmpty();
        if (trocarSenha) {
            validarSenha(senhaPura);
            usuario.setSenha(Usuario.gerarHash(senhaPura));
        }
        usuarioDAO.alterar(usuario, trocarSenha);
    }

    public void alterarStatus(Usuario usuario, boolean ativar) throws RegraNegocioException, SQLException {
        usuario.setStatus(ativar ? StatusCadastro.ATIVO : StatusCadastro.INATIVO);
        salvar(usuario, null);
    }

    public void promover(Usuario usuario) throws RegraNegocioException, SQLException {
        usuario.setCargo(Cargo.ADMIN);
        salvar(usuario, null);
    }

    // RF03: qualquer usuário redefine a própria senha mediante autenticação prévia (senha atual)
    public void redefinirSenha(String senhaAtual, String novaSenha, String confirmacao)
            throws RegraNegocioException, SQLException {
        Usuario logado = Sessao.getUsuarioLogado();
        Usuario atual = usuarioDAO.buscarPorId(logado.getId());

        if (senhaAtual.isEmpty() || !atual.senhaConfere(senhaAtual)) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        validarSenha(novaSenha);
        if (!novaSenha.equals(confirmacao)) {
            throw new RegraNegocioException("A confirmação não confere com a nova senha.");
        }

        String hash = Usuario.gerarHash(novaSenha);
        usuarioDAO.alterarSenha(atual.getId(), hash);
        logado.setSenha(hash);
    }

    private void validarDados(Usuario usuario) throws RegraNegocioException {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome do usuário.");
        }
        if (usuario.getNome().length() > 50) {
            throw new RegraNegocioException("O nome deve ter no máximo 50 caracteres.");
        }
        if (usuario.getLogin() == null || !usuario.getLogin().matches("\\S{3,30}")) {
            throw new RegraNegocioException("O login deve ter de 3 a 30 caracteres, sem espaços.");
        }
        if (usuario.getCargo() == null) {
            throw new RegraNegocioException("Selecione o cargo do usuário.");
        }
    }

    private void validarSenha(String senha) throws RegraNegocioException {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException("A senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    // Regras de negócio da alteração de contas (RF01 e RF02)
    private void validarAlteracao(Usuario atual, Usuario novo) throws RegraNegocioException {
        if (atual.isSeed()) {
            throw new RegraNegocioException("A conta de administrador padrão só pode ter a senha alterada "
                    + "(em Minha conta).");
        }
        if (atual.getCargo() == Cargo.ADMIN && novo.getCargo() == Cargo.VENDEDOR) {
            throw new RegraNegocioException("Não é possível rebaixar um administrador.");
        }
        boolean desativando = atual.isAtivo() && novo.getStatus() == StatusCadastro.INATIVO;
        if (desativando && atual.getId().equals(Sessao.getUsuarioLogado().getId())) {
            throw new RegraNegocioException("Você não pode desativar o próprio usuário.");
        }
    }
}
