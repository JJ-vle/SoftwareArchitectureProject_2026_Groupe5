package demo.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire global des exceptions pour retourner des réponses HTTP cohérentes.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les IllegalArgumentException (erreurs de validation/auth)
     * → 401 Unauthorized ou 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage();
        
        // 401 pour les erreurs d'authentification
        if (msg != null && (msg.contains("Invalid credentials") 
                || msg.contains("Invalid token")
                || msg.contains("Token expired")
                || msg.contains("Unauthorized"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", msg));
        }
        
        // 400 pour le reste
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", msg != null ? msg : "Bad request"));
    }

    /**
     * Gère les IllegalStateException (conflits métier)
     * → 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Conflict"));
    }

    /**
     * Gère les RuntimeException génériques
     * → 400 Bad Request ou 404 Not Found
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();
        
        // 404 pour les ressources introuvables
        if (msg != null && (msg.contains("not found") || msg.contains("Not found"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", msg));
        }
        
        // 400 pour le reste
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", msg != null ? msg : "Bad request"));
    }

    /**
     * Gère toutes les autres exceptions inattendues
     * → 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}
