package com.empresa.pdv.services;

import com.empresa.pdv.exceptions.RegraNegocioException;
import com.empresa.pdv.models.Categoria;
import com.empresa.pdv.repository.CategoriaRepository;

import java.time.Duration;
import java.util.List;

public class CategoriaService {
    private static final Duration PRAZO_DEVOLUCAO_PADRAO = Duration.ofDays(7);
    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Alimentos", "Bebidas", "Higiene", "Limpeza", "Eletrônicos", "Vestuário");

    private final CategoriaRepository repository;

    public CategoriaService() {
        this(new CategoriaRepository());
    }

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
        semearCategoriasPadrao();
    }

    private void semearCategoriasPadrao() {
        for (String nome : CATEGORIAS_PADRAO) {
            Categoria categoria = new Categoria(repository.proximoId(), nome, PRAZO_DEVOLUCAO_PADRAO);
            categoria.setSeed(true);
            repository.salvar(categoria);
        }
    }

    public Categoria cadastrar(String nome) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isBlank()) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        if (repository.existePorNome(nomeNormalizado)) {
            throw new RegraNegocioException("Já existe uma categoria com este nome.");
        }
        return repository.salvar(new Categoria(repository.proximoId(), nomeNormalizado, PRAZO_DEVOLUCAO_PADRAO));
    }

    public List<Categoria> listarTodos() {
        return repository.listarTodos();
    }
}

