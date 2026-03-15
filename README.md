# Mini API Spring Boot - Projet de démonstration

## Résumé du projet

**Architecture générale**
- Application Spring Boot unique jouant le rôle de backend, exposée derrière un reverse proxy Nginx.
- Nginx gère le contrôle d'accès par subrequest (`auth_request`) avant de router vers les services A et B.
- RabbitMQ assure la communication asynchrone entre le service d'authentification et le service de notification (email).
- MailHog simule un serveur SMTP pour réceptionner et visualiser les emails de test.

**Authentification (`AuthService`, `AuthRestController`)**
- Les mots de passe ne sont pas stockés dans `User` : une entité dédiée `Credential` (type, hash BCrypt, actif) les isole.
- Hachage des secrets via `HashUtil` utilisant **BCrypt** (`spring-security-crypto`).
- Tokens opaques (`AuthToken`) stockés en base H2 via `AuthTokenRepository` (création, validation, expiration, révocation).
- Inscription (`/auth/register`) avec envoi d'un email de vérification via RabbitMQ → MailHog.
- Vérification d'email (`/auth/verify`) avec token à usage unique et expiration 15 min.

**Administration (`AdminService`, `AdminController`)**
- Endpoints protégés (création/modification/suppression d'utilisateurs, gestion des credentials) réservés au rôle `ROLE_ADMIN`.
- Vérification manuelle du rôle via le token Bearer fourni dans le header `Authorization`.

**Contrôle d'accès par service**
- Chaque utilisateur possède un ou plusieurs droits `SERVICE_A` / `SERVICE_B` en plus de son rôle.
- Nginx vérifie ces droits avant chaque requête vers `/a/` ou `/b/` via `GET /auth/validate`.

**Initialisation des données (`DataInitializer`)**
- `student1` : accès service A uniquement (`SERVICE_A`), mot de passe `password`.
- `student2` : accès service B uniquement (`SERVICE_B`), mot de passe `password`.
- `admin` : accès complet A + B + rôle `ROLE_ADMIN`, mot de passe `adminpass`.

## Launch

```
mvn spring-boot:run
```

## Demarrage Et Tests (Docker + Maven)

### 1) Commandes pour lancer le projet

Lancement en 2 terminaux:
d'abord
1. Infrastructure Docker (RabbitMQ, MailHog, Nginx):
```bash
docker compose up
```
puis
2. Application Spring Boot:
```bash
mvn -DskipTests spring-boot:run
```

Explications:
- `docker compose up` demarre les services techniques utilises par l'application:
  - RabbitMQ (broker de messages)
  - MailHog (boite mail de test)
  - Nginx (reverse proxy)
- `mvn -DskipTests spring-boot:run` compile et lance l'API Spring Boot sans executer les tests unitaires/integration.
### 2) Explication des 4 URLs qui servent au projet

- `http://localhost:15672/#/`
  - Interface web RabbitMQ (management).
  - Permet de voir exchanges, queues, bindings, messages, consommateurs.
  - Identifiants par defaut (compose actuel): `guest / guest`.

- `http://localhost:8025/`
  - Interface web MailHog.
  - Permet de voir les emails envoyes par l'application (ex: email de verification).

- `http://localhost:8080/`
  - Port direct de l'application Spring Boot (Tomcat).
  - Acces sans passer par Nginx.
  - c'est donc un url qui sert au managment des requetes il est normal qu'il affiche {"error":"Internal server error"}

- `http://localhost:80/`
  - Entree Nginx (reverse proxy).
  - Les routes `/auth`, `/a`, `/b` passent par cette couche.
  - C'est l'URL a utiliser pour tester le comportement "gateway" (auth_request, filtrage des acces).
  - c'est donc un url qui sert au managment des requetes il est normal qu'il affiche {"error":"Internal server error"}

### 3) Commandes pour tester les endpoints

#### PowerShell (via Nginx - port 80)

Script de test:

```powershell
$body = @{ identifier="student1"; password="password" } | ConvertTo-Json
$response = Invoke-RestMethod -Uri http://localhost/auth/login -Method POST -Body $body -ContentType "application/json"
$token = $response.value
```
Explication:
- On construit d'abord un JSON de login pour l'utilisateur `student1`.
- On envoie ce JSON a `POST /auth/login` via Nginx (`http://localhost`).
- La reponse contient un token JWT/opaque dans `value`, stocke dans `$token` pour les appels proteges suivants.

```powershell
Invoke-RestMethod -Uri http://localhost/a/products -Method GET -Headers @{ Authorization = "Bearer $token" }
```
Explication:
- Cette commande appelle le service A avec le token de `student1`.
- Le header `Authorization: Bearer <token>` est obligatoire pour passer le controle d'acces de la gateway.
- Comme `student1` possede le droit `SERVICE_A`, la requete doit reussir.

```powershell
Invoke-RestMethod -Uri http://localhost/b/products -Method GET -Headers @{ Authorization = "Bearer $token" }
```
Explication:
- Cette commande teste l'acces de `student1` au service B.
- Le token est valide, mais l'autorite `SERVICE_B` est absente pour cet utilisateur.
- Le resultat attendu est donc un refus d'acces (`Forbidden`).

```powershell
$bodyAdmin = @{ identifier="admin"; password="adminpass" } | ConvertTo-Json
$responseAdmin = Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -Body $bodyAdmin -ContentType "application/json"
$adminToken = $responseAdmin.value
```
ou
```powershell
$resp = Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/login -ContentType 'application/json' -Body '{"identifier":"admin","password":"adminpass"}'
$token = $resp.value
Write-Output $token
```
Explication:
- On repete la sequence de login, cette fois avec le compte `admin`.
- Le token recupere est stocke dans `$adminToken`.
- Ce token porte les autorites admin (et les acces services A/B dans l'initialisation actuelle).

```powershell
Invoke-RestMethod -Uri http://localhost/b/products -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
```
Explication:
- Cette commande verifie qu'un admin peut acceder au service B.
- Le token admin est envoye dans le header `Authorization`.
- Le resultat attendu est une reponse positive (pas de `Forbidden`).

**Variante : appel direct a Spring Boot sans passer par Nginx**

```powershell
Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -Body $body -ContentType "application/json"
```
Explication:
- Meme appel de login que precedemment, mais en contactant directement Spring Boot sur le port `8080` au lieu de passer par Nginx (`localhost:80`).
- Utile pour tester le backend en isolation, sans la couche reverse proxy ni le controle d'acces de la gateway.
- Le token retourne est identique et peut etre reutilise dans les appels suivants.

#### Bash / curl (direct Spring Boot - port 8080)

**Authentification**
- POST /auth/login
  - Login et recuperer le token (admin) :
  - Description : login, renvoie un token opaque.
  - Body JSON : `{ "identifier": "admin", "password": "adminpass" }`
  - Exemple :

```bash
# Afficher la reponse brute
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"adminpass"}'

# Extraire uniquement la valeur du token avec jq :
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"adminpass"}' | jq -r '.value')
echo $TOKEN
```
- GET /auth/validate/{token}
  - Description : vérifie que le token existe et n'est pas expiré.
  - Exemple :
```bash
curl -i http://localhost:8080/auth/validate \
  -H "Authorization: Bearer <TOKEN>"
```

- DELETE /auth/logout/{token}
  - Description : supprime (révoque) le token.
  - Exemple :
```bash
curl -i -X DELETE http://localhost:8080/auth/logout/<TOKEN>
```

**Administration** - necessite un token avec `ROLE_ADMIN`

- POST /admin/users
  - Description : crée un utilisateur (sans mot de passe).
  - Body JSON : `{ "identifier": "newuser" }`
  - Exemple :
```bash
curl -i -X POST http://localhost:8080/admin/users \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"identifier":"newuser"}'
```

- POST /admin/users/{identifier}/credentials
  - Description : ajoute un credential pour l'utilisateur (ex : mot de passe).
  - Body JSON : `{ "type": "PASSWORD", "secret": "newpass" }`
  - Exemple :
```bash
curl -i -X POST http://localhost:8080/admin/users/newuser/credentials \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"type":"PASSWORD","secret":"newpass"}'
```

- DELETE /admin/users/{identifier}
  - Description : supprime l'utilisateur, ses credentials et ses tokens.
  - Exemple :
```bash
curl -i -X DELETE http://localhost:8080/admin/users/newuser \
  -H "Authorization: Bearer <TOKEN>"
```

**Produits**

- Lister les produits du service A :
```bash
curl http://localhost:8080/a
```

- Recuperer un produit par id :
```bash
curl http://localhost:8080/a/1
```

- Creer un produit :
```bash
curl -i -X POST http://localhost:8080/a \
  -H "Content-Type: application/json" \
  -d '{"id":"3","name":"Walnut"}'
```

- Produits du service B :
```bash
curl http://localhost:8080/b/products
```

## Routes

| Méthode | URL                                         | Arguments / Headers                                            | Retour / Description                                                                 |
| ------- | ------------------------------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| POST    | /auth/login                                 | Body JSON `{ "identifier", "password" }`                   | 200 AuthToken (JSON) / 401 Unauthorized - Authentifie un utilisateur               |
| DELETE  | /auth/logout/{token}                        | Path `token`                                                   | 200 "Logged out successfully" - Révoque le token                                 |
| GET     | /auth/users                                 | -                                                              | 200 Liste des `User` (endpoint de debug)                                           |
| POST    | /auth/register                              | Body JSON `{ "email", "password" }`                        | 201 Created / 409 Conflict / 400 Bad Request - Inscription + envoi email vérif.    |
| GET     | /auth/verify                                | Query params `tokenId`, `token`                                | 200 "Email verified successfully" / 400 Bad Request - Vérification d'email       |
| GET     | /auth/validate                              | Header `Authorization: Bearer <token>` (opt. `X-Target-Service`) | 200 / 401 / 403 - Validation utilisée par la gateway (Nginx); vérifie droits service |

| Méthode | URL                                         | Arguments / Headers                                            | Retour / Description                                                                 |
| ------- | ------------------------------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| POST    | /admin/users                                | Header `Authorization: Bearer <admin-token>`, Body `{identifier}` | 201 User / 403 Forbidden / 409 Conflict / 400 Bad Request - Crée un utilisateur    |
| GET     | /admin/users                                | Header `Authorization`                                         | 200 Liste des utilisateurs - Endpoints d'administration                           |
| GET     | /admin/users/{identifier}                   | Header `Authorization`                                         | 200 User - Récupère un utilisateur                                                 |
| PUT     | /admin/users/{identifier}                   | Header `Authorization`, Body `{identifier}`                    | 200 Updated User - Modifie l'identifiant de l'utilisateur                         |
| DELETE  | /admin/users/{identifier}                   | Header `Authorization`                                         | 200 / 403 / 404 - Supprime l'utilisateur et ses données                            |

| Méthode | URL                                         | Arguments / Headers                                            | Retour / Description                                                                 |
| ------- | ------------------------------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| POST    | /admin/users/{identifier}/credentials       | Header `Authorization`, Body `{ "type", "secret" }`        | 201 Credential / 403 / 400 - Ajoute un credential (ex: password)                  |
| GET     | /admin/users/{identifier}/credentials       | Header `Authorization`                                         | 200 Liste des credentials                                                         |
| DELETE  | /admin/credentials/{id}                     | Header `Authorization`                                         | 200 - Supprime le credential identifié                                             |

| Méthode | URL                                         | Arguments / Headers                                            | Retour / Description                                                                 |
| ------- | ------------------------------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| GET     | /a/products                                 | Header `Authorization` (via Nginx)                             | 200 "Products from A" - Service A (exposé derrière Nginx)                        |
| GET     | /b/products                                 | Header `Authorization` (via Nginx)                             | 200 "Products from B" - Service B (exposé derrière Nginx)                        |

Notes:
- L'API est conçue pour fonctionner derrière Nginx. La gateway appelle `/auth/validate` en lui passant l'en-tête `X-Target-Service` (valeurs: `A` ou `B`) pour vérifier que le token possède l'autorité `SERVICE_A` ou `SERVICE_B`.
- Les endpoints d'administration requièrent un token avec `ROLE_ADMIN`.
- Les mots de passe sont gérés via l'entité `Credential` (hash BCrypt) ; les tokens opaques sont persistés dans `AuthToken`.