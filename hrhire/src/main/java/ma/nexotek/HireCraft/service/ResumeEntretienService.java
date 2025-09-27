package ma.nexotek.HireCraft.service;



import ma.nexotek.HireCraft.dto.ResumeDto;

import java.util.List;

public interface ResumeEntretienService {
    ResumeDto createResume(ResumeDto dto);
    List<ResumeDto> getAllResumes();
    ResumeDto getResumeById(Long id);
    void deleteResume(Long id);
    ResumeDto updateResume(ResumeDto dto);
}

