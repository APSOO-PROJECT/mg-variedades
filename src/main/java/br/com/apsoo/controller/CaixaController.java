package br.com.apsoo.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.apsoo.dao.CaixaDAO;
import br.com.apsoo.dao.MovimentacaoCaixaDAO;
import br.com.apsoo.model.Caixa;
import br.com.apsoo.model.EventoCaixa;
import br.com.apsoo.model.MovimentacaoCaixa;
import br.com.apsoo.model.ResumoCaixa;
import br.com.apsoo.model.TipoOperacao;

// Casos de uso Abrir Caixa, Movimentar Caixa, Fechar Caixa e Consultar Caixa
public class CaixaController {

    private final CaixaDAO caixaDAO = new CaixaDAO();
    private final MovimentacaoCaixaDAO movimentacaoDAO = new MovimentacaoCaixaDAO();

    // Status exibido a qualquer usuário (RF13)
    public boolean isCaixaAberto() throws SQLException {
        return caixaDAO.buscarAberto() != null;
    }

    public Caixa buscarAberto() throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return caixaDAO.buscarAberto();
    }

    // Data, hora e usuário são registrados automaticamente (RF08)
    public void abrirCaixa(BigDecimal fundoTroco) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        if (fundoTroco == null || fundoTroco.signum() < 0) {
            throw new RegraNegocioException("Informe um valor inicial (fundo de troco) válido.");
        }
        if (caixaDAO.buscarAberto() != null) {
            throw new RegraNegocioException("Já existe um caixa aberto.");
        }

        Caixa caixa = new Caixa(null, fundoTroco, LocalDateTime.now());
        caixaDAO.incluir(caixa, Sessao.getUsuarioLogado().getId());
    }

    public ResumoCaixa resumir(Caixa caixa) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return caixaDAO.resumir(caixa);
    }

    public void registrarSangria(BigDecimal valor, String motivo) throws RegraNegocioException, SQLException {
        Caixa caixa = exigirCaixaAberto();
        validarMovimentacao(valor, motivo);

        // Regra de Negócio: a sangria não pode exceder o saldo em dinheiro disponível
        BigDecimal disponivel = caixaDAO.resumir(caixa).calcularSaldoEsperado();
        if (valor.compareTo(disponivel) > 0) {
            throw new RegraNegocioException("O valor da sangria excede o saldo em dinheiro disponível no caixa.");
        }
        registrar(caixa, TipoOperacao.SANGRIA, valor, motivo);
    }

    public void registrarSuprimento(BigDecimal valor, String motivo) throws RegraNegocioException, SQLException {
        Caixa caixa = exigirCaixaAberto();
        validarMovimentacao(valor, motivo);
        registrar(caixa, TipoOperacao.SUPRIMENTO, valor, motivo);
    }

    // Compara o valor esperado com o contado; havendo quebra, a justificativa é obrigatória (RF10)
    public Caixa fecharCaixa(BigDecimal valorContado, String justificativa)
            throws RegraNegocioException, SQLException {
        Caixa caixa = exigirCaixaAberto();
        if (valorContado == null || valorContado.signum() < 0) {
            throw new RegraNegocioException("Informe o valor contado em caixa.");
        }

        BigDecimal esperado = caixaDAO.resumir(caixa).calcularSaldoEsperado();
        boolean quebra = valorContado.compareTo(esperado) != 0;
        if (quebra && (justificativa == null || justificativa.isBlank())) {
            throw new RegraNegocioException("Há divergência entre o valor esperado e o contado (quebra de caixa). "
                    + "Registre uma justificativa para concluir o fechamento.");
        }

        caixa.setDataHoraFechamento(LocalDateTime.now());
        caixa.setSaldoEsperado(esperado);
        caixa.setSaldoReal(valorContado);
        caixa.setJustificativaQuebra(quebra ? justificativa.trim() : null);
        if (caixaDAO.fechar(caixa, Sessao.getUsuarioLogado().getId()) == 0) {
            throw new RegraNegocioException("Este caixa já foi fechado.");
        }
        return caixa;
    }

    // Período de referência: dia, mês ou ano da data informada (RF13)
    public List<EventoCaixa> consultarHistorico(Periodo periodo, LocalDate referencia)
            throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        LocalDate inicio = periodo.inicio(referencia);
        return caixaDAO.listarEventos(inicio.atStartOfDay(), periodo.fim(inicio).atStartOfDay());
    }

    private Caixa exigirCaixaAberto() throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        Caixa caixa = caixaDAO.buscarAberto();
        if (caixa == null) {
            throw new RegraNegocioException("Não há caixa aberto.");
        }
        return caixa;
    }

    private void validarMovimentacao(BigDecimal valor, String motivo) throws RegraNegocioException {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Informe um valor maior que zero.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo é obrigatório.");
        }
        if (motivo.length() > 100) {
            throw new RegraNegocioException("O motivo deve ter no máximo 100 caracteres.");
        }
    }

    private void registrar(Caixa caixa, TipoOperacao tipo, BigDecimal valor, String motivo) throws SQLException {
        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa(caixa, Sessao.getUsuarioLogado(), tipo,
                valor, motivo.trim());
        movimentacao.setDataHora(LocalDateTime.now());
        movimentacaoDAO.incluir(movimentacao);
    }
}
