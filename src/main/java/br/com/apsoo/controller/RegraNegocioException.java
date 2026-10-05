package br.com.apsoo.controller;

// Violação de regra de negócio; a mensagem é exibida ao usuário
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
