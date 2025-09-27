import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Clock, User, Calendar, RefreshCw, Mail, Phone, AlertCircle, CheckCircle, TrendingUp, Filter, FileText, Briefcase } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useRecentActivities } from "@/hooks/useRecentActivities";
import { useState } from "react";

const getActivityIcon = (type: string) => {
  switch (type) {
    case "application":
      return User;
    case "interview":
      return Calendar;
    case "test":
      return Clock;
    case "offer_published":
    case "published_offer":
      return Briefcase;
    case "offer_created":
      return FileText;
    default:
      return User;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case "nouveau":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "en cours":
      return "bg-orange-100 text-orange-800 border-orange-200";
    case "planifié":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "complété":
    case "entretien passé":
      return "bg-green-100 text-green-800 border-green-200";
    case "accepté":
      return "bg-emerald-100 text-emerald-800 border-emerald-200";
    case "rejeté":
      return "bg-red-100 text-red-800 border-red-200";
    case "publié":
      return "bg-purple-100 text-purple-800 border-purple-200";
    case "créé":
      return "bg-indigo-100 text-indigo-800 border-indigo-200";
    case "brouillon":
      return "bg-gray-100 text-gray-800 border-gray-200";
    default:
      return "bg-gray-100 text-gray-800 border-gray-200";
  }
};

const getActivityTypeLabel = (type: string) => {
  switch (type) {
    case "application":
      return "Candidature";
    case "interview":
      return "Entretien";
    case "test":
      return "Test";
    case "offer_published":
    case "published_offer":
      return "Offre publiée";
    case "offer_created":
      return "Offre créée";
    default:
      return "Activité";
  }
};

interface RecentActivityProps {
  limit?: number;
  showStats?: boolean;
  showFilters?: boolean;
  includeOffers?: boolean; // New prop to control if offers should be included
}

export function RecentActivity({ 
  limit = 5, 
  showStats = true, 
  showFilters = false,
  includeOffers = true // Default to true to include offers
}: RecentActivityProps) {
  const { 
    activities, 
    stats, 
    loading, 
    error, 
    refresh, 
    refreshStats, 
    fetchFromLastDays, 
    isEmpty 
  } = useRecentActivities(limit, includeOffers);

  const [filterDays, setFilterDays] = useState<string>("all");
  const [activityTypeFilter, setActivityTypeFilter] = useState<string>("all");

  const handleFilterChange = (value: string) => {
    setFilterDays(value);
    if (value === "all") {
      refresh();
    } else {
      const days = parseInt(value);
      fetchFromLastDays(days, limit);
    }
  };

  const handleActivityTypeFilterChange = (value: string) => {
    setActivityTypeFilter(value);
    // This would trigger a re-fetch with activity type filter
    // You'll need to implement this in your API
  };

  // Filter activities based on type if a filter is selected
  const filteredActivities = activityTypeFilter === "all" 
    ? activities 
    : activities.filter(activity => activity.type === activityTypeFilter);

  return (
    <div className="space-y-4">
      
      {/* Main Activities Card */}
      <Card className="animate-fade-in">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-semibold flex items-center gap-2">
              {loading ? (
                <RefreshCw className="h-5 w-5 animate-spin text-blue-500" />
              ) : filteredActivities.length > 0 ? (
                <CheckCircle className="h-5 w-5 text-green-500" />
              ) : (
                <User className="h-5 w-5 text-gray-400" />
              )}
              Activité récente
              {!loading && filteredActivities.length > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {filteredActivities.length}
                </Badge>
              )}
            </CardTitle>
            
            <div className="flex items-center gap-2">
              {/* Activity Type Filter */}
              {showFilters && (
                <Select value={activityTypeFilter} onValueChange={handleActivityTypeFilterChange}>
                  <SelectTrigger className="w-40">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Toutes les activités</SelectItem>
                    <SelectItem value="application">Candidatures</SelectItem>
                    <SelectItem value="interview">Entretiens</SelectItem>
                    <SelectItem value="test">Tests</SelectItem>
                    {includeOffers && (
                      <>
                        <SelectItem value="offer_published">Offres publiées</SelectItem>
                        <SelectItem value="offer_created">Offres créées</SelectItem>
                      </>
                    )}
                  </SelectContent>
                </Select>
              )}

              {/* Time Period Filter */}
              {showFilters && (
                <Select value={filterDays} onValueChange={handleFilterChange}>
                  <SelectTrigger className="w-32">
                    <Filter className="h-4 w-4 mr-1" />
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">Toutes</SelectItem>
                    <SelectItem value="1">Aujourd'hui</SelectItem>
                    <SelectItem value="7">7 jours</SelectItem>
                    <SelectItem value="30">30 jours</SelectItem>
                  </SelectContent>
                </Select>
              )}
              
              <Button 
                onClick={refresh} 
                variant="outline" 
                size="sm" 
                className="gap-2"
                disabled={loading}
              >
                <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                Actualiser
              </Button>
            </div>
          </div>
        </CardHeader>
        
        <CardContent className="space-y-4">
          {loading ? (
            // Loading skeleton
            <div className="space-y-4">
              <div className="flex items-center justify-center py-4">
                <div className="flex items-center gap-2 text-sm text-blue-600">
                  <RefreshCw className="h-4 w-4 animate-spin" />
                  Chargement des activités récentes depuis la base de données...
                </div>
              </div>
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-start gap-3 p-4 rounded-lg border border-gray-100">
                  <div className="w-10 h-10 bg-gray-200 animate-pulse rounded-full"></div>
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 animate-pulse rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 animate-pulse rounded w-1/2"></div>
                    <div className="flex gap-2">
                      <div className="h-4 bg-gray-200 animate-pulse rounded w-16"></div>
                      <div className="h-4 bg-gray-200 animate-pulse rounded w-20"></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : isEmpty ? (
            // Empty state
            <div className="text-center py-8">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <User className="h-8 w-8 text-gray-400" />
              </div>
              <h3 className="text-sm font-medium text-gray-900 mb-2">
                Aucune activité récente
              </h3>
              <p className="text-xs text-gray-500 mb-4">
                Les nouvelles candidatures et offres publiées apparaîtront ici
              </p>
              <div className="text-xs text-gray-400 bg-gray-50 p-3 rounded border">
                💡 Les données sont triées par <code>createdAt</code> (plus récentes en premier)
              </div>
            </div>
          ) : (
            // Activities list
            <div className="space-y-3">
              {filteredActivities.map((activity, index) => {
                const IconComponent = getActivityIcon(activity.type);
                const isOffer = activity.type === 'offer_published' || activity.type === 'offer_created' || activity.type === 'published_offer';
                
                return (
                  <div 
                    key={`${activity.id}-${index}`} 
                    className="flex items-start gap-3 p-4 rounded-lg border border-gray-100 hover:bg-gray-50/50 transition-all duration-200 hover:shadow-sm"
                  >
                    <div className={`p-2 rounded-full flex-shrink-0 ${
                      isOffer ? 'bg-purple-50' : 'bg-blue-50'
                    }`}>
                      <IconComponent className={`h-4 w-4 ${
                        isOffer ? 'text-purple-600' : 'text-blue-600'
                      }`} />
                    </div>
                    
                    <div className="flex-1 min-w-0">
                      {/* Activity type badge */}
                      <div className="flex items-center gap-2 mb-1">
                        <Badge variant="outline" className="text-xs">
                          {getActivityTypeLabel(activity.type)}
                        </Badge>
                        {activity.offreTitre && (
                          <span className="text-xs text-gray-500">
                            {activity.offreTitre}
                          </span>
                        )}
                      </div>

                      {/* Main message */}
                      <p className="text-sm font-medium text-gray-900 mb-1">
                        {activity.message}
                      </p>
                      
                      {/* Candidate name or job title */}
                      {isOffer ? (
                        <p className="text-sm font-semibold text-purple-800 mb-2">
                          {activity.poste || activity.candidate}
                        </p>
                      ) : (
                        <p className="text-sm font-semibold text-gray-800 mb-2">
                          {activity.candidate}
                        </p>
                      )}
                      
                      {/* Contact information (only for candidates) */}
                      {!isOffer && (activity.email || activity.telephone) && (
                        <div className="space-y-1 mb-3">
                          {activity.email && (
                            <div className="flex items-center gap-1 text-xs text-gray-600">
                              <Mail className="h-3 w-3 flex-shrink-0" />
                              <span className="truncate">{activity.email}</span>
                            </div>
                          )}
                          {activity.telephone && (
                            <div className="flex items-center gap-1 text-xs text-gray-600">
                              <Phone className="h-3 w-3 flex-shrink-0" />
                              <span>{activity.telephone}</span>
                            </div>
                          )}
                        </div>
                      )}
                      
                      {/* Footer with time and status */}
                      <div className="flex items-center justify-between">
                        <span className="text-xs text-gray-500 flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {activity.time}
                        </span>
                        <Badge 
                          variant="secondary" 
                          className={`text-xs font-medium ${getStatusColor(activity.status)}`}
                        >
                          {activity.status}
                        </Badge>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
          
          {/* Footer info */}
          {!loading && !isEmpty && !error && (
            <div className="pt-3 border-t border-gray-100">
              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>
                  {filteredActivities.length} activité{filteredActivities.length > 1 ? 's' : ''} récente{filteredActivities.length > 1 ? 's' : ''}
                  {activityTypeFilter !== "all" && (
                    <span className="ml-1">
                      ({getActivityTypeLabel(activityTypeFilter).toLowerCase()})
                    </span>
                  )}
                </span>
              </div>
              {stats && (
                <div className="text-xs text-gray-400 mt-1">
                  Dernière mise à jour: {new Date(stats.lastUpdated).toLocaleTimeString('fr-FR')}
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}