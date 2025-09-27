import React, { createContext, useContext, useEffect, useState } from "react";
import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_APP_KEYCLOAK_URL || "http://localhost:8080",
  realm: import.meta.env.VITE_APP_KEYCLOAK_REALM || "hirecraft",
  clientId: import.meta.env.VITE_APP_KEYCLOAK_CLIENT_ID || "react-client",
});

interface AuthContextType {
  keycloak: Keycloak;
  isAuthenticated: boolean;
  keycloakReady: boolean;
}

const AuthContext = createContext<AuthContextType>({
  keycloak,
  isAuthenticated: false,
  keycloakReady: false,
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [keycloakReady, setKeycloakReady] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    keycloak
      .init({
        onLoad: "check-sso",
        silentCheckSsoRedirectUri:
          window.location.origin + "/silent-check-sso.html",
        pkceMethod: "S256",
      })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        setKeycloakReady(true);
        if (authenticated && keycloak.token) {
          localStorage.setItem("authToken", keycloak.token);
        }
        console.log("✅ Keycloak ready:", authenticated);
      })
      .catch((err) => {
        console.error("❌ Keycloak init error", err);
        setKeycloakReady(true); // éviter blocage infini
      });
  }, []);

  return (
    <AuthContext.Provider value={{ keycloak, isAuthenticated, keycloakReady }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
