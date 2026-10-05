package br.com.apsoo.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import br.com.apsoo.dao.ProdutoDAO;
import br.com.apsoo.dao.RelatorioDAO;
import br.com.apsoo.model.LinhaVendaCategoria;
import br.com.apsoo.model.Produto;
import br.com.apsoo.model.RelatorioDevolucoes;
import br.com.apsoo.model.RelatorioProdutosVendidos;
import br.com.apsoo.model.RelatorioVendas;
import br.com.apsoo.model.VendasVendedor;

// Consultas e relatórios: somente administrador
public class RelatorioController {

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // RF15: relatório de vendas filtrado por dia, mês ou ano
    public RelatorioVendas consultarVendas(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return relatorioDAO.consultarVendas(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    // RF16: vendas por categoria, com as categorias sem venda zeradas
    public List<LinhaVendaCategoria> consultarVendasPorCategoria(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return relatorioDAO.listarVendasPorCategoria(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    // RF18: produtos que atingiram a quantidade mínima, com última atualização de estoque no período
    public List<Produto> consultarProdutosEmFalta(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return produtoDAO.listarEstoqueBaixo(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    // RF20: produto mais vendido, mais vendidos por categoria e produtos sem venda
    public RelatorioProdutosVendidos consultarProdutosVendidos(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return relatorioDAO.consultarProdutosVendidos(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    // RF21: valor total vendido e quantidade de vendas por vendedor
    public List<VendasVendedor> consultarVendasPorVendedor(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return relatorioDAO.consultarVendasPorVendedor(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    // RF22: devoluções entre a data inicial e a final, ambas inclusive
    public RelatorioDevolucoes consultarDevolucoes(LocalDate dataInicial, LocalDate dataFinal)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        if (dataInicial == null || dataFinal == null) {
            throw new RegraNegocioException("Informe a data inicial e a data final.");
        }
        if (dataInicial.isAfter(dataFinal)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }
        return relatorioDAO.consultarDevolucoes(dataInicial.atStartOfDay(), dataFinal.plusDays(1).atStartOfDay());
    }
}
