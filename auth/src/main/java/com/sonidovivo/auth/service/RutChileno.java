package com.sonidovivo.auth.service;

import java.util.Locale;

public final class RutChileno {

    private RutChileno() {
    }

    public static String normalizar(String run) {
        return run.replace(".", "")
                .replace("-", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }

    public static boolean esValido(String run) {
        String limpio = normalizar(run);

        if (!limpio.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digitoIngresado = limpio.charAt(limpio.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        for (int indice = cuerpo.length() - 1; indice >= 0; indice--) {
            suma += Character.getNumericValue(cuerpo.charAt(indice)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resultado = 11 - (suma % 11);
        char digitoEsperado = switch (resultado) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> Character.forDigit(resultado, 10);
        };

        return digitoIngresado == digitoEsperado;
    }
}
