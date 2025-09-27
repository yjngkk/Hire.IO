import { StatCard } from "@/components/dashboard/StatsCard";
import { RecentActivity } from "@/components/dashboard/RecentActivity";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, Search, User, Calendar, Clock, RefreshCw, Video, MapPin } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useDashboardStats } from "@/hooks/useDashboardStats";
import { useUpcomingEvents } from "@/hooks/useUpcomingEvents";

export function Dashboard() {
  const navigate = useNavigate();
  const { 
    activeJobs, 
    totalApplications, 
    totalInterviews, 
    activeTests,
    weeklyApplications,
    weeklyInterviews,
    weeklyTestsSent,
    weeklyJobs,
    jobsWeeklyChange,
    applicationsWeeklyChange,
    interviewsWeeklyChange,
    testsWeeklyChange,
    loading,
    error,
    refresh
  } = useDashboardStats();

  const { 
    events: upcomingEvents, 
    loading: eventsLoading, 
    error: eventsError,
    refresh: refreshEvents 
  } = useUpcomingEvents(7);

  // Fonction pour gérer le clic sur un événement
  const handleEventClick = (event: any) => {
    if (event.type === 'entretien') {
      navigate(`/interviews`); // ou `/interviews/${event.entretienId}` si vous avez une page de détail
    } else {
      navigate(`/onboarding`);
    }
  };

  // Fonction pour obtenir l'icône appropriée selon le type d'événement
  const getEventIcon = (event: any) => {
    if (event.type === 'entretien') {
      return event.meetLink ? <Video className="h-4 w-4" /> : <MapPin className="h-4 w-4" />;
    }
    return <Calendar className="h-4 w-4" />;
  };

  // Fonction pour obtenir la couleur du statut
  const getStatusColor = (event: any) => {
    if (event.type === 'entretien') {
      switch (event.status) {
        case 'prévu':
          return 'bg-blue-100 text-blue-700';
        case 'en_cours':
          return 'bg-green-100 text-green-700';
        case 'pause':
          return 'bg-yellow-100 text-yellow-700';
        default:
          return 'bg-gray-100 text-gray-700';
      }
    }
    
    // Pour les événements de procédure
    if (event.relativeTimeDescription === 'En retard') {
      return 'bg-red-100 text-red-700';
    } else if (event.relativeTimeDescription === 'Aujourd\'hui') {
      return 'bg-orange-100 text-orange-700';
    } else if (event.relativeTimeDescription === 'Demain') {
      return 'bg-yellow-100 text-yellow-700';
    } else {
      return 'bg-blue-100 text-blue-700';
    }
  };

  if (error) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
            <p className="text-muted-foreground mt-1">
              Vue d'ensemble de votre activité de recrutement
            </p>
          </div>
          <Button onClick={refresh} className="gap-2" variant="outline">
            <RefreshCw className="h-4 w-4" />
            Actualiser
          </Button>
        </div>
        
        <Card className="p-6">
          <div className="text-center">
            <h3 className="text-lg font-semibold text-red-600 mb-2">
              Erreur de chargement
            </h3>
            <p className="text-muted-foreground mb-4">{error}</p>
            <Button onClick={refresh} variant="outline">
              Réessayer
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
          <p className="text-muted-foreground mt-1">
            Vue d'ensemble de votre activité de recrutement
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={refresh} variant="outline" size="sm" className="gap-2">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            Actualiser
          </Button>
          <Button onClick={() => navigate("/create-job")} className="gap-2">
            <Plus className="h-4 w-4" />
            Créer une offre
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Offres publiées"
          value={loading ? "..." : activeJobs}
          description={loading ? "Chargement..." : `${weeklyJobs > 0 ? '': ''}${Math.abs(weeklyJobs)} nouvelles cette semaine`}
          icon={Search}
          trend={loading ? undefined : { value: jobsWeeklyChange, isPositive: jobsWeeklyChange >= 0 }}
        />
        <StatCard
          title="Candidatures"
          value={loading ? "..." : totalApplications}
          description={loading ? "Chargement..." : `${weeklyApplications} ont postulés cette semaine`}
          icon={User}
          trend={loading ? undefined : { value: applicationsWeeklyChange, isPositive: applicationsWeeklyChange >= 0 }}
        />
        <StatCard
          title="Entretiens"
          value={loading ? "..." : totalInterviews}
          description={loading ? "Chargement..." : `${weeklyInterviews} planifiés cette semaine`}
          icon={Calendar}
          trend={loading ? undefined : { value: interviewsWeeklyChange, isPositive: interviewsWeeklyChange >= 0 }}
        />
        <StatCard
          title="Tests en cours"
          value={loading ? "..." : activeTests}
          description={loading ? "Chargement..." : `${weeklyTestsSent} envoyés cette semaine`}
          icon={Clock}
          trend={loading ? undefined : { value: testsWeeklyChange, isPositive: testsWeeklyChange >= 0 }}
        />
      </div>

      {/* Main Content Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <RecentActivity />
        </div>
        
        <div className="space-y-6">
          {/* Quick Actions */}
          <Card className="animate-fade-in">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">Actions rapides</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button 
                variant="outline" 
                className="w-full justify-start gap-2"
                onClick={() => navigate("/create-job")}
              >
                <Plus className="h-4 w-4" />
                Créer une nouvelle offre
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start gap-2"
                onClick={() => navigate("/applications")}
              >
                <User className="h-4 w-4" />
                Voir les candidatures
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start gap-2"
                onClick={() => navigate("/interviews")}
              >
                <Calendar className="h-4 w-4" />
                Planifier un entretien
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start gap-2"
                onClick={() => navigate("/tests")}
              >
                <Clock className="h-4 w-4" />
                Envoyer un test
              </Button>
            </CardContent>
          </Card>

          {/* Upcoming Events with Interviews */}
          <Card className="animate-fade-in">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-lg font-semibold">Prochains événements</CardTitle>
              {eventsError && (
                <Button 
                  onClick={refreshEvents} 
                  variant="ghost" 
                  size="sm"
                  className="h-6 w-6 p-0"
                >
                  <RefreshCw className="h-3 w-3" />
                </Button>
              )}
            </CardHeader>
            <CardContent className="space-y-3">
              {eventsLoading ? (
                <div className="space-y-3">
                  {[1, 2, 3].map(i => (
                    <div key={i} className="p-3 rounded-lg border border-border">
                      <div className="h-4 bg-muted animate-pulse rounded mb-2"></div>
                      <div className="h-3 bg-muted animate-pulse rounded mb-1"></div>
                      <div className="h-3 bg-muted animate-pulse rounded"></div>
                    </div>
                  ))}
                </div>
              ) : eventsError ? (
                <div className="p-4 text-center">
                  <p className="text-sm text-red-600 mb-2">Erreur de chargement</p>
                  <p className="text-xs text-muted-foreground mb-3">{eventsError}</p>
                  <Button onClick={refreshEvents} variant="outline" size="sm">
                    Réessayer
                  </Button>
                </div>
              ) : upcomingEvents.length === 0 ? (
                <div className="p-4 text-center">
                  <p className="text-sm text-muted-foreground">
                    Aucun événement à venir dans les 7 prochains jours
                  </p>
                </div>
              ) : (
                upcomingEvents.map((event, index) => (
                  <div 
                    key={event.procedureId || event.entretienId || index} 
                    className="p-3 rounded-lg border border-border hover:bg-muted/50 transition-colors cursor-pointer"
                    onClick={() => handleEventClick(event)}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          {getEventIcon(event)}
                          <p className="text-sm font-medium text-foreground">
                            {event.title}
                          </p>
                        </div>
                        <p className="text-xs text-muted-foreground">
                          {event.candidatName} - {event.candidatPoste}
                        </p>
                        <div className="flex items-center gap-2 mt-1">
                          <p className="text-xs text-muted-foreground">
                            {event.formattedDueDate}
                          </p>
                          <span className="text-xs text-muted-foreground">•</span>
                          <p className="text-xs font-medium">
                            {event.responsible}
                          </p>
                          {event.type === 'entretien' && event.meetLink && (
                            <>
                              <span className="text-xs text-muted-foreground">•</span>
                              <span className="text-xs text-blue-600">En ligne</span>
                            </>
                          )}
                        </div>
                      </div>
                      <div className="flex flex-col items-end gap-1">
                        <span 
                          className={`text-xs px-2 py-1 rounded-full ${getStatusColor(event)}`}
                        >
                          {event.relativeTimeDescription}
                        </span>
                        {event.type === 'entretien' && (
                          <span className="text-xs px-2 py-1 rounded-full bg-gray-100 text-gray-600">
                            Entretien
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}