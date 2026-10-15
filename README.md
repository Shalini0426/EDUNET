# SafeSphere

SafeSphere is a safety and emergency response application with an existing React/TypeScript frontend and a Spring Boot backend. The backend also provides a responsive, server-rendered Thymeleaf application for account registration, login, profile, dashboard, contacts, and feedback.

## Project structure

```text
.
├── .github/
├── .vscode/
├── frontend/
├── backend/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── .gitignore
└── README.md
```

## Run the frontend and backend on one address

The production frontend is served by Spring Boot, so the site, login, registration, dashboard, and backend APIs use the same origin (`http://localhost:8080`). Opening the root URL takes signed-out visitors directly to the existing themed login page; signed-in users go to their dashboard. The Vite server remains available for frontend-only development.

Build the frontend from the repository root:

```powershell
Set-Location frontend
npm install
npm run build
```

Then start Spring Boot:

```powershell
Set-Location safesphere\backend
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080/` to reach the login page. The React build is copied into Spring Boot's static resources during Maven's resource-processing phase. Rebuild the frontend after frontend changes and before starting or packaging the backend.

The backend requires Java 17 and MySQL. Create a database:

```sql
CREATE DATABASE safesphere_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the environment before starting Spring Boot; credentials are not stored in the repository. The Vite development server (`npm run dev`) still runs separately on port 5173 and proxies `/api` and `/ws` to port 8080.

Open `http://localhost:8080/register` to create an account. After entering the six-digit email code, you are signed in and redirected directly to the dashboard. The code expires after five minutes; no more than three codes can be sent within 15 minutes.

### Email OTP / SMTP

Configure an SMTP service before registering users:

```powershell
$env:MAIL_HOST = "your-smtp-host"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "your-smtp-user"
$env:MAIL_PASSWORD = "your-smtp-app-password"
$env:MAIL_FROM = "verified-sender@example.com"
$env:MAIL_SMTP_AUTH = "true"
$env:MAIL_SMTP_STARTTLS = "true"
```

SMTP credentials must come from a secret store or environment variables. If mail is not configured, account creation reports that delivery failed; no OTP is displayed in the browser or written to logs.

### Google sign-in

Create OAuth 2.0 web credentials in Google Cloud Console. Add `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI, then set:

```powershell
$env:GOOGLE_CLIENT_ID = "your-google-client-id"
$env:GOOGLE_CLIENT_SECRET = "your-google-client-secret"
```

Google sign-in is shown only when both settings are configured. The application requires Google to report a verified email and never grants administrator privileges through OAuth.

### Google Maps routes

The Plan a Journey page requests a walking route and SafeRide requests a driving route using the Google Maps JavaScript API. Enable **Maps JavaScript API** and **Directions API** in Google Cloud, enable billing, then create a browser API key restricted to the application’s HTTP referrers (for local use, `http://localhost:8080/*`). Set the key in the same PowerShell session used to start Spring Boot:

```powershell
$env:GOOGLE_MAPS_API_KEY = "your-restricted-browser-api-key"
```

The backend exposes this browser key only to authenticated users at `/api/maps/config`; Google Maps requires a client-side key, so HTTP-referrer and API restrictions are essential. Without the key, the route forms explain that Maps is not configured. Route estimates are not a safety assessment, SOS dispatch, or live journey/SafeRide tracking. Current location is requested only after the user selects **Use my current location**.

### Administrator bootstrap and production settings

For a new database, optionally provision the first administrator through environment variables before the first application start:

```powershell
$env:SAFESPHERE_ADMIN_EMAIL = "administrator@example.com"
$env:SAFESPHERE_ADMIN_PASSWORD = "a-unique-strong-password"
```

The bootstrap runs only when no administrator exists. Keep this password in a secret manager and remove the bootstrap environment values after setup. Administrator user management is available through `/api/users`; administrator accounts cannot delete themselves.

`SESSION_COOKIE_SECURE` defaults to `false` for local HTTP development. Set it to `true` behind HTTPS in production. Sessions use HTTP-only, SameSite=Lax cookies, CSRF protection, session-fixation protection, and a three-session concurrency limit. The default `spring.jpa.hibernate.ddl-auto=update` is convenient for local development; use reviewed schema migrations for production database changes.

### Added routes

| Feature | Route |
|---|---|
| Server-rendered login / registration / OTP | `/login`, `/register`, `/verify-email` |
| Member dashboard and profile | `/dashboard`, `/profile` |
| Emergency-contact CRUD | `/contacts`, `/api/emergency-contacts` |
| Feedback CRUD and average rating | `/feedback`, `/api/feedback`, `GET /api/feedback/rating` |
| Incident CRUD | `/api/incidents` |
| Administrator user CRUD | `/api/users` |

All mutating browser and API requests require CSRF protection. Member records are scoped to their owner; administrator operations require the `ADMIN` role. Existing safe-zone, risk, analytics, AI, and incident-review functionality remains in the backend; protected APIs now require a signed-in user or an administrator as appropriate.

Run backend tests with:

```powershell
Set-Location backend
.\mvnw.cmd test
```

Tests use an isolated in-memory H2 database and do not require MySQL, SMTP, or Google credentials.
