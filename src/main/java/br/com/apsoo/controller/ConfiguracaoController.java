package br.com.apsoo.controller;

import java.util.prefs.BackingStoreException;

import br.com.apsoo.dao.PreferenciaDAO;
import br.com.apsoo.model.ModoAcao;
import br.com.apsoo.model.Usuario;

// Preferências do usuário autenticado
public class ConfiguracaoController {

    private final PreferenciaDAO preferenciaDAO = new PreferenciaDAO();

    // Padrão: na própria tela (também quando ainda não há usuário autenticado)
    public ModoAcao getModoAcao() {
        Usuario usuario = Sessao.getUsuarioLogado();
        return usuario == null ? ModoAcao.TELA : preferenciaDAO.buscarModoAcao(usuario.getLogin());
    }

    public void definirModoAcao(ModoAcao modo) throws RegraNegocioException {
        Usuario usuario = Sessao.getUsuarioLogado();
        if (usuario == null) {
            throw new RegraNegocioException("É necessário estar autenticado para alterar as configurações.");
        }
        try {
            preferenciaDAO.salvarModoAcao(usuario.getLogin(), modo);
        } catch (BackingStoreException e) {
            throw new RegraNegocioException("Não foi possível salvar a preferência neste computador.");
        }
    }
}
