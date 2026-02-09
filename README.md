# Mini API Spring Boot — Projet de démonstration

Résumé bref
- Suppression du champ `password` dans l'entité `User`.
- Ajout d'une entité `Credential` pour stocker des credentials (type, hash, actif).
- Hashing simple des secrets via `HashUtil` (SHA-256) — à remplacer par BCrypt en production.
- Tokens opaques (`AuthToken`) stockés en mémoire via `InMemoryStore` (création, validation, révocation possible).
- Initialisation des données via `@Component` `DataInitializer` et `@PostConstruct` (création d'un user `student1` et d'un admin `admin`).
- `AuthRestController` : login, logout, validate token.
- `AdminController` : endpoints protégés (création utilisateur, ajout credential, suppression utilisateur) vérifiant manuellement le rôle `ROLE_ADMIN`.

Démarrage de l'application
- Avec Maven (si installé) :
```bash
mvn -DskipTests spring-boot:run
```
- Ou avec la commande Java utilisée par VSCode (exemple) :
```bash
java @<classpath-argfile> demo.DemoApp --server.port=8081
```

Remarques :
- Par défaut l'application écoute sur le port 8080 ; si le port est occupé, utilisez `--server.port=8081`.
- Le hachage actuel est SHA-256 dans `HashUtil`. Pour la sécurité, remplacez par BCrypt.

Endpoints et exemples (curl)

1) Auth

- POST /auth/login
  - Description : login, renvoie un token opaque.
  - Body JSON : `{ "identifier": "admin", "password": "adminpass" }`
  - Exemple :
```bash
curl -i -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"adminpass"}'
```

- GET /auth/validate/{token}
  - Description : vérifie que le token existe et n'est pas expiré.
  - Exemple :
```bash
curl -i http://localhost:8081/auth/validate/<TOKEN>
```

- DELETE /auth/logout/{token}
  - Description : supprime (révoque) le token.
  - Exemple :
```bash
curl -i -X DELETE http://localhost:8081/auth/logout/<TOKEN>
```

2) Admin (protégé — nécessite `Authorization: Bearer <TOKEN>` d'un user avec `ROLE_ADMIN`)

- POST /admin/users
  - Description : crée un utilisateur (sans mot de passe).
  - Body JSON : `{ "identifier": "newuser" }`
  - Exemple :
```bash
curl -i -X POST http://localhost:8081/admin/users \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"identifier":"newuser"}'
```

- POST /admin/users/{identifier}/credentials
  - Description : ajoute un credential pour l'utilisateur (ex : mot de passe).
  - Body JSON : `{ "type": "PASSWORD", "secret": "newpass" }`
  - Exemple :
```bash
curl -i -X POST http://localhost:8081/admin/users/newuser/credentials \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"type":"PASSWORD","secret":"newpass"}'
```

- DELETE /admin/users/{identifier}
  - Description : supprime l'utilisateur, ses credentials et ses tokens.
  - Exemple :
```bash
curl -i -X DELETE http://localhost:8081/admin/users/newuser \
  -H "Authorization: Bearer <TOKEN>"
```

3) Produits (existant dans le projet)

- GET /products
  - Exemple : `curl http://localhost:8081/products`
- GET /products/{id}
  - Exemple : `curl http://localhost:8081/products/1`
- POST /products
  - Exemple :
```bash
curl -i -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{"id":"3","name":"Walnut"}'
```

Exemples PowerShell (Invoke-RestMethod)

- Login & récupérer token :
```powershell
$resp = Invoke-RestMethod -Method Post -Uri http://localhost:8081/auth/login -ContentType 'application/json' -Body '{"identifier":"admin","password":"adminpass"}'
$token = $resp.value
Write-Output $token
```

- Validate :
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8081/auth/validate/$token"
```


Exemples Bash (curl)

- Login & récupérer token :
```bash
curl -i -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"adminpass"}'
```

# Pour extraire la valeur du token si vous avez `jq` :
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{"identifier":"admin","password":"adminpass"}' | jq -r '.value')
echo $TOKEN
```

- Validate :
```bash
curl -i http://localhost:8081/auth/validate/<TOKEN>
```

- Logout :
```bash
curl -i -X DELETE http://localhost:8081/auth/logout/<TOKEN>
```

- Create user (admin) :
```bash
curl -i -X POST http://localhost:8081/admin/users \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"identifier":"newuser"}'
```

- Add credential to user :
```bash
curl -i -X POST http://localhost:8081/admin/users/newuser/credentials \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"type":"PASSWORD","secret":"newpass"}'
```

- Delete user :
```bash
curl -i -X DELETE http://localhost:8081/admin/users/newuser \
  -H "Authorization: Bearer <TOKEN>"
```


## Routes

| Méthode | URL                  | Arguments                     | Retour                           |
| ------- | -------------------- | ----------------------------- | -------------------------------- |
| POST    | /auth/login          | JSON `{identifier, password}` | 200 AuthToken / 401 Unauthorized |
| DELETE  | /auth/logout/{token} | Path `token`                  | 200 "Logged out successfully"    |
| GET     | /auth/users          | –                             | 200 Liste des utilisateurs       |

## Launch

```
mvn spring-boot:run
```
