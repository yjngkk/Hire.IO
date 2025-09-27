package ma.nexotek.HireCraft.service.impl;


import ma.nexotek.HireCraft.dto.ResumeDto;
import ma.nexotek.HireCraft.model.ResumeEntretien;
import ma.nexotek.HireCraft.repository.ResumeEntretienRepository;
import ma.nexotek.HireCraft.service.ResumeEntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeEntretienServiceImpl implements ResumeEntretienService {

    @Autowired
    private ResumeEntretienRepository repository;

    private ResumeDto mapToDto(ResumeEntretien entity) {
        ResumeDto dto = new ResumeDto();
        dto.setId(entity.getId()); // important
        dto.setNomCandidat(entity.getNomCandidat());
        dto.setPoste(entity.getPoste());
        dto.setInterviewer(entity.getInterviewer());
        dto.setDateEntretien(entity.getDateEntretien());
        dto.setScore(entity.getScore());
        dto.setRecommandation(entity.getRecommandation());
        dto.setNotes(entity.getNotes());
        dto.setPointsForts(entity.getPointsForts());
        dto.setPointsAmelioration(entity.getPointsAmelioration());
        dto.setProchainesEtapes(entity.getProchainesEtapes());
        return dto;
    }


    private ResumeEntretien mapToEntity(ResumeDto dto) {
        ResumeEntretien entity = new ResumeEntretien();
        entity.setNomCandidat(dto.getNomCandidat());
        entity.setPoste(dto.getPoste());
        entity.setInterviewer(dto.getInterviewer());
        entity.setDateEntretien(dto.getDateEntretien());
        entity.setScore(dto.getScore());
        entity.setRecommandation(dto.getRecommandation());
        entity.setNotes(dto.getNotes());
        entity.setPointsForts(dto.getPointsForts());
        entity.setPointsAmelioration(dto.getPointsAmelioration());
        entity.setProchainesEtapes(dto.getProchainesEtapes());
        return entity;
    }

    @Override
    public ResumeDto createResume(ResumeDto dto) {
        ResumeEntretien saved = repository.save(mapToEntity(dto));
        return mapToDto(saved);
    }

    @Override
    public List<ResumeDto> getAllResumes() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResumeDto getResumeById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Résumé introuvable avec l'id " + id));
    }
    @Override
    public ResumeDto updateResume(ResumeDto dto) {
        ResumeEntretien entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Résumé introuvable avec l'id " + dto.getId()));
        // Mettre à jour les champs
        entity.setNomCandidat(dto.getNomCandidat());
        entity.setPoste(dto.getPoste());
        entity.setInterviewer(dto.getInterviewer());
        entity.setDateEntretien(dto.getDateEntretien());
        entity.setScore(dto.getScore());
        entity.setRecommandation(dto.getRecommandation());
        entity.setNotes(dto.getNotes());
        entity.setPointsForts(dto.getPointsForts());
        entity.setPointsAmelioration(dto.getPointsAmelioration());
        entity.setProchainesEtapes(dto.getProchainesEtapes());
        ResumeEntretien saved = repository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public void deleteResume(Long id) {
        repository.deleteById(id);
    }
}
