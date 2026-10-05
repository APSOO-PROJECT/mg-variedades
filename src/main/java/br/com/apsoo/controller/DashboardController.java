package br.com.apsoo.controller;

import java.sql.SQLException;

import br.com.apsoo.dao.CaixaDAO;
import br.com.apsoo.dao.ProdutoDAO;
import br.com.apsoo.dao.RelatorioDAO;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.MetricasDia;
import br.com.apsoo.model.Usuario;

// Caso de uso Exibir Métricas (RF17): o conteúdo varia conforme o perfil do usuário
public class DashboardController {

    private final CaixaDAO caixaDAO = new CaixaDAO();
    private final RelatorioDAO relatorioDAO = new RelatorioDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // O vendedor vê apenas os números das próprias vendas; o administrador vê os de toda a loja
    public MetricasDia carregar() throws SQLException {
        Usuario usuario = Sessao.getUsuarioLogado();
        Caixa caixa = caixaDAO.buscarAberto();

        MetricasDia metricas = new MetricasDia();
        if (caixa != null) {
            metricas = relatorioDAO.consultarMetricasDoCaixa(caixa.getId(),
                    usuario.isAdmin() ? null : usuario.getId());
        }
        metricas.setProdutosEstoqueBaixo(produtoDAO.contarEstoqueBaixo());
        return metricas;
    }
}
