package ma.nexotek.HireCraft.mapper;


import ma.nexotek.HireCraft.dto.FormRequestDTO;
import ma.nexotek.HireCraft.dto.FormResponseDTO;
import ma.nexotek.HireCraft.model.Form;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FormMapper {

    public Form toEntity(FormRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Form form = new Form();
        form.setTitle(dto.getTitle());
        form.setMissions(dto.getMissions());
        form.setLocation(dto.getLocation());
        form.setContractType(dto.getContractType());
        form.setLevel(dto.getLevel());
        form.setSkills(dto.getSkills());
        form.setTone(dto.getTone());
        form.setCategorie(dto.getCategorie());
        return form;
    }

    public FormResponseDTO toResponseDTO(Form entity) {
        if (entity == null) {
            return null;
        }

        FormResponseDTO dto = new FormResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMissions(entity.getMissions());
        dto.setLocation(entity.getLocation());
        dto.setContractType(entity.getContractType());
        dto.setLevel(entity.getLevel());
        dto.setSkills(entity.getSkills());
        dto.setTone(entity.getTone());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setApplicationLink(entity.getApplicationLink());
        dto.setPublished(entity.getPublished());
        dto.setPublishedAt(entity.getPublishedAt());

        return dto;
    }

    public List<FormResponseDTO> toResponseDTOList(List<Form> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(Form entity, FormRequestDTO dto) {
        if (entity != null && dto != null) {
            entity.setTitle(dto.getTitle());
            entity.setMissions(dto.getMissions());
            entity.setLocation(dto.getLocation());
            entity.setContractType(dto.getContractType());
            entity.setLevel(dto.getLevel());
            entity.setSkills(dto.getSkills());
            entity.setTone(dto.getTone());
        }
    }
}
