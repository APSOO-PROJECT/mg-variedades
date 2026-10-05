package br.com.apsoo.controller;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import br.com.apsoo.dao.CategoriaDAO;
import br.com.apsoo.model.Categoria;
import br.com.apsoo.model.CategoriaHistorico;
import br.com.apsoo.model.StatusCadastro;

public class CategoriaController {

    // Regra de Negócio: sem prazo definido, vale o padrão de 7 dias corridos
    private static final int PRAZO_PADRAO_HORAS = 7 * 24;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listar() throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return categoriaDAO.listarTodas();
    }

    public void salvar(Categoria categoria) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();

        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        if (categoria.getNome().length() > 40) {
            throw new RegraNegocioException("O nome da categoria deve ter no máximo 40 caracteres.");
        }

        Categoria existente = categoriaDAO.buscarPorNome(categoria.getNome());
        if (existente != null && !existente.getId().equals(categoria.getId())) {
            throw new RegraNegocioException("Já existe uma categoria com este nome.");
        }
        if (categoria.getPrazoDevolucaoHoras() <= 0) {
            categoria.setPrazoDevolucaoHoras(PRAZO_PADRAO_HORAS);
        }

        if (categoria.getId() == null) {
            categoria.setStatus(StatusCadastro.ATIVO);
            categoriaDAO.incluir(categoria);
        } else {
            categoriaDAO.alterar(categoria, Sessao.getUsuarioLogado().getId());
        }
    }

    public void alterarStatus(Categoria categoria, boolean ativar) throws RegraNegocioException, SQLException {
        categoria.setStatus(ativar ? StatusCadastro.ATIVO : StatusCadastro.INATIVO);
        salvar(categoria);
    }

    // Regra de Negócio: categoria com produtos vinculados só pode ser desativada
    public void excluir(Categoria categoria) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        Categoria atual = categoriaDAO.buscarPorId(categoria.getId());
        if (atual.getQuantidadeProdutos() > 0) {
            throw new RegraNegocioException("Esta categoria possui produtos vinculados e não pode ser excluída. "
                    + "Desative-a para que deixe de ser oferecida em novos produtos.");
        }
        try {
            categoriaDAO.excluir(categoria.getId());
        } catch (SQLIntegrityConstraintViolationException e) {
            // Categorias com histórico de alterações são preservadas para consulta
            throw new RegraNegocioException("Esta categoria possui histórico registrado e não pode ser excluída. "
                    + "Desative-a.");
        }
    }

    public List<CategoriaHistorico> historico(Categoria categoria) throws RegraNegocioException, SQLException {
        Sessao.exigirAdmin();
        return categoriaDAO.listarHistorico(categoria.getId());
    }
}
