import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Check, Users, Calendar, FileText, Zap, Building, Star, ArrowRight, Play } from "lucide-react";
import { useAuth } from "@/KeycloakProvider";
import React, { useEffect, useState } from "react";

export function LandingPage() {
  const { keycloak, keycloakReady, isAuthenticated } = useAuth();
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (keycloakReady && isAuthenticated) {
      window.location.href = "/dashboard";
    }
  }, [keycloakReady, isAuthenticated]);

  useEffect(() => {
    setIsVisible(true);
  }, []);

  const handleLogin = () => {
    console.log("handleLogin appelé");
    if (keycloak == undefined) {
      console.log("Keycloak est undefined !");
    } else {
      console.log("Keycloak est défini, on appelle login()");
      keycloak.login();
    }
  };

  const testimonials = [
    {
      name: "Sarah Martin",
      role: "DRH chez TechCorp",
      content: "Hire.IO a transformé notre processus de recrutement. Nous avons réduit notre temps d'embauche de 40%.",
      rating: 5
    },
    {
      name: "Ahmed Benali",
      role: "Fondateur de StartupX",
      content: "Interface intuitive et fonctionnalités complètes. Un must-have pour toute équipe RH moderne.",
      rating: 5
    },
    {
      name: "Marie Dubois",
      role: "Recruteuse Senior",
      content: "La gestion des candidatures n'a jamais été aussi simple. Je recommande vivement !",
      rating: 5
    }
  ];

  const stats = [
    { number: "500+", label: "Entreprises clientes" },
    { number: "15k+", label: "Candidatures traitées" },
    { number: "40%", label: "Temps économisé" },
    { number: "98%", label: "Satisfaction client" }
  ];

  const features = [
    { 
      icon: FileText, 
      title: "Gestion des offres", 
      desc: "Créez et publiez vos offres d'emploi facilement avec l'IA",
      color: "text-blue-600"
    },
    { 
      icon: Users, 
      title: "Suivi intelligent", 
      desc: "Organisez et suivez toutes vos candidatures avec des analytics",
      color: "text-green-600"
    },
    { 
      icon: Calendar, 
      title: "Planification automatique", 
      desc: "Programmez vos entretiens sans effort avec notre système intelligent",
      color: "text-purple-600"
    },
    { 
      icon: Zap, 
      title: "Tests personnalisés", 
      desc: "Évaluez les compétences avec des tests techniques sur mesure",
      color: "text-yellow-600"
    },
    { 
      icon: Building, 
      title: "Onboarding fluide", 
      desc: "Intégrez vos nouveaux employés avec des parcours personnalisés",
      color: "text-red-600"
    },
    { 
      icon: Users, 
      title: "Collaboration équipe", 
      desc: "Travaillez ensemble sur vos processus RH en temps réel",
      color: "text-indigo-600"
    }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 overflow-x-hidden">
      {/* Header avec animation */}
      <header className={`border-b bg-white/80 backdrop-blur-xl supports-[backdrop-filter]:bg-white/60 fixed w-full top-0 z-50 transition-all duration-500 shadow-sm ${isVisible ? 'translate-y-0' : '-translate-y-full'}`}>
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3 group">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl flex items-center justify-center text-white font-bold transition-all duration-300 group-hover:scale-110 group-hover:rotate-12">
              H
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              Hire.IO
            </span>
          </div>

          <Button 
            onClick={handleLogin} 
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 transition-all duration-300 hover:scale-105 shadow-lg hover:shadow-xl"
          >
            Se connecter
          </Button>
        </div>
      </header>

      {/* Hero Section avec animations */}
      <section className="container mx-auto px-4 py-32 text-center relative overflow-hidden">
        {/* Background decorative elements */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-20 left-10 w-72 h-72 bg-gradient-to-r from-blue-400/20 to-purple-400/20 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-20 right-10 w-96 h-96 bg-gradient-to-r from-purple-400/20 to-pink-400/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }}></div>
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-gradient-to-r from-blue-300/10 to-green-300/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '2s' }}></div>
        </div>

        <div className={`relative z-10 transition-all duration-1000 ${isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}>
          <Badge variant="secondary" className="mb-6 px-4 py-2 bg-gradient-to-r from-blue-100 to-purple-100 text-blue-700 border-blue-200 hover:scale-105 transition-all duration-300 cursor-default">
            🚀 Plateforme RH Nouvelle Génération
          </Badge>
          
          <h1 className="text-5xl md:text-7xl font-bold mb-8 leading-tight">
            <span className="bg-gradient-to-r from-blue-600 via-purple-600 to-blue-800 bg-clip-text text-transparent">
              Révolutionnez
            </span>
            <br />
            <span className="text-gray-800">
              votre recrutement
            </span>
          </h1>
          
          <p className="text-xl md:text-2xl text-gray-600 mb-12 max-w-4xl mx-auto leading-relaxed font-light">
            La première plateforme RH propulsée par l'IA qui transforme votre façon de recruter.
            <br />
            <span className="text-blue-600 font-medium">Plus rapide. Plus intelligent. Plus humain.</span>
          </p>
          
          <div className="flex flex-col sm:flex-row gap-6 justify-center mb-16">
            <Button 
              size="lg" 
              className="text-lg px-10 py-4 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 transition-all duration-300 hover:scale-105 shadow-xl hover:shadow-2xl group"
              onClick={handleLogin}
            >
              Démarrer gratuitement
              <ArrowRight className="ml-3 h-5 w-5 group-hover:translate-x-2 transition-transform" />
            </Button>
            <Button 
              variant="outline" 
              size="lg" 
              className="text-lg px-10 py-4 border-2 border-gray-300 hover:border-blue-500 hover:bg-blue-50 transition-all duration-300 hover:scale-105 group"
            >
              <Play className="mr-3 h-5 w-5 group-hover:scale-110 transition-transform" />
              Voir la démo
            </Button>
          </div>

          {/* Stats section avec animations */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 max-w-5xl mx-auto">
            {stats.map((stat, index) => (
              <div 
                key={index}
                className={`text-center group cursor-default transition-all duration-700 hover:scale-110 ${isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}
                style={{ transitionDelay: `${index * 200}ms` }}
              >
                <div className="text-3xl md:text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2 group-hover:scale-110 transition-transform">
                  {stat.number}
                </div>
                <div className="text-gray-600 font-medium">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section avec hover effects améliorés */}
      <section className="container mx-auto px-4 py-24 bg-white/50 backdrop-blur-sm">
        <div className="text-center mb-20">
          <h2 className="text-4xl font-bold mb-6 text-gray-800">
            Tout ce dont vous avez besoin
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Une suite complète d'outils intelligents pour révolutionner votre processus de recrutement
          </p>
        </div>
        
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <Card 
              key={index}
              className="border-0 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-4 group cursor-pointer bg-white/80 backdrop-blur-sm overflow-hidden"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-blue-50/50 to-purple-50/50 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              <CardHeader className="relative z-10 p-8">
                <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center mb-6 group-hover:scale-110 transition-all duration-300 ${feature.color}`}>
                  <feature.icon className="h-8 w-8" />
                </div>
                <CardTitle className="text-xl mb-3 group-hover:text-blue-600 transition-colors duration-300">
                  {feature.title}
                </CardTitle>
                <CardDescription className="text-gray-600 leading-relaxed">
                  {feature.desc}
                </CardDescription>
              </CardHeader>
            </Card>
          ))}
        </div>
      </section>

      {/* Testimonials Section améliorée */}
      <section className="container mx-auto px-4 py-24">
        <div className="text-center mb-20">
          <h2 className="text-4xl font-bold mb-6 text-gray-800">
            Ils nous font confiance
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Découvrez pourquoi des centaines d'entreprises choisissent Hire.IO
          </p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {testimonials.map((testimonial, index) => (
            <Card 
              key={index}
              className="hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 bg-white border-0 shadow-lg group overflow-hidden"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-blue-50/30 to-purple-50/30 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              <CardHeader className="relative z-10 p-8">
                <div className="flex mb-4">
                  {[...Array(testimonial.rating)].map((_, i) => (
                    <Star key={i} className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                  ))}
                </div>
                <CardDescription className="text-lg italic text-gray-700 mb-6 leading-relaxed">
                  "{testimonial.content}"
                </CardDescription>
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold">
                    {testimonial.name.split(' ').map(n => n[0]).join('')}
                  </div>
                  <div>
                    <div className="font-semibold text-gray-800">{testimonial.name}</div>
                    <div className="text-sm text-gray-600">{testimonial.role}</div>
                  </div>
                </div>
              </CardHeader>
            </Card>
          ))}
        </div>
      </section>

      {/* Pricing Section avec effets visuels */}
      <section className="container mx-auto px-4 py-24 bg-gradient-to-br from-slate-50 to-blue-50/30">
        <div className="text-center mb-20">
          <h2 className="text-4xl font-bold mb-6 text-gray-800">
            Tarifs transparents et flexibles
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Choisissez le plan parfait pour votre équipe, avec possibilité d'évolution
          </p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {/* Plan Starter */}
          <Card className="relative hover:shadow-2xl transition-all duration-500 hover:-translate-y-4 bg-white group overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-gray-50/50 to-blue-50/50 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <CardHeader className="text-center relative z-10 p-8">
              <div className="w-16 h-16 bg-gradient-to-r from-gray-400 to-gray-600 rounded-2xl mx-auto mb-4 flex items-center justify-center">
                <FileText className="h-8 w-8 text-white" />
              </div>
              <CardTitle className="text-2xl mb-2">Starter</CardTitle>
              <CardDescription className="text-gray-600">Parfait pour débuter</CardDescription>
              <div className="mt-6">
                <span className="text-4xl font-bold text-gray-800">Gratuit</span>
                <p className="text-gray-600 mt-2">Pour toujours</p>
              </div>
            </CardHeader>
            <CardContent className="space-y-4 p-8 pt-0 relative z-10">
              {[
                "5 offres d'emploi actives",
                "50 candidatures/mois",
                "Support email",
                "Interface intuitive"
              ].map((feature, i) => (
                <div key={i} className="flex items-center gap-3">
                  <Check className="h-5 w-5 text-green-500 flex-shrink-0" />
                  <span className="text-gray-700">{feature}</span>
                </div>
              ))}
              <Button 
                variant="outline" 
                className="w-full mt-8 hover:scale-105 transition-all duration-300 border-2 hover:border-blue-500 hover:bg-blue-50"
                onClick={handleLogin}
              >
                Commencer gratuitement
              </Button>
            </CardContent>
          </Card>

          {/* Plan Pro */}
          <Card className="relative border-2 border-blue-500 shadow-2xl scale-105 hover:scale-110 transition-all duration-500 bg-white group overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-blue-50/50 to-purple-50/50"></div>
            <Badge className="absolute -top-3 left-1/2 transform -translate-x-1/2 bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-1 animate-pulse">
              ⭐ Le plus populaire
            </Badge>
            <CardHeader className="text-center relative z-10 p-8 pt-12">
              <div className="w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl mx-auto mb-4 flex items-center justify-center">
                <Zap className="h-8 w-8 text-white" />
              </div>
              <CardTitle className="text-2xl mb-2">Pro</CardTitle>
              <CardDescription className="text-gray-600">Pour les équipes ambitieuses</CardDescription>
              <div className="mt-6">
                <span className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">490 MAD</span>
                <span className="text-gray-600 ml-2">/mois</span>
                <p className="text-gray-600 mt-2">Facturé mensuellement</p>
              </div>
            </CardHeader>
            <CardContent className="space-y-4 p-8 pt-0 relative z-10">
              {[
                "Offres illimitées",
                "1000 candidatures/mois",
                "Tests techniques IA",
                "Analytiques avancées",
                "Support prioritaire",
                "Intégrations API"
              ].map((feature, i) => (
                <div key={i} className="flex items-center gap-3">
                  <Check className="h-5 w-5 text-green-500 flex-shrink-0" />
                  <span className="text-gray-700">{feature}</span>
                </div>
              ))}
              <Button 
                className="w-full mt-8 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 transition-all duration-300 hover:scale-105 shadow-lg hover:shadow-xl"
                onClick={handleLogin}
              >
                Choisir Pro
              </Button>
            </CardContent>
          </Card>

          {/* Plan Enterprise */}
          <Card className="relative hover:shadow-2xl transition-all duration-500 hover:-translate-y-4 bg-white group overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-purple-50/50 to-pink-50/50 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <CardHeader className="text-center relative z-10 p-8">
              <div className="w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-500 rounded-2xl mx-auto mb-4 flex items-center justify-center">
                <Building className="h-8 w-8 text-white" />
              </div>
              <CardTitle className="text-2xl mb-2">Enterprise</CardTitle>
              <CardDescription className="text-gray-600">Solutions sur mesure</CardDescription>
              <div className="mt-6">
                <span className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">Sur mesure</span>
                <p className="text-gray-600 mt-2">Devis personnalisé</p>
              </div>
            </CardHeader>
            <CardContent className="space-y-4 p-8 pt-0 relative z-10">
              {[
                "Tout du plan Pro",
                "Candidatures illimitées",
                "Infrastructure dédiée",
                "Personnalisation complète",
                "Support 24/7",
                "Formation équipe"
              ].map((feature, i) => (
                <div key={i} className="flex items-center gap-3">
                  <Check className="h-5 w-5 text-green-500 flex-shrink-0" />
                  <span className="text-gray-700">{feature}</span>
                </div>
              ))}
              <Button 
                variant="outline" 
                className="w-full mt-8 hover:scale-105 transition-all duration-300 border-2 hover:border-purple-500 hover:bg-purple-50"
              >
                Nous contacter
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* CTA Section finale */}
      <section className="container mx-auto px-4 py-24 text-center relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-500/10 via-purple-500/10 to-blue-500/10 rounded-3xl"></div>
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-blue-100/20 via-transparent to-transparent"></div>
        
        <div className="relative z-10 max-w-4xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold mb-6 text-gray-800">
            Transformez votre recrutement
            <span className="block bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              dès aujourd'hui
            </span>
          </h2>
          <p className="text-xl text-gray-600 mb-12 leading-relaxed">
            Rejoignez les entreprises innovantes qui révolutionnent leur processus RH avec Hire.IO.
            <br />
            <strong className="text-blue-600">Essai gratuit, sans engagement, configuration en 5 minutes.</strong>
          </p>
          
          <div className="flex flex-col sm:flex-row gap-6 justify-center">
            <Button 
              size="lg" 
              className="text-xl px-12 py-6 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 transition-all duration-300 hover:scale-105 shadow-xl hover:shadow-2xl group"
              onClick={handleLogin}
            >
              Commencer l'essai gratuit
              <ArrowRight className="ml-3 h-6 w-6 group-hover:translate-x-2 transition-transform" />
            </Button>
            <Button 
              variant="outline" 
              size="lg" 
              className="text-xl px-12 py-6 border-2 border-gray-300 hover:border-blue-500 hover:bg-blue-50 transition-all duration-300 hover:scale-105"
            >
              Réserver une démo
            </Button>
          </div>
          
          <p className="text-sm text-gray-500 mt-8">
            🔒 Sécurisé • ⚡ Installation rapide • 🎯 Support expert inclus
          </p>
        </div>
      </section>

      {/* Footer modernisé */}
      <footer className="border-t bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-12">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center gap-3 mb-6 md:mb-0 group">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl flex items-center justify-center text-white font-bold group-hover:scale-110 transition-all duration-300 group-hover:rotate-12">
                H
              </div>
              <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Hire.IO
              </span>
            </div>
            
            <div className="text-center md:text-right">
              <p className="text-gray-600 mb-2">
                Révolutionnez votre processus de recrutement
              </p>
              <p className="text-sm text-gray-500">
                © 2024 Hire.IO. Tous droits réservés. Fait avec ❤️ au Maroc
              </p>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}