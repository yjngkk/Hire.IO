
# Hire.io Backend (`hrhire`)

## Overview

The `hrhire` backend is a Java Spring Boot application powering the Hire.io platform. It manages candidate applications, interviews, onboarding, tests, and integrates advanced AI for HR workflows. The backend is containerized for easy deployment and secured via Keycloak authentication.

---


## Technologies Used

- **Java** (Spring Boot)
- **Maven** (build tool)
- **Docker** (containerization)
- **Docker Compose** (multi-service orchestration)
- **Keycloak** (authentication)

---


## AI Integration & Unique Contributions

The backend features a custom AI module that enhances the hiring process:

- **ONNX Models & Tokenizers:**
   - Fine-tuned MiniLM-L6-v2 model for resume and job offer semantic analysis
   - Tokenizer and model are stored in `src/main/resources/models`
- **RESTful AI APIs:**
   - Endpoints for generating job offers and candidate tests using Large Language Models (LLMs) (OpenAI, Mistral, Groq, Hugging Face)
   - Automated content creation for HR workflows
- **Value Added:**
   - Improves matching accuracy between candidates and job offers
   - Enables dynamic, AI-powered test and offer generation
   - Represents a unique, advanced contribution to the project

For details, see the backend source and API documentation.

---


## Folder Structure (Backend)

```
hrhire/
├── src/
│   ├── main/
│   │   ├── java/            # Java source code
│   │   └── resources/
│   │       ├── models/      # ONNX models & tokenizers
│   │       └── ...          # Config, templates, etc.
│   └── test/                # Test code
├── target/                  # Build output
├── Dockerfile               # Backend Docker config
├── docker-compose.yaml      # Multi-service orchestration
├── pom.xml                  # Maven config
└── ...                      # Other backend files
```

---


## Setup & Installation

### Prerequisites

- Java 17+ & Maven
- Docker & Docker Compose

### Backend Setup

```sh
cd hrhire
./mvnw spring-boot:run
```

### Docker Compose (Backend Only)

```sh
cd hrhire
docker-compose up --build
```

---


## Usage

- Backend API runs at `http://localhost:8080` (default Spring Boot port)
- Authentication is enforced via Keycloak (see below for setup)

---


### Keycloak Authentication Setup (Backend)

1. **Install and Run Keycloak**
   - Download Keycloak from https://www.keycloak.org/downloads
   - Run Keycloak:
     ```sh
     ./bin/standalone.sh
     ```
     or on Windows:
     ```sh
     .\\bin\\standalone.bat
     ```

2. **Access Keycloak Admin Console**
   - Go to `http://localhost:8080` (default Keycloak port)
   - Login with admin credentials (set during first startup)

3. **Create a Realm**
   - In the admin console, click "Add Realm"
   - Enter a name (e.g., `hireio`), save

4. **Create a Client**
   - In your realm, go to "Clients" > "Create"
   - Set Client ID (e.g., `hireio-backend`)
   - Set Client Protocol: `openid-connect`
   - Set Root URL: `http://localhost:8080`
   - Save and configure access settings (enable "Standard Flow", set redirect URIs for backend endpoints)

5. **Create a User**
   - Go to "Users" > "Add User"
   - Enter username, email, etc.
   - Save, then set a password in "Credentials" tab

6. **Configure Backend**
   - Update your backend Keycloak configuration (see `src/main/resources/application.properties`)
   - Set realm, client ID, and Keycloak server URL

7. **Test Authentication**
   - Start the backend
   - Access protected endpoints using the created user

---


## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---


## License

Specify your license here (e.g., MIT, Apache 2.0)

---


## Contact

For questions or support, contact the project maintainer.
