import Keycloak from 'keycloak-js';

// Configuration Keycloak
const keycloakConfig = {
  url: import.meta.env.VITE_APP_KEYCLOAK_URL|| "http://localhost:8080",
  realm: import.meta.env.VITE_APP_KEYCLOAK_REALM || "hirecraft",
  clientId: import.meta.env.VITE_APP_KEYCLOAK_CLIENT_ID || "react-client",
};

// Initialiser Keycloak
const keycloak = new Keycloak(keycloakConfig);

export default keycloak;