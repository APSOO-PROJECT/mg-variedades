package br.com.apsoo.model;

public class Categoria {
    private Integer id;
    private String nome;
    private int prazoDevolucaoHoras;
    private StatusCadastro status;
    private int quantidadeProdutos; // preenchido apenas nas listagens

    public Categoria() {}

    public Categoria(Integer id, String nome, int prazoDevolucaoHoras, StatusCadastro status) {
        this.id = id;
        this.nome = nome;
        this.prazoDevolucaoHoras = prazoDevolucaoHoras;
        this.status = status;
    }

    public boolean isAtiva() {
        return status == StatusCadastro.ATIVO;
    }

    // Métodos Getters e Setters
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }
    public void setQuantidadeProdutos(int quantidadeProdutos) {
        this.quantidadeProdutos = quantidadeProdutos;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPrazoDevolucaoHoras() {
        return prazoDevolucaoHoras;
    }
    public void setPrazoDevolucaoHoras(int prazoDevolucaoHoras) {
        this.prazoDevolucaoHoras = prazoDevolucaoHoras;
    }

    public StatusCadastro getStatus() {
        return status;
    }
    public void setStatus(StatusCadastro status) {
        this.status = status;
    }

    // Ex.: 24 -> "1 dia", 120 -> "5 dias", 36 -> "36 horas"
    public String descricaoPrazo() {
        if (prazoDevolucaoHoras % 24 == 0) {
            int dias = prazoDevolucaoHoras / 24;
            return dias + (dias == 1 ? " dia" : " dias");
        }
        return prazoDevolucaoHoras + " horas";
    }

    @Override
    public String toString() {
        return nome;
    }
}
