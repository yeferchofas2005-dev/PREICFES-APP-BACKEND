package com.edu.preicfesGratuito.util;

import java.security.SecureRandom;

/**
 * Generador de contrasenas iniciales seguras para carga masiva de estudiantes.
 */
public final class PasswordGeneratorUtil {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALL = UPPER + LOWER + DIGITS;
    private static final int DEFAULT_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGeneratorUtil() {
    }

    public static String generarContrasenaSegura() {
        char[] chars = new char[DEFAULT_LENGTH];
        chars[0] = UPPER.charAt(RANDOM.nextInt(UPPER.length()));
        chars[1] = LOWER.charAt(RANDOM.nextInt(LOWER.length()));
        chars[2] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));

        for (int i = 3; i < DEFAULT_LENGTH; i++) {
            chars[i] = ALL.charAt(RANDOM.nextInt(ALL.length()));
        }

        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
