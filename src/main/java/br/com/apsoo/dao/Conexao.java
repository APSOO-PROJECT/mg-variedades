package br.com.apsoo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexao {

    // Tempo limite de 2 segundos por tentativa (RNF2); a sessão usa o fuso do sistema para que NOW()
    // do banco e as datas gravadas pelo Java sejam consistentes nos filtros por período
    private static final String URL = "jdbc:mysql://localhost:3310/mg_variedades"
            + "?connectTimeout=2000&socketTimeout=2000"
            + "&connectionTimeZone=LOCAL&forceConnectionTimeZoneToSession=true";
    private static final String USUARIO = "root";
    private static final String SENHA = "123";
    private static final int TENTATIVAS = 5;

    private Conexao() {
    }

    // Tenta conectar até 5 vezes antes de desistir (RNF2)
    public static Connection getConexao() throws SQLException {
        SQLException ultimoErro = null;
        for (int i = 0; i < TENTATIVAS; i++) {
            try {
                return DriverManager.getConnection(URL, USUARIO, SENHA);
            } catch (SQLException e) {
                ultimoErro = e;
            }
        }
        throw new SQLException("Não foi possível estabelecer a conexão com o banco de dados.", ultimoErro);
    }

    public static void fechar(Connection conexao) throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }
}
