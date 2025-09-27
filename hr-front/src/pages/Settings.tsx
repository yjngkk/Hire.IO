
import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { 
  Link, 
  Briefcase, 
  Globe, 
  Linkedin, 
  Users, 
  Plus, 
  Edit, 
  Trash2, 
  CheckCircle, 
  XCircle, 
  Settings as SettingsIcon,
  Bot,
  FileText,
  Shield
} from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const platforms = [
  { id: "linkedin", name: "LinkedIn", icon: Linkedin, color: "text-blue-600", status: "connected" },
  { id: "indeed", name: "Indeed", icon: Briefcase, color: "text-blue-700", status: "disconnected" },
  { id: "website", name: "Site web", icon: Globe, color: "text-green-600", status: "connected" },
  { id: "apec", name: "APEC", icon: Briefcase, color: "text-purple-600", status: "disconnected" }
];

const mockUsers = [
  { id: 1, name: "Jean Dupont", email: "jean@company.com", role: "Admin", status: "active", lastLogin: "2024-01-15" },
  { id: 2, name: "Marie Martin", email: "marie@company.com", role: "Recruteur", status: "active", lastLogin: "2024-01-14" },
  { id: 3, name: "Paul Durand", email: "paul@company.com", role: "Recruteur", status: "inactive", lastLogin: "2024-01-10" }
];

const mockContractTypes = [
  { id: 1, name: "CDI", description: "Contrat à Durée Indéterminée", active: true, custom: false },
  { id: 2, name: "CDD", description: "Contrat à Durée Déterminée", active: true, custom: false },
  { id: 3, name: "Freelance", description: "Travailleur indépendant", active: true, custom: true },
  { id: 4, name: "Stage", description: "Stage conventionné", active: true, custom: false }
];

export function Settings() {
  const { toast } = useToast();
  const [users, setUsers] = useState(mockUsers);
  const [contractTypes, setContractTypes] = useState(mockContractTypes);
  const [newUser, setNewUser] = useState({ name: "", email: "", role: "Recruteur" });
  const [newContract, setNewContract] = useState({ name: "", description: "" });
  const [gptSettings, setGptSettings] = useState({
    enabled: true,
    tone: "professional",
    quota: 100,
    features: {
      jobGeneration: true,
      cvAnalysis: true,
      candidateScoring: true
    }
  });

  const handlePlatformConnect = (platformId: string) => {
    toast({
      title: "Connexion en cours",
      description: `Configuration de l'intégration ${platformId}...`,
    });
  };

  const handleUserCreate = () => {
    if (!newUser.name || !newUser.email) {
      toast({
        title: "Erreur",
        description: "Veuillez remplir tous les champs",
        variant: "destructive",
      });
      return;
    }

    const user = {
      id: users.length + 1,
      ...newUser,
      status: "active" as const,
      lastLogin: new Date().toISOString().split('T')[0]
    };

    setUsers([...users, user]);
    setNewUser({ name: "", email: "", role: "Recruteur" });
    toast({
      title: "Utilisateur créé",
      description: `${newUser.name} a été ajouté avec succès`,
    });
  };

  const handleUserDelete = (userId: number) => {
    setUsers(users.filter(u => u.id !== userId));
    toast({
      title: "Utilisateur supprimé",
      description: "L'utilisateur a été supprimé avec succès",
    });
  };

  const handleContractCreate = () => {
    if (!newContract.name || !newContract.description) {
      toast({
        title: "Erreur",
        description: "Veuillez remplir tous les champs",
        variant: "destructive",
      });
      return;
    }

    const contract = {
      id: contractTypes.length + 1,
      ...newContract,
      active: true,
      custom: true
    };

    setContractTypes([...contractTypes, contract]);
    setNewContract({ name: "", description: "" });
    toast({
      title: "Type de contrat créé",
      description: `${newContract.name} a été ajouté avec succès`,
    });
  };

  const handleContractDelete = (contractId: number) => {
    setContractTypes(contractTypes.filter(c => c.id !== contractId));
    toast({
      title: "Type de contrat supprimé",
      description: "Le type de contrat a été supprimé avec succès",
    });
  };

  const handleGptSettingsUpdate = () => {
    toast({
      title: "Configuration IA mise à jour",
      description: "Les paramètres ont été sauvegardés avec succès",
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Paramètres Généraux</h1>
        <p className="text-muted-foreground mt-2">
          Configuration et administration de la plateforme
        </p>
      </div>

      <Tabs defaultValue="platforms" className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="platforms">
            <Link className="w-4 h-4 mr-2" />
            Connecteurs
          </TabsTrigger>
          <TabsTrigger value="users">
            <Users className="w-4 h-4 mr-2" />
            Utilisateurs
          </TabsTrigger>
          <TabsTrigger value="contracts">
            <FileText className="w-4 h-4 mr-2" />
            Contrats
          </TabsTrigger>
          <TabsTrigger value="ai">
            <Bot className="w-4 h-4 mr-2" />
            Intelligence Artificielle
          </TabsTrigger>
        </TabsList>

        {/* Connecteurs Plateformes */}
        <TabsContent value="platforms" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Connecteurs plateformes de recrutement</CardTitle>
              <CardDescription>
                Gérez vos intégrations avec les principales plateformes de recrutement
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {platforms.map((platform) => {
                  const IconComponent = platform.icon;
                  const isConnected = platform.status === "connected";
                  
                  return (
                    <Card key={platform.id} className="p-4">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <IconComponent className={`h-8 w-8 ${platform.color}`} />
                          <div>
                            <h3 className="font-semibold">{platform.name}</h3>
                            <div className="flex items-center gap-2 mt-1">
                              {isConnected ? (
                                <>
                                  <CheckCircle className="h-4 w-4 text-green-500" />
                                  <Badge className="bg-green-100 text-green-800">Connecté</Badge>
                                </>
                              ) : (
                                <>
                                  <XCircle className="h-4 w-4 text-red-500" />
                                  <Badge className="bg-red-100 text-red-800">Déconnecté</Badge>
                                </>
                              )}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Switch checked={isConnected} />
                          <Button 
                            variant="outline" 
                            size="sm"
                            onClick={() => handlePlatformConnect(platform.name)}
                          >
                            <SettingsIcon className="h-4 w-4 mr-2" />
                            Configurer
                          </Button>
                        </div>
                      </div>
                    </Card>
                  );
                })}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Gestion des utilisateurs */}
        <TabsContent value="users" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle>Gestion des utilisateurs</CardTitle>
                  <CardDescription>
                    Créez et gérez les comptes utilisateurs et leurs permissions
                  </CardDescription>
                </div>
                <Dialog>
                  <DialogTrigger asChild>
                    <Button>
                      <Plus className="h-4 w-4 mr-2" />
                      Nouvel utilisateur
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Créer un utilisateur</DialogTitle>
                      <DialogDescription>
                        Ajoutez un nouvel utilisateur à la plateforme
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor="name">Nom complet</Label>
                        <Input
                          id="name"
                          value={newUser.name}
                          onChange={(e) => setNewUser({...newUser, name: e.target.value})}
                          placeholder="Jean Dupont"
                        />
                      </div>
                      <div>
                        <Label htmlFor="email">Email</Label>
                        <Input
                          id="email"
                          type="email"
                          value={newUser.email}
                          onChange={(e) => setNewUser({...newUser, email: e.target.value})}
                          placeholder="jean@company.com"
                        />
                      </div>
                      <div>
                        <Label htmlFor="role">Rôle</Label>
                        <Select value={newUser.role} onValueChange={(value) => setNewUser({...newUser, role: value})}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="Admin">Administrateur</SelectItem>
                            <SelectItem value="Recruteur">Recruteur</SelectItem>
                            <SelectItem value="Manager">Manager</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <Button onClick={handleUserCreate} className="w-full">
                        Créer utilisateur
                      </Button>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Utilisateur</TableHead>
                    <TableHead>Rôle</TableHead>
                    <TableHead>Statut</TableHead>
                    <TableHead>Dernière connexion</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{user.name}</div>
                          <div className="text-sm text-muted-foreground">{user.email}</div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">
                          <Shield className="h-3 w-3 mr-1" />
                          {user.role}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {user.status === "active" ? (
                          <Badge className="bg-green-100 text-green-800">Actif</Badge>
                        ) : (
                          <Badge className="bg-red-100 text-red-800">Inactif</Badge>
                        )}
                      </TableCell>
                      <TableCell>{new Date(user.lastLogin).toLocaleDateString('fr-FR')}</TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-2">
                          <Button variant="outline" size="sm">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="outline" size="sm">
                                <Trash2 className="h-4 w-4 text-red-500" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Supprimer l'utilisateur</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Êtes-vous sûr de vouloir supprimer {user.name} ? Cette action est irréversible.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Annuler</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => handleUserDelete(user.id)}
                                  className="bg-red-600 hover:bg-red-700"
                                >
                                  Supprimer
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Types de contrat */}
        <TabsContent value="contracts" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle>Types de contrat</CardTitle>
                  <CardDescription>
                    Paramétrez les types de contrats disponibles sur la plateforme
                  </CardDescription>
                </div>
                <Dialog>
                  <DialogTrigger asChild>
                    <Button>
                      <Plus className="h-4 w-4 mr-2" />
                      Nouveau type
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Créer un type de contrat</DialogTitle>
                      <DialogDescription>
                        Ajoutez un nouveau type de contrat personnalisé
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor="contractName">Nom du contrat</Label>
                        <Input
                          id="contractName"
                          value={newContract.name}
                          onChange={(e) => setNewContract({...newContract, name: e.target.value})}
                          placeholder="Alternance"
                        />
                      </div>
                      <div>
                        <Label htmlFor="contractDescription">Description</Label>
                        <Textarea
                          id="contractDescription"
                          value={newContract.description}
                          onChange={(e) => setNewContract({...newContract, description: e.target.value})}
                          placeholder="Contrat d'alternance avec formation"
                        />
                      </div>
                      <Button onClick={handleContractCreate} className="w-full">
                        Créer type de contrat
                      </Button>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {contractTypes.map((contract) => (
                  <Card key={contract.id} className="p-4">
                    <div className="flex justify-between items-start">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold">{contract.name}</h3>
                          {contract.custom && (
                            <Badge variant="outline" className="text-xs">Personnalisé</Badge>
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">{contract.description}</p>
                        <div className="flex items-center gap-2">
                          <Switch checked={contract.active} />
                          <span className="text-sm">
                            {contract.active ? "Actif" : "Inactif"}
                          </span>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Button variant="outline" size="sm">
                          <Edit className="h-4 w-4" />
                        </Button>
                        {contract.custom && (
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="outline" size="sm">
                                <Trash2 className="h-4 w-4 text-red-500" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Supprimer le type de contrat</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Êtes-vous sûr de vouloir supprimer le type "{contract.name}" ? Cette action est irréversible.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Annuler</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => handleContractDelete(contract.id)}
                                  className="bg-red-600 hover:bg-red-700"
                                >
                                  Supprimer
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        )}
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Configuration IA */}
        <TabsContent value="ai" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Configuration Intelligence Artificielle</CardTitle>
              <CardDescription>
                Paramétrez les fonctionnalités GPT et d'intelligence artificielle
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-medium">Activer l'IA</h3>
                  <p className="text-sm text-muted-foreground">
                    Active ou désactive toutes les fonctionnalités d'intelligence artificielle
                  </p>
                </div>
                <Switch 
                  checked={gptSettings.enabled}
                  onCheckedChange={(checked) => setGptSettings({...gptSettings, enabled: checked})}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="tone">Ton de communication</Label>
                    <Select value={gptSettings.tone} onValueChange={(value) => setGptSettings({...gptSettings, tone: value})}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="professional">Professionnel</SelectItem>
                        <SelectItem value="friendly">Amical</SelectItem>
                        <SelectItem value="formal">Formel</SelectItem>
                        <SelectItem value="casual">Décontracté</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="quota">Quota mensuel (utilisations)</Label>
                    <Input
                      id="quota"
                      type="number"
                      value={gptSettings.quota}
                      onChange={(e) => setGptSettings({...gptSettings, quota: parseInt(e.target.value)})}
                    />
                  </div>
                </div>

                <Card className="p-4">
                  <h4 className="font-medium mb-3">Fonctionnalités disponibles</h4>
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <span className="text-sm font-medium">Génération d'offres</span>
                        <p className="text-xs text-muted-foreground">Création automatique d'offres d'emploi</p>
                      </div>
                      <Switch 
                        checked={gptSettings.features.jobGeneration}
                        onCheckedChange={(checked) => setGptSettings({
                          ...gptSettings, 
                          features: {...gptSettings.features, jobGeneration: checked}
                        })}
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <span className="text-sm font-medium">Analyse de CV</span>
                        <p className="text-xs text-muted-foreground">Analyse automatique des candidatures</p>
                      </div>
                      <Switch 
                        checked={gptSettings.features.cvAnalysis}
                        onCheckedChange={(checked) => setGptSettings({
                          ...gptSettings, 
                          features: {...gptSettings.features, cvAnalysis: checked}
                        })}
                      />
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <span className="text-sm font-medium">Scoring candidats</span>
                        <p className="text-xs text-muted-foreground">Attribution automatique de scores</p>
                      </div>
                      <Switch 
                        checked={gptSettings.features.candidateScoring}
                        onCheckedChange={(checked) => setGptSettings({
                          ...gptSettings, 
                          features: {...gptSettings.features, candidateScoring: checked}
                        })}
                      />
                    </div>
                  </div>
                </Card>
              </div>

              <div>
                <Label htmlFor="aiInstructions">Instructions personnalisées</Label>
                <Textarea
                  id="aiInstructions"
                  placeholder="Ajoutez des instructions spécifiques pour personnaliser le comportement de l'IA..."
                  className="mt-2"
                />
              </div>

              <Button onClick={handleGptSettingsUpdate} className="w-full">
                Sauvegarder la configuration
              </Button>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
