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

    ///////////////// USERS //////////////////

    // CREATE USER
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

    // READ ALL USERS
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestHeader("Authorization") String auth){

        return ResponseEntity.ok(adminService.getAllUsers(auth));
    }

    // READ ONE USER
    @GetMapping("/users/{identifier}")
    public ResponseEntity<?> getUser(
            @RequestHeader("Authorization") String auth,
            @PathVariable String identifier){

        return ResponseEntity.ok(adminService.getUser(auth, identifier));
    }

    // UPDATE USER
    @PutMapping("/users/{identifier}")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String auth,
            @PathVariable String identifier,
            @RequestBody Map<String,String> body){

        return ResponseEntity.ok(
                adminService.updateUser(auth, identifier, body.get("identifier"))
        );
    }

    // DELETE USER
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

    ///////////////// CREDENTIALS //////////////////

    // ADD CREDENTIAL 
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
    
    // GET USER CREDENTIALS
    @GetMapping("/users/{identifier}/credentials")
    public ResponseEntity<?> getCredentials(
            @RequestHeader("Authorization") String auth,
            @PathVariable String identifier){

        return ResponseEntity.ok(adminService.getCredentials(auth, identifier));
    }

    // DELETE CREDENTIAL
    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<?> deleteCredential(
            @RequestHeader("Authorization") String auth,
            @PathVariable String id){

        adminService.deleteCredential(auth, id);
        return ResponseEntity.ok().build();
    }

}
