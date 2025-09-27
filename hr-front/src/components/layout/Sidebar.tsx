
import { NavLink, useLocation } from "react-router-dom";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";
import { 
  Calendar,
  Clock,
  Plus,
  Search,
  User,
  FileText,
  Settings
} from "lucide-react";
import { useTestStatus } from "@/contexts/TestStatusContext";

const navigationItems = [
  {
    title: "Dashboard",
    url: "/",
    icon: Calendar,
    section: "main"
  },
  {
    title: "Créer une offre",
    url: "/create-job",
    icon: Plus,
    section: "main"
  },
  {
    title: "Offres d'emploi",
    url: "/jobs",
    icon: Search,
    section: "main"
  },
  {
    title: "Candidatures",
    url: "/applications",
    icon: User,
    section: "recruitment"
  },
  {
    title: "Tests & Évaluations",
    url: "/tests",
    icon: Clock,
    section: "recruitment"
  },
  {
    title: "Entretiens",
    url: "/interviews",
    icon: Calendar,
    section: "recruitment"
  },
  {
    title: "Onboarding",
    url: "/onboarding",
    icon: FileText,
    section: "management"
  },
  {
    title: "Paramètres",
    url: "/settings",
    icon: Settings,
    section: "admin"
  }
];

export function AppSidebar() {
  const { state } = useSidebar();
  const location = useLocation();
  const currentPath = location.pathname;
  const { testStarted, testSubmitted } = useTestStatus();

  const isActive = (path: string) => {
    if (path === "/") {
      return currentPath === "/";
    }
    return currentPath.startsWith(path);
  };

  const getNavClassName = (path: string) => {
    return isActive(path) 
      ? "bg-primary text-primary-foreground font-medium" 
      : "hover:bg-muted/50 text-muted-foreground hover:text-foreground";
  };

  const groupedItems = {
    main: navigationItems.filter(item => item.section === "main"),
    recruitment: navigationItems.filter(item => item.section === "recruitment"),
    management: navigationItems.filter(item => item.section === "management"),
    admin: navigationItems.filter(item => item.section === "admin")
  };

  const collapsed = state === "collapsed";

  return (
    <Sidebar className={collapsed ? "w-16" : "w-64"} collapsible="icon">
      <SidebarContent className="pt-4">
        <div className="px-4 pb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center text-primary-foreground font-bold text-sm">
              H
            </div>
            {!collapsed && (
              <div>
                <h1 className="text-lg font-bold text-primary">Hire.IO</h1>
                <p className="text-xs text-muted-foreground">Plateforme RH</p>
              </div>
            )}
          </div>
        </div>

        <SidebarGroup>
          <SidebarGroupLabel>Principal</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {groupedItems.main.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink 
                      to={item.url} 
                      className={getNavClassName(item.url)}
                      onClick={(e) => { if (testStarted && !testSubmitted) { e.preventDefault(); alert("Veuillez soumettre le test avant de naviguer ailleurs."); } }}
                    >
                      <item.icon className="h-4 w-4" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupLabel>Recrutement</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {groupedItems.recruitment.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink 
                      to={item.url} 
                      className={getNavClassName(item.url)}
                      onClick={(e) => { if (testStarted && !testSubmitted) { e.preventDefault(); alert("Veuillez soumettre le test avant de naviguer ailleurs."); } }}
                    >
                      <item.icon className="h-4 w-4" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupLabel>Gestion</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {groupedItems.management.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink 
                      to={item.url} 
                      className={getNavClassName(item.url)}
                      onClick={(e) => { if (testStarted && !testSubmitted) { e.preventDefault(); alert("Veuillez soumettre le test avant de naviguer ailleurs."); } }}
                    >
                      <item.icon className="h-4 w-4" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupLabel>Administration</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {groupedItems.admin.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink 
                      to={item.url} 
                      className={getNavClassName(item.url)}
                      onClick={(e) => { if (testStarted && !testSubmitted) { e.preventDefault(); alert("Veuillez soumettre le test avant de naviguer ailleurs."); } }}
                    >
                      <item.icon className="h-4 w-4" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
