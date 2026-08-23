package com.empresa.pdv.models;

import java.time.Duration;
import java.util.Objects;

public class Categoria {
    private Long id;
    private String nome;
    private Duration prazoDevolucao;
    private boolean seed;

    public Categoria() {
    }

    public Categoria(Long id, String nome, Duration prazoDevolucao) {
        this.id = id;
        this.nome = nome;
        this.prazoDevolucao = prazoDevolucao;
    }

    // Método getters e setters
    public Long getId(){
        return id; 
    }
    public String getNome(){
        return nome;
    }
    public void setNome(String nome){
        this.nome = nome;
    }
    public Duration getPrazoDevolucao(){
        return prazoDevolucao;
    }
    public void setPrazoDevolucao(Duration prazoDevolucao){
        this.prazoDevolucao = prazoDevolucao; 
    }
    public boolean isSeed(){
        return seed; 
    }
    public void setSeed(boolean seed){
        this.seed = seed; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof Categoria categoria)){
            return false;
        }

        return Objects.equals(id, categoria.id);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

    @Override
    public String toString(){
        return nome; 
    }
}
