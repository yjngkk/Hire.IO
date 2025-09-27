package ma.nexotek.HireCraft.controller;

import ma.nexotek.HireCraft.dto.TimelineEventDTO;
import ma.nexotek.HireCraft.mapper.TimelineEventMapper;
import ma.nexotek.HireCraft.model.TimelineEvent;
import ma.nexotek.HireCraft.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@CrossOrigin(origins = "*")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineEventMapper timelineEventMapper;

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<TimelineEventDTO>> getCandidateTimeline(@PathVariable Long candidatId) {
        try {
            List<TimelineEvent> events = timelineService.getCandidateTimeline(candidatId);
            List<TimelineEventDTO> eventDTOs = timelineEventMapper.toDTOList(events);
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}



