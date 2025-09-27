import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { useToast } from "@/hooks/use-toast";
import { Video, Copy, ExternalLink, Trash2, RefreshCw } from "lucide-react";
import { zoomApi } from "../api/zoomApi";

interface ZoomMeetingCardProps {
  interviewId: number;
  zoomMeetingId?: string;
  zoomJoinUrl?: string;
  zoomStartUrl?: string;
  zoomPassword?: string;
  onZoomMeetingCreated?: (meetingInfo: any) => void;
  onZoomMeetingDeleted?: () => void;
}

export function ZoomMeetingCard({
  interviewId,
  zoomMeetingId,
  zoomJoinUrl,
  zoomStartUrl,
  zoomPassword,
  onZoomMeetingCreated,
  onZoomMeetingDeleted
}: ZoomMeetingCardProps) {
  const [isCreating, setIsCreating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const { toast } = useToast();

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast({
      title: "Copié",
      description: `${label} a été copié dans le presse-papiers`
    });
  };

  const createZoomMeeting = async () => {
    setIsCreating(true);
    try {
      const response = await zoomApi.createZoomForInterview(interviewId);
      if (response.success) {
        onZoomMeetingCreated?.(response);
        toast({
          title: "Réunion Zoom créée",
          description: "La réunion Zoom a été créée avec succès"
        });
      } else {
        toast({
          variant: "destructive",
          title: "Erreur",
          description: response.message || "Impossible de créer la réunion Zoom"
        });
      }
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Erreur",
        description: "Erreur lors de la création de la réunion Zoom: " + (error.message || "Erreur inconnue")
      });
    } finally {
      setIsCreating(false);
    }
  };

  const deleteZoomMeeting = async () => {
    if (!zoomMeetingId) return;
    
    setIsDeleting(true);
    try {
      await zoomApi.deleteMeeting(parseInt(zoomMeetingId));
      onZoomMeetingDeleted?.();
      toast({
        title: "Réunion Zoom supprimée",
        description: "La réunion Zoom a été supprimée avec succès"
      });
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Erreur",
        description: "Erreur lors de la suppression de la réunion Zoom: " + (error.message || "Erreur inconnue")
      });
    } finally {
      setIsDeleting(false);
    }
  };

  const joinMeeting = () => {
    if (zoomJoinUrl) {
      window.open(zoomJoinUrl, '_blank');
    }
  };

  const startMeeting = () => {
    if (zoomStartUrl) {
      window.open(zoomStartUrl, '_blank');
    }
  };

  if (!zoomMeetingId) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Video className="h-4 w-4" />
            Réunion Zoom
          </CardTitle>
          <CardDescription>
            Créer une réunion Zoom pour cet entretien
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Button 
            onClick={createZoomMeeting} 
            disabled={isCreating}
            className="w-full"
          >
            {isCreating ? (
              <>
                <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                Création en cours...
              </>
            ) : (
              <>
                <Video className="h-4 w-4 mr-2" />
                Créer une réunion Zoom
              </>
            )}
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base flex items-center gap-2">
          <Video className="h-4 w-4" />
          Réunion Zoom
        </CardTitle>
        <CardDescription>
          Réunion Zoom créée - ID: {zoomMeetingId}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Lien de participation */}
        {zoomJoinUrl && (
          <div className="space-y-2">
            <label className="text-sm font-medium">Lien de participation</label>
            <div className="flex gap-2">
              <Input 
                value={zoomJoinUrl} 
                readOnly 
                className="flex-1 text-sm"
              />
              <Button 
                variant="outline" 
                size="icon"
                onClick={() => copyToClipboard(zoomJoinUrl, "Lien de participation")}
              >
                <Copy className="h-4 w-4" />
              </Button>
              <Button 
                variant="outline" 
                size="icon"
                onClick={joinMeeting}
              >
                <ExternalLink className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}

        {/* Mot de passe */}
        {zoomPassword && (
          <div className="space-y-2">
            <label className="text-sm font-medium">Mot de passe</label>
            <div className="flex gap-2">
              <Input 
                value={zoomPassword} 
                readOnly 
                className="flex-1 text-sm"
              />
              <Button 
                variant="outline" 
                size="icon"
                onClick={() => copyToClipboard(zoomPassword, "Mot de passe")}
              >
                <Copy className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-2">
          {zoomStartUrl && (
            <Button 
              onClick={startMeeting}
              className="flex-1"
            >
              <Video className="h-4 w-4 mr-2" />
              Démarrer la réunion
            </Button>
          )}
          
          <Button 
            variant="outline"
            onClick={joinMeeting}
            className="flex-1"
          >
            <ExternalLink className="h-4 w-4 mr-2" />
            Rejoindre
          </Button>
        </div>

        {/* Suppression */}
        <Button 
          variant="destructive" 
          size="sm"
          onClick={deleteZoomMeeting}
          disabled={isDeleting}
          className="w-full"
        >
          {isDeleting ? (
            <>
              <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
              Suppression...
            </>
          ) : (
            <>
              <Trash2 className="h-4 w-4 mr-2" />
              Supprimer la réunion Zoom
            </>
          )}
        </Button>
      </CardContent>
    </Card>
  );
}
