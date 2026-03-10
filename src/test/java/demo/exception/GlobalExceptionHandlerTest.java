package demo.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // --- Tests IllegalArgumentException ---

    @Test
    void invalidCredentials_returns401() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Invalid credentials", resp.getBody().get("error"));
    }

    @Test
    void invalidToken_returns401() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleIllegalArgument(new IllegalArgumentException("Invalid token"));
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void tokenExpired_returns401() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleIllegalArgument(new IllegalArgumentException("Token expired"));
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void genericIllegalArgument_returns400() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleIllegalArgument(new IllegalArgumentException("some error"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // --- Tests IllegalStateException ---

    @Test
    void illegalState_returns409() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleIllegalState(new IllegalStateException("user exists"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("user exists", resp.getBody().get("error"));
    }

    // --- Tests RuntimeException ---

    @Test
    void notFound_returns404() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleRuntime(new RuntimeException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void genericRuntime_returns400() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleRuntime(new RuntimeException("something wrong"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // --- Tests Exception générale ---

    @Test
    void unexpectedException_returns500() {
        ResponseEntity<Map<String, String>> resp = 
                handler.handleGeneral(new Exception("crash"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Internal server error", resp.getBody().get("error"));
    }
}
