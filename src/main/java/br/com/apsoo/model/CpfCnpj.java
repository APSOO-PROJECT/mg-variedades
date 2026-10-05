package br.com.apsoo.model;

public final class CpfCnpj {

    private CpfCnpj() {
    }

    // Remove a máscara (pontos, traço e barra), deixando apenas os dígitos
    public static String limpar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    // 52998224725 -> 529.982.247-25 | 11222333000181 -> 11.222.333/0001-81
    public static String formatar(String valor) {
        String digitos = limpar(valor);
        if (digitos.length() == 11) {
            return digitos.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (digitos.length() == 14) {
            return digitos.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return valor;
    }

    public static boolean isValido(String valor) {
        String digitos = limpar(valor);
        if (digitos.matches("(\\d)\\1+")) {
            return false;
        }
        if (digitos.length() == 11) {
            return digitoConfere(digitos, 9, new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2})
                    && digitoConfere(digitos, 10, new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
        }
        if (digitos.length() == 14) {
            return digitoConfere(digitos, 12, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2})
                    && digitoConfere(digitos, 13, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        }
        return false;
    }

    private static boolean digitoConfere(String digitos, int posicao, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (digitos.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        int esperado = resto < 2 ? 0 : 11 - resto;
        return esperado == digitos.charAt(posicao) - '0';
    }
}
