package demo.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour HashUtil (BCrypt).
 */
class HashUtilTest {

    @Test
    void hash_returnsBCryptFormat() {
        String hash = HashUtil.hash("monPassword");
        // BCrypt commence toujours par $2a$ ou $2b$
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                "Le hash doit être au format BCrypt");
    }

    @Test
    void hash_nullInput_returnsNull() {
        assertNull(HashUtil.hash(null));
    }

    @Test
    void matches_correctPassword_returnsTrue() {
        String hash = HashUtil.hash("monPassword");
        assertTrue(HashUtil.matches("monPassword", hash));
    }

    @Test
    void matches_wrongPassword_returnsFalse() {
        String hash = HashUtil.hash("monPassword");
        assertFalse(HashUtil.matches("mauvaisPassword", hash));
    }

    @Test
    void matches_nullInputs_returnsFalse() {
        assertFalse(HashUtil.matches(null, "hash"));
        assertFalse(HashUtil.matches("clear", null));
        assertFalse(HashUtil.matches(null, null));
    }

    @Test
    void hash_twoHashesAreDifferent_butBothMatch() {
        // BCrypt génère un salt aléatoire, donc 2 hash du même texte sont différents
        String hash1 = HashUtil.hash("monPassword");
        String hash2 = HashUtil.hash("monPassword");
        
        assertNotEquals(hash1, hash2, "Deux hash BCrypt du même texte doivent être différents (salt)");
        
        // Mais les deux doivent matcher avec le texte original
        assertTrue(HashUtil.matches("monPassword", hash1));
        assertTrue(HashUtil.matches("monPassword", hash2));
    }
}
