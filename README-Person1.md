# Worklyn — Person 1 Backend (One-click local runner)

This folder packages the Person 1 backend from the supplied Worklyn source snapshot into the README's `backend/` layout and adds a Windows double-click launcher.

## One-click startup (Windows)

1. Install and start Docker Desktop (Linux containers / WSL 2).
2. Extract this folder somewhere writable.
3. Double-click **`START-PERSON1.bat`**.
4. On first run, enter an admin email and a strong password (12–72 UTF-8 bytes). The password prompt is hidden.
5. Wait for the green success message. The launcher builds the backend (including running Maven tests), starts MongoDB + Redis, bootstraps the first admin, disables bootstrap, restarts the backend, then verifies login/JWT `/me` and unauthenticated `401` behavior.

The launcher defaults to API port **8082** so it does not conflict with Worklyn on 8081 or Metasfresh on 8080. MongoDB and Redis are isolated inside this Compose project and do not publish host ports. A local `.env` file with a generated JWT secret is created automatically and is git-ignored.

To stop containers without deleting data, double-click `STOP-PERSON1.bat`. **Do not use `docker compose down -v` unless you intentionally want to erase the local database volumes and admin account.**

## What is included for Person 1

- Spring Boot 3.2.x / Java 17 application foundation; Mongo auditing and caching.
- MongoDB configuration and transaction manager; Compose starts a single-node replica set so MongoDB transactions can work locally.
- Redis template and cache manager; employee profile cache namespace is `emp:<id>` with one-hour TTL.
- Uniform API error responses and global exception mapping.
- BCrypt password hashing, JWT signing/verification, stateless Spring Security, authentication entry point and access-denied handler.
- `POST /api/v1/auth/login`, admin-only `POST /api/v1/auth/register`, authenticated `GET /api/v1/auth/me`.
- Controlled first-admin bootstrap. It is disabled by default, creates no second admin, stores only a BCrypt hash, and the launcher recreates the backend with bootstrap disabled immediately after provisioning.
- Employee create, get-by-ID, update, status transition, search, department/status filters, pagination, sorting, validation, role checks, duplicate handling, and user-profile linking.
- Focused unit tests including tests for bootstrap safety.

## API endpoints

Base URL: `http://localhost:8082` (unless `WORKLYN_HOST_PORT` is changed in `.env`).

| Method | Path | Access |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Public |
| `POST` | `/api/v1/auth/register` | ADMIN |
| `GET` | `/api/v1/auth/me` | Authenticated |
| `POST` | `/api/v1/employees` | ADMIN, HR |
| `GET` | `/api/v1/employees/{id}` | Authenticated; employees can view their own profile only |
| `PUT` | `/api/v1/employees/{id}` | ADMIN, HR |
| `PATCH` | `/api/v1/employees/{id}/status` | ADMIN, HR |
| `GET` | `/api/v1/employees` | ADMIN, HR, MANAGER; supports `q`, `department`, `status`, `page`, `size`, `sortBy`, `direction` |
| `GET` | `/actuator/health` | Public health check |

Auth example:

```json
POST /api/v1/auth/login
{
  "email": "your-admin-email@example.com",
  "password": "your-admin-password"
}
```

Use the returned `token` as `Authorization: Bearer <token>` for protected requests. Password hashes are not part of `UserResponse`.

## Tests and troubleshooting

- Run the automated tests locally: from PowerShell, `cd backend; .\mvnw.cmd clean test` (requires JDK 17 and Maven Wrapper download access).
- Check containers: `docker compose ps`.
- Backend logs: `docker compose logs --tail=150 backend`.
- Run smoke checks again: `powershell -NoProfile -ExecutionPolicy Bypass -File .\Test-Worklyn-Person1.ps1`.
- If host port 8082 is already occupied, edit `WORKLYN_HOST_PORT=8083` in `.env` and rerun the launcher. If you intentionally delete the Compose volumes, also delete `.person1-bootstrap-complete` so the launcher will offer the first-admin bootstrap again.

## Scope notes

The README's detailed Person 1 employee contract defines create, get, update, status transitions, and list/search/filter/pagination. Although F-03 is titled “Employee profile CRUD,” section 9.4 does not specify a DELETE endpoint or deletion semantics. This package does not invent a destructive delete operation; confirm the intended policy with the team before adding one. The README also describes initializing a default LeaveBalance during employee creation, but the Person 2 LeaveService is not included in this Person 1 package; the existing source leaves that as an integration point.

## Verification status

The folder is assembled from the supplied Worklyn source snapshot and includes a launcher that performs live smoke checks on your machine. This generation environment could not download Maven from Maven Central, so I could not independently run the full Maven build here. Treat the build and runtime as verified only after `START-PERSON1.bat` finishes successfully on your Docker Desktop machine.
