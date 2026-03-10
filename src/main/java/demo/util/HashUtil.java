package demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire de hachage avec BCrypt (conforme aux exigences du TP).
 * BCrypt génère un salt aléatoire à chaque hash, donc on ne peut pas
 * comparer deux hash directement - il faut utiliser matches().
 */
public final class HashUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    private HashUtil() {}

    /**
     * Hash un texte en clair avec BCrypt.
     * @param input le texte en clair
     * @return le hash BCrypt (commence par $2a$)
     */
    public static String hash(String input) {
        if (input == null) return null;
        return encoder.encode(input);
    }

    /**
     * Vérifie si un texte en clair correspond à un hash BCrypt.
     * @param clearText le texte en clair
     * @param hashedText le hash BCrypt stocké
     * @return true si le texte correspond au hash
     */
    public static boolean matches(String clearText, String hashedText) {
        if (clearText == null || hashedText == null) return false;
        return encoder.matches(clearText, hashedText);
    }
}
