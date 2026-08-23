package com.empresa.pdv.repository;

import com.empresa.pdv.models.Categoria;

public class CategoriaRepository extends InMemoryRepository<Categoria> {
    public CategoriaRepository() {
        super(Categoria::getId);
    }

    public boolean existePorNome(String nome) {
        return listarTodos().stream()
                .anyMatch(categoria -> categoria.getNome().equalsIgnoreCase(nome));
    }
}
