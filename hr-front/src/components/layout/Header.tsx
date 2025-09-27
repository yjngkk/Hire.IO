
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";
import { User } from "lucide-react";
import { useAuth } from "@/KeycloakProvider";

export function Header() {
    const { keycloak, keycloakReady, isAuthenticated } = useAuth();

  function handleLogout() {
    keycloak.logout({
      redirectUri: window.location.origin + '/'
    });
  }
  return (
    <header className="h-16 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
      <div className="flex items-center justify-between h-full px-4">
        <div className="flex items-center gap-4">
          <SidebarTrigger className="h-8 w-8" />
          <div className="hidden md:block">
            <h2 className="text-lg font-semibold text-foreground">
              Plateforme de Recrutement
            </h2>
            <p className="text-sm text-muted-foreground">
              Gérez efficacement vos processus RH
            </p>
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" className="gap-2">
            <User className="h-4 w-4" />
            <span className="hidden sm:inline">Profil</span>
          </Button>
          <Button variant="ghost" size="sm" className="gap-2" onClick={handleLogout}>
            <span className="hidden sm:inline">Se déconnecter</span>
             </Button>
        </div>
      </div>
    </header>
  );
}
