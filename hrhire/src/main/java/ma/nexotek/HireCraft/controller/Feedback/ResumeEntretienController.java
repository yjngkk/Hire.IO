package ma.nexotek.HireCraft.controller.Feedback;


import ma.nexotek.HireCraft.dto.ResumeDto;
import ma.nexotek.HireCraft.service.ResumeEntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeEntretienController {

    @Autowired
    private ResumeEntretienService service;

    @PostMapping
    public ResumeDto createResume(@RequestBody ResumeDto dto) {
        return service.createResume(dto);
    }

    @GetMapping
    public List<ResumeDto> getAllResumes() {
        return service.getAllResumes();
    }

    @GetMapping("/{id}")
    public ResumeDto getResumeById(@PathVariable Long id) {
        return service.getResumeById(id);
    }
    @PutMapping("/{id}")
    public ResumeDto updateResume(@PathVariable Long id, @RequestBody ResumeDto dto) {
        dto.setId(id);
        return service.updateResume(dto);
    }
    @DeleteMapping("/{id}")
    public void deleteResume(@PathVariable Long id) {
        service.deleteResume(id);
    }
}
