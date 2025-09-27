package ma.nexotek.HireCraft.controller;

import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@Slf4j
@CrossOrigin(origins = "*") // Ajustez selon vos besoins de sécurité
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "5") int limit) {
        try {

            if (limit <= 0 || limit > 50) {
                limit = 5; // Valeur par défaut sécurisée
            }

            List<Map<String, Object>> activities = activityService.getRecentActivities(limit);

            return ResponseEntity.ok(activities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/last-days")
    public ResponseEntity<List<Map<String, Object>>> getActivitiesFromLastDays(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {

            if (days <= 0 || days > 365) {
                days = 7; // Valeur par défaut
            }
            if (limit <= 0 || limit > 100) {
                limit = 10; // Valeur par défaut
            }

            List<Map<String, Object>> activities = activityService.getActivitiesFromLastDays(days, limit);

            return ResponseEntity.ok(activities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getActivityStats() {
        try {

            Map<String, Object> stats = activityService.getActivityStats();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Activity Controller is working",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}