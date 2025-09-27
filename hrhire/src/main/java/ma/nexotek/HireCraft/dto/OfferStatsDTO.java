package ma.nexotek.HireCraft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for offer statistics used in the recent activity dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferStatsDTO {

    // Published offers statistics
    private int publishedToday;
    private int publishedThisWeek;
    private int publishedThisMonth;
    private int totalPublished;

    // Created offers statistics
    private int createdToday;
    private int createdThisWeek;
    private int createdThisMonth;
    private int totalCreated;

    // Metadata
    private String lastUpdated;

    // Additional calculated fields
    private double publishedRatio; // publishedToday / createdToday ratio
    private int pendingPublication; // totalCreated - totalPublished

    /**
     * Calculate derived statistics
     */
    public void calculateDerivedStats() {
        // Calculate published ratio (avoid division by zero)
        this.publishedRatio = createdToday > 0 ? (double) publishedToday / createdToday : 0.0;

        // Calculate pending publications
        this.pendingPublication = totalCreated - totalPublished;
    }

    /**
     * Get formatted published ratio as percentage
     */
    public String getPublishedRatioFormatted() {
        return String.format("%.1f%%", publishedRatio * 100);
    }

    /**
     * Check if there's high activity today
     */
    public boolean isHighActivityToday() {
        return createdToday >= 5 || publishedToday >= 3;
    }

    /**
     * Get activity level description
     */
    public String getActivityLevel() {
        int totalActivityToday = createdToday + publishedToday;

        if (totalActivityToday >= 10) {
            return "High";
        } else if (totalActivityToday >= 5) {
            return "Medium";
        } else if (totalActivityToday >= 1) {
            return "Low";
        } else {
            return "None";
        }
    }
}