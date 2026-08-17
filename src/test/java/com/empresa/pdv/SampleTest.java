package com.empresa.pdv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleTest {

    @Test
    @DisplayName("Deve validar a inicialização do ambiente de testes JUnit")
    void testEnvironment() {
        assertTrue(true, "Ambiente de testes configurado com sucesso.");
    }
}