import React, { useEffect } from "react";
import { useAuth } from "@/KeycloakProvider";

interface ProtectedRouteProps {
  children: React.ReactElement;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  useEffect(() => {
    if (keycloakReady && !isAuthenticated) {
      keycloak.login(); 
    }
  }, [keycloakReady, isAuthenticated, keycloak]);

  if (!keycloakReady) {
    return <div>Chargement de Keycloak...</div>;
  }

  if (!isAuthenticated) {
    return <div>Redirection vers la page de connexion...</div>;
  }

  return children;
};

export default ProtectedRoute;
