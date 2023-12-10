package com.example.myapplication;

import android.os.Build;
import android.util.Patterns;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class metodos {
    public static String hashPassword(String password) {
        try {
            // Generar salt aleatorio
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Configurar parámetros para la derivación de clave
            int iterations = 600000;
            int keyLength = 128; // Longitud de la clave en bits
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);

            // Generar la clave en formato PBKDF2-SHA256
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Codificar el resultado en formato Base64
            String encodedHash = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encodedHash = Base64.getEncoder().encodeToString(hash);
            }
            String encodedSalt = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encodedSalt = Base64.getEncoder().encodeToString(salt);
            }

            // Construir la cadena final en el formato deseado
            String encryptedPassword = "pbkdf2_sha256$" + iterations + "$" + encodedSalt + "$" + encodedHash;
            return encryptedPassword;


        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    public static Boolean passwordValidate(String password){
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[$;._\\-*]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }



}
