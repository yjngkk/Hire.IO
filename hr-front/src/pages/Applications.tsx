import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Search, Mail, Calendar, Star } from "lucide-react";
import { CVViewer } from "@/components/applications/CVViewer";
import { CandidateTimeline } from "@/components/applications/CandidateTimeline";
import { ContactOptions } from "@/components/applications/ContactOptions";
import apiService from "@/config/apiService";

export function Applications() {
	const [searchTerm, setSearchTerm] = useState("");
	const [applications, setApplications] = useState<any[]>([]);
	const [showGoodTalents, setShowGoodTalents] = useState(false);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		const fetchCandidates = async () => {
			try {
				setLoading(true);
				const data = await apiService.get<any[]>("/candidats");
				setApplications(data || []);
			} catch (err) {
				console.error("Erreur lors de la récupération des candidats:", err);
			} finally {
				setLoading(false);
			}
		};

		fetchCandidates();
	}, []);

	const filteredApplications = applications.filter((app: any) => {
		const term = searchTerm.toLowerCase();
		const matches = (app.nom || "").toLowerCase().includes(term) || (app.poste || "").toLowerCase().includes(term);
		const isGood = showGoodTalents ? !!app.bonTalent : true;
		return matches && isGood;
	});

	const getStatusBadge = (status: string) => {
		const styles: Record<string, string> = {
			CREATED: "bg-blue-100 text-blue-800",
			PENDING: "bg-yellow-100 text-yellow-800",
			ACCEPTED: "bg-green-100 text-green-800",
			REJECTED: "bg-red-100 text-red-800",
			ERROR: "bg-red-100 text-red-800",
			TEST_SENT: "bg-purple-100 text-purple-800",
		};

		const labels: Record<string, string> = {
			CREATED: "Nouveau",
			PENDING: "En attente",
			REJECTED: "Rejeté",
			ERROR: "Erreur",
			TEST_SENT: "Test envoyé",
			ACCEPTED: "Accepté",
		};

		const styleClass = styles[status] || "bg-gray-100 text-gray-800";
		const label = labels[status] || status;

		return (
			<span className={`px-2 py-1 rounded-full text-xs font-medium ${styleClass}`}>
				{label}
			</span>
		);
	};

const getScoreColor = (score: number) => {
  // Gestion des cas invalides
  if (!score || isNaN(score)) return "text-gray-500";
  
  // Logique des couleurs avec marges adaptées
  if (score >= 85) return "text-green-600";      // Excellent
  if (score >= 75) return "text-green-500";      // Très bien
  if (score >= 65) return "text-yellow-500";     // Bien
  if (score >= 50) return "text-orange-500";     // Moyen
  if (score >= 35) return "text-orange-600";     // Faible
  return "text-red-600";                         // Très faible
};
	const toggleGoodTalent = async (application: any) => {
		try {
			const payload = {
				nom: application.nom,
				email: application.email,
				telephone: application.telephone,
				poste: application.poste,
				notes: application.notes || "",
				offreId: undefined,
				bonTalent: !application.bonTalent,
			};
			const updated = await apiService.put(`/candidats/${application.id}/info`, payload);
			setApplications((prev) => prev.map((a: any) => (a.id === application.id ? { ...a, bonTalent: updated.bonTalent } : a)));
		} catch (e) {
			console.error("Erreur lors de la mise à jour du flag bon_talent", e);
		}
	};

	if (loading) {
		return (
			<div className="flex items-center justify-center h-64">
				<div className="text-lg">Chargement des candidatures...</div>
			</div>
		);
	}

	return (
		<div className="space-y-6">
			<div className="flex justify-between items-center">
				<div>
					<h1 className="text-3xl font-bold">Candidatures</h1>
					<p className="text-muted-foreground mt-2">Gérez vos candidatures</p>
				</div>
				<div className="flex items-center gap-2" />
			</div>

			<Card>
				<CardHeader>
					<div className="flex items-center space-x-2">
						<Search className="h-4 w-4" />
						<Input
							placeholder="Rechercher un candidat ou un poste..."
							value={searchTerm}
							onChange={(e) => setSearchTerm(e.target.value)}
							className="max-w-sm"
						/>
						<div className="ml-auto flex items-center gap-2">
							<Button
								variant={showGoodTalents ? "default" : "outline"}
								size="sm"
								onClick={() => setShowGoodTalents(!showGoodTalents)}
								className={showGoodTalents ? "bg-yellow-500 hover:bg-yellow-600 text-white" : ""}
							>
								<Star className={`h-4 w-4 mr-2 ${showGoodTalents ? "fill-white" : ""}`} />
								Bons talents
							</Button>
						</div>
					</div>
				</CardHeader>
				<CardContent>
					<div className="space-y-4">
						{filteredApplications.map((application: any) => (
							<Card key={application.id} className="p-4 hover:shadow-md transition-shadow">
								<div className="flex justify-between items-start">
									<div className="flex items-start gap-4">
										<Avatar>
											<AvatarFallback>
												{(application.nom || "")
													.split(" ")
													.map((n: string) => n[0])
													.join("")
													.toUpperCase()}
											</AvatarFallback>
										</Avatar>
										<div className="space-y-2">
											<h3 className="text-lg font-semibold">{application.nom}</h3>
											<div className="flex items-center gap-1">
												<Star className="h-3 w-3" />
												<span>{application.formTitre}</span>
											</div>
											<div className="flex items-center gap-4 text-sm text-muted-foreground">
												<div className="flex items-center gap-1">
													<Mail className="h-3 w-3" />
													<span>{application.email}</span>
												</div>
												<div className="flex items-center gap-1">
													<Calendar className="h-3 w-3" />
													<span>Candidaté le {new Date("2023-03-23").toLocaleDateString("fr-FR")}</span>
												</div>
											</div>
											<div className="flex items-center gap-4">
												{getStatusBadge(application.processStatus)}
												<div className="flex items-center gap-1">
													<Star className="h-4 w-4 text-yellow-500" />
													<span className={`font-semibold ${getScoreColor(application.cvScore)}`}>Cv Score IA:  {application.cvScore} </span>
												</div>
											</div>
										</div>
									</div>
									<div className="flex gap-2">
										<CVViewer
											candidateId={application.id}
											candidatePhone={application.telephone}
											candidatePoste={application.poste}
											candidateName={application.nom}
											candidateEmail={application.email}
										/>
										<CandidateTimeline candidateName={application.nom} candidateId={application.id} />
										<ContactOptions
											candidateName={application.nom}
											candidateEmail={application.email}
											candidatePhone={application.telephone}
											candidatId={application.id}
										/>
										<Button
											variant={application.bonTalent ? "default" : "outline"}
											size="sm"
											onClick={() => toggleGoodTalent(application)}
											className={`ml-2 ${application.bonTalent ? "bg-yellow-500 hover:bg-yellow-600 text-white" : ""}`}
										>
											<Star className={`h-4 w-4 mr-2 ${application.bonTalent ? "fill-white" : ""}`} />
											{application.bonTalent ? "Bon talent" : "Marquer bon talent"}
										</Button>
									</div>
								</div>
							</Card>
						))}
					</div>
				</CardContent>
			</Card>
		</div>
	);
}
