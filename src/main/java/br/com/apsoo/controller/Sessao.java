package br.com.apsoo.controller;

import br.com.apsoo.model.Usuario;

// Guarda o usuário autenticado enquanto o sistema está aberto
public final class Sessao {

    private static Usuario usuarioLogado;

    private Sessao() {
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void iniciar(Usuario usuario) {
        usuarioLogado = usuario;
    }

    // RNF5: operações administrativas só podem ser feitas pelo administrador
    public static void exigirAdmin() throws RegraNegocioException {
        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            throw new RegraNegocioException("Apenas o administrador pode realizar esta operação.");
        }
    }

    // Logout (RNF10)
    public static void encerrar() {
        usuarioLogado = null;
    }
}
