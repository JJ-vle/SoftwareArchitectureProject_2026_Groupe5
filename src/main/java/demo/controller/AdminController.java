package demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.Credential;
import demo.model.User;
import demo.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {

        try {
            User user = adminService.createUser(auth, body.get("identifier"));
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/users/{identifier}/credentials")
    public ResponseEntity<Object> addCredential(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String identifier,
            @RequestBody Map<String, String> body) {

        try {
            Credential cred = adminService.addCredential(
                    auth,
                    identifier,
                    body.get("type"),
                    body.get("secret"));

            return ResponseEntity.status(HttpStatus.CREATED).body(cred);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{identifier}")
    public ResponseEntity<Object> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String identifier) {

        try {
            adminService.deleteUser(auth, identifier);
            return ResponseEntity.ok("User deleted");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
