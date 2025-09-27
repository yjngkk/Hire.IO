package ma.nexotek.HireCraft.service.Onboarding;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ma.nexotek.HireCraft.dto.ProcedureDTO;
import ma.nexotek.HireCraft.model.Candidat;
import ma.nexotek.HireCraft.model.Procedure;
import ma.nexotek.HireCraft.model.ProcedureStatus;
import ma.nexotek.HireCraft.repository.CandidatRepository;
import ma.nexotek.HireCraft.repository.ProcedureRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final CandidatRepository candidatRepository;

    // Existing methods
    public List<ProcedureDTO> getAllProcedures() {
        return procedureRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> getProceduresByCandidatId(Long candidatId) {
        return procedureRepository.findByCandidatIdOrderByOrderIndex(candidatId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProcedureDTO> getProcedureById(Long id) {
        return procedureRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1:00 AM
    @Transactional
    public void updateProcedureStatuses() {
        log.info("Starting daily procedure status update");

        List<Procedure> procedures = procedureRepository.findByCompletedFalse();
        int updated = 0;

        for (Procedure procedure : procedures) {
            ProcedureStatus oldStatus = procedure.getStatus();
            procedure.updateStatus(); // This will recalculate status based on current date

            if (oldStatus != procedure.getStatus()) {
                updated++;
                log.debug("Procedure {} status changed from {} to {}",
                        procedure.getId(), oldStatus, procedure.getStatus());
            }
        }

        if (updated > 0) {
            procedureRepository.saveAll(procedures);
            log.info("Updated status for {} procedures", updated);
        } else {
            log.info("No procedure status changes needed");
        }
    }

    public ProcedureDTO createProcedure(ProcedureDTO procedureDTO) {
        Candidat candidat = candidatRepository.findById(procedureDTO.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + procedureDTO.getCandidatId()));

        Procedure procedure = new Procedure();
        procedure.setTitle(procedureDTO.getTitle());
        procedure.setResponsible(procedureDTO.getResponsible());
        procedure.setDescription(procedureDTO.getDescription());
        procedure.setCompleted(false);
        procedure.setCandidat(candidat);
        procedure.setStatus(ProcedureStatus.PENDING);

        // Handle the new dueDate format
        if (procedureDTO.getDueDate() != null) {
            procedure.setDueDate(procedureDTO.getDueDate());
            procedure.setDeadlineFormat("CUSTOM"); // Mark as custom datetime
        } else if (procedureDTO.getDeadline() != null) {
            procedure.setDeadlineFormat(procedureDTO.getDeadline()); // Legacy format
        }

        // Set order index
        if (procedureDTO.getOrderIndex() != null) {
            procedure.setOrderIndex(procedureDTO.getOrderIndex());
        } else {
            List<Procedure> existingProcedures = procedureRepository.findByCandidatId(candidat.getId());
            int nextOrder = existingProcedures.size() + 1;
            procedure.setOrderIndex(nextOrder);
        }

        Procedure savedProcedure = procedureRepository.save(procedure);
        return convertToDTO(savedProcedure);
    }

    public Optional<ProcedureDTO> updateProcedure(Long id, ProcedureDTO procedureDTO) {
        return procedureRepository.findById(id)
                .map(procedure -> {
                    procedure.setTitle(procedureDTO.getTitle());
                    procedure.setResponsible(procedureDTO.getResponsible());
                    procedure.setDescription(procedureDTO.getDescription());

                    // Handle the new dueDate format
                    if (procedureDTO.getDueDate() != null) {
                        procedure.setDueDate(procedureDTO.getDueDate());
                        procedure.setDeadlineFormat("CUSTOM"); // Mark as custom datetime
                    } else if (procedureDTO.getDeadline() != null) {
                        procedure.setDeadlineFormat(procedureDTO.getDeadline()); // Legacy format
                    }

                    // Update order index if provided
                    if (procedureDTO.getOrderIndex() != null) {
                        procedure.setOrderIndex(procedureDTO.getOrderIndex());
                    }

                    // If candidatId is provided and different, update the relationship
                    if (procedureDTO.getCandidatId() != null &&
                            !procedureDTO.getCandidatId().equals(procedure.getCandidat().getId())) {
                        Candidat newCandidat = candidatRepository.findById(procedureDTO.getCandidatId())
                                .orElseThrow(() -> new RuntimeException("Candidat not found with id: " + procedureDTO.getCandidatId()));

                        procedure.setCandidat(newCandidat);
                    }

                    Procedure savedProcedure = procedureRepository.save(procedure);
                    return convertToDTO(savedProcedure);
                });
    }

    public Optional<ProcedureDTO> toggleProcedureCompletion(Long id) {
        return procedureRepository.findById(id)
                .map(procedure -> {
                    procedure.setCompleted(!procedure.getCompleted());
                    Procedure savedProcedure = procedureRepository.save(procedure);
                    return convertToDTO(savedProcedure);
                });
    }

    public boolean deleteProcedure(Long id) {
        if (procedureRepository.existsById(id)) {
            procedureRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ProcedureStatsDTO getCompletionStats() {
        List<Procedure> procedures = procedureRepository.findAll();
        long completedCount = procedures.stream().mapToLong(p -> p.getCompleted() ? 1 : 0).sum();
        long totalCount = procedures.size();
        double completionPercentage = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;

        return new ProcedureStatsDTO(totalCount, completedCount, completionPercentage);
    }

    public ProcedureStatsDTO getCompletionStatsByCandidat(Long candidatId) {
        List<Procedure> procedures = procedureRepository.findByCandidatId(candidatId);
        long completedCount = procedures.stream().mapToLong(p -> p.getCompleted() ? 1 : 0).sum();
        long totalCount = procedures.size();
        double completionPercentage = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;

        return new ProcedureStatsDTO(totalCount, completedCount, completionPercentage);
    }

    public List<ProcedureDTO> resetAllProcedures() {
        List<Procedure> procedures = procedureRepository.findAll();
        procedures.forEach(procedure -> procedure.setCompleted(false));
        List<Procedure> savedProcedures = procedureRepository.saveAll(procedures);
        return savedProcedures.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> resetProceduresByCandidat(Long candidatId) {
        List<Procedure> procedures = procedureRepository.findByCandidatId(candidatId);
        procedures.forEach(procedure -> procedure.setCompleted(false));
        List<Procedure> savedProcedures = procedureRepository.saveAll(procedures);
        return savedProcedures.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UpcomingEventDTO> getUpcomingEvents() {
        return getUpcomingEvents(1, 5); // Default: today only, max 5 candidates
    }

    public List<UpcomingEventDTO> getTodayEvents() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Get procedures due today using actual due dates
        List<Procedure> todayProcedures = procedureRepository
                .findByCompletedFalseAndDueDateBetweenOrderByDueDate(startOfDay, endOfDay);

        Map<Long, Procedure> proceduresByCandidat = new LinkedHashMap<>();

        for (Procedure procedure : todayProcedures) {
            Long candidatId = procedure.getCandidat().getId();

            // Take only the first procedure per candidate
            if (!proceduresByCandidat.containsKey(candidatId)) {
                proceduresByCandidat.put(candidatId, procedure);
            }
        }

        return proceduresByCandidat.values().stream()
                .map(this::convertToUpcomingEvent)
                .collect(Collectors.toList());
    }

    // Enhanced new methods from your enhanced service
    public List<ProcedureDTO> getProceduresByStatus(ProcedureStatus status) {
        return procedureRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> getOverdueProcedures() {
        return procedureRepository.findByStatusAndCompletedFalse(ProcedureStatus.OVERDUE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> getProceduresDueToday() {
        return procedureRepository.findByStatusAndCompletedFalse(ProcedureStatus.DUE_TODAY).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Enhanced upcoming events - now uses actual due dates
    public List<UpcomingEventDTO> getUpcomingEvents(int daysAhead, int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(daysAhead);

        // Get procedures with due dates within the specified range
        List<Procedure> upcomingProcedures = procedureRepository
                .findByCompletedFalseAndDueDateBetweenOrderByDueDate(now, endDate);

        // Group by candidat and take first procedure per candidat
        Map<Long, Procedure> proceduresByCandidat = new LinkedHashMap<>();

        for (Procedure procedure : upcomingProcedures) {
            Long candidatId = procedure.getCandidat().getId();
            if (!proceduresByCandidat.containsKey(candidatId)) {
                proceduresByCandidat.put(candidatId, procedure);
            }
        }

        return proceduresByCandidat.values().stream()
                .map(this::convertToUpcomingEvent)
                .limit(limit)
                .collect(Collectors.toList());
    }


    // Conversion methods
    private ProcedureDTO convertToDTO(Procedure procedure) {
        return ProcedureDTO.builder()
                .id(procedure.getId())
                .title(procedure.getTitle())
                .responsible(procedure.getResponsible())
                .deadline(procedure.getDeadlineFormat())
                .dueDate(procedure.getDueDate())
                .status(procedure.getStatus())
                .completed(procedure.getCompleted())
                .description(procedure.getDescription())
                .orderIndex(procedure.getOrderIndex())
                .createdDate(procedure.getCreatedDate())
                .updatedDate(procedure.getUpdatedDate())
                .candidatId(procedure.getCandidat().getId())
                .candidatNom(procedure.getCandidat().getNom())
                .candidatEmail(procedure.getCandidat().getEmail())
                .candidatPoste(procedure.getCandidat().getPoste())
                .templateId(procedure.getTemplate() != null ? procedure.getTemplate().getId() : null)
                .templateTitle(procedure.getTemplate() != null ? procedure.getTemplate().getTitle() : null)
                .daysUntilDue(procedure.getDaysUntilDue())
                .isOverdue(procedure.isOverdue())
                .isDueToday(procedure.isDueToday())
                .build();
    }

    private UpcomingEventDTO convertToUpcomingEvent(Procedure procedure) {
        // Count other incomplete procedures for this candidat
        long totalForCandidat = procedureRepository.countByCandidatIdAndCompletedFalse(procedure.getCandidat().getId());

        String displayTitle = procedure.getTitle();
        if (totalForCandidat > 1) {
            displayTitle = procedure.getTitle();
        }

        return new UpcomingEventDTO(
                procedure.getId(),
                displayTitle,
                procedure.getCandidat().getNom(),
                procedure.getCandidat().getPoste(),
                procedure.getDueDate(),
                procedure.getDeadlineFormat(),
                procedure.getCompleted(),
                procedure.getResponsible(),
                procedure.getStatus()
        );
    }

    // Legacy method for backward compatibility
    private UpcomingEventDTO calculateDueDateWithCandidateInfo(Procedure procedure) {
        try {
            LocalDateTime createdDate = procedure.getCreatedDate();
            if (createdDate == null) {
                createdDate = LocalDateTime.now();
            }

            String deadline = procedure.getDeadlineFormat();
            if (deadline == null || deadline.trim().isEmpty()) {
                return null;
            }

            LocalDateTime dueDate = parseDeadline(createdDate, deadline);
            if (dueDate == null) {
                return null;
            }

            // Count total incomplete procedures for this candidate
            Long candidatId = procedure.getCandidat().getId();
            long totalProceduresForCandidat = procedureRepository.findByCandidatId(candidatId)
                    .stream()
                    .filter(p -> !p.getCompleted())
                    .count();

            // Modify title to indicate if there are more procedures
            String displayTitle = procedure.getTitle();
            if (totalProceduresForCandidat > 1) {
                displayTitle = procedure.getTitle();
            }

            return new UpcomingEventDTO(
                    procedure.getId(),
                    displayTitle,
                    procedure.getCandidat().getNom(),
                    procedure.getCandidat().getPoste(),
                    dueDate,
                    deadline,
                    procedure.getCompleted(),
                    procedure.getResponsible(),
                    procedure.getStatus()
            );
        } catch (Exception e) {
            return null;
        }
    }

    // Legacy deadline parsing for backward compatibility
    private LocalDateTime parseDeadline(LocalDateTime baseDate, String deadline) {
        try {
            deadline = deadline.trim().toUpperCase();

            if (deadline.startsWith("J+")) {
                String daysStr = deadline.substring(2);
                int days = Integer.parseInt(daysStr);
                return baseDate.plusDays(days);
            } else if (deadline.startsWith("J-")) {
                String daysStr = deadline.substring(2);
                int days = Integer.parseInt(daysStr);
                return baseDate.minusDays(days);
            } else if (deadline.equals("J")) {
                return baseDate;
            } else {
                return parseOtherDeadlineFormats(baseDate, deadline);
            }
        } catch (NumberFormatException e) {
            return baseDate.plusDays(1); // Default fallback
        }
    }

    private LocalDateTime parseOtherDeadlineFormats(LocalDateTime baseDate, String deadline) {
        deadline = deadline.trim().toUpperCase();

        if (deadline.contains("JOUR")) {
            String[] parts = deadline.split("\\s+");
            if (parts.length >= 1) {
                try {
                    int days = Integer.parseInt(parts[0]);
                    return baseDate.plusDays(days);
                } catch (NumberFormatException e) {
                    return baseDate.plusDays(1);
                }
            }
        } else if (deadline.contains("SEMAINE")) {
            String[] parts = deadline.split("\\s+");
            if (parts.length >= 1) {
                try {
                    int weeks = Integer.parseInt(parts[0]);
                    return baseDate.plusWeeks(weeks);
                } catch (NumberFormatException e) {
                    return baseDate.plusWeeks(1);
                }
            }
        }

        return baseDate.plusDays(1); // Default fallback
    }

    // COMPATIBLE ProcedureStatsDTO - matches controller expectations
    public static class ProcedureStatsDTO {
        private final long totalProcedures;
        private final long completedProcedures;
        private final double completionPercentage;

        // Enhanced fields (optional - for future use)
        private final long pendingProcedures;
        private final long dueTodayProcedures;
        private final long overdueProcedures;

        // Original constructor (for backward compatibility)
        public ProcedureStatsDTO(long totalProcedures, long completedProcedures, double completionPercentage) {
            this.totalProcedures = totalProcedures;
            this.completedProcedures = completedProcedures;
            this.completionPercentage = completionPercentage;
            this.pendingProcedures = 0;
            this.dueTodayProcedures = 0;
            this.overdueProcedures = 0;
        }

        // Enhanced constructor
        public ProcedureStatsDTO(long totalProcedures, long completedProcedures, double completionPercentage,
                                 long pendingProcedures, long dueTodayProcedures, long overdueProcedures) {
            this.totalProcedures = totalProcedures;
            this.completedProcedures = completedProcedures;
            this.completionPercentage = completionPercentage;
            this.pendingProcedures = pendingProcedures;
            this.dueTodayProcedures = dueTodayProcedures;
            this.overdueProcedures = overdueProcedures;
        }

        // Getters
        public long getTotalProcedures() { return totalProcedures; }
        public long getCompletedProcedures() { return completedProcedures; }
        public double getCompletionPercentage() { return completionPercentage; }
        public long getPendingProcedures() { return pendingProcedures; }
        public long getDueTodayProcedures() { return dueTodayProcedures; }
        public long getOverdueProcedures() { return overdueProcedures; }
    }

    // UpcomingEventDTO - FIXED VERSION with timezone-aware time calculation
    public static class UpcomingEventDTO {
        private final Long procedureId;
        private final String title;
        private final String candidatName;
        private final String candidatPoste;
        private final LocalDateTime dueDate;
        private final String deadlineFormat;
        private final boolean completed;
        private final String responsible;
        private final ProcedureStatus status;

        public UpcomingEventDTO(Long procedureId, String title, String candidatName,
                                String candidatPoste, LocalDateTime dueDate, String deadlineFormat,
                                boolean completed, String responsible, ProcedureStatus status) {
            this.procedureId = procedureId;
            this.title = title;
            this.candidatName = candidatName;
            this.candidatPoste = candidatPoste;
            this.dueDate = dueDate;
            this.deadlineFormat = deadlineFormat;
            this.completed = completed;
            this.responsible = responsible;
            this.status = status;
        }

        // Getters
        public Long getProcedureId() { return procedureId; }
        public String getTitle() { return title; }
        public String getCandidatName() { return candidatName; }
        public String getCandidatPoste() { return candidatPoste; }
        public LocalDateTime getDueDate() { return dueDate; }
        public String getDeadlineFormat() { return deadlineFormat; }
        public boolean isCompleted() { return completed; }
        public String getResponsible() { return responsible; }
        public ProcedureStatus getStatus() { return status; }

        public String getFormattedDueDate() {
            if (dueDate == null) return "Non définie";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
            return dueDate.format(formatter);
        }

        public String getRelativeTimeDescription() {
            if (dueDate == null) return "Non définie";

            // Get current time in the same context
            LocalDateTime now = LocalDateTime.now();

            // Calculate the difference in total minutes for precision
            Duration duration = Duration.between(now, dueDate);
            long totalMinutes = duration.toMinutes();
            long totalHours = duration.toHours();
            long totalDays = duration.toDays();

            // Handle overdue cases (negative duration)
            if (totalMinutes < 0) {
                long overdueMinutes = Math.abs(totalMinutes);
                long overdueHours = Math.abs(totalHours);
                long overdueDays = Math.abs(totalDays);

                if (overdueMinutes < 60) {
                    return "En retard de " + overdueMinutes + " min";
                } else if (overdueHours < 24) {
                    return "En retard de " + overdueHours + "h";
                } else {
                    return "En retard de " + overdueDays + " jour" + (overdueDays > 1 ? "s" : "");
                }
            }

            // Handle upcoming cases (positive duration)
            if (totalMinutes < 60) {
                if (totalMinutes <= 0) {
                    return "Maintenant";
                } else {
                    return "Dans " + totalMinutes + " min";
                }
            } else if (totalHours < 24) {
                return "Dans " + totalHours + "h";
            } else if (totalDays == 1) {
                return "Demain";
            } else {
                return "Dans " + totalDays + " jour" + (totalDays > 1 ? "s" : "");
            }
        }

        public String getStatusBadgeClass() {
            if (status == null) return "bg-gray-100 text-gray-800";

            return switch (status) {
                case COMPLETED -> "bg-green-100 text-green-800";
                case OVERDUE -> "bg-red-100 text-red-800";
                case DUE_TODAY -> "bg-yellow-100 text-yellow-800";
                case PENDING -> "bg-blue-100 text-blue-800";
            };
        }
    }
}