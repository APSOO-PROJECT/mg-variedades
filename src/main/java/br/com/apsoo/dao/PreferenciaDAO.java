package br.com.apsoo.dao;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import br.com.apsoo.model.ModoAcao;

// Preferências de interface de cada usuário, guardadas neste computador (não vão para o banco de dados)
public class PreferenciaDAO {

    private final Preferences preferencias = Preferences.userNodeForPackage(PreferenciaDAO.class);

    // Sem preferência salva (ou valor inválido), o padrão é abrir na própria tela
    public ModoAcao buscarModoAcao(String login) {
        String valor = preferencias.get(chaveModoAcao(login), ModoAcao.TELA.name());
        try {
            return ModoAcao.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return ModoAcao.TELA;
        }
    }

    public void salvarModoAcao(String login, ModoAcao modo) throws BackingStoreException {
        preferencias.put(chaveModoAcao(login), modo.name());
        preferencias.flush();
    }

    private String chaveModoAcao(String login) {
        return "modoAcao." + login.toLowerCase();
    }
}
