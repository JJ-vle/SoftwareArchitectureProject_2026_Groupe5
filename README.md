# StartSpring
Minimal Spring REST project

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
