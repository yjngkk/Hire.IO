package ma.nexotek.HireCraft.mapper;

import ma.nexotek.HireCraft.dto.CandidatDTO;
import ma.nexotek.HireCraft.dto.DocumentDTO;
import ma.nexotek.HireCraft.dto.EntretienDTO;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Document;
import ma.nexotek.HireCraft.model.Entretien;

public class EntretienMapper {

    public static EntretienDTO toDto(Entretien entretien) {
        if (entretien == null) {
            return null;
        }
        EntretienDTO dto = new EntretienDTO();
        dto.setId(entretien.getId());
        dto.setDateHeure(entretien.getDateHeure());
        dto.setDuree(entretien.getDuree());
        dto.setType(entretien.getType());
        dto.setInterviewer(entretien.getInterviewer());
        dto.setStatut(entretien.getStatut());
        dto.setNotes(entretien.getNotes());
        dto.setMeetLink(entretien.getMeetLink());
        dto.setOrganizerEmail(entretien.getOrganizerEmail());
        dto.setInterviewerEmail(entretien.getInterviewerEmail());
        dto.setPauseDuration(entretien.getPauseDuration());
        dto.setPauseStartTime(entretien.getPauseStartTime());
        
        // Champs Zoom
        dto.setZoomMeetingId(entretien.getZoomMeetingId());
        dto.setZoomMeetingUuid(entretien.getZoomMeetingUuid());
        dto.setZoomJoinUrl(entretien.getZoomJoinUrl());
        dto.setZoomStartUrl(entretien.getZoomStartUrl());
        dto.setZoomPassword(entretien.getZoomPassword());
        dto.setZoomHostId(entretien.getZoomHostId());

        Candidat candidat = entretien.getCandidat();
        if (candidat != null) {
            CandidatDTO candidatDTO = new CandidatDTO(candidat);
            // Map CV shallowly to avoid loading LOB content and proxies
            Document cv = candidat.getCv();
            if (cv != null) {
                try {
                    DocumentDTO documentDTO = new DocumentDTO(
                            cv.getId(),
                            cv.getName(),
                            cv.getFilePath(),
                            cv.getType(),
                            cv.getSize(),
                            cv.getUploadDate()
                    );
                    candidatDTO.setCv(documentDTO);
                } catch (Exception e) {
                    // Handle LOB access exceptions gracefully
                    System.err.println("Warning: Could not access Document properties due to LOB access issue: " + e.getMessage());
                    // Create minimal DocumentDTO with safe fallbacks
                    DocumentDTO documentDTO = new DocumentDTO(
                            cv.getId(),
                            "Document unavailable",
                            "",
                            "unknown",
                            0L,
                            null
                    );
                    candidatDTO.setCv(documentDTO);
                }
            }
            dto.setCandidat(candidatDTO);
        }

        return dto;
    }
}



